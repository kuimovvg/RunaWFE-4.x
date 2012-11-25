package ru.runa.service.client;

import javax.security.auth.Subject;

import ru.runa.service.delegate.DelegateFactory;
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
    private final Subject subject;
    private final Long processId;

    public DelegateProcessVariableProvider(Subject subject, Long processId) {
        this.subject = subject;
        this.processId = processId;
    }

    @Override
    public Long getProcessId() {
        return processId;
    }

    @Override
    public Object getValue(String variableName) {
        return getVariable(variableName).getValue();
    }

    @Override
    public WfVariable getVariable(String variableName) {
        return DelegateFactory.getExecutionService().getVariable(subject, processId, variableName);
    }
}
