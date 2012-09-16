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
package ru.runa.bpm.graph.node;

import org.dom4j.Element;

import ru.runa.bpm.graph.def.ExecutableProcessDefinition;
import ru.runa.bpm.graph.def.Transition;
import ru.runa.bpm.graph.exe.ExecutionContext;
import ru.runa.bpm.graph.exe.Token;
import ru.runa.bpm.jpdl.xml.JpdlXmlReader;
import ru.runa.bpm.jpdl.xml.Parsable;
import ru.runa.bpm.taskmgmt.def.Task;
import ru.runa.bpm.taskmgmt.exe.TaskInstance;
import ru.runa.bpm.taskmgmt.exe.TaskMgmtInstance;

/**
 * is a node that relates to one or more tasks. Property <code>signal</code>
 * specifies how task completion triggers continuation of execution.
 */
public class TaskNode extends InteractionNode implements Parsable {
    private static final long serialVersionUID = 1L;
    /**
     * execution always continues, regardless whether tasks are created or still
     * unfinished.
     */
    public static final int SIGNAL_UNSYNCHRONIZED = 0;
    /**
     * execution never continues, regardless whether tasks are created or still
     * unfinished.
     */
    public static final int SIGNAL_NEVER = 1;
    /**
     * proceeds execution when the first task instance is completed. when no
     * tasks are created on entrance of this node, execution is continued.
     */
    public static final int SIGNAL_FIRST = 2;
    /**
     * proceeds execution when the first task instance is completed. when no
     * tasks are created on entrance of this node, execution waits in the task
     * node till tasks are created.
     */
    public static final int SIGNAL_FIRST_WAIT = 3;
    /**
     * proceeds execution when the last task instance is completed. when no
     * tasks are created on entrance of this node, execution is continued.
     */
    public static final int SIGNAL_LAST = 4;
    /**
     * proceeds execution when the last task instance is completed. when no
     * tasks are created on entrance of this node, execution waits in the task
     * node till tasks are created.
     */
    public static final int SIGNAL_LAST_WAIT = 5;

    public static int parseSignal(String text) {
        if ("unsynchronized".equalsIgnoreCase(text)) {
            return SIGNAL_UNSYNCHRONIZED;
        } else if ("never".equalsIgnoreCase(text)) {
            return SIGNAL_NEVER;
        } else if ("first".equalsIgnoreCase(text)) {
            return SIGNAL_FIRST;
        } else if ("first-wait".equalsIgnoreCase(text)) {
            return SIGNAL_FIRST_WAIT;
        } else if ("last-wait".equalsIgnoreCase(text)) {
            return SIGNAL_LAST_WAIT;
        } else { // return default
            return SIGNAL_LAST;
        }
    }

    public static String signalToString(int signal) {
        if (signal == SIGNAL_UNSYNCHRONIZED) {
            return "unsynchronized";
        } else if (signal == SIGNAL_NEVER) {
            return "never";
        } else if (signal == SIGNAL_FIRST) {
            return "first";
        } else if (signal == SIGNAL_FIRST_WAIT) {
            return "first-wait";
        } else if (signal == SIGNAL_LAST) {
            return "last";
        } else if (signal == SIGNAL_LAST_WAIT) {
            return "last-wait";
        } else {
            return null;
        }
    }

    int signal = SIGNAL_LAST;
    boolean createTasks = true;
    boolean endTasks;

    @Override
    public NodeType getNodeType() {
        return NodeType.Task;
    }

    @Override
    public void read(ExecutableProcessDefinition processDefinition, Element element, JpdlXmlReader jpdlReader) {
        // get the signal
        String signalText = element.attributeValue("signal");
        if (signalText != null) {
            signal = parseSignal(signalText);
        }

        // create tasks
        String createTasksText = element.attributeValue("create-tasks");
        if (createTasksText != null) {
            if ("false".equalsIgnoreCase(createTasksText)) {
                createTasks = false;
            }
        }

        // create tasks
        String removeTasksText = element.attributeValue("end-tasks");
        if (removeTasksText != null) {
            if ("true".equalsIgnoreCase(removeTasksText)) {
                endTasks = true;
            }
        }

        // parse the tasks
        jpdlReader.readTasks(processDefinition, element, this);
    }

    @Override
    public void execute(ExecutionContext executionContext) {
        TaskMgmtInstance tmi = getTaskMgmtInstance(executionContext.getToken());
        // if this tasknode should create instances
        if (createTasks) {
            for (Task task : tasks) {
                tmi.createTaskInstance(executionContext, task);
            }
        }

        // check if we should continue execution
        boolean continueExecution;
        switch (signal) {
        case SIGNAL_UNSYNCHRONIZED:
            continueExecution = true;
            break;
        case SIGNAL_FIRST:
        case SIGNAL_LAST:
            continueExecution = tmi.getSignallingTasks(executionContext).isEmpty();
            break;
        default:
            continueExecution = false;
        }

        if (continueExecution) {
            leave(executionContext);
        }
    }

    @Override
    public void leave(ExecutionContext executionContext, Transition transition) {
        removeTaskInstanceSynchronization(executionContext, executionContext.getToken());
        super.leave(executionContext, transition);
    }

    public boolean completionTriggersSignal(TaskInstance taskInstance) {
        boolean completionTriggersSignal;
        switch (signal) {
        case SIGNAL_FIRST:
        case SIGNAL_FIRST_WAIT:
            completionTriggersSignal = true;
            break;
        case SIGNAL_LAST:
        case SIGNAL_LAST_WAIT:
            completionTriggersSignal = isLastToComplete(taskInstance);
            break;
        default:
            completionTriggersSignal = false;
        }
        return completionTriggersSignal;
    }

    private boolean isLastToComplete(TaskInstance taskInstance) {
        Token token = taskInstance.getToken();
        TaskMgmtInstance tmi = getTaskMgmtInstance(token);
        boolean lastToComplete = true;
        for (TaskInstance other : tmi.getTaskInstances()) {
            if (token != null && token.equals(other.getToken()) && !other.equals(taskInstance) && other.isSignalling() && !other.hasEnded()) {
                lastToComplete = false;
                break;
            }
        }
        return lastToComplete;
    }

    private void removeTaskInstanceSynchronization(ExecutionContext executionContext, Token token) {
        TaskMgmtInstance tmi = getTaskMgmtInstance(token);
        for (TaskInstance taskInstance : tmi.getTaskInstances()) {
            if (token.equals(taskInstance.getToken())) {
                // remove signalling
                if (taskInstance.isSignalling()) {
                    taskInstance.setSignalling(false);
                }
                // if this is a non-finished task and all those
                // tasks should be finished
                if (!taskInstance.hasEnded() && endTasks) {
                    if (tasks.contains(taskInstance.getTask())) {
                        // end this task
                        taskInstance.end(executionContext);
                    }
                }
            }
        }
    }

    private TaskMgmtInstance getTaskMgmtInstance(Token token) {
        return token.getProcessInstance().getTaskMgmtInstance();
    }

    public int getSignal() {
        return signal;
    }

    public boolean getCreateTasks() {
        return createTasks;
    }

    public boolean isEndTasks() {
        return endTasks;
    }

    public void setCreateTasks(boolean createTasks) {
        this.createTasks = createTasks;
    }

    public void setEndTasks(boolean endTasks) {
        this.endTasks = endTasks;
    }

    public void setSignal(int signal) {
        this.signal = signal;
    }

}
