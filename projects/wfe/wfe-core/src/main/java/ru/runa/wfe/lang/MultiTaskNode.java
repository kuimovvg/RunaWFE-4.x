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

import ru.runa.wfe.commons.TypeConversionUtil;
import ru.runa.wfe.execution.ExecutionContext;
import ru.runa.wfe.execution.Token;
import ru.runa.wfe.task.Task;
import ru.runa.wfe.task.TaskFactory;
import ru.runa.wfe.user.Executor;

import com.google.common.base.Preconditions;

/**
 * is a node that relates to one or more tasks. Property <code>signal</code>
 * specifies how task completion triggers continuation of execution.
 */
public class MultiTaskNode extends InteractionNode implements Synchronizable {
    private static final long serialVersionUID = 1L;

    @Autowired
    private TaskFactory taskFactory;

    private TaskExecutionMode mode;
    private boolean async;
    private String executorsVariableName;

    @Override
    public void validate() {
        super.validate();
        Preconditions.checkNotNull(executorsVariableName, "executorsVariableName");
    }

    @Override
    public boolean isAsync() {
        return async;
    }

    @Override
    public void setAsync(boolean async) {
        this.async = async;
    }

    public void setMode(TaskExecutionMode mode) {
        this.mode = mode;
    }

    public String getExecutorsVariableName() {
        return executorsVariableName;
    }

    public void setExecutorsVariableName(String executorsVariableName) {
        this.executorsVariableName = executorsVariableName;
    }

    @Override
    public NodeType getNodeType() {
        return NodeType.MultiTaskNode;
    }

    @Override
    public void execute(ExecutionContext executionContext) {
        TaskDefinition taskDefinition = getFirstTaskNotNull();
        List<Object> executorIds = executionContext.getVariableProvider().getValueNotNull(List.class, executorsVariableName);
        boolean tasksCreated = false;
        for (Object executorId : executorIds) {
            Executor executor = TypeConversionUtil.convertTo(executorId, Executor.class);
            Task task = taskFactory.create(executionContext, taskDefinition);
            task.assignExecutor(executionContext, executor, false);
            taskFactory.notify(executionContext, task);
            tasksCreated = true;
        }
        // check if we should continue execution
        if (async || !tasksCreated) {
            leave(executionContext);
        }
    }

    @Override
    public void leave(ExecutionContext executionContext, Transition transition) {
        for (Task task : executionContext.getProcess().getTasks()) {
            if (executionContext.getToken().equals(task.getToken())) {
                // if this is a non-finished task and all those tasks should be
                // finished
                if (task.isActive()) {
                    // end this task
                    task.end(executionContext, transition, false);
                }
            }
        }
        super.leave(executionContext, transition);
    }

    public boolean isCompletionTriggersSignal(Task task) {
        switch (mode) {
        case first:
            return true;
        case last:
            return isLastTaskToComplete(task);
        default:
            return false;
        }
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
