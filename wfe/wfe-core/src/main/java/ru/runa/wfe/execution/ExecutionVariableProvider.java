package ru.runa.wfe.execution;

import ru.runa.wfe.lang.ProcessDefinition;
import ru.runa.wfe.var.AbstractVariableProvider;
import ru.runa.wfe.var.VariableDefinition;
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
        return executionContext.getVariable(variableName);
    }

    @Override
    public WfVariable getVariable(String variableName) {
        ProcessDefinition processDefinition = executionContext.getProcessDefinition();
        VariableDefinition variableDefinition = processDefinition.getVariable(variableName, true);
        if (variableDefinition == null || !variableDefinition.isPublicAccess()) {
            // TODO checkReadToVariablesAllowed(subject, task);
        }
        Object variableValue = getValue(variableName);
        if (variableDefinition != null) {
            return new WfVariable(variableDefinition, variableValue);
        }
        return new WfVariable(variableName, variableValue);
    }
}
