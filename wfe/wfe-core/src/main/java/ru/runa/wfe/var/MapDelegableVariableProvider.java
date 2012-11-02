package ru.runa.wfe.var;

import java.util.Map;

public class MapDelegableVariableProvider extends AbstractVariableProvider {
    private final Map<String, ? extends Object> variables;
    private final IVariableProvider delegate;

    public MapDelegableVariableProvider(Map<String, ? extends Object> variables, IVariableProvider delegate) {
        this.variables = variables;
        this.delegate = delegate;
    }

    @Override
    public Object get(String variableName) {
        Object object = variables.get(variableName);
        if (object != null) {
            return object;
        }
        if (delegate != null) {
            return delegate.get(variableName);
        }
        return null;
    }

}
