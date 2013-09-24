package ru.runa.wfe.extension.handler;

import org.dom4j.Element;

public class ParamDef {
    private final String name;
    private String variableName;
    private String value;
    private boolean optional;

    public ParamDef(Element element) {
        name = element.attributeValue("name");
        variableName = element.attributeValue("variable");
        value = element.attributeValue("value");
        optional = Boolean.parseBoolean(element.attributeValue("optional", "false"));
    }

    public String getName() {
        return name;
    }

    public boolean isOptional() {
        return optional;
    }

    public String getVariableName() {
        return variableName;
    }

    public void setVariableName(String variableName) {
        this.variableName = variableName;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
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
