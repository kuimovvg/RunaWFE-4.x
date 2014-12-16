package ru.runa.wfe.var;

import java.util.Map;
import java.util.Set;

import org.apache.commons.logging.LogFactory;

import com.google.common.base.Objects;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;

public class ScriptingComplexVariable extends ComplexVariable {
    private Set<String> changedAttributeNames = Sets.newHashSet();

    public ScriptingComplexVariable(ComplexVariable complexVariable) {
        super(complexVariable.getUserType());
        for (Map.Entry<String, Object> e : complexVariable.entrySet()) {
            super.put(e.getKey(), e.getValue());
        }
    }

    public Map<String, Object> getChangedVariables(String parentName) {
        Map<String, Object> result = Maps.newHashMap();
        for (String attributeName : changedAttributeNames) {
            Object object = super.get(attributeName);
            String variableName = parentName + VariableUserType.DELIM + attributeName;
            if (object instanceof ScriptingComplexVariable) {
                result.putAll(((ScriptingComplexVariable) object).getChangedVariables(variableName));
            } else {
                result.put(variableName, object);
            }
        }
        return result;
    }

    @Override
    public Object get(Object key) {
        Object object = super.get(key);
        if (object == null) {
            for (VariableDefinition definition : getUserType().getAttributes()) {
                if (Objects.equal(key, definition.getScriptingName())) {
                    object = super.get(definition.getName());
                    break;
                }
            }
        }
        if (object instanceof ComplexVariable) {
            object = new ScriptingComplexVariable((ComplexVariable) object);
            super.put((String) key, object);
        }
        return object;
    }

    @Override
    public Object put(String key, Object value) {
        String variableName = null;
        for (VariableDefinition definition : getUserType().getAttributes()) {
            if (Objects.equal(key, definition.getName())) {
                variableName = definition.getName();
                break;
            }
            if (Objects.equal(key, definition.getScriptingName())) {
                variableName = definition.getName();
                break;
            }
        }
        if (variableName == null) {
            variableName = key;
            LogFactory.getLog(getClass()).warn("Trying to set undefined '" + variableName + "' in " + this);
        }
        changedAttributeNames.add(variableName);
        return super.put(variableName, value);
    }

}
