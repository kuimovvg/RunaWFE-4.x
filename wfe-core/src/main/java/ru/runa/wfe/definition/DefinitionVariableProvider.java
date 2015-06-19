package ru.runa.wfe.definition;

import ru.runa.wfe.lang.ProcessDefinition;
import ru.runa.wfe.var.AbstractVariableProvider;
import ru.runa.wfe.var.ComplexVariable;
import ru.runa.wfe.var.VariableDefinition;
import ru.runa.wfe.var.dto.WfVariable;

public class DefinitionVariableProvider extends AbstractVariableProvider {
    private final ProcessDefinition processDefinition;

    public DefinitionVariableProvider(ProcessDefinition processDefinition) {
        this.processDefinition = processDefinition;
    }

    @Override
    public Long getProcessDefinitionId() {
        return processDefinition.getId();
    }

    @Override
    public String getProcessDefinitionName() {
        return processDefinition.getName();
    }

    @Override
    public Long getProcessId() {
        return null;
    }

    @Override
    public Object getValue(String variableName) {
        Object object = processDefinition.getDefaultVariableValues().get(variableName);
        if (object != null) {
            return object;
        }
        WfVariable variable = getVariable(variableName);
        if (variable != null) {
            return variable.getValue();
        }
        return null;
    }

    @Override
    public WfVariable getVariable(String variableName) {
        VariableDefinition variableDefinition = processDefinition.getVariable(variableName, true);
        if (variableDefinition != null) {
            Object value = null;
            if (variableDefinition.getUserType() != null) {
                value = new ComplexVariable(variableDefinition.getUserType());
            }
            return new WfVariable(variableDefinition, value);
        }
        return null;
    }

}
