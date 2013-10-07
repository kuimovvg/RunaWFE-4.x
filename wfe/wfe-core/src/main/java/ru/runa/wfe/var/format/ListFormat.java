package ru.runa.wfe.var.format;

import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.json.simple.JSONArray;
import org.json.simple.parser.JSONParser;

import ru.runa.wfe.commons.TypeConversionUtil;

import com.google.common.collect.Lists;

@SuppressWarnings({ "unchecked", "rawtypes" })
public class ListFormat implements VariableFormat, VariableFormatContainer {
    private static final Log log = LogFactory.getLog(ListFormat.class);
    private String componentClassName;

    @Override
    public Class<?> getJavaClass() {
        return List.class;
    }

    @Override
    public void setComponentClassNames(String[] componentClassNames) {
        if (componentClassNames.length == 1 && componentClassNames[0] != null) {
            componentClassName = componentClassNames[0];
        } else {
            componentClassName = StringFormat.class.getName();
        }
    }

    @Override
    public String getComponentClassName(int index) {
        if (index == 0) {
            return componentClassName;
        }
        return null;
    }

    @Override
    public List<?> parse(String json) throws Exception {
        JSONParser parser = new JSONParser();
        JSONArray array = (JSONArray) parser.parse(json);
        List result = Lists.newArrayListWithExpectedSize(array.size());
        VariableFormat componentFormat = FormatCommons.create(componentClassName);
        for (String string : (List<String>) array) {
            try {
                result.add(componentFormat.parse(String.valueOf(string)));
            } catch (Exception e) {
                log.warn(e);
                result.add(null);
            }
        }
        return result;
    }

    @Override
    public String format(Object object) {
        List<?> list = (List<?>) object;
        JSONArray array = new JSONArray();
        VariableFormat componentFormat = FormatCommons.create(componentClassName);
        for (Object o : list) {
            o = TypeConversionUtil.convertTo(componentFormat.getJavaClass(), o);
            String value = componentFormat.format(o);
            array.add(value);
        }
        return array.toJSONString();
    }
}
