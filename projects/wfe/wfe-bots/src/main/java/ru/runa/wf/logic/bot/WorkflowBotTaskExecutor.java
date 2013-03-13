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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import ru.runa.wfe.InternalApplicationException;
import ru.runa.wfe.bot.Bot;
import ru.runa.wfe.bot.BotTask;
import ru.runa.wfe.commons.ClassLoaderUtil;
import ru.runa.wfe.execution.logic.ProcessExecutionErrors;
import ru.runa.wfe.execution.logic.ProcessExecutionException;
import ru.runa.wfe.extension.TaskHandler;
import ru.runa.wfe.presentation.BatchPresentationFactory;
import ru.runa.wfe.service.client.DelegateProcessVariableProvider;
import ru.runa.wfe.service.delegate.Delegates;
import ru.runa.wfe.task.TaskAlreadyCompletedException;
import ru.runa.wfe.task.dto.WfTask;
import ru.runa.wfe.user.User;
import ru.runa.wfe.var.IVariableProvider;

import com.google.common.base.Objects;
import com.google.common.base.Throwables;
import com.google.common.collect.Maps;

/**
 * Execute task handlers for particular bot.
 * 
 * Configures and executes task handler in same method.
 * 
 * @author Dofs
 * @since 4.0
 */
public class WorkflowBotTaskExecutor implements Runnable {
    private final Log log = LogFactory.getLog(WorkflowBotTaskExecutor.class);

    /**
     * This time completed tasks stay in existingBots (prevent tasks double
     * execution)
     */
    private final long completedTasksHoldPeriod = 10000;
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

    private final Map<String, BotTask> botTasks = Maps.newHashMap();
    private final User user;
    private final Bot bot;
    private final WfTask task;
    private final WorkflowBotTaskExecutor parent;

    private final Set<WorkflowBotTaskExecutor> existingBots;
    // This need for thread execution stuck detection and stuck thread
    // termination
    private long startTime = -1;
    private long resetTime = -1;
    private Thread executionThread = null;
    private boolean taskInterrupting = false;

    public WorkflowBotTaskExecutor(User user, Bot bot, List<BotTask> tasks) {
        this.user = user;
        task = null;
        parent = null;
        this.bot = bot;
        existingBots = new HashSet<WorkflowBotTaskExecutor>();
        for (BotTask botTask : tasks) {
            botTasks.put(botTask.getName(), botTask);
        }
    }

    private WorkflowBotTaskExecutor(WorkflowBotTaskExecutor parent, WfTask task) {
        user = parent.user;
        botTasks.putAll(parent.botTasks);
        bot = parent.bot;
        this.task = task;
        this.parent = parent;
        existingBots = null;
    }

    public WorkflowBotTaskExecutor createTask(WfTask task) {
        if (parent != null || this.task != null) {
            throw new InternalApplicationException("AsyncWorkflowBot task can be created only by template");
        }
        WorkflowBotTaskExecutor result = new WorkflowBotTaskExecutor(this, task);
        if (existingBots.contains(result)) {
            for (WorkflowBotTaskExecutor bot : existingBots) {
                if (bot.equals(result)) {
                    if (bot.botStatus != BotExecutionStatus.failed) {
                        throw new InternalApplicationException("Incorrect AsyncWorkflowBot usage - only failed tasks may be recreated.");
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
        Set<WorkflowBotTaskExecutor> failedBotsToRestart = new HashSet<WorkflowBotTaskExecutor>();
        for (Iterator<WorkflowBotTaskExecutor> botIterator = existingBots.iterator(); botIterator.hasNext();) {
            WorkflowBotTaskExecutor bot = botIterator.next();
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
            if (!existingBots.contains(new WorkflowBotTaskExecutor(this, task)) || failedBotsToRestart.contains(new WorkflowBotTaskExecutor(this, task))) {
                result.add(task);
            }
        }
        return result;
    }

    public Bot getBot() {
        return bot;
    }

    private void doHandle() throws Exception {
        TaskHandler taskHandler = null;
        IVariableProvider variableProvider = new DelegateProcessVariableProvider(user, task.getProcessId());
        try {
            String botTaskName = BotTaskConfigurationUtils.getBotTaskName(user, task);
            BotTask botTask = botTasks.get(botTaskName);
            if (botTask == null) {
                log.error("No handler for bot task " + botTaskName + ", " + bot);
                throw new ProcessExecutionException(ProcessExecutionException.BOT_TASK_MISSED, botTaskName, bot.getUsername());
            }
            taskHandler = ClassLoaderUtil.instantiate(botTask.getTaskHandlerClassName());
            try {
                if (BotTaskConfigurationUtils.isExtendedBotTaskConfiguration(botTask.getConfiguration())) {
                    if (botTask.getConfiguration() != null) {
                        byte[] configuration = BotTaskConfigurationUtils.substituteConfiguration(user, task, botTask.getConfiguration(),
                                variableProvider);
                        taskHandler.setConfiguration(configuration);
                    }
                } else {
                    taskHandler.setConfiguration(botTask.getConfiguration());
                }
                log.info("Configured taskHandler for " + botTask.getName());
                ProcessExecutionErrors.removeBotTaskConfigurationError(bot, botTask.getName());
            } catch (Throwable th) {
                ProcessExecutionErrors.addBotTaskConfigurationError(bot, botTask.getName(), th);
                log.error("Can't create handler for bot " + bot + " (task is " + botTask + ")", th);
                throw new ProcessExecutionException(ProcessExecutionException.BOT_TASK_CONFIGURATION_ERROR, th, botTaskName, th.getMessage());
            }

            log.info("Starting bot task " + task + " with config \n" + taskHandler.getConfiguration());
            Map<String, Object> variables = taskHandler.handle(user, variableProvider, task);
            if (variables == null) {
                variables = new HashMap<String, Object>();
            }
            Object skipTaskCompletion = variables.remove(TaskHandler.SKIP_TASK_COMPLETION_VARIABLE_NAME);
            if (Objects.equal(Boolean.TRUE, skipTaskCompletion)) {
                log.info("Bot task " + task + " postponed (skipTaskCompletion) by task handler " + taskHandler.getClass());
            } else {
                Delegates.getExecutionService().completeTask(user, task.getId(), variables);
                log.debug("Handled bot task " + task + ", " + bot + " by " + taskHandler.getClass());
            }
            ProcessExecutionErrors.removeProcessError(task.getProcessId(), task.getName());
        } catch (TaskAlreadyCompletedException e) {
            log.warn(task + " already handled");
            ProcessExecutionErrors.removeProcessError(task.getProcessId(), task.getName());
        } catch (Throwable th) {
            ProcessExecutionErrors.addProcessError(task.getProcessId(), task.getName(), th);
            if (taskHandler != null) {
                try {
                    taskHandler.onRollback(user, variableProvider, task);
                } catch (Exception e) {
                    log.error("onRollbacl failed in task handler " + taskHandler, e);
                }
            }
            throw Throwables.propagate(th);
        }
    }

    @Override
    public void run() {
        try {
            if (task == null) {
                log.error("AsyncWorkflowBot called without task - something goes wrong.");
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
            resetTime = System.currentTimeMillis() + completedTasksHoldPeriod;
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
