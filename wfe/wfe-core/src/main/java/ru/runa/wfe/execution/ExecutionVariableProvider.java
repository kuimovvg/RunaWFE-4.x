package ru.runa.wfe.execution;

import ru.runa.wfe.commons.SystemProperties;
import ru.runa.wfe.lang.ProcessDefinition;
import ru.runa.wfe.var.AbstractVariableProvider;
import ru.runa.wfe.var.ComplexVariable;
import ru.runa.wfe.var.VariableDefinition;
import ru.runa.wfe.var.VariableUserType;
import ru.runa.wfe.var.dto.WfVariable;

public class ExecutionVariableProvider extends AbstractVariableProvider {
    private final ExecutionContext executionContext;

    public ExecutionVariableProvider(ExecutionContext executionContext) {
        this.executionContext = executionContext;
    }

    @Override
    public Long getProcessId() {
        return executionContext.getProcess().getId();
    }

    @Override
    public Object getValue(String variableName) {
        WfVariable variable = getVariable(variableName);
        return variable != null ? variable.getValue() : null;
    }

    @Override
    public WfVariable getVariable(String variableName) {
        ProcessDefinition processDefinition = executionContext.getProcessDefinition();
        VariableDefinition variableDefinition = processDefinition.getVariable(variableName, true);
        if (variableDefinition == null || !variableDefinition.isPublicAccess()) {
            // TODO checkReadToVariablesAllowed(subject, task);
        }
        if (variableDefinition != null) {
            Object variableValue;
            if (variableDefinition.isComplex()) {
                variableValue = loadComplexVariable(variableDefinition.getName(), variableDefinition);
            } else {
                variableValue = executionContext.getVariableValue(variableName);
            }
            return new WfVariable(variableDefinition, variableValue);
        }
        if (SystemProperties.isV3CompatibilityMode() || SystemProperties.isAllowedNotDefinedVariables()) {
            return new WfVariable(variableName, executionContext.getVariableValue(variableName));
        }
        log.warn("No variable defined by name '" + variableName + "', returning null");
        return null;
    }

    private ComplexVariable loadComplexVariable(String prefix, VariableDefinition variableDefinition) {
        ComplexVariable complexVariable = new ComplexVariable();
        for (VariableDefinition attributeDefinition : variableDefinition.getUserType().getAttributes()) {
            String fullName = prefix + VariableUserType.DELIM + attributeDefinition.getName();
            Object value;
            if (attributeDefinition.isComplex()) {
                value = loadComplexVariable(fullName, attributeDefinition);
            } else {
                value = executionContext.getVariableValue(fullName);
            }
            complexVariable.put(attributeDefinition.getName(), value);
        }
        return complexVariable;
    }
    
}
