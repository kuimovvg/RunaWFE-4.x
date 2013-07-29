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
            StringBuffer html = new StringBuffer();
            html.append("<table class=\"displayLinkedLists\" rowsCount=\"").append(rowsCount).append("\">");
            StringBuffer header = new StringBuffer();
            header.append("<tr class=\"header\">");
            boolean headerVisible = false;
            for (String variableName : variableNames) {
                String headerVariableName = variableName + "_header";
                Object value = variableProvider.getValue(headerVariableName);
                if (value != null) {
                    headerVisible = true;
                }
                header.append("<td><b>").append(value != null ? value : "&nbsp;").append("</b></td>");
            }
            header.append("</tr>");
            if (headerVisible) {
                html.append(header);
            }
            for (int row = 0; row < rowsCount; row++) {
                renderRow(html, variableNames, lists, componentFormatClassNames, row);
            }
            html.append("</table>");
            return html.toString();
        }
        return "-";
    }

    protected void renderRow(StringBuffer html, List<String> variableNames, List<List<?>> lists, List<String> componentFormatClassNames, int row) {
        html.append("<tr row=\"").append(row).append("\">");
        for (int column = 0; column < variableNames.size(); column++) {
            Object o = (lists.get(column).size() > row) ? lists.get(column).get(row) : null;
            String componentClassName = componentFormatClassNames.get(column);
            renderColumn(html, variableNames.get(column), componentClassName, o, row, column);
        }
        html.append("</tr>");
    }

    protected void renderColumn(StringBuffer html, String variableName, String componentClassName, Object value, int row, int column) {
        String inputName = variableName + "[" + row + "]";
        html.append("<td column=\"").append(column).append("\">");
        html.append(getComponentOutput(inputName, componentClassName, value, row));
        html.append("</td>");
    }

    protected String getComponentOutput(String variableName, String componentClassName, Object value, int row) {
        if (FileFormat.class.getName().equals(componentClassName)) {
            return ViewUtil.getFileOutput(webHelper, variableProvider.getProcessId(), variableName, (FileVariable) value, row, null);
        } else {
            return ViewUtil.getComponentOutput(user, variableName, componentClassName, value);
        }
    }

}
