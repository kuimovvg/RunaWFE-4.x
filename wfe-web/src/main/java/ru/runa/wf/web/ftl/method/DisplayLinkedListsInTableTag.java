package ru.runa.wf.web.ftl.method;

import java.util.ArrayList;
import java.util.List;

import ru.runa.wfe.commons.ftl.FreemarkerTag;
import freemarker.template.TemplateModelException;

public class DisplayLinkedListsInTableTag extends FreemarkerTag {
    private static final long serialVersionUID = 1L;

    @Override
    protected Object executeTag() throws TemplateModelException {
        try {
            List<List<?>> lists = new ArrayList<List<?>>();
            int i = 0;
            int rowsCount = 0;
            while (true) {
                String listVarName = getParameterAs(String.class, i);
                if (listVarName == null) {
                    break;
                }
                List<?> list = getVariableAs(List.class, listVarName, false);
                lists.add(list);
                if (list.size() > rowsCount) {
                    rowsCount = list.size();
                }
                i++;
            }
            if (lists.size() > 0) {
                StringBuffer buffer = new StringBuffer();
                buffer.append("<table class=\"displayLinkedLists\">");
                for (i = 0; i < rowsCount; i++) {
                    buffer.append("<tr>");
                    for (List<?> list : lists) {
                        Object o = (list.size() > i) ? list.get(i) : "";
                        buffer.append("<td>").append(o).append("</td>");
                    }
                    buffer.append("</tr>");
                }
                buffer.append("</table>");
                return buffer.toString();
            }
            return "-";
        } catch (Exception e) {
            throw new TemplateModelException(e);
        }
    }

}
