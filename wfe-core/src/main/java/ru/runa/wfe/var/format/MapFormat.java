package ru.runa.wfe.var.format;

import java.util.Map;

public class MapFormat implements VariableFormat<Map<?, ?>> {

    @Override
    public Class<?> getJavaClass() {
        return Map.class;
    }

    @Override
    public Map<?, ?> parse(String[] source) {
        throw new UnsupportedOperationException();
    }

    @Override
    public String format(Map<?, ?> object) {
        return object.toString();
    }

}
