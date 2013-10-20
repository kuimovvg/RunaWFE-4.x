package ru.cg.runaex.components_plugin.component_parameter;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.codec.binary.StringUtils;
import org.eclipse.ui.views.properties.PropertyDescriptor;

import ru.cg.runaex.components.GpdRunaConfigComponent;
import ru.cg.runaex.components.bean.component.part.RequireRuleComponentPart;
import ru.cg.runaex.components.parser.ComponentParser;
import ru.cg.runaex.components_plugin.property_editor.require_rule.RequireRulePropertyDescriptor;
import ru.runa.gpd.formeditor.ftl.bean.ComponentParameter;

public class RequireRuleParameter extends ComponentParameter<RequireRuleComponentPart> {

    @Override
    protected String convertValueToString() {
        String strValue = rawValue.toString();
        if (!GpdRunaConfigComponent.REQUIRED.equals(strValue) && !GpdRunaConfigComponent.NOT_REQUIRED.equals(strValue)) {
            strValue = Base64.encodeBase64String(StringUtils.getBytesUtf8(strValue));
        }

        return strValue;
    }

    @Override
    protected RequireRuleComponentPart convertValueFromString(String valueStr) {
        return ComponentParser.parseRequireRule(valueStr);
    }

    @Override
    public RequireRuleComponentPart getNullValue() {
        return new RequireRuleComponentPart(false);
    }

    @Override
    public PropertyDescriptor createPropertyDescriptor(int propertyId) {
        return new RequireRulePropertyDescriptor(propertyId, param.label);
    }

}
