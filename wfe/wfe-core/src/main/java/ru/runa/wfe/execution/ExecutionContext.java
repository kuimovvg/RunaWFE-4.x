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
package ru.runa.wfe.execution;

import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;

import ru.runa.wfe.InternalApplicationException;
import ru.runa.wfe.audit.ProcessLog;
import ru.runa.wfe.audit.VariableDeleteLog;
import ru.runa.wfe.audit.dao.ProcessLogDAO;
import ru.runa.wfe.commons.ApplicationContextFactory;
import ru.runa.wfe.commons.SystemProperties;
import ru.runa.wfe.commons.TypeConversionUtil;
import ru.runa.wfe.execution.dao.NodeProcessDAO;
import ru.runa.wfe.lang.Node;
import ru.runa.wfe.lang.ProcessDefinition;
import ru.runa.wfe.lang.SwimlaneDefinition;
import ru.runa.wfe.task.Task;
import ru.runa.wfe.user.Executor;
import ru.runa.wfe.var.ComplexVariable;
import ru.runa.wfe.var.IVariableProvider;
import ru.runa.wfe.var.Variable;
import ru.runa.wfe.var.VariableCreator;
import ru.runa.wfe.var.VariableDefinition;
import ru.runa.wfe.var.dao.VariableDAO;
import ru.runa.wfe.var.format.StringFormat;
import ru.runa.wfe.var.format.VariableFormat;

import com.google.common.base.Objects;
import com.google.common.base.Preconditions;
import com.google.common.collect.Maps;

public class ExecutionContext {
    private static Log log = LogFactory.getLog(ExecutionContext.class);
    private final ProcessDefinition processDefinition;
    private final Token token;
    private final Map<String, Object> transientVariables = Maps.newHashMap();
    @Autowired
    private VariableCreator variableCreator;
    @Autowired
    private NodeProcessDAO nodeProcessDAO;
    @Autowired
    private ProcessLogDAO processLogDAO;
    @Autowired
    private VariableDAO variableDAO;

    public ExecutionContext(ProcessDefinition processDefinition, Token token) {
        this.processDefinition = processDefinition;
        this.token = token;
        Preconditions.checkNotNull(token, "token");
        ApplicationContextFactory.getContext().getAutowireCapableBeanFactory().autowireBean(this);
    }

    public ExecutionContext(ProcessDefinition processDefinition, Process process) {
        this(processDefinition, process.getRootToken());
    }

    public ExecutionContext(ProcessDefinition processDefinition, Task task) {
        this(processDefinition, task.getToken());
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
        return getToken().getNodeNotNull(getProcessDefinition());
    }

    public ProcessDefinition getProcessDefinition() {
        return processDefinition;
    }

    public Process getProcess() {
        Process process = getToken().getProcess();
        Preconditions.checkNotNull(process, "process");
        return process;
    }

    public Token getToken() {
        return token;
    }

    public Task getTask() {
        return getProcess().getTask(getToken().getNodeId());
    }

    public NodeProcess getParentNodeProcess() {
        return nodeProcessDAO.getNodeProcessByChild(getProcess().getId());
    }

    public List<Process> getActiveSubprocesses() {
        return nodeProcessDAO.getSubprocesses(getProcess(), getToken().getNodeId(), getToken(), true);
    }

    public List<Process> getSubprocessesRecursively() {
        return nodeProcessDAO.getSubprocessesRecursive(getProcess());
    }

    /**
     * @return the variable value with the given name.
     */
    public Object getVariableValue(String name) {
        Variable<?> variable = variableDAO.get(getProcess(), name);
        if (variable != null) {
            return variable.getValue();
        }
        Swimlane swimlane = getProcess().getSwimlane(name);
        if (swimlane != null) {
            return swimlane.getExecutor();
        }
        return null;
    }

