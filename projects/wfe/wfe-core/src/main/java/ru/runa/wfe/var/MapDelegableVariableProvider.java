package ru.runa.wfe.var;

import java.util.Map;

import ru.runa.wfe.var.dto.WfVariable;

import com.google.common.collect.Maps;

public class MapDelegableVariableProvider extends DelegableVariableProvider {
    protected final Map<String, Object> values = Maps.newHashMap();

    public MapDelegableVariableProvider(Map<String, ? extends Object> variables, IVariableProvider delegate) {
        super(delegate);
        for (Map.Entry<String, Object> entry : ((Map<String, Object>) variables).entrySet()) {
            add(entry.getKey(), entry.getValue());
        }
    }

    public void add(String variableName, Object object) {
        values.put(variableName, object);
        if (object instanceof ComplexVariable) {
            Map<String, Object> expanded = ((ComplexVariable) object).expand(variableName);
            for (Map.Entry<String, Object> entry : expanded.entrySet()) {
                if (entry.getValue() != null) {
                    values.put(entry.getKey(), entry.getValue());
                }
            }
        }
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
        object = super.getValue(variableName);
        if (object instanceof ComplexVariable) {
            // merge local values
            ComplexVariable complexVariable = (ComplexVariable) object;
            for (Map.Entry<String, Object> entry : values.entrySet()) {
                if (entry.getKey().startsWith(variableName + VariableUserType.DELIM)) {
                    String attributeName = entry.getKey().substring(variableName.length() + VariableUserType.DELIM.length());
                    complexVariable.mergeAttribute(attributeName, entry.getValue());
                }
            }
        }
        return object;
    }

    @Override
    public WfVariable getVariable(String variableName) {
        if (values.containsKey(variableName)) {
            Object object = values.get(variableName);
            if (object instanceof WfVariable) {
                return (WfVariable) object;
            }
            WfVariable variable = super.getVariable(variableName);
            if (variable != null) {
                log.debug("Setting " + variable + " value to " + object);
                variable.setValue(object);
            }
            return variable;
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
