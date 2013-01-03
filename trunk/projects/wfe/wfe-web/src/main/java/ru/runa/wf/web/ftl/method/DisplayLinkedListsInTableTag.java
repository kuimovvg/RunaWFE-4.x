package ru.runa.wf.web.ftl.method;

import java.util.ArrayList;
import java.util.List;

import ru.runa.wfe.commons.ftl.FreemarkerTag;
import freemarker.template.TemplateModelException;

public class DisplayLinkedListsInTableTag extends FreemarkerTag {
    private static final long serialVersionUID = 1L;

    @Override
    protected Object executeTag() throws TemplateModelException {
        List<List<?>> lists = new ArrayList<List<?>>();
        int i = 0;
        int rowsCount = 0;
        while (true) {
            String listVarName = getParameterAs(String.class, i);
            if (listVarName == null) {
                break;
            }
            List<?> list = variableProvider.getValueNotNull(List.class, listVarName);
            lists.add(list);
            if (list.size() > rowsCount) {
                rowsCount = list.size();
            }
            i++;
        }
        if (lists.size() > 0) {
            StringBuffer buffer = new StringBuffer();
            buffer.append("<table class=\"displayLinkedLists\">");
            buffer.append("<tr class=\"header\">");
            for (i = 0; i < lists.size(); i++) {
                String headerVariableName = getParameterAs(String.class, i) + "_header";
                Object o = variableProvider.getValue(headerVariableName);
                String value = ViewUtil.getVarOut(o, subject, webHelper, variableProvider.getProcessId(), headerVariableName, 0, null);
                buffer.append("<td>").append(value).append("</td>");
            }
            buffer.append("</tr>");
            for (i = 0; i < rowsCount; i++) {
                buffer.append("<tr>");
                for (List<?> list : lists) {
                    Object o = (list.size() > i) ? list.get(i) : "";
                    String listVarName = getParameterAs(String.class, i);
                    String value = ViewUtil.getVarOut(o, subject, webHelper, variableProvider.getProcessId(), listVarName, i, null);
                    buffer.append("<td>").append(value).append("</td>");
                    buffer.append("<td>").append(o).append("</td>");
                }
                buffer.append("</tr>");
            }
            buffer.append("</table>");
            return buffer.toString();
        }
        return "-";
    }

}
