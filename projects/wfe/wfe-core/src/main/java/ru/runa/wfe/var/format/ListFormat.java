package ru.runa.wfe.var.format;

import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.json.simple.JSONArray;
import org.json.simple.parser.JSONParser;

import ru.runa.wfe.commons.TypeConversionUtil;
import ru.runa.wfe.var.VariableDefinition;
import ru.runa.wfe.var.VariableDefinitionAware;

@SuppressWarnings({ "unchecked", "rawtypes" })
public class ListFormat implements VariableFormat, VariableFormatContainer, VariableDefinitionAware {
    private static final Log log = LogFactory.getLog(ListFormat.class);
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
    public List<?> parse(String json) throws Exception {
        JSONParser parser = new JSONParser();
        JSONArray array = (JSONArray) parser.parse(json);
        return (List<?>) FormatCommons.parseJSON(this, array);
    }

    @Override
    public String format(Object object) {
        if (object == null) {
            return null;
        }
        List<?> list = (List<?>) object;
        JSONArray array = new JSONArray();
        VariableFormat componentFormat = FormatCommons.createComponent(this, 0);
        for (Object o : list) {
            o = TypeConversionUtil.convertTo(componentFormat.getJavaClass(), o);
            array.add(componentFormat.format(o));
        }
        return array.toJSONString();
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
