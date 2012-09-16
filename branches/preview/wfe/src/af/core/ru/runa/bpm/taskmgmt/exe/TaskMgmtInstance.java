/*
 * JBoss, Home of Professional Open Source
 * Copyright 2005, JBoss Inc., and individual contributors as indicated
 * by the @authors tag. See the copyright.txt in the distribution for a
 * full listing of individual contributors.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */
package ru.runa.bpm.taskmgmt.exe;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import ru.runa.bpm.graph.def.DelegationException;
import ru.runa.bpm.graph.exe.ExecutionContext;
import ru.runa.bpm.graph.exe.Token;
import ru.runa.bpm.instantiation.Delegation;
import ru.runa.bpm.module.exe.ModuleInstance;
import ru.runa.bpm.taskmgmt.def.AssignmentHandler;
import ru.runa.bpm.taskmgmt.def.Swimlane;
import ru.runa.bpm.taskmgmt.def.Task;
import ru.runa.bpm.taskmgmt.log.TaskCreateLog;
import ru.runa.commons.ftl.ExpressionEvaluator;

/**
 * process instance extension for managing tasks on a process instance.
 */
public class TaskMgmtInstance extends ModuleInstance {
    private static final Log log = LogFactory.getLog(TaskMgmtInstance.class);

    private static final long serialVersionUID = 1L;

    Map<String, SwimlaneInstance> swimlaneInstances;
    Set<TaskInstance> taskInstances;

    public TaskMgmtInstance() {
    }

    public static String getDefaultTaskDeadline() { // TODO: use spring
        return ResourceBundle.getBundle("common_settings").getString("default.task.deadline");
    }

    public String getDueDate(Task task) {
        if (task.getDueDate() != null) {
            return task.getDueDate();
        } else {
            return getDefaultTaskDeadline();
        }
    }

    /**
     * creates a new task instance on the given task, in the given execution
     * context.
     */
    public TaskInstance createTaskInstance(ExecutionContext executionContext, Task task) {
        // instantiate the new task instance
        TaskInstance taskInstance = new TaskInstance(task);

        // bind the task instance to the TaskMgmtInstance
        addTaskInstance(taskInstance);

        // assign an id to the task instance
        // TODO Services.assignId(taskInstance);

        Token token = executionContext.getToken();
        taskInstance.setToken(token);
        taskInstance.setProcessInstance(token.getProcessInstance());

        Date dueDate = ExpressionEvaluator.evaluateDuration(getDueDate(task), executionContext);
        taskInstance.setDueDate(dueDate);

        try {
            // update the executionContext
            executionContext.setTaskInstance(taskInstance);

            taskInstance.setDescription(task.getDescription());

            // create the task instance
            taskInstance.create(executionContext);

            // if this task instance is created for a task, perform
            // assignment
            if (task != null) {
                taskInstance.assign(executionContext);
            }

        } finally {
            // clean the executionContext
            executionContext.setTaskInstance(null);
        }

        // log this creation
        // WARNING: The events create and assign are fired in the right
        // order, but
        // the logs are still not ordered properly.
        token.addLog(new TaskCreateLog(taskInstance, taskInstance.getAssignedActorId()));
        return taskInstance;
    }

    public SwimlaneInstance getInitializedSwimlaneInstance(ExecutionContext executionContext, Swimlane swimlane, boolean reassign) {
        // initialize the swimlane
        if (swimlaneInstances == null) {
            swimlaneInstances = new HashMap<String, SwimlaneInstance>();
        }
        SwimlaneInstance swimlaneInstance = swimlaneInstances.get(swimlane.getName());
        boolean notInitialized = swimlaneInstance == null;
        if (notInitialized) {
            swimlaneInstance = new SwimlaneInstance(swimlane);
            addSwimlaneInstance(executionContext, swimlaneInstance);
        }
        if (notInitialized || reassign) {
            // assign the swimlaneInstance
            performAssignment(swimlane.getDelegation(), swimlaneInstance, executionContext);
        }
        return swimlaneInstance;
    }

