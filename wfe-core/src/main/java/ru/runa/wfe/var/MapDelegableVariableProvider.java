package ru.runa.wfe.var;

import java.util.Map;

import ru.runa.wfe.var.dto.WfVariable;

import com.google.common.collect.Maps;

public class MapDelegableVariableProvider extends DelegableVariableProvider {
    private final Map<String, WfVariable> variables = Maps.newHashMap();
    private final Map<String, Object> values;

    // TODO remove 1-st parameter from constructor
    public MapDelegableVariableProvider(Map<String, ? extends Object> variables, IVariableProvider delegate) {
        super(delegate);
        this.values = (Map<String, Object>) variables;
    }

    public void addValue(String variableName, Object object) {
        values.put(variableName, object);
    }

    public Object removeValue(String variableName) {
        return values.remove(variableName);
    }

    public void addVariable(WfVariable variable) {
        variables.put(variable.getDefinition().getName(), variable);
    }

    public Object removeVariable(String variableName) {
        return variables.remove(variableName);
    }

    @Override
    public Object getValue(String variableName) {
        Object object = values.get(variableName);
        if (object != null) {
            return object;
        }
        WfVariable variable = variables.get(variableName);
        if (variable != null) {
            return variable.getValue();
        }
        return super.getValue(variableName);
    }

    @Override
    public WfVariable getVariable(String variableName) {
        WfVariable variable = variables.get(variableName);
        if (variable != null) {
            return variable;
        }
        return super.getVariable(variableName);
    }
}
