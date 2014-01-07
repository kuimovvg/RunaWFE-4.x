package ru.runa.wfe.var.format;

import java.util.List;

import org.apache.commons.logging.LogFactory;
import org.json.simple.JSONArray;

import ru.runa.wfe.commons.TypeConversionUtil;
import ru.runa.wfe.commons.web.WebHelper;
import ru.runa.wfe.user.User;
import ru.runa.wfe.var.VariableDefinition;
import ru.runa.wfe.var.VariableDefinitionAware;

import com.google.common.collect.Lists;

@SuppressWarnings({ "unchecked" })
public class ListFormat extends VariableFormat implements VariableFormatContainer, VariableDefinitionAware, VariableDisplaySupport {
    private String componentClassName;
    private VariableDefinition variableDefinition;

    @Override
    public Class<?> getJavaClass() {
        return List.class;
    }

    @Override
    public String getName() {
        VariableFormat componentFormat = FormatCommons.createComponent(this, 0);
        return "list(" + componentFormat.getName() + ")";
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
    public List<?> convertFromStringValue(String json) throws Exception {
        return (List<?>) parseJSON(json);
    }

    @Override
    public String convertToStringValue(Object object) {
        return formatJSON(object);
    }

    @Override
    protected Object convertFromJSONValue(Object jsonValue) {
        JSONArray array = (JSONArray) jsonValue;
        List<Object> result = Lists.newArrayListWithExpectedSize(array.size());
        VariableFormat componentFormat = FormatCommons.createComponent((VariableFormatContainer) this, 0);
        for (Object object : array) {
            try {
                result.add(componentFormat.convertFromJSONValue(object));
            } catch (Exception e) {
                LogFactory.getLog(ListFormat.class).warn("Value = " + object, e);
                result.add(null);
            }
        }
        return result;
    }

    @Override
    protected Object convertToJSONValue(Object value) {
        List<?> list = (List<?>) value;
        JSONArray array = new JSONArray();
        VariableFormat componentFormat = FormatCommons.createComponent(this, 0);
        for (Object o : list) {
            o = TypeConversionUtil.convertTo(componentFormat.getJavaClass(), o);
            array.add(componentFormat.formatJSON(o));
        }
        return array;
    }

    @Override
    public VariableDefinition getVariableDefinition() {
        return variableDefinition;
    }

    @Override
    public void setVariableDefinition(VariableDefinition variableDefinition) {
        this.variableDefinition = variableDefinition;
    }

    @Override
    public String formatHtml(User user, WebHelper webHelper, Long processId, String name, Object object, Object context) {
        List<Object> list = (List<Object>) object;
        StringBuffer b = new StringBuffer();
        b.append("<table class=\"list\">");
        VariableFormat componentFormat = FormatCommons.createComponent(this, 0);
        for (int i = 0; i < list.size(); i++) {
            b.append("<tr><td class=\"list\">");
            Object o = list.get(i);
            o = TypeConversionUtil.convertTo(componentFormat.getJavaClass(), o);
            String value;
            if (componentFormat instanceof VariableDisplaySupport) {
                value = ((VariableDisplaySupport) componentFormat).formatHtml(user, webHelper, processId, name, o, list);
            } else {
                value = componentFormat.format(o);
            }
            b.append(value);
            b.append("</td></tr>");
        }
        b.append("</table>");
        return b.toString();
    }
    
}