    public void performAssignment(Delegation assignmentDelegation, Assignable assignable, ExecutionContext executionContext) {
        try {
            AssignmentHandler assignmentHandler = (AssignmentHandler) assignmentDelegation.getInstance();
            assignmentHandler.assign(assignable, executionContext);
        } catch (Exception exception) {
            throw new DelegationException(exception, executionContext);
        }
    }

    /**
     * creates a task instance on the rootToken, and assigns it to the currently
     * authenticated user.
     */
    public TaskInstance createStartTaskInstance(ExecutionContext executionContext, Long actorId) {
        TaskInstance taskInstance = null;
        Task startTask = executionContext.getProcessDefinition().getStartStateNotNull().getFirstTaskNotNull();
        if (startTask != null) {
            taskInstance = createTaskInstance(executionContext, startTask);
            taskInstance.setActorId(executionContext, actorId.toString());
        }
        return taskInstance;
    }

    /**
     * is the collection of {@link TaskInstance}s on the given token that are
     * not ended.
     */
    public Collection<TaskInstance> getUnfinishedTasks(Token token) {
        Collection<TaskInstance> unfinishedTasks = new ArrayList<TaskInstance>();
        if (taskInstances != null) {
            Iterator<TaskInstance> iter = taskInstances.iterator();
            while (iter.hasNext()) {
                TaskInstance task = iter.next();
                if ((!task.hasEnded()) && (token != null) && (token.equals(task.getToken()))) {
                    unfinishedTasks.add(task);
                }
            }
        }
        return unfinishedTasks;
    }

    /**
     * is the collection of {@link TaskInstance}s for the given token that can
     * trigger the token to continue.
     */
    public Collection<TaskInstance> getSignallingTasks(ExecutionContext executionContext) {
        Collection<TaskInstance> signallingTasks = new ArrayList<TaskInstance>();
        if (taskInstances != null) {
            Iterator<TaskInstance> iter = taskInstances.iterator();
            while (iter.hasNext()) {
                TaskInstance taskInstance = iter.next();
                if (taskInstance.isSignalling() && (executionContext.getToken().equals(taskInstance.getToken()))) {
                    signallingTasks.add(taskInstance);
                }
            }
        }
        return signallingTasks;
    }

    /**
     * returns all the taskInstances for the this process instance. This
     * includes task instances that have been completed previously.
     */
    public Collection<TaskInstance> getTaskInstances() {
        return taskInstances;
    }

    public void addTaskInstance(TaskInstance taskInstance) {
        if (taskInstances == null) {
            taskInstances = new HashSet<TaskInstance>();
        }
        taskInstances.add(taskInstance);
        taskInstance.setTaskMgmtInstance(this);
    }

    // swimlane instances
    // ///////////////////////////////////////////////////////

    public Map<String, SwimlaneInstance> getSwimlaneInstances() {
        return swimlaneInstances;
    }

    public void addSwimlaneInstance(ExecutionContext executionContext, SwimlaneInstance swimlaneInstance) {
        if (swimlaneInstances == null) {
            swimlaneInstances = new HashMap<String, SwimlaneInstance>();
        }
        swimlaneInstances.put(swimlaneInstance.getName(), swimlaneInstance);
        swimlaneInstance.setTaskMgmtInstanceWithSync(executionContext, this);
    }

    public SwimlaneInstance getSwimlaneInstance(String swimlaneName) {
        return swimlaneInstances != null ? swimlaneInstances.get(swimlaneName) : null;
    }

    /**
     * removes signalling capabilities from all task instances related to the
     * given token.
     */
    public void removeSignalling(Token token) {
        if (taskInstances != null) {
            Iterator<TaskInstance> iter = taskInstances.iterator();
            while (iter.hasNext()) {
                TaskInstance taskInstance = iter.next();
                if ((token != null) && (token.equals(taskInstance.getToken()))) {
                    taskInstance.setSignalling(false);
                }
            }
        }
    }
}
