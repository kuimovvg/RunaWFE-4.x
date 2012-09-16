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
package ru.runa.bpm.graph.exe;

import java.util.Map;

import ru.runa.bpm.context.exe.ContextInstance;
import ru.runa.bpm.graph.def.ExecutableProcessDefinition;
import ru.runa.bpm.graph.def.Node;
import ru.runa.bpm.graph.def.Transition;
import ru.runa.bpm.taskmgmt.def.Task;
import ru.runa.bpm.taskmgmt.exe.TaskInstance;
import ru.runa.bpm.taskmgmt.exe.TaskMgmtInstance;

import com.google.common.base.Preconditions;
import com.google.common.collect.Maps;

public class ExecutionContext {
    private final ExecutableProcessDefinition processDefinition;
    private final Token token;
    // TODO [not error-prone]
    private Node transitionSource = null;
    private Task task = null;
    private TaskInstance taskInstance = null;
    private ProcessInstance subProcessInstance = null;
    private Map<String, Object> transientVariables = Maps.newHashMap();

    public ExecutionContext(ExecutableProcessDefinition processDefinition, Token token) {
        this.processDefinition = processDefinition;
        this.token = token;
    }

    public ExecutionContext(ExecutableProcessDefinition processDefinition, ProcessInstance processInstance) {
        this.processDefinition = processDefinition;
        this.token = processInstance.getRootToken();
    }

    /**
     * retrieves the transient variable for the given name.
     */
    public Object getTransientVariable(String name) {
        return transientVariables.get(name);
    }

    /**
     * sets the transient variable for the given name to the given value.
     */
    public void setTransientVariable(String name, Object value) {
        transientVariables.put(name, value);
    }

    public Node getNode() {
        return getToken().getNode();
    }

    public ExecutableProcessDefinition getProcessDefinition() {
        return processDefinition;
    }

    public ProcessInstance getProcessInstance() {
        ProcessInstance processInstance = getToken().getProcessInstance();
        Preconditions.checkNotNull(processInstance, "processInstance");
        return processInstance;
    }

    @Override
    public String toString() {
        return "ExecutionContext[" + token + "]";
    }

    // convenience methods
    // //////////////////////////////////////////////////////

    /**
     * set a process variable.
     */
    public void setVariable(String name, Object value) {
        if (taskInstance != null) {
            taskInstance.setVariable(this, name, value);
        } else {
            getContextInstance().setVariable(this, name, value, getToken());
        }
    }

    /**
     * get a process variable.
     */
    public Object getVariable(String name) {
        if (taskInstance != null) {
            return taskInstance.getVariable(name);
        } else {
            return getContextInstance().getVariable(name, getToken());
        }
    }

    /**
     * leave this node over the given transition. This method is only available
     * on node actions. Not on actions that are executed on events. Actions on
     * events cannot change the flow of execution.
     */
    public void leaveNode(Transition transition) {
        getNode().leave(this, transition);
    }

    public ContextInstance getContextInstance() {
        return getProcessInstance().getContextInstance();
    }

    public TaskMgmtInstance getTaskMgmtInstance() {
        return getProcessInstance().getTaskMgmtInstance();
    }

    // getters and setters
    // //////////////////////////////////////////////////////

    public void setTaskInstance(TaskInstance taskInstance) {
        this.taskInstance = taskInstance;
        this.task = (taskInstance != null ? taskInstance.getTask() : null);
    }

    public Token getToken() {
        Preconditions.checkNotNull(token, "token");
        return token;
    }

    public Node getTransitionSource() {
        return transitionSource;
    }

    public void setTransitionSource(Node transitionSource) {
        this.transitionSource = transitionSource;
    }

    public Task getTask() {
        return task;
    }

    public TaskInstance getTaskInstance() {
        return taskInstance;
    }

    public ProcessInstance getSubProcessInstance() {
        return subProcessInstance;
    }

    public void setSubProcessInstance(ProcessInstance subProcessInstance) {
        this.subProcessInstance = subProcessInstance;
    }

}
