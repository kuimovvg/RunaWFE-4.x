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
import java.util.Timer;
import java.util.TimerTask;

import javax.security.auth.Subject;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import ru.runa.af.AuthenticationException;
import ru.runa.af.AuthorizationException;
import ru.runa.af.bot.Bot;
import ru.runa.af.bot.BotException;
import ru.runa.delegate.DelegateFactory;
import ru.runa.wf.TaskStub;
import ru.runa.wf.presentation.WFProfileStrategy;
import ru.runa.wf.service.ExecutionService;

/**
 * Created on 03.03.2005
 * 
 */
public class WorkflowMultitaskBot extends Bot {

    protected static final Log log = LogFactory.getLog(WorkflowMultitaskBot.class);

    private final int maxTaskListLength;

    private final Map<String, TaskHandler> taskHandlerMap = new HashMap<String, TaskHandler>();

    private static Set<TaskStub> taskInProgressSet = new HashSet<TaskStub>();

    private final Map<String, Integer> timeoutMap = new HashMap<String, Integer>();

    private final long startTimeout;

    public WorkflowMultitaskBot(Subject subject, int maxTaskListLength, long startTimeout) {
        super(subject);
        this.maxTaskListLength = maxTaskListLength;
        this.startTimeout = startTimeout;
    }

    public void addTask(String taskName, TaskHandler taskHandler, int timeout) {
        taskHandlerMap.put(taskName, taskHandler);
        timeoutMap.put(taskName, timeout);
    }

    public void removeTask(String taskName) {
        taskHandlerMap.remove(taskName);
    }

    public String[] getAllowedTaskNames() {
        Set<String> keySet = taskHandlerMap.keySet();
        return keySet.toArray(new String[keySet.size()]);
    }

    public int getMaxAllowedTaskListSize() {
        return maxTaskListLength;
    }

    public int getTaskListSize() throws BotException {
        try {
            return getTasks().size();
        } catch (AuthorizationException e) {
            throw new BotException(e);
        } catch (AuthenticationException e) {
            throw new BotException(e);
        }
    }

    private static Timer taskTimeoutTimer = null;

    private Timer getTaskTimeoutTimer() {
        if (taskTimeoutTimer == null) {
            taskTimeoutTimer = new Timer();
        }
        return taskTimeoutTimer;

    }

    private boolean isTaskInProgress(TaskStub taskStub) {
        synchronized (taskInProgressSet) {
            return taskInProgressSet.contains(taskStub);
        }
    }

    private void addTaskInProgress(TaskStub taskStub) {
        synchronized (taskInProgressSet) {
            if (!taskInProgressSet.add(taskStub)) {
                throw new IllegalStateException("Task " + taskStub + " already in progress");
            }
        }
    }

    private void removeTaskInProgress(TaskStub taskStub) {
        synchronized (taskInProgressSet) {
            if (!taskInProgressSet.remove(taskStub)) {
                throw new IllegalStateException("Task " + taskStub + " was not executed");
            }
        }
    }

    public void execute() throws BotException {
        try {
            if (startTimeout > 0) {
                Thread.sleep(startTimeout);
            }
            Set<TaskStub> tasksToDoSet = getTasks();
            for (TaskStub taskStub : tasksToDoSet) {
                Integer timeout = timeoutMap.get(taskStub.getName());
                if (timeout != null) {
                    getTaskTimeoutTimer().schedule(new TaskTimeoutTimerTask(taskStub, this), timeout.intValue());
                }
            }

            for (Iterator<TaskStub> iter = tasksToDoSet.iterator(); iter.hasNext();) {
                TaskStub currentTaskStub = iter.next();
                if (!isTaskInProgress(currentTaskStub)) {
                    try {
                        addTaskInProgress(currentTaskStub);
                        doTask(currentTaskStub);
                        iter.remove();
                    } catch (Throwable e) {
                        log.error("Unable to handle task " + currentTaskStub + ", bot " + this + " because of " + e.getMessage(), e);
                    } finally {
                        removeTaskInProgress(currentTaskStub);
                    }
                }
            }
        } catch (Exception e) {
            throw new BotException(e);
        }
    }

    private Set<TaskStub> getTasks() throws AuthorizationException, AuthenticationException {
        ExecutionService executionService = DelegateFactory.getInstance().getExecutionService();
        List<TaskStub> receivedTasks = executionService.getTasks(getSubject(), WFProfileStrategy.TASK_DEFAULT_BATCH_PRESENTATION_FACTORY
                .getDefaultBatchPresentation());
        Set<TaskStub> tasksToDoSet = new HashSet<TaskStub>();
        tasksToDoSet.addAll(receivedTasks);
        return tasksToDoSet;
    }

    protected void doTask(TaskStub taskStub) throws TaskHandlerException {
        TaskHandler taskHandler = taskHandlerMap.get(taskStub.getName());
        if (taskHandler != null) {
            taskHandler.handle(getSubject(), taskStub);
            log.debug("Handled task " + taskStub + ", bot " + this);
        } else {
            log.warn("No handler for task " + taskStub + ", bot " + this);
        }
    }

    private class TaskTimeoutTimerTask extends TimerTask {

        private final TaskStub taskStub;

        private final WorkflowMultitaskBot bot;

        public TaskTimeoutTimerTask(TaskStub taskStub, WorkflowMultitaskBot bot) {
            this.taskStub = taskStub;
            this.bot = bot;

        }

        public void run() {
            try {
                if (getTasks().contains(taskStub)) {
                    log.warn("Task time out:" + taskStub + ", bot " + bot);
                }
            } catch (Exception e) {
                log.error("Unable to monitor task " + taskStub + ", bot " + bot, e);
            }
        }
    }
}
