package ru.runa.wf.web.ftl.method;

import java.util.ArrayList;
import java.util.List;

import ru.runa.wfe.commons.ftl.FreemarkerTag;
import ru.runa.wfe.var.dto.WfVariable;
import ru.runa.wfe.var.format.FormatCommons;
import ru.runa.wfe.var.format.VariableFormat;

import com.google.common.collect.Lists;

import freemarker.template.TemplateModelException;

public class DisplayLinkedListsTag extends FreemarkerTag {
    private static final long serialVersionUID = 1L;

    @Override
    protected Object executeTag() throws TemplateModelException {
        List<String> variableNames = Lists.newArrayList();
        List<VariableFormat> componentFormats = Lists.newArrayList();
        List<List<?>> lists = Lists.newArrayList();
        String firstParameter = getParameterAsString(0);
        boolean componentView = !"false".equals(firstParameter);
        int i = ("false".equals(firstParameter) || "true".equals(firstParameter)) ? 1 : 0;
        int rowsCount = 0;
        while (true) {
            String variableName = getParameterAsString(i);
            if (variableName == null) {
                break;
            }
            WfVariable variable = variableProvider.getVariableNotNull(variableName);
            VariableFormat componentFormat = FormatCommons.createComponent(variable, 0);
            List<Object> list = (List<Object>) variable.getValue();
            if (list == null) {
                list = new ArrayList<Object>();
            }
            variableNames.add(variableName);
            componentFormats.add(componentFormat);
            lists.add(list);
            if (list.size() > rowsCount) {
                rowsCount = list.size();
            }
            i++;
        }
        if (variableNames.size() > 0) {
            StringBuffer html = new StringBuffer();
            html.append("<table class=\"displayLinkedLists\" rowsCount=\"").append(rowsCount).append("\">");
            html.append(ViewUtil.generateTableHeader(variableNames, variableProvider, null));
            for (int row = 0; row < rowsCount; row++) {
                renderRow(html, variableNames, lists, componentFormats, componentView, row);
            }
            html.append("</table>");
            return html.toString();
        }
        return "-";
    }

    protected void renderRow(StringBuffer html, List<String> variableNames, List<List<?>> lists, List<VariableFormat> componentFormats,
            boolean componentView, int row) {
        html.append("<tr row=\"").append(row).append("\">");
        for (int column = 0; column < variableNames.size(); column++) {
            Object o = (lists.get(column).size() > row) ? lists.get(column).get(row) : null;
            VariableFormat componentFormat = componentFormats.get(column);
            renderColumn(html, variableNames.get(column), componentFormat, o, componentView, row, column);
        }
        html.append("</tr>");
    }

    protected void renderColumn(StringBuffer html, String variableName, VariableFormat componentFormat, Object value, boolean componentView, int row,
            int column) {
        String inputName = variableName + "[" + row + "]";
        html.append("<td column=\"").append(column).append("\">");
        html.append(getComponentOutput(inputName, componentFormat, value, componentView, row));
        html.append("</td>");
    }

    protected String getComponentOutput(String variableName, VariableFormat componentFormat, Object value, boolean componentView, int row) {
        if (componentView) {
            return ViewUtil.getComponentOutput(user, webHelper, variableProvider.getProcessId(), variableName, componentFormat, value);
        } else {
            return ViewUtil.getOutput(user, webHelper, variableProvider.getProcessId(), variableName, componentFormat, value);
        }
    }

}
