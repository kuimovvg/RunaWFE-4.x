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

import java.util.HashSet;
import java.util.List;

import ru.runa.wfe.commons.TypeConversionUtil;
import ru.runa.wfe.execution.ExecutionContext;
import ru.runa.wfe.execution.Token;
import ru.runa.wfe.lang.utils.MultiInstanceParameters;
import ru.runa.wfe.task.Task;
import ru.runa.wfe.user.Executor;

import com.google.common.base.Preconditions;

/**
 * is a node that relates to one or more tasks. Property <code>signal</code>
 * specifies how task completion triggers continuation of execution.
 */
public class MultiTaskNode extends BaseTaskNode {
    private static final long serialVersionUID = 1L;

    private TaskExecutionMode executionMode;
    private String executorsDiscriminator;
    private String executorsDiscriminatorUsage;

    @Override
    public void validate() {
        super.validate();
        Preconditions.checkNotNull(executorsDiscriminator, "executorsVariableName in " + this);
    }

    public String getExecutorsDiscriminatorUsage() {
        return executorsDiscriminatorUsage;
    }

    public void setExecutorsDiscriminatorUsage(String executorsDiscriminatorMode) {
        this.executorsDiscriminatorUsage = executorsDiscriminatorMode;
    }

    public String getExecutorsDiscriminator() {
        return executorsDiscriminator;
    }

    public void setExecutorsDiscriminator(String executorsDiscriminator) {
        this.executorsDiscriminator = executorsDiscriminator;
    }

    public TaskExecutionMode getExecutionMode() {
        return executionMode;
    }

    public void setExecutionMode(TaskExecutionMode executionMode) {
        this.executionMode = executionMode;
    }

    @Override
    public NodeType getNodeType() {
        return NodeType.MULTI_TASK_STATE;
    }

    @Override
    public void execute(ExecutionContext executionContext) {
        TaskDefinition taskDefinition = getFirstTaskNotNull();
        MultiInstanceParameters parameters = new MultiInstanceParameters(executionContext, this);
        List<?> executors = (List<?>) parameters.getDiscriminatorValue();
        boolean tasksCreated = false;
        for (Object executorIdentity : new HashSet<Object>(executors)) {
            Executor executor = TypeConversionUtil.convertTo(Executor.class, executorIdentity);
            if (executor == null) {
                log.debug("Executor is null for identity " + executorIdentity);
                continue;
            }
            taskFactory.create(executionContext, taskDefinition, null, executor);
            tasksCreated = true;
        }
        if (!tasksCreated) {
            log.debug("no tasks were created in " + this);
        }
        // check if we should continue execution
        if (async || !tasksCreated) {
            log.debug("continue execution " + this);
            leave(executionContext);
        }
    }

    public boolean isCompletionTriggersSignal(Task task) {
        switch (executionMode) {
        case FIRST:
            return true;
        case LAST:
            return isLastTaskToComplete(task);
        default:
            return false;
        }
    }

    private boolean isLastTaskToComplete(Task task) {
        Token token = task.getToken();
        boolean lastToComplete = true;
        for (Task other : token.getTasks()) {
            if (!other.equals(task)) {
                lastToComplete = false;
                break;
            }
        }
        return lastToComplete;
    }

}
