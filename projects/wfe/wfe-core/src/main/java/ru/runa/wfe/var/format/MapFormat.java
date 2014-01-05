package ru.runa.wfe.var.format;

import java.util.Map;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import ru.runa.wfe.commons.TypeConversionUtil;
import ru.runa.wfe.var.VariableDefinition;
import ru.runa.wfe.var.VariableDefinitionAware;

import com.google.common.collect.Maps;

@SuppressWarnings({ "unchecked", "rawtypes" })
public class MapFormat implements VariableFormat, VariableFormatContainer, VariableDefinitionAware {
    private static final Log log = LogFactory.getLog(MapFormat.class);
    private String keyFormatClassName;
    private String valueFormatClassName;
    private VariableDefinition variableDefinition;

    @Override
    public Class<?> getJavaClass() {
        return Map.class;
    }

    @Override
    public String getName() {
        VariableFormat keyFormat = FormatCommons.createComponent(this, 0);
        VariableFormat valueFormat = FormatCommons.createComponent(this, 1);
        return "map(" + keyFormat.getName() + ", " + valueFormat.getName() + ")";
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
    public Map<?, ?> parse(String json) throws Exception {
        JSONParser parser = new JSONParser();
        JSONObject object = (JSONObject) parser.parse(json);
        Map result = Maps.newHashMapWithExpectedSize(object.size());
        VariableFormat keyFormat = FormatCommons.createComponent(this, 0);
        VariableFormat valueFormat = FormatCommons.createComponent(this, 1);
        for (Map.Entry<Object, Object> entry : (Set<Map.Entry<Object, Object>>) object.entrySet()) {
            result.put(convert(keyFormat, entry.getKey()), convert(valueFormat, entry.getValue()));
        }
        return result;
    }
    
    private Object convert(VariableFormat format, Object source) throws Exception {
        if (format instanceof UserTypeFormat) {
            return ((UserTypeFormat) format).parse((JSONObject) source);
        } else {
            return format.parse((String) source);
        }
    }

    @Override
    public String format(Object map) {
        if (map == null) {
            return null;
        }
        JSONObject object = new JSONObject();
        VariableFormat keyFormat = FormatCommons.createComponent(this, 0);
        VariableFormat valueFormat = FormatCommons.createComponent(this, 1);
        for (Map.Entry<Object, Object> entry : ((Map<Object, Object>) map).entrySet()) {
            Object keyValue = TypeConversionUtil.convertTo(keyFormat.getJavaClass(), entry.getKey());
            Object valueValue = TypeConversionUtil.convertTo(valueFormat.getJavaClass(), entry.getValue());
            object.put(keyFormat.format(keyValue), valueFormat.format(valueValue));
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
