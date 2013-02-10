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

import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import ru.runa.service.client.DelegateProcessVariableProvider;
import ru.runa.service.delegate.Delegates;
import ru.runa.wfe.WfException;
import ru.runa.wfe.bot.Bot;
import ru.runa.wfe.bot.BotTask;
import ru.runa.wfe.commons.ClassLoaderUtil;
import ru.runa.wfe.execution.logic.ProcessExecutionErrors;
import ru.runa.wfe.handler.bot.TaskHandler;
import ru.runa.wfe.presentation.BatchPresentationFactory;
import ru.runa.wfe.task.dto.WfTask;
import ru.runa.wfe.user.User;
import ru.runa.wfe.var.IVariableProvider;

import com.google.common.base.Objects;
import com.google.common.base.Throwables;
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
        scheduled, completed, failed
    };

    private BotExecutionStatus botStatus = BotExecutionStatus.scheduled;

    private final Map<String, TaskHandler> taskHandlers = Maps.newHashMap();
    private final Map<String, byte[]> extendedConfigurations = Maps.newHashMap();
    private final User user;
    private final Bot bot;
    private final WfTask task;
    private final WorkflowBot parent;

    private final Set<WorkflowBot> existingBots;
    // This need for thread execution stuck detection and stuck thread
    // termination
    private long startTime = -1;
    private long resetTime = -1;
    private Thread executionThread = null;
    private boolean taskInterrupting = false;

    public WorkflowBot(User user, Bot bot, List<BotTask> tasks) {
        this.user = user;
        task = null;
        parent = null;
        this.bot = bot;
        existingBots = new HashSet<WorkflowBot>();
        for (BotTask botTask : tasks) {
            try {
                TaskHandler handler = ClassLoaderUtil.instantiate(botTask.getTaskHandlerClassName());
                if (BotTaskConfigurationUtils.isExtendedBotTaskConfiguration(botTask.getConfiguration())) {
                    extendedConfigurations.put(botTask.getName(), botTask.getConfiguration());
                } else {
                    handler.setConfiguration(botTask.getConfiguration());
                }
                taskHandlers.put(botTask.getName(), handler);
                log.info("Configured taskHandler for " + botTask.getName());
                ProcessExecutionErrors.removeBotTaskConfigurationError(bot, botTask.getName());
            } catch (Throwable th) {
                ProcessExecutionErrors.addBotTaskConfigurationError(bot, botTask.getName(), th);
                log.error("Can't create handler for bot " + bot + " (task is " + botTask + ")", th);
            }
        }
    }

    private WorkflowBot(WorkflowBot parent, WfTask task) {
        user = parent.user;
        taskHandlers.putAll(parent.taskHandlers);
        extendedConfigurations.putAll(parent.extendedConfigurations);
        bot = parent.bot;
        this.task = task;
        this.parent = parent;
        existingBots = null;
    }

    public WorkflowBot createTask(WfTask task) {
        if (parent != null || this.task != null) {
            throw new WfException("WorkflowBot task can be created only by template");
        }
        WorkflowBot result = new WorkflowBot(this, task);
        if (existingBots.contains(result)) {
            for (WorkflowBot bot : existingBots) {
                if (bot.equals(result)) {
                    if (bot.botStatus != BotExecutionStatus.failed) {
                        throw new WfException("Incorrect WorkflowBot usage - only failed tasks may be recreated.");
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

    public Set<WfTask> getNewTasks() {
        List<WfTask> currentTasks = Delegates.getExecutionService().getTasks(user, BatchPresentationFactory.TASKS.createNonPaged());
        Set<WfTask> result = new HashSet<WfTask>();
        Set<WorkflowBot> failedBotsToRestart = new HashSet<WorkflowBot>();
        for (Iterator<WorkflowBot> botIterator = existingBots.iterator(); botIterator.hasNext();) {
            WorkflowBot bot = botIterator.next();
            if (bot.botStatus == BotExecutionStatus.completed && bot.resetTime < System.currentTimeMillis()) {
                // Completed bot task hold time is elapsed
                botIterator.remove();
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

    public Bot getBot() {
        return bot;
    }

    private void doHandle() throws Exception {
        try {
            String botTaskName = BotTaskConfigurationUtils.getBotTaskName(user, task);
            TaskHandler taskHandler = taskHandlers.get(botTaskName);
            if (taskHandler == null) {
                log.warn("No handler for bot task " + botTaskName + ", " + bot);
                ProcessExecutionErrors.addBotTaskNotFoundProcessError(task, bot, botTaskName);
                return;
            }
            IVariableProvider variableProvider = new DelegateProcessVariableProvider(user, task.getProcessId());

            byte[] extendedConfiguration = extendedConfigurations.get(botTaskName);
            if (extendedConfiguration != null) {
                byte[] configuration = BotTaskConfigurationUtils.substituteConfiguration(user, task, extendedConfiguration, variableProvider);
                taskHandler.setConfiguration(configuration);
            }

            log.info("Starting bot task " + task + " with config \n" + taskHandler.getConfiguration());
            Map<String, Object> variables = taskHandler.handle(user, variableProvider, task);
            if (variables == null) {
                variables = Maps.newHashMap();
            }
            Object skipTaskCompletion = variables.remove(TaskHandler.SKIP_TASK_COMPLETION_VARIABLE_NAME);
            if (Objects.equal(Boolean.TRUE, skipTaskCompletion)) {
                log.info("Bot task " + task + " postponed (skipTaskCompletion) by task handler " + taskHandler.getClass());
            } else {
                Delegates.getExecutionService().completeTask(user, task.getId(), variables);
                log.debug("Handled bot task " + task + ", " + bot + " by " + taskHandler.getClass());
            }
            ProcessExecutionErrors.removeProcessError(task.getProcessId(), task.getName());
        } catch (Throwable th) {
            ProcessExecutionErrors.addProcessError(task.getProcessId(), task.getName(), th);
            Throwables.propagateIfInstanceOf(th, Exception.class);
            throw Throwables.propagate(th);
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
            botStatus = BotExecutionStatus.completed;
            resetTime = System.currentTimeMillis() + complettedTasksHoldPeriod;
            return;
        } catch (Throwable e) {
            log.error("Error execution " + bot + " for task " + task, e);
            logBotError(task, e);
            botStatus = BotExecutionStatus.failed;
            // Double delay if exists
            long newDelay = resetTime == -1 ? failedTasksDelayPeriod : (resetTime - startTime) * 2;
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
        boolean tmp = taskInterrupting;
        taskInterrupting = status;
        if (status) {
            botStatus = BotExecutionStatus.failed;
        }
        return tmp;
    }

    @Override
    public String toString() {
        if (parent == null) {
            return "Template " + bot;
        } else {
            return bot + " with task " + task;
        }
    }

    private void logBotError(WfTask task, Throwable th) {
        BotLogger botLogger = BotStationResources.createBotLogger();
        if (botLogger == null) {
            return;
        }
        botLogger.logError(task, th);
    }
}
