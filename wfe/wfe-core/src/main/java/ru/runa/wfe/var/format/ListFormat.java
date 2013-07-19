package ru.runa.wfe.var.format;

import java.util.ArrayList;
import java.util.List;

import ru.runa.wfe.commons.TypeConversionUtil;
import ru.runa.wfe.commons.web.WebHelper;
import ru.runa.wfe.user.User;

public class ListFormat implements VariableFormat<List<?>>, VariableDisplaySupport<List<?>>, VariableFormatContainer {
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
            this.componentClassName = componentClassNames[0];
        } else {
            this.componentClassName = StringFormat.class.getName();
        }
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
            list.add(componentFormat.parse(new String[] { string }));
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

    @Override
    public String getHtml(User user, WebHelper webHelper, Long processId, String name, List<?> list) {
        StringBuffer html = new StringBuffer();
        html.append(LIST_START);
        for (int i = 0; i < list.size(); i++) {
            if (i != 0) {
                html.append(LIST_DELIMITER);
            }
            Object object = list.get(i);
            String value = FormatCommons.getVarOut(user, object, webHelper, processId, name, i, null);
            html.append(value);
        }
        html.append(LIST_END);
        return html.toString();
    }

}
