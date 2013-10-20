package ru.cg.runaex.components_plugin.component_parameter;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.codec.binary.StringUtils;
import org.eclipse.ui.views.properties.PropertyDescriptor;

import ru.runa.gpd.formeditor.ftl.bean.ComponentParameter;
import ru.cg.runaex.components.bean.component.part.DefaultValue;
import ru.cg.runaex.components.bean.component.part.DefaultValueType;
import ru.cg.runaex.components.parser.ComponentParser;
import ru.cg.runaex.components_plugin.database_property_editor.DefaultValuePropertyDescriptor;

public class DefaultValueParameter extends ComponentParameter<DefaultValue> {

    @Override
    protected String convertValueToString() {
        String val = "";
        if (rawValue.getType() == DefaultValueType.EXECUTE_GROOVY) {
            String encodeBase64Value = Base64.encodeBase64String(StringUtils.getBytesUtf8(rawValue.getValue()));
            val = new StringBuilder(rawValue.getType().toString()).append(".").append(encodeBase64Value).toString();
        } else {
            val = rawValue.toString();
        }
        return val;
    }

    @Override
    public DefaultValue getNullValue() {
        return new DefaultValue(DefaultValueType.NONE, "");
    }

    @Override
    public PropertyDescriptor createPropertyDescriptor(int propertyId) {
        return new DefaultValuePropertyDescriptor(propertyId, param.label);
    }

    @Override
    protected DefaultValue convertValueFromString(String valueStr) {
        DefaultValue defaultValue = ComponentParser.parseDefaultValue(valueStr);
        if (valueStr != null) {
            DefaultValueType valueType = defaultValue.getType();
            String defaultValueStr = defaultValue.getValue() != null ? defaultValue.getValue() : "";
            defaultValue = new DefaultValue(valueType, defaultValueStr);
        }

        return defaultValue;
    }
}