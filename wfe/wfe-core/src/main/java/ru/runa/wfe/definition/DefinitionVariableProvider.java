package ru.runa.wfe.definition;

import java.util.List;

import ru.runa.wfe.var.AbstractVariableProvider;
import ru.runa.wfe.var.VariableDefinition;
import ru.runa.wfe.var.dto.WfVariable;

import com.google.common.base.Objects;

public class DefinitionVariableProvider extends AbstractVariableProvider {
    private final List<VariableDefinition> variableDefinitions;

    public DefinitionVariableProvider(List<VariableDefinition> variableDefinitions) {
        this.variableDefinitions = variableDefinitions;
    }

    @Override
    public Long getProcessId() {
        return null;
    }

    @Override
    public Object getValue(String variableName) {
        return null;
    }

    @Override
    public WfVariable getVariable(String variableName) {
        for (VariableDefinition variableDefinition : variableDefinitions) {
            if (Objects.equal(variableName, variableDefinition.getName())) {
                return new WfVariable(variableDefinition, null);
            }
        }
        return null;
    }

}
