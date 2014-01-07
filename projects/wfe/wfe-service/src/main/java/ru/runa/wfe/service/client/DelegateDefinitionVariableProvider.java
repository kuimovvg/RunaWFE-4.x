package ru.runa.wfe.service.client;

import java.util.List;
import java.util.Map;

import ru.runa.wfe.lang.SwimlaneDefinition;
import ru.runa.wfe.service.delegate.Delegates;
import ru.runa.wfe.user.User;
import ru.runa.wfe.var.AbstractVariableProvider;
import ru.runa.wfe.var.VariableDefinition;
import ru.runa.wfe.var.dto.WfVariable;

import com.google.common.base.Objects;
import com.google.common.collect.Maps;

public class DelegateDefinitionVariableProvider extends AbstractVariableProvider {
    private final User user;
    private final Long definitionId;

    public DelegateDefinitionVariableProvider(User user, Long definitionId) {
        this.user = user;
        this.definitionId = definitionId;
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
        List<VariableDefinition> variableDefinitions = Delegates.getDefinitionService().getVariableDefinitions(user, definitionId);
        Map<String, VariableDefinition> expanded = Maps.newHashMap();
        for (VariableDefinition variableDefinition : variableDefinitions) {
            expanded.put(variableDefinition.getName(), variableDefinition);
            if (variableDefinition.isComplex()) {
                for (VariableDefinition child : variableDefinition.expandComplexVariable()) {
                    expanded.put(child.getName(), child);
                }
            }
        }
        if (expanded.containsKey(variableName)) {
            return new WfVariable(expanded.get(variableName), null);
        }
        List<SwimlaneDefinition> swimlaneDefinitions = Delegates.getDefinitionService().getSwimlaneDefinitions(user, definitionId);
        for (SwimlaneDefinition swimlaneDefinition : swimlaneDefinitions) {
            if (Objects.equal(variableName, swimlaneDefinition.getName())) {
                return new WfVariable(swimlaneDefinition.toVariableDefinition(), null);
            }
        }
        return null;
    }

}
