package ru.runa.wfe.var.format;

import java.util.ArrayList;
import java.util.List;

import ru.runa.wfe.commons.web.WebHelper;
import ru.runa.wfe.user.User;

public class ListFormat implements VariableFormat<List<?>>, VariableDisplaySupport<List<?>> {

    @Override
    public Class<? extends List<?>> getJavaClass() {
        return (Class<? extends List<?>>) List.class;
    }

    @Override
    public List<?> parse(String[] source) {
        ArrayList list = new ArrayList(source.length);
        for (String string : source) {
            list.add(string);
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
            String value = FormatCommons.getVarOut(object, webHelper, processId, name, i, null);
            html.append(value);
        }
        html.append("]");
        return html.toString();
    }

}
