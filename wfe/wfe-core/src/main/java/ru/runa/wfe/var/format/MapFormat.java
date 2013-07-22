package ru.runa.wfe.var.format;

import java.util.Map;

public class MapFormat implements VariableFormat<Map<?, ?>>, VariableFormatContainer {
    private String keyFormatClassName;
    private String valueFormatClassName;

    @Override
    public Class<?> getJavaClass() {
        return Map.class;
    }

    @Override
    public void setComponentClassNames(String[] componentClassNames) {
        if (componentClassNames.length == 2 && componentClassNames[0] != null && componentClassNames[1] != null) {
            keyFormatClassName = componentClassNames[0];
            valueFormatClassName = componentClassNames[1];
        } else {
            keyFormatClassName = StringFormat.class.getName();
            valueFormatClassName = StringFormat.class.getName();
        }
    }

    @Override
    public String getComponentClassName(int index) {
        if (index == 0) {
            return keyFormatClassName;
        }
        if (index == 1) {
            return valueFormatClassName;
        }
        return null;
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
