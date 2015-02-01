package ru.runa.wfe.var.format;

import java.util.List;
import java.util.Map;
import java.util.Set;

import org.json.simple.JSONObject;

import ru.runa.wfe.commons.TypeConversionUtil;
import ru.runa.wfe.commons.web.WebHelper;
import ru.runa.wfe.user.User;
import ru.runa.wfe.var.VariableUserType;

import com.google.common.collect.Maps;

@SuppressWarnings({ "unchecked", "rawtypes" })
public class MapFormat extends VariableFormat implements VariableFormatContainer, VariableDisplaySupport {
    public static final String KEY_NULL_VALUE = "null";
    private String keyFormatClassName;
    private String valueFormatClassName;
    // TODO find more convenient way to reference user types
    private Map<String, VariableUserType> userTypes;

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
    public Map<String, VariableUserType> getUserTypes() {
        return userTypes;
    }

    @Override
    public void setUserTypes(Map<String, VariableUserType> userTypes) {
        this.userTypes = userTypes;
    }

    @Override
    public List<?> convertFromStringValue(String json) throws Exception {
        return (List<?>) parseJSON(json);
    }

    @Override
    public String convertToStringValue(Object object) {
        return formatJSON(object);
    }

    @Override
    public Map<?, ?> convertFromJSONValue(Object json) {
        JSONObject object = (JSONObject) json;
        Map result = Maps.newHashMapWithExpectedSize(object.size());
        VariableFormat keyFormat = FormatCommons.createComponent(this, 0);
        VariableFormat valueFormat = FormatCommons.createComponent(this, 1);
        for (Map.Entry<Object, Object> entry : (Set<Map.Entry<Object, Object>>) object.entrySet()) {
            Object key = keyFormat.convertFromJSONValue(entry.getKey());
            Object value = valueFormat.convertFromJSONValue(entry.getValue());
            result.put(key, value);
        }
        return result;
    }

    @Override
    public Object convertToJSONValue(Object map) {
        JSONObject object = new JSONObject();
        VariableFormat keyFormat = FormatCommons.createComponent(this, 0);
        VariableFormat valueFormat = FormatCommons.createComponent(this, 1);
        for (Map.Entry<Object, Object> entry : ((Map<Object, Object>) map).entrySet()) {
            Object keyValue = TypeConversionUtil.convertTo(keyFormat.getJavaClass(), entry.getKey());
            Object valueValue = TypeConversionUtil.convertTo(valueFormat.getJavaClass(), entry.getValue());
            object.put(keyFormat.convertToJSONValue(keyValue), valueFormat.convertToJSONValue(valueValue));
        }
        return object;
    }

    @Override
    public String formatHtml(User user, WebHelper webHelper, Long processId, String name, Object map) {
        StringBuffer b = new StringBuffer();
        b.append("<table class=\"list\">");
        VariableFormat keyFormat = FormatCommons.createComponent(this, 0);
        VariableFormat valueFormat = FormatCommons.createComponent(this, 1);
        for (Map.Entry<Object, Object> entry : ((Map<Object, Object>) map).entrySet()) {
            b.append("<tr><td class=\"list\">");
            String value;
            Object keyValue = TypeConversionUtil.convertTo(keyFormat.getJavaClass(), entry.getKey());
            if (keyFormat instanceof VariableDisplaySupport) {
                value = ((VariableDisplaySupport) keyFormat).formatHtml(user, webHelper, processId, name, keyValue);
            } else {
                value = keyFormat.format(keyValue);
            }
            b.append(value);
            b.append("</td><td class=\"list\">");
            Object valueValue = TypeConversionUtil.convertTo(valueFormat.getJavaClass(), entry.getValue());
            if (valueFormat instanceof VariableDisplaySupport) {
                String componentName = name + COMPONENT_QUALIFIER_START + keyFormat.format(keyValue) + COMPONENT_QUALIFIER_END;
                value = ((VariableDisplaySupport) valueFormat).formatHtml(user, webHelper, processId, componentName, valueValue);
            } else {
                value = valueFormat.format(valueValue);
            }
            b.append(value);
            b.append("</td></tr>");
        }
        b.append("</table>");
        return b.toString();
    }

    @Override
    public String toString() {
        VariableFormat keyFormat = FormatCommons.createComponent(this, 0);
        VariableFormat valueFormat = FormatCommons.createComponent(this, 1);
        return getClass().getName() + "(" + keyFormat.getName() + ", " + valueFormat.getName() + ")";
    }

}
