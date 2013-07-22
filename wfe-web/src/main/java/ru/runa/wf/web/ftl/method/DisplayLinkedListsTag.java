package ru.runa.wf.web.ftl.method;

import java.util.ArrayList;
import java.util.List;

import ru.runa.wfe.commons.ftl.FreemarkerTag;
import ru.runa.wfe.var.FileVariable;
import ru.runa.wfe.var.dto.WfVariable;
import ru.runa.wfe.var.format.FileFormat;

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
            String elementFormatClassName = ViewUtil.getElementFormatClassName(variable, 0);
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
            buffer.append("<table class=\"displayLinkedLists\" rowsCount=\"").append(rowsCount).append("\">");
            StringBuffer header = new StringBuffer();
            header.append("<tr class=\"header\">");
            boolean headerValueNotNull = false;
            for (String variableName : variableNames) {
                String headerVariableName = variableName + "_header";
                WfVariable headerVariable = variableProvider.getVariableNotNull(headerVariableName);
                String value = ViewUtil.getOutput(user, webHelper, variableProvider.getProcessId(), headerVariable);
                if (headerVariable.getValue() != null) {
                    headerValueNotNull = true;
                }
                header.append("<td><b>").append(value).append("</b></td>");
            }
            header.append("</tr>");
            if (headerValueNotNull) {
                buffer.append(header);
            }
            for (int row = 0; row < rowsCount; row++) {
                renderRow(buffer, variableNames, lists, componentFormatClassNames, row);
            }
            buffer.append("</table>");
            return buffer.toString();
        }
        return "-";
    }

    protected void renderRow(StringBuffer buffer, List<String> variableNames, List<List<?>> lists, List<String> componentFormatClassNames, int row) {
        buffer.append("<tr row=\"").append(row).append("\">");
        for (int column = 0; column < variableNames.size(); column++) {
            Object o = (lists.get(column).size() > row) ? lists.get(column).get(row) : null;
            String variableName = variableNames.get(column);
            String componentClassName = componentFormatClassNames.get(column);
            String value;
            if (FileFormat.class.getName().equals(componentClassName)) {
                value = ViewUtil.getFileOutput(webHelper, variableProvider.getProcessId(), variableName, (FileVariable) o, row, null);
            } else {
                value = ViewUtil.getOutput(user, webHelper, variableProvider.getProcessId(), variableName, componentClassName, o);
            }
            buffer.append("<td column=\"").append(column).append("\">").append(value).append("</td>");
        }
        buffer.append("</tr>");
    }

}
