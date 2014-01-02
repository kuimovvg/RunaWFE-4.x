package ru.runa.wfe.var;

import java.util.Map;

import ru.runa.wfe.var.dto.WfVariable;

public class MapDelegableVariableProvider extends DelegableVariableProvider {
    private final Map<String, Object> values;

    // TODO remove 1-st parameter from constructor
    public MapDelegableVariableProvider(Map<String, ? extends Object> variables, IVariableProvider delegate) {
        super(delegate);
        this.values = (Map<String, Object>) variables;
    }

    public void add(String variableName, Object object) {
        values.put(variableName, object);
    }

    public void add(WfVariable variable) {
        values.put(variable.getDefinition().getName(), variable);
    }

    public Object remove(String variableName) {
        return values.remove(variableName);
    }

    @Override
    public Object getValue(String variableName) {
        Object object = values.get(variableName);
        if (object instanceof WfVariable) {
            return ((WfVariable) object).getValue();
        }
        if (object != null) {
            return object;
        }
        return super.getValue(variableName);
    }

    @Override
    public WfVariable getVariable(String variableName) {
        Object object = values.get(variableName);
        if (object instanceof WfVariable) {
            return (WfVariable) object;
        }
        return super.getVariable(variableName);
    }
    
    @Override
    public AbstractVariableProvider getSameProvider(Long processId) {
        if (delegate instanceof AbstractVariableProvider) {
            AbstractVariableProvider same = ((AbstractVariableProvider) delegate).getSameProvider(processId);
            return new MapDelegableVariableProvider(values, same);
        }
        return super.getSameProvider(processId);
    }

}
