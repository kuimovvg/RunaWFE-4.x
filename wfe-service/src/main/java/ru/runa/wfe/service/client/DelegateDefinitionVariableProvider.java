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
    public String getProcessDefinitionName() {
        return Delegates.getDefinitionService().getProcessDefinition(user, definitionId).getName();
    }

    @Override
    public Object getValue(String variableName) {
        return null;
    }

    @Override
    public WfVariable getVariable(String variableName) {
        VariableDefinition variableDefinition = Delegates.getDefinitionService().getVariableDefinition(user, definitionId, variableName);
        if (variableDefinition != null) {
            return new WfVariable(variableDefinition, null);
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
