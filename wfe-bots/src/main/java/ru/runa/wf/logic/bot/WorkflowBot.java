/*
 * This file is part of the RUNA WFE project.
 * 
 * This program is free software; you can redistribute it and/or 
 * modify it under the terms of the GNU Lesser General Public License 
 * as published by the Free Software Foundation; version 2.1 
 * of the License. 
 * 
 * This program is distributed in the hope that it will be useful, 
 * but WITHOUT ANY WARRANTY; without even the implied warranty of 
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the 
 * GNU Lesser General Public License for more details. 
 * 
 * You should have received a copy of the GNU Lesser General Public License 
 * along with this program; if not, write to the Free Software 
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA.
 */
package ru.runa.wf.logic.bot;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.security.auth.Subject;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import ru.runa.service.client.DelegateProcessVariableProvider;
import ru.runa.service.delegate.DelegateFactory;
import ru.runa.wfe.InternalApplicationException;
import ru.runa.wfe.bot.Bot;
import ru.runa.wfe.bot.BotTask;
import ru.runa.wfe.commons.ClassLoaderUtil;
import ru.runa.wfe.handler.bot.TaskHandler;
import ru.runa.wfe.presentation.BatchPresentationFactory;
import ru.runa.wfe.security.AuthenticationException;
import ru.runa.wfe.security.AuthorizationException;
import ru.runa.wfe.task.dto.WfTask;
import ru.runa.wfe.var.IVariableProvider;

import com.google.common.base.Objects;
import com.google.common.collect.Maps;

public class WorkflowBot implements Runnable {
    private final Log log = LogFactory.getLog(WorkflowBot.class);

    /**
     * This time completted tasks stay in existingBots (prevent tasks double
     * execution)
     */
    private final long complettedTasksHoldPeriod = 10000;
    /**
     * first wait is 30 sec. Next wait is 2*wait, but no more
     * failedTasksMaxDelayPeriod
     */
    private final long failedTasksDelayPeriod = 30000;
    /**
     * 4*32 min
     */
    private final long failedTasksMaxDelayPeriod = 4 * 1920000;

    private enum BotExecutionStatus {
        scheduled, completted, failed
    };

    private BotExecutionStatus botStatus = BotExecutionStatus.scheduled;

    private final Map<String, TaskHandler> taskHandlerMap;
    private final Subject subject;
    private final String botName;
    private final WfTask task;
    private final WorkflowBot parent;
    private final long botDeplay;

    private final Set<WorkflowBot> existingBots;
    // This need for thread execution stuck detection and stuck thread
    // termination
    private long startTime = -1;
    private long resetTime = -1;
    private Thread executionThread = null;
    private boolean isTaskInterrupting = false;

    // Creating WorkflowBot template object
    public WorkflowBot(Bot bot, List<BotTask> tasks) throws AuthenticationException {
        subject = DelegateFactory.getAuthenticationService().authenticate(bot.getUsername(), bot.getPassword());
        HashMap<String, TaskHandler> handlers = new HashMap<String, TaskHandler>();
        for (BotTask task : tasks) {
            try {
                TaskHandler handler = ClassLoaderUtil.instantiate(task.getTaskHandlerClassName());
                handler.setConfiguration(task.getConfiguration());
                handlers.put(task.getName(), handler);
                log.info("Configured taskHandler for " + task.getName());
            } catch (Throwable th) {
                log.error("Can't create handler for bot " + bot.getUsername() + " (task is " + task + ")", th);
            }
        }
        taskHandlerMap = handlers;
        task = null;
        parent = null;
        botName = bot.getUsername();
        botDeplay = bot.getStartTimeout();
        existingBots = new HashSet<WorkflowBot>();
    }

    private WorkflowBot(WorkflowBot parent, WfTask task) {
        subject = parent.subject;
        taskHandlerMap = parent.taskHandlerMap;
        botName = parent.botName;
        this.task = task;
        this.parent = parent;
        botDeplay = parent.botDeplay;
        existingBots = null;
    }

    public WorkflowBot createTask(WfTask task) {
        if (parent != null || this.task != null) {
            throw new InternalApplicationException("WorkflowBot task can be created only by template");
        }
        WorkflowBot result = new WorkflowBot(this, task);
        if (existingBots.contains(result)) {
            for (WorkflowBot bot : existingBots) {
                if (bot.equals(result)) {
                    if (bot.botStatus != BotExecutionStatus.failed) {
                        throw new InternalApplicationException("Incorrect WorkflowBot usage - only failed tasks may be recreated.");
                    }
                    result = bot;
                    break;
                }
            }
            result.botStatus = BotExecutionStatus.scheduled;
        } else {
            existingBots.add(result);
        }
        return result;
    }

