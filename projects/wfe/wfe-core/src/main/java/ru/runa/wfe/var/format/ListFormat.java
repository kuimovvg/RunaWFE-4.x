package ru.runa.wfe.var.format;

import java.util.ArrayList;
import java.util.List;

import javax.security.auth.Subject;

import ru.runa.wfe.commons.web.WebHelper;

public class ListFormat implements VariableFormat<List<?>>, VariableDisplaySupport<List<?>> {

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
    public String getHtml(Subject subject, WebHelper webHelper, Long processId, String name, List<?> list) {
        StringBuffer html = new StringBuffer();
        html.append("<div class=\"listFormatItem\">");
        for (int i = 0; i < list.size(); i++) {
            Object object = list.get(i);
            String value = FormatCommons.getVarOut(object, subject, webHelper, processId, name, i, null);
            html.append(value);
        }
        html.append("</div>");
        return html.toString();
    }

}
