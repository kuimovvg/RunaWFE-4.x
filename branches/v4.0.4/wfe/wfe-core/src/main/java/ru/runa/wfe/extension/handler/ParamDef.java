package ru.runa.wfe.extension.handler;

import org.dom4j.Element;

public class ParamDef {
    private final String name;
    private final String variableName;
    private final String value;

    public ParamDef(Element element) {
        name = element.attributeValue("name");
        variableName = element.attributeValue("variable");
        value = element.attributeValue("value");
    }

    public String getName() {
        return name;
    }

    public String getVariableName() {
        return variableName;
    }

    public String getValue() {
        return value;
    }

    @Override
    public String toString() {
        StringBuffer b = new StringBuffer(name);
        if (variableName != null) {
            b.append(" [var=").append(variableName).append("]");
        }
        if (value != null) {
            b.append(" [value=").append(value).append("]");
        }
        return b.toString();
    }

}
