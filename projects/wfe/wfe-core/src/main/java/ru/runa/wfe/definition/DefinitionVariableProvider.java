package ru.runa.wfe.definition;

import ru.runa.wfe.lang.ProcessDefinition;
import ru.runa.wfe.var.AbstractVariableProvider;
import ru.runa.wfe.var.VariableDefinition;
import ru.runa.wfe.var.dto.WfVariable;

public class DefinitionVariableProvider extends AbstractVariableProvider {
    private final ProcessDefinition processDefinition;

    public DefinitionVariableProvider(ProcessDefinition processDefinition) {
        this.processDefinition = processDefinition;
    }

    @Override
    public Long getProcessId() {
        return null;
    }

    @Override
    public Object getValue(String variableName) {
        return processDefinition.getDefaultVariableValues().get(variableName);
    }

    @Override
    public WfVariable getVariable(String variableName) {
        VariableDefinition variableDefinition = processDefinition.getVariable(variableName, true);
        if (variableDefinition != null) {
            return new WfVariable(variableDefinition, null);
        }
        return null;
    }

}
