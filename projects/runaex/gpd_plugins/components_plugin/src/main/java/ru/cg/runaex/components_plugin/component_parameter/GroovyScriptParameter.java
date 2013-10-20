package ru.cg.runaex.components_plugin.component_parameter;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.codec.binary.StringUtils;
import org.eclipse.ui.views.properties.PropertyDescriptor;
import ru.runa.gpd.formeditor.ftl.bean.ComponentParameter;

public class GroovyScriptParameter extends ComponentParameter<String> {

    @Override
    protected String convertValueToString() {
        return Base64.encodeBase64String(StringUtils.getBytesUtf8(rawValue.toString()));
    }

    @Override
    public String getNullValue() {
        return "";
    }

    @Override
    public PropertyDescriptor createPropertyDescriptor(int propertyId) {
        return new GroovyCodePropertyDescriptor(propertyId, param.label);
    }

    @Override
    protected String convertValueFromString(String valueStr) {
    	if(valueStr!=null && Base64.isBase64(valueStr)){
    		return StringUtils.newStringUtf8(Base64.decodeBase64(valueStr)); 
    	}else{
    		return valueStr;
    	}
    }
}