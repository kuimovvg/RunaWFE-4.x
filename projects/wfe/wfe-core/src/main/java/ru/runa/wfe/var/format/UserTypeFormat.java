package ru.runa.wfe.var.format;

import java.util.Map;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import com.google.common.collect.Maps;

import ru.runa.wfe.commons.TypeConversionUtil;
import ru.runa.wfe.var.ComplexVariable;
import ru.runa.wfe.var.VariableDefinition;
import ru.runa.wfe.var.VariableDefinitionAware;

public class UserTypeFormat implements VariableFormat, VariableDefinitionAware {
    private static final Log log = LogFactory.getLog(UserTypeFormat.class);
    private VariableDefinition variableDefinition;

    @Override
    public Class<?> getJavaClass() {
        return ComplexVariable.class;
    }

    @Override
    public String getName() {
        return buildFormatDescriptor(variableDefinition).toString();
    }

    private static JSONObject buildFormatDescriptor(VariableDefinition variableDefinition) {
        JSONObject result = new JSONObject();
        for (VariableDefinition attributeDefinition : variableDefinition.getUserType().getAttributes()) {
            Object value;
            if (attributeDefinition.isComplex()) {
                value = buildFormatDescriptor(attributeDefinition);
            } else {
                value = FormatCommons.create(attributeDefinition).getName();
            }
            result.put(attributeDefinition.getName(), value);
        }
        return result;
    }

    @Override
    public Object parse(String json) throws Exception {
        JSONParser parser = new JSONParser();
        JSONObject object = (JSONObject) parser.parse(json);
        return parse(object);
    }
    
    public ComplexVariable parse(JSONObject object) throws Exception {
        ComplexVariable result = new ComplexVariable();
        for (VariableDefinition attributeDefinition : variableDefinition.getUserType().getAttributes()) {
            try {
                VariableFormat attributeFormat = FormatCommons.create(attributeDefinition);
                Object attributeValue = object.get(attributeDefinition.getName());
                attributeValue = TypeConversionUtil.convertTo(attributeFormat.getJavaClass(), attributeValue);
                attributeValue = attributeFormat.format(attributeValue);
                result.put(attributeDefinition.getName(), attributeValue);
            } catch (Exception e) {
                log.warn(attributeDefinition.toString(), e);
            }
        }
        return result;
    }

    @Override
    public String format(Object serializable) {
        if (serializable == null) {
            return null;
        }
        ComplexVariable complexVariable = (ComplexVariable) serializable;
        JSONObject object = new JSONObject();
        for (VariableDefinition attributeDefinition : variableDefinition.getUserType().getAttributes()) {
            VariableFormat attributeFormat = FormatCommons.create(attributeDefinition);
            Object attributeValue = complexVariable.get(attributeDefinition.getName());
            attributeValue = TypeConversionUtil.convertTo(attributeFormat.getJavaClass(), attributeValue);
            attributeValue = attributeFormat.format(attributeValue);
            object.put(attributeDefinition.getName(), attributeValue);
        }
        return object.toString();
    }

    @Override
    public VariableDefinition getVariableDefinition() {
        return variableDefinition;
    }
    
    @Override
    public void setVariableDefinition(VariableDefinition variableDefinition) {
        this.variableDefinition = variableDefinition;
    }

}
