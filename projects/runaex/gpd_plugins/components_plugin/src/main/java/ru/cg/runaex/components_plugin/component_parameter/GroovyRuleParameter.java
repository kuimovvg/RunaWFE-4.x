package ru.cg.runaex.components_plugin.component_parameter;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.codec.binary.StringUtils;
import org.eclipse.ui.views.properties.PropertyDescriptor;

import ru.cg.runaex.components.bean.component.part.GroovyRuleComponentPart;
import ru.cg.runaex.components.parser.ComponentParser;
import ru.cg.runaex.components_plugin.property_editor.require_rule.GroovyRulePropertyDescriptor;
import ru.runa.gpd.formeditor.ftl.bean.ComponentParameter;

public class GroovyRuleParameter extends ComponentParameter<GroovyRuleComponentPart> {

    @Override
    protected String convertValueToString() {
    	return Base64.encodeBase64String(StringUtils.getBytesUtf8(rawValue.getGroovyScript()));
    }

    @Override
    protected GroovyRuleComponentPart convertValueFromString(String valueStr) {
        return ComponentParser.parseGroovyRule(valueStr);
    }

    @Override
    public GroovyRuleComponentPart getNullValue() {
        return new GroovyRuleComponentPart("");
    }

    @Override
    public PropertyDescriptor createPropertyDescriptor(int propertyId) {
        return new GroovyRulePropertyDescriptor(propertyId, param.label);
    }

}
