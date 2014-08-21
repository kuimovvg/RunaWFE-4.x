package ru.runa.wfe.var;

import java.util.HashMap;
import java.util.Map;

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
            String nameRemainder = name.substring(firstDotIndex + 1);
            Object existing = get(attributeName);
            if (existing instanceof ComplexVariable) {
                ((ComplexVariable) existing).mergeAttribute(nameRemainder, value);
            } else {
                // TODO create and put new ComplexVariable
                put(attributeName, value);
            }
        } else {
            put(name, value);
        }
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
}
