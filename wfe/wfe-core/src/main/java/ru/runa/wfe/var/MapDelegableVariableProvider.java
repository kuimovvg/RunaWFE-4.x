package ru.runa.wfe.var;

import java.util.Map;

public class MapDelegableVariableProvider extends DelegableVariableProvider {
    private final Map<String, Object> variables;

    public MapDelegableVariableProvider(Map<String, ? extends Object> variables, IVariableProvider delegate) {
        super(delegate);
        this.variables = (Map<String, Object>) variables;
    }

    public void addValue(String variableName, Object object) {
        variables.put(variableName, object);
    }

    @Override
    public Object getValue(String variableName) {
        Object object = variables.get(variableName);
        if (object != null) {
            return object;
        }
        return super.getValue(variableName);
    }

}
