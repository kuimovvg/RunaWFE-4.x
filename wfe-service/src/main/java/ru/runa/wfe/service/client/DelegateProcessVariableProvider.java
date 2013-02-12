package ru.runa.wfe.service.client;

import ru.runa.wfe.service.delegate.Delegates;
import ru.runa.wfe.user.User;
import ru.runa.wfe.var.AbstractVariableProvider;
import ru.runa.wfe.var.dto.WfVariable;

/**
 * Implementation which uses service call for each variable retrieval (through
 * RunaWFE delegates).
 * 
 * @author Dofs
 * @since 4.0
 */
public class DelegateProcessVariableProvider extends AbstractVariableProvider {
    private final User user;
    private final Long processId;

    public DelegateProcessVariableProvider(User user, Long processId) {
        this.user = user;
        this.processId = processId;
    }

    @Override
    public Long getProcessId() {
        return processId;
    }

    @Override
    public Object getValue(String variableName) {
        return getVariableNotNull(variableName).getValue();
    }

    @Override
    public WfVariable getVariable(String variableName) {
        return Delegates.getExecutionService().getVariable(user, processId, variableName);
    }
}