    public void setVariableValue(String name, Object value) {
        Preconditions.checkNotNull(name, "name is null");
        SwimlaneDefinition swimlaneDefinition = getProcessDefinition().getSwimlane(name);
        if (swimlaneDefinition != null) {
            log.debug("Assigning swimlane '" + name + "' value '" + value + "'");
            Swimlane swimlane = getProcess().getSwimlaneNotNull(swimlaneDefinition);
            swimlane.assignExecutor(this, TypeConversionUtil.convertTo(Executor.class, value), true);
            return;
        }
        VariableDefinition variableDefinition = getProcessDefinition().getVariable(name, false);
        if (!SystemProperties.isV3CompatibilityMode()) {
            if (variableDefinition == null && !SystemProperties.isAllowedNotDefinedVariables()) {
                throw new InternalApplicationException("Variable '" + name
                        + "' is not defined in process definition and setting 'undefined.variables.allowed'=false");
            }
            if (value != null && variableDefinition != null && SystemProperties.isStrongVariableFormatEnabled()) {
                Class<?> definedClass = variableDefinition.getFormatNotNull().getJavaClass();
                if (!definedClass.isAssignableFrom(value.getClass())) {
                    if (SystemProperties.isVariableAutoCastingEnabled()) {
                        try {
                            value = TypeConversionUtil.convertTo(definedClass, value);
                        } catch (Exception e) {
                            throw new InternalApplicationException("Variable '" + name + "' defined as '" + definedClass
                                    + "' but value is instance of '" + value.getClass() + "'", e);
                        }
                    } else {
                        throw new InternalApplicationException("Variable '" + name + "' defined as '" + definedClass + "' but value is instance of '"
                                + value.getClass() + "'");
                    }
                }
            }
        }
        if (value instanceof ComplexVariable) {
            ComplexVariable complexVariable = (ComplexVariable) value;
            Map<String, Object> expanded = complexVariable.expand(name);
            for (Map.Entry<String, Object> entry : expanded.entrySet()) {
                setVariableValue(entry.getKey(), entry.getValue());
            }
            return;
        }
        if (value == null && variableDefinition.getUserType() != null) {
            for (VariableDefinition definition : variableDefinition.expandComplexVariable(false)) {
                setVariableValue(definition.getName(), null);
            }
            return;
        }
        Variable<?> variable = variableDAO.get(getProcess(), name);
        // if there is exist variable and it doesn't support the current type
        if (variable != null && !variable.supports(value)) {
            log.debug("Variable type is changing: deleting old variable '" + name + "' from '" + this + "'");
            variableDAO.delete(variable);
            addLog(new VariableDeleteLog(variable));
            variable = null;
        }
        if (variable == null) {
            if (value != null) {
                VariableFormat format = variableDefinition != null ? variableDefinition.getFormatNotNull() : new StringFormat();
                variable = variableCreator.create(this, name, value, format);
                variableDAO.create(variable);
            }
        } else {
            if (Objects.equal(value, variable.getValue())) {
                // order is valuable due to Timestamp.equals implementation
                return;
            }
            log.debug("Updating variable '" + name + "' in '" + getProcess() + "' to '" + value + "'"
                    + (value != null ? " of " + value.getClass() : ""));
            VariableFormat format = variableDefinition != null ? variableDefinition.getFormatNotNull() : new StringFormat();
            variable.setValue(this, value, format);
        }
    }

    /**
     * Adds all the given variables. It doesn't remove any existing variables
     * unless they are overwritten by the given variables.
     */
    public void setVariableValues(Map<String, Object> variables) {
        for (Map.Entry<String, Object> entry : variables.entrySet()) {
            setVariableValue(entry.getKey(), entry.getValue());
        }
    }

    /**
     * @return variable provider for this process.
     */
    public IVariableProvider getVariableProvider() {
        return new ExecutionVariableProvider(this);
    }

    public void addLog(ProcessLog processLog) {
        processLogDAO.addLog(processLog, getProcess(), token);
    }

    @Override
    public String toString() {
        return Objects.toStringHelper(this).add("processId", getToken().getProcess().getId()).add("tokenId", getToken().getId()).toString();
    }

}
