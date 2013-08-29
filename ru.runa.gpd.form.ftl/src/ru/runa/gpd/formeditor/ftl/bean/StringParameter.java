package ru.runa.gpd.formeditor.ftl.bean;

import org.apache.commons.lang.StringEscapeUtils;
import org.eclipse.ui.views.properties.PropertyDescriptor;
import org.eclipse.ui.views.properties.TextPropertyDescriptor;

public class StringParameter extends ComponentParameter<String> {

    @Override
    protected String convertValueToString() {
        return rawValue;
    }

    @Override
    public String getNullValue() {
        return "";
    }

    @Override
    public PropertyDescriptor createPropertyDescriptor(int propertyId) {
        TextPropertyDescriptor stringPropertyDescriptor = new TextPropertyDescriptor(propertyId, param.label);
        return stringPropertyDescriptor;
    }

    @Override
    protected String convertValueFromString(String valueStr) {
        return StringEscapeUtils.unescapeXml(valueStr);
    }

}
