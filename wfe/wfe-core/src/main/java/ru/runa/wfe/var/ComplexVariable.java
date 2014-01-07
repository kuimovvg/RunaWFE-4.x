package ru.runa.wfe.var;

import java.util.HashMap;
import java.util.Map;

import com.google.common.collect.Maps;

public class ComplexVariable extends HashMap<String, Object> {
    private static final long serialVersionUID = 1L;

    @Override
    public Object get(Object key) {
        return super.get(key);
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
