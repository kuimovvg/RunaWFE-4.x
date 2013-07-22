package ru.runa.wfe.var.format;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import ru.runa.wfe.commons.TypeConversionUtil;

public class ListFormat implements VariableFormat<List<?>>, VariableFormatContainer {
    private static final Log log = LogFactory.getLog(ListFormat.class);
    private static final String LIST_DELIMITER = ", ";
    private static final String LIST_END = "]";
    private static final String LIST_START = "[";
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
    public List<?> parse(String[] source) throws Exception {
        if (source != null && source.length == 1 && source[0].startsWith(LIST_START) && source[0].endsWith(LIST_END)) {
            String s = source[0].substring(LIST_START.length(), source[0].length() - LIST_END.length());
            source = s.split(LIST_DELIMITER, -1);
        }
        ArrayList list = new ArrayList(source.length);
        VariableFormat<?> componentFormat = FormatCommons.create(componentClassName);
        for (String string : source) {
            try {
                list.add(componentFormat.parse(new String[] { string }));
            } catch (Exception e) {
                log.warn(e);
                list.add(null);
            }
        }
        return list;
    }

    @Override
    public String format(List<?> list) {
        VariableFormat<Object> componentFormat = FormatCommons.create(componentClassName);
        StringBuffer text = new StringBuffer();
        text.append(LIST_START);
        for (int i = 0; i < list.size(); i++) {
            if (i != 0) {
                text.append(LIST_DELIMITER);
            }
            Object object = list.get(i);
            object = TypeConversionUtil.convertTo(componentFormat.getJavaClass(), object);
            String value = componentFormat.format(object);
            text.append(value);
        }
        text.append(LIST_END);
        return text.toString();
    }

}
