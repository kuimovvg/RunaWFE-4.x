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

import ru.runa.service.client.DelegateProcessVariableProvider;
import ru.runa.service.delegate.Delegates;
import ru.runa.service.wf.ExecutionService;
import ru.runa.wfe.bot.BotRunner;
import ru.runa.wfe.handler.bot.ITaskHandler;
import ru.runa.wfe.presentation.BatchPresentationFactory;
import ru.runa.wfe.security.AuthenticationException;
import ru.runa.wfe.security.AuthorizationException;
import ru.runa.wfe.task.dto.WfTask;
import ru.runa.wfe.var.IVariableProvider;

import com.google.common.base.Objects;
import com.google.common.collect.Maps;

/**
 * Created on 03.03.2005
 * 
 */
public class MultitaskBotRunner extends BotRunner {

    protected static final Log log = LogFactory.getLog(MultitaskBotRunner.class);

    private final int maxTaskListLength;

    private final Map<String, ITaskHandler> taskHandlerMap = new HashMap<String, ITaskHandler>();

    private static Set<WfTask> taskInProgressSet = new HashSet<WfTask>();

    private final Map<String, Integer> timeoutMap = new HashMap<String, Integer>();

    private final long startTimeout;

    public MultitaskBotRunner(Subject subject, int maxTaskListLength, long startTimeout) {
        super(subject);
        this.maxTaskListLength = maxTaskListLength;
        this.startTimeout = startTimeout;
    }

    public void addTask(String taskName, ITaskHandler taskHandler, int timeout) {
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

    public int getTaskListSize() {
        return getTasks().size();
    }

    private static Timer taskTimeoutTimer = null;

    private synchronized Timer getTaskTimeoutTimer() {
        if (taskTimeoutTimer == null) {
            taskTimeoutTimer = new Timer();
        }
        return taskTimeoutTimer;

    }

    private boolean isTaskInProgress(WfTask taskStub) {
        synchronized (taskInProgressSet) {
            return taskInProgressSet.contains(taskStub);
        }
    }

    private void addTaskInProgress(WfTask taskStub) {
        synchronized (taskInProgressSet) {
            if (!taskInProgressSet.add(taskStub)) {
                throw new IllegalStateException("Task " + taskStub + " already in progress");
            }
        }
    }

    private void removeTaskInProgress(WfTask taskStub) {
        synchronized (taskInProgressSet) {
            if (!taskInProgressSet.remove(taskStub)) {
                throw new IllegalStateException("Task " + taskStub + " was not executed");
            }
        }
    }

    @Override
    public void execute() throws Exception {
        if (startTimeout > 0) {
            Thread.sleep(startTimeout);
        }
        Set<WfTask> tasksToDoSet = getTasks();
        for (WfTask taskStub : tasksToDoSet) {
            Integer timeout = timeoutMap.get(taskStub.getName());
            if (timeout != null) {
                getTaskTimeoutTimer().schedule(new TaskTimeoutTimerTask(taskStub, this), timeout.intValue());
            }
        }

        for (Iterator<WfTask> iter = tasksToDoSet.iterator(); iter.hasNext();) {
            WfTask currentTaskStub = iter.next();
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
    }

    private Set<WfTask> getTasks() throws AuthorizationException, AuthenticationException {
        ExecutionService executionService = Delegates.getExecutionService();
        List<WfTask> receivedTasks = executionService.getTasks(getSubject(), BatchPresentationFactory.TASKS.createNonPaged());
        Set<WfTask> tasksToDoSet = new HashSet<WfTask>();
        tasksToDoSet.addAll(receivedTasks);
        return tasksToDoSet;
    }

    private void doTask(WfTask task) throws Exception {
        ITaskHandler taskHandler = taskHandlerMap.get(task.getName());
        if (taskHandler != null) {
            IVariableProvider variableProvider = new DelegateProcessVariableProvider(getSubject(), task.getProcessId());
            log.info("Starting bot task " + task.getName() + " in process " + task.getProcessId() + " with config " + taskHandler.getConfiguration());
            Map<String, Object> variables = taskHandler.handle(getSubject(), variableProvider, task);
            if (variables == null) {
                variables = Maps.newHashMap();
            }
            Object skipTaskCompletion = variables.remove(ITaskHandler.SKIP_TASK_COMPLETION_VARIABLE_NAME);
            if (Objects.equal(Boolean.TRUE, skipTaskCompletion)) {
                log.info("Bot task '" + task + "' postponed (skipTaskCompletion) by task handler " + taskHandler.getClass());
            } else {
                Delegates.getExecutionService().completeTask(getSubject(), task.getId(), variables);
                log.info("Bot task '" + task + "' completed by task handler " + taskHandler.getClass() + ", bot = " + this);
            }
        } else {
            log.warn("No handler for bot task " + task + ", bot " + this);
        }
    }

    private class TaskTimeoutTimerTask extends TimerTask {

        private final WfTask taskStub;

        private final MultitaskBotRunner bot;

        public TaskTimeoutTimerTask(WfTask taskStub, MultitaskBotRunner bot) {
            this.taskStub = taskStub;
            this.bot = bot;

        }

        @Override
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
