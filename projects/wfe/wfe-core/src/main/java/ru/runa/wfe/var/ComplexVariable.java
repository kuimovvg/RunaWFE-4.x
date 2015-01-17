package ru.runa.wfe.var;

import java.util.HashMap;
import java.util.Map;

import ru.runa.wfe.InternalApplicationException;

import com.google.common.collect.Maps;

public class ComplexVariable extends HashMap<String, Object> {
    private static final long serialVersionUID = 1L;
    private final VariableUserType userType;

    public ComplexVariable(VariableUserType userType) {
        this.userType = userType;
    }

    public ComplexVariable(VariableDefinition variableDefinition) {
        this(variableDefinition.getUserType());
    }

    public VariableUserType getUserType() {
        return userType;
    }

    public void mergeAttribute(String name, Object value) {
        int firstDotIndex = name.indexOf(VariableUserType.DELIM);
        if (firstDotIndex != -1) {
            String attributeName = name.substring(0, firstDotIndex);
            VariableDefinition attributeDefinition = userType.getAttributeNotNull(attributeName);
            if (attributeDefinition.getUserType() == null) {
                throw new InternalApplicationException("Trying to set complex value to non-complex attribute: " + name);
            }
            String nameRemainder = name.substring(firstDotIndex + 1);
            ComplexVariable existing = (ComplexVariable) get(attributeName);
            if (existing == null) {
                existing = new ComplexVariable(attributeDefinition);
                put(attributeName, existing);
            }
            existing.mergeAttribute(nameRemainder, value);
        } else {
            put(name, value);
        }
    }

    @Override
    public Object get(Object key) {
        String attributeName = (String) key;
        int dotIndex = attributeName.indexOf(VariableUserType.DELIM);
        if (dotIndex != -1) {
            String embeddedComplexVariable = attributeName.substring(0, dotIndex);
            String embeddedAttributeName = attributeName.substring(dotIndex + 1);
            ComplexVariable embeddedVariable = (ComplexVariable) super.get(embeddedComplexVariable);
            if (embeddedVariable != null) {
                return embeddedVariable.get(embeddedAttributeName);
            }
        }
        return super.get(key);
    }

    public Object get(Object key, Object defaultValue) {
        Object value = get(key);
        return value != null ? value : defaultValue;
    }

    public Map<String, Object> expand(String prefix) {
        Map<String, Object> result = Maps.newHashMap();
        for (Map.Entry<String, Object> entry : entrySet()) {
            String name = prefix + VariableUserType.DELIM + entry.getKey();
            if (entry.getValue() instanceof ComplexVariable) {
                result.putAll(((ComplexVariable) entry.getValue()).expand(name));
            } else {
                result.put(name, entry.getValue());
            }
        }
        return result;
    }

    @Override
    public boolean isEmpty() {
        for (VariableDefinition attributeDefinition : userType.getAttributes()) {
            Object object = get(attributeDefinition.getName());
            if (object instanceof ComplexVariable) {
                if (!((ComplexVariable) object).isEmpty()) {
                    return false;
                }
            } else {
                if (object != null) {
                    return false;
                }
            }
        }
        return true;
    }
}
