package ru.runa.wf.web.ftl.method;

import java.util.ArrayList;
import java.util.List;

import ru.runa.wfe.commons.ftl.FreemarkerTag;
import ru.runa.wfe.var.dto.WfVariable;
import ru.runa.wfe.var.format.FormatCommons;
import ru.runa.wfe.var.format.StringFormat;

import com.google.common.collect.Lists;

import freemarker.template.TemplateModelException;

public class DisplayLinkedListsTag extends FreemarkerTag {
    private static final long serialVersionUID = 1L;

    @Override
    protected Object executeTag() throws TemplateModelException {
        List<String> variableNames = Lists.newArrayList();
        List<String> componentFormatClassNames = Lists.newArrayList();
        List<List<?>> lists = Lists.newArrayList();
        int i = 0;
        int rowsCount = 0;
        while (true) {
            String variableName = getParameterAs(String.class, i);
            if (variableName == null) {
                break;
            }
            WfVariable variable = variableProvider.getVariableNotNull(variableName);
            String[] componentClassNames = variable.getDefinition().getFormatComponentClassNames();
            String elementFormatClassName = (componentClassNames.length > 0) ? componentClassNames[0] : StringFormat.class.getName();
            List<Object> list = (List<Object>) variable.getValue();
            if (list == null) {
                list = new ArrayList<Object>();
            }
            variableNames.add(variableName);
            componentFormatClassNames.add(elementFormatClassName);
            lists.add(list);
            if (list.size() > rowsCount) {
                rowsCount = list.size();
            }
            i++;
        }
        if (variableNames.size() > 0) {
            StringBuffer buffer = new StringBuffer();
            buffer.append("<table class=\"displayLinkedLists\">");
            StringBuffer header = new StringBuffer();
            header.append("<tr class=\"header\">");
            boolean headerValueNotNull = false;
            for (String variableName : variableNames) {
                String headerVariableName = variableName + "_header";
                Object o = variableProvider.getValue(headerVariableName);
                String value = FormatCommons.getVarOut(user, o, webHelper, variableProvider.getProcessId(), headerVariableName, 0, null);
                if (o != null) {
                    headerValueNotNull = true;
                }
                header.append("<td><b>").append(value).append("</b></td>");
            }
            header.append("</tr>");
            if (headerValueNotNull) {
                buffer.append(header);
            }
            for (i = 0; i < rowsCount; i++) {
                buffer.append("<tr>");
                for (List<?> list : lists) {
                    Object o = (list.size() > i) ? list.get(i) : "";
                    String variableName = variableNames.get(i);
                    String value = FormatCommons.getVarOut(user, o, webHelper, variableProvider.getProcessId(), variableName, i, null);
                    buffer.append("<td>").append(value).append("</td>");
                }
                buffer.append("</tr>");
            }
            buffer.append("</table>");
            return buffer.toString();
        }
        return "-";
    }

}
