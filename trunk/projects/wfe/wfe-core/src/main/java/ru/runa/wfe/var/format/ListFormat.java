package ru.runa.wfe.var.format;

import java.util.ArrayList;
import java.util.List;

import ru.runa.wfe.commons.TypeConversionUtil;
import ru.runa.wfe.commons.web.WebHelper;
import ru.runa.wfe.user.User;

public class ListFormat implements VariableFormat<List<?>>, VariableDisplaySupport<List<?>>, VariableFormatContainer {
    private String[] componentClassNames;

    @Override
    public Class<?> getJavaClass() {
        return List.class;
    }

    @Override
    public void setComponentClassNames(String[] componentClassNames) {
        if (componentClassNames.length == 1) {
            this.componentClassNames = componentClassNames;
        } else {
            this.componentClassNames = new String[] { StringFormat.class.getName() };
        }
    }

    @Override
    public List<?> parse(String[] source) {
        ArrayList list = new ArrayList(source.length);
        VariableFormat<?> componentFormat = FormatCommons.create(componentClassNames[0]);
        for (String string : source) {
            list.add(TypeConversionUtil.convertTo(componentFormat.getJavaClass(), string));
        }
        return list;
    }

    @Override
    public String format(List<?> obj) {
        return obj.toString();
    }

    @Override
    public String getHtml(User user, WebHelper webHelper, Long processId, String name, List<?> list) {
        StringBuffer html = new StringBuffer();
        html.append("[");
        for (int i = 0; i < list.size(); i++) {
            if (i != 0) {
                html.append(", ");
            }
            Object object = list.get(i);
            String value = FormatCommons.getVarOut(user, object, webHelper, processId, name, i, null);
            html.append(value);
        }
        html.append("]");
        return html.toString();
    }

}
