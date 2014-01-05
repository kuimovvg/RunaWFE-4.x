package ru.runa.wf.web.ftl.method;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import ru.runa.wfe.commons.ftl.AjaxFreemarkerTag;
import ru.runa.wfe.var.dto.WfVariable;
import ru.runa.wfe.var.format.FormatCommons;
import ru.runa.wfe.var.format.StringFormat;
import ru.runa.wfe.var.format.VariableFormat;

import com.google.common.collect.Lists;

import freemarker.template.TemplateModelException;

/**
 * shared code with {@link InputVariableTag}.
 * 
 * @author dofs
 * @since 4.0.5
 */
public class EditLinkedListsTag extends AjaxFreemarkerTag {
    private static final long serialVersionUID = 1L;

    @Override
    protected String renderRequest() throws TemplateModelException {
        boolean allowToAddElements = getParameterAs(boolean.class, 0);
        boolean allowToChangeElements = getParameterAs(boolean.class, 1);
        boolean allowToDeleteElements = getParameterAs(boolean.class, 2);
        List<String> variableNames = Lists.newArrayList();
        List<VariableFormat> componentFormats = Lists.newArrayList();
        List<List<?>> lists = Lists.newArrayList();
        StringBuffer rowTemplate = new StringBuffer();
        StringBuffer jsHandlers = new StringBuffer();
        StringBuffer jsVariableNamesArray = new StringBuffer();
        int i = 3;
        int rowsCount = 0;
        while (true) {
            String variableName = getParameterAsString(i);
            if (variableName == null) {
                break;
            }
            WfVariable variable = variableProvider.getVariable(variableName);
            VariableFormat componentFormat = FormatCommons.createComponent(variable, 0);
            List<Object> list = variableProvider.getValue(List.class, variableName);
            if (list == null) {
                list = new ArrayList<Object>();
            }
            if (variableNames.size() != 0) {
                jsVariableNamesArray.append(", ");
            }
            jsVariableNamesArray.append("\"").append(variableName).append("\"");
            variableNames.add(variableName);
            componentFormats.add(componentFormat);
            lists.add(list);
            jsHandlers.append(ViewUtil.getComponentJSFunction(componentFormat));
            rowTemplate.append("<td>");
            String inputTag = getComponentInput(variableName + "[]", componentFormat, null, true);
            inputTag = inputTag.replaceAll("\"", "'");
            rowTemplate.append(inputTag);
            rowTemplate.append("</td>");
            if (list.size() > rowsCount) {
                rowsCount = list.size();
            }
            i++;
        }
        if (variableNames.size() > 0) {
            StringBuffer html = new StringBuffer();
            Map<String, String> substitutions = new HashMap<String, String>();
            substitutions.put("ROW_TEMPLATE", rowTemplate.toString());
            substitutions.put("JS_HANDLERS", jsHandlers.toString());
            substitutions.put("VARIABLE_NAMES", jsVariableNamesArray.toString());
            html.append(exportScript("scripts/EditLinkedListsTag.js", substitutions, true));
            html.append("<table id=\"editLinkedLists\" class=\"editLinkedLists\" rowsCount=\"").append(rowsCount).append("\">");
            String operationsColumn = null;
            if (allowToAddElements || allowToDeleteElements) {
                operationsColumn = "<th style=\"width: 30px;\">";
                if (allowToAddElements) {
                    operationsColumn += "<input type=\"button\" id=\"editLinkedListsButtonAdd\" value=\" + \" />";
                }
                operationsColumn += "</th>";
            }
            html.append(ViewUtil.generateTableHeader(variableNames, variableProvider, operationsColumn));
            for (String variableName : variableNames) {
                html.append(ViewUtil.getHiddenInput(variableName + ".size", StringFormat.class, rowsCount));
            }
            for (int row = 0; row < rowsCount; row++) {
                renderRow(html, variableNames, lists, componentFormats, row, allowToChangeElements, allowToDeleteElements);
            }
            html.append("</table>");
            return html.toString();
        }
        return "-";
    }

    protected void renderRow(StringBuffer html, List<String> variableNames, List<List<?>> lists, List<VariableFormat> componentFormats, int row,
            boolean allowToChangeElements, boolean allowToDeleteElements) {
        html.append("<tr row=\"").append(row).append("\">");
        for (int column = 0; column < variableNames.size(); column++) {
            Object o = (lists.get(column).size() > row) ? lists.get(column).get(row) : null;
            VariableFormat componentFormat = componentFormats.get(column);
            renderColumn(html, variableNames.get(column), componentFormat, o, row, column, allowToChangeElements);
        }
        if (allowToDeleteElements) {
            html.append("<td><input type='button' value=' - ' onclick=\"removeRow(this);\" /></td>");
        }
        html.append("</tr>");
    }

    protected void renderColumn(StringBuffer html, String variableName, VariableFormat componentFormat, Object value, int row, int column, boolean enabled) {
        String inputName = variableName + "[" + row + "]";
        html.append("<td column=\"").append(column).append("\">");
        html.append(getComponentInput(inputName, componentFormat, value, enabled));
        html.append("</td>");
    }

    protected String getComponentInput(String inputName, VariableFormat componentFormat, Object value, boolean enabled) {
        if (enabled) {
            return ViewUtil.getComponentInput(user, webHelper, inputName, componentFormat, value);
        }
        String html = ViewUtil.getComponentOutput(user, webHelper, variableProvider.getProcessId(), inputName, componentFormat, value);
        html += ViewUtil.getHiddenInput(inputName, componentFormat.getClass(), value);
        return html;
    }

    @Override
    public void processAjaxRequest(HttpServletRequest request, HttpServletResponse response) throws Exception {
    }

}
