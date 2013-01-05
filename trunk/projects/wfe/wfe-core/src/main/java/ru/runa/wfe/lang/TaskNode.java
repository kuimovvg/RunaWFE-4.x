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

import org.springframework.beans.factory.annotation.Autowired;

import ru.runa.wfe.execution.ExecutionContext;
import ru.runa.wfe.task.Task;
import ru.runa.wfe.task.TaskFactory;

/**
 * is a node that relates to one or more tasks. Property <code>signal</code>
 * specifies how task completion triggers continuation of execution.
 */
public class TaskNode extends InteractionNode implements Synchronizable {
    private static final long serialVersionUID = 1L;

    @Autowired
    private TaskFactory taskFactory;

    private boolean async;

    @Override
    public boolean isAsync() {
        return async;
    }

    @Override
    public void setAsync(boolean async) {
        this.async = async;
    }

    @Override
    public NodeType getNodeType() {
        return NodeType.TaskNode;
    }

    @Override
    public void execute(ExecutionContext executionContext) {
        for (TaskDefinition taskDefinition : taskDefinitions) {
            Task task = taskFactory.create(executionContext, taskDefinition);
            taskFactory.assign(executionContext, taskDefinition, task);
            taskFactory.notify(executionContext, task);
        }
        // check if we should continue execution
        if (async) {
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
}
