package ru.runa.wfe.var;

import ru.runa.wfe.var.dto.WfVariable;

public class DelegableVariableProvider extends AbstractVariableProvider {
    final IVariableProvider delegate;

    public DelegableVariableProvider(IVariableProvider delegate) {
        this.delegate = delegate;
    }

    @Override
    public Long getProcessId() {
        return delegate.getProcessId();
    }

    @Override
    public Object getValue(String variableName) {
        return delegate.getValue(variableName);
    }

    @Override
    public WfVariable getVariable(String variableName) {
        return delegate.getVariable(variableName);
    }

}
