package ru.runa.wfe.var.format;

import java.util.Map;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import com.google.common.collect.Maps;

@SuppressWarnings({ "unchecked", "rawtypes" })
public class MapFormat implements VariableFormat, VariableFormatContainer {
    private static final Log log = LogFactory.getLog(MapFormat.class);
    private String keyFormatClassName;
    private String valueFormatClassName;

    @Override
    public Class<?> getJavaClass() {
        return Map.class;
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
        VariableFormat keyFormat = FormatCommons.create(keyFormatClassName);
        VariableFormat valueFormat = FormatCommons.create(valueFormatClassName);
        for (Map.Entry<String, String> entry : (Set<Map.Entry<String, String>>) object.entrySet()) {
            try {
                result.put(keyFormat.parse(entry.getKey()), valueFormat.parse(entry.getValue()));
            } catch (Exception e) {
                log.warn(entry.toString(), e);
            }
        }
        return result;
    }

    @Override
    public String format(Object map) {
        JSONObject object = new JSONObject((Map<?, ?>) map);
        return object.toJSONString();
    }

}
