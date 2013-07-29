package ru.runa.wf.web.ftl.method;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import ru.runa.wfe.commons.ftl.AjaxFreemarkerTag;
import ru.runa.wfe.var.dto.WfVariable;
import ru.runa.wfe.var.format.StringFormat;

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
        List<String> componentFormatClassNames = Lists.newArrayList();
        List<List<?>> lists = Lists.newArrayList();
        StringBuffer rowTemplate = new StringBuffer();
        StringBuffer jsHandlers = new StringBuffer();
        StringBuffer jsVariableNamesArray = new StringBuffer();
        int i = 3;
        int rowsCount = 0;
        while (true) {
            String variableName = getParameterAs(String.class, i);
            if (variableName == null) {
                break;
            }
            WfVariable variable = variableProvider.getVariable(variableName);
            String elementFormatClassName = ViewUtil.getElementFormatClassName(variable, 0);
            List<Object> list = variableProvider.getValue(List.class, variableName);
            if (list == null) {
                list = new ArrayList<Object>();
            }
            if (variableNames.size() != 0) {
                jsVariableNamesArray.append(", ");
            }
            jsVariableNamesArray.append("\"").append(variableName).append("\"");
            variableNames.add(variableName);
            componentFormatClassNames.add(elementFormatClassName);
            lists.add(list);
            jsHandlers.append(ViewUtil.getComponentJSFunction(elementFormatClassName));
            rowTemplate.append("<td>");
            String inputTag = getComponentInput(variableName + "[]", elementFormatClassName, null, true);
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
            StringBuffer header = new StringBuffer();
            header.append("<tr class=\"header\">");
            boolean headerVisible = false;
            boolean operationsColumnVisible = allowToAddElements || allowToDeleteElements;
            for (String variableName : variableNames) {
                String headerVariableName = variableName + "_header";
                Object value = variableProvider.getValue(headerVariableName);
                if (value != null) {
                    headerVisible = true;
                }
                header.append("<td><b>").append(value != null ? value : "&nbsp;").append("</b></td>");
                html.append(ViewUtil.getHiddenInput(variableName + ".size", StringFormat.class.getName(), rowsCount));
            }
            if (operationsColumnVisible) {
                header.append("<td style=\"width: 30px;\">");
            }
            if (allowToAddElements) {
                headerVisible = true;
                header.append("<input type=\"button\" id=\"editLinkedListsButtonAdd\" value=\" + \" />");
            }
            if (operationsColumnVisible) {
                header.append("</td>");
            }
            header.append("</tr>");
            if (headerVisible) {
                html.append(header);
            }
            for (int row = 0; row < rowsCount; row++) {
                renderRow(html, variableNames, lists, componentFormatClassNames, row, allowToChangeElements, allowToDeleteElements);
            }
            html.append("</table>");
            return html.toString();
        }
        return "-";
    }

    protected void renderRow(StringBuffer html, List<String> variableNames, List<List<?>> lists, List<String> componentFormatClassNames, int row,
            boolean allowToChangeElements, boolean allowToDeleteElements) {
        html.append("<tr row=\"").append(row).append("\">");
        for (int column = 0; column < variableNames.size(); column++) {
            Object o = (lists.get(column).size() > row) ? lists.get(column).get(row) : null;
            String componentFormatClassName = componentFormatClassNames.get(column);
            renderColumn(html, variableNames.get(column), componentFormatClassName, o, row, column, allowToChangeElements);
        }
        if (allowToDeleteElements) {
            html.append("<td><input type='button' value=' - ' onclick=\"removeRow(this);\" /></td>");
        }
        html.append("</tr>");
    }

    protected void renderColumn(StringBuffer html, String variableName, String format, Object value, int row, int column, boolean enabled) {
        String inputName = variableName + "[" + row + "]";
        html.append("<td column=\"").append(column).append("\">");
        html.append(getComponentInput(inputName, format, value, enabled));
        html.append("</td>");
    }

    protected String getComponentInput(String inputName, String componentFormatClassName, Object value, boolean enabled) {
        if (enabled) {
            return ViewUtil.getComponentInput(user, inputName, componentFormatClassName, value);
        }
        String html = ViewUtil.getComponentOutput(user, inputName, componentFormatClassName, value);
        html += ViewUtil.getHiddenInput(inputName, componentFormatClassName, value);
        return html;
    }

    @Override
    public void processAjaxRequest(HttpServletRequest request, HttpServletResponse response) throws Exception {
    }

}
