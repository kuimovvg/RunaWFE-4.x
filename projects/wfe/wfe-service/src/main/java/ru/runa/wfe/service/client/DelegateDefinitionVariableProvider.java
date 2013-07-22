package ru.runa.wfe.service.client;

import java.util.List;

import ru.runa.wfe.lang.SwimlaneDefinition;
import ru.runa.wfe.service.delegate.Delegates;
import ru.runa.wfe.user.User;
import ru.runa.wfe.var.AbstractVariableProvider;
import ru.runa.wfe.var.VariableDefinition;
import ru.runa.wfe.var.dto.WfVariable;

import com.google.common.base.Objects;

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
        List<VariableDefinition> variableDefinitions = Delegates.getDefinitionService().getVariables(user, definitionId);
        for (VariableDefinition variableDefinition : variableDefinitions) {
            if (Objects.equal(variableName, variableDefinition.getName())) {
                return new WfVariable(variableDefinition, null);
            }
        }
        List<SwimlaneDefinition> swimlaneDefinitions = Delegates.getDefinitionService().getSwimlanes(user, definitionId);
        for (SwimlaneDefinition swimlaneDefinition : swimlaneDefinitions) {
            if (Objects.equal(variableName, swimlaneDefinition.getName())) {
                return new WfVariable(swimlaneDefinition.toVariableDefinition(), null);
            }
        }
        return null;
    }

}
