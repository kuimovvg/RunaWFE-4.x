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
package ru.runa.wfe.lang;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;

import ru.runa.wfe.execution.ExecutionContext;
import ru.runa.wfe.execution.Token;
import ru.runa.wfe.task.Task;
import ru.runa.wfe.task.TaskFactory;

import com.google.common.collect.Lists;

/**
 * is a node that relates to one or more tasks. Property <code>signal</code> specifies how task completion triggers continuation of execution.
 */
public class TaskNode extends InteractionNode {
    private static final long serialVersionUID = 1L;
    /**
     * execution always continues, regardless whether tasks are created or still unfinished.
     */
    public static final int SIGNAL_UNSYNCHRONIZED = 0;
    /**
     * execution never continues, regardless whether tasks are created or still unfinished.
     */
    public static final int SIGNAL_NEVER = 1;
    /**
     * proceeds execution when the first task is completed. when no tasks are created on entrance of this node, execution is continued.
     */
    public static final int SIGNAL_FIRST = 2;
    /**
     * proceeds execution when the first task is completed. when no tasks are created on entrance of this node, execution waits in the task node till tasks are created.
     */
    public static final int SIGNAL_FIRST_WAIT = 3;
    /**
     * proceeds execution when the last task is completed. when no tasks are created on entrance of this node, execution is continued.
     */
    public static final int SIGNAL_LAST = 4;
    /**
     * proceeds execution when the last task is completed. when no tasks are created on entrance of this node, execution waits in the task node till tasks are created.
     */
    public static final int SIGNAL_LAST_WAIT = 5;

    @Autowired
    private TaskFactory taskFactory;

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

    private int signal;
    private boolean endTasks;

    public void setEndTasks(boolean endTasks) {
        this.endTasks = endTasks;
    }

    public void setSignal(int signal) {
        this.signal = signal;
    }

    @Override
    public NodeType getNodeType() {
        return NodeType.Task;
    }

    @Override
    public void execute(ExecutionContext executionContext) {
        List<Task> tasks = Lists.newArrayList();
        for (TaskDefinition taskDefinition : taskDefinitions) {
            tasks.add(taskFactory.create(executionContext, taskDefinition));
        }
        // check if we should continue execution
        boolean continueExecution;
        switch (signal) {
        case SIGNAL_UNSYNCHRONIZED:
            continueExecution = true;
            break;
        case SIGNAL_FIRST:
        case SIGNAL_LAST:
            continueExecution = tasks.isEmpty();
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
        for (Task task : executionContext.getProcess().getTasks()) {
            if (executionContext.getToken().equals(task.getToken())) {
                // if this is a non-finished task and all those tasks should be finished
                if (endTasks && task.isActive()) {
                    // end this task
                    task.end(executionContext, transition, false);
                }
            }
        }
        super.leave(executionContext, transition);
    }

    public boolean isCompletionTriggersSignal(Task task) {
        boolean completionTriggersSignal;
        switch (signal) {
        case SIGNAL_FIRST:
        case SIGNAL_FIRST_WAIT:
            completionTriggersSignal = true;
            break;
        case SIGNAL_LAST:
        case SIGNAL_LAST_WAIT:
            completionTriggersSignal = isLastTaskToComplete(task);
            break;
        default:
            completionTriggersSignal = false;
        }
        return completionTriggersSignal;
    }

    private boolean isLastTaskToComplete(Task task) {
        Token token = task.getToken();
        boolean lastToComplete = true;
        for (Task other : task.getProcess().getTasks()) {
            if (token.equals(other.getToken()) && !other.equals(task) && other.isActive()) {
                lastToComplete = false;
                break;
            }
        }
        return lastToComplete;
    }

}