    public Set<WfTask> getNewTasks() throws AuthenticationException, AuthorizationException {
        List<WfTask> currentTasks = DelegateFactory.getExecutionService().getTasks(subject, BatchPresentationFactory.TASKS.createNonPaged());
        Set<WfTask> result = new HashSet<WfTask>();
        Set<WorkflowBot> failedBotsToRestart = new HashSet<WorkflowBot>();
        for (Iterator<WorkflowBot> botIterator = existingBots.iterator(); botIterator.hasNext();) {
            WorkflowBot bot = botIterator.next();
            if (bot.botStatus == BotExecutionStatus.completted && bot.resetTime < System.currentTimeMillis()) {
                botIterator.remove(); // Completted bot task hold time is
                                      // elapsed
            }
            if (bot.botStatus == BotExecutionStatus.failed && bot.resetTime < System.currentTimeMillis()) {
                // Search in currentTasks
                for (WfTask task : currentTasks) {
                    if (Objects.equal(task.getId(), bot.task.getId())) {
                        failedBotsToRestart.add(bot);
                        break;
                    }
                }
            }
        }
        for (WfTask task : currentTasks) {
            if (!existingBots.contains(new WorkflowBot(this, task)) || failedBotsToRestart.contains(new WorkflowBot(this, task))) {
                result.add(task);
            }
        }
        return result;
    }

    public long getBotDelay() {
        return botDeplay;
    }

    private void doHandle() throws Exception {
        TaskHandler taskHandler = taskHandlerMap.get(task.getName());
        if (taskHandler == null) {
            log.warn("No handler for task " + task + ", bot " + botName);
            return;
        }
        IVariableProvider variableProvider = new DelegateProcessVariableProvider(subject, task.getProcessId());
        Map<String, Object> variables = taskHandler.handle(subject, variableProvider, task);
        if (variables == null) {
            variables = Maps.newHashMap();
        }
        Object skipTaskCompletion = variables.remove(TaskHandler.SKIP_TASK_COMPLETION_VARIABLE_NAME);
        if (Objects.equal(Boolean.TRUE, skipTaskCompletion)) {
            log.info("Task '" + task + "' postponed (skipTaskCompletion) by task handler " + taskHandler.getClass());
        } else {
            DelegateFactory.getExecutionService().completeTask(subject, task.getId(), variables);
            log.debug("Handled task " + task + ", bot " + botName + " by " + taskHandler.getClass());
        }
    }

    @Override
    public void run() {
        try {
            if (task == null) {
                log.error("WorkflowBot called without task - something goes wrong.");
                return;
            }
            if (resetTime != -1) { // Save delay period for failed bot
                long delay = resetTime - startTime;
                startTime = System.currentTimeMillis();
                resetTime = startTime + delay;
            } else {
                startTime = System.currentTimeMillis();
            }
            executionThread = Thread.currentThread();
            doHandle();
            botStatus = BotExecutionStatus.completted;
            resetTime = System.currentTimeMillis() + complettedTasksHoldPeriod;
            return;
        } catch (Throwable e) {
            log.error("Error execution bot " + botName + " for task " + task, e);
            logBotsError(task, e);
            botStatus = BotExecutionStatus.failed;
            long newDelay = resetTime == -1 ? failedTasksDelayPeriod : (resetTime - startTime) * 2; // Double
                                                                                                    // delay
                                                                                                    // if
                                                                                                    // exists
            if (newDelay > failedTasksMaxDelayPeriod) {
                newDelay = failedTasksMaxDelayPeriod;
            }
            startTime = System.currentTimeMillis();
            resetTime = startTime + newDelay;
        }
        executionThread = null;
        return;
    }

    public long getStartTime() {
        return startTime;
    }

    public Thread getExecutionThread() {
        return executionThread;
    }

    public boolean setTaskInerruptStatus(boolean status) {
        boolean tmp = isTaskInterrupting;
        isTaskInterrupting = status;
        if (status) {
            botStatus = BotExecutionStatus.failed;
        }
        return tmp;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        if (parent == null) {
            builder.append("Template bot '").append(botName).append("'");
        } else {
            builder.append("BotRunner '").append(botName).append("' with task [").append(task).append("]");
        }
        return builder.toString();
    }

    @Override
    public int hashCode() {
        if (task == null) {
            return -1;
        }
        return task.getId().hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null || !(obj instanceof WorkflowBot)) {
            return false;
        }
        WorkflowBot wfBot = (WorkflowBot) obj;
        // Long currentId = taskStub == null ? -1 : taskStub.getId();
        // Long otherId = wfBot.taskStub == null ? -1 : wfBot.taskStub.getId();
        return Objects.equal(task, wfBot.task);
    }

    private void logBotsError(WfTask task, Throwable th) {
        BotLogger botLogger = BotStationResources.createBotLogger();
        if (botLogger == null) {
            return;
        }
        botLogger.logError(task, th);
    }
}
