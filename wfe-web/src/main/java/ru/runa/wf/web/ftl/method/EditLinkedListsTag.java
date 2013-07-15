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
        String jsHandlers = "";
        int i = 3;
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
            jsHandlers += ViewUtil.getComponentJSFunction(elementFormatClassName);
            rowTemplate.append("<td>");
            String inputTag = ViewUtil.getComponentInput(user, variableName, elementFormatClassName, null, true);
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
            substitutions.put("JS_HANDLERS", jsHandlers);
            html.append(exportScript("scripts/EditLinkedListsTag.js", substitutions, true));
            html.append("<table id=\"editLinkedLists\" class=\"editLinkedLists\">");
            html.append("<tr class=\"header\">");
            for (String variableName : variableNames) {
                String headerVariableName = variableName + "_header";
                Object o = variableProvider.getValue(headerVariableName);
                String value = FormatCommons.getVarOut(user, o, webHelper, variableProvider.getProcessId(), headerVariableName, 0, null);
                html.append("<td><b>").append(value).append("</b></td>");
            }
            html.append("<td>");
            if (allowToAddElements) {
                html.append("<input type=\"button\" id=\"editLinkedListsButtonAdd\" value=\" + \" />");
            }
            html.append("</td>");
            html.append("</tr>");
            for (int row = 0; row < rowsCount; row++) {
                String trId = "editLinkedLists" + (row + 1);
                html.append("<tr id=\"").append(trId).append("\" class=\"cloned\">");
                for (int column = 0; column < variableNames.size(); column++) {
                    Object o = (lists.get(column).size() > row) ? lists.get(column).get(row) : "";
                    String variableName = variableNames.get(column);
                    html.append("<td>");
                    html.append(ViewUtil.getComponentInput(user, variableName, componentFormatClassNames.get(column), o, allowToChangeElements));
                    html.append("</td>");
                }
                html.append("<td>");
                if (allowToDeleteElements) {
                    html.append("<input type='button' value=' - ' onclick=\"$('#").append(trId).append("').remove();\" />");
                }
                html.append("</td>");
                html.append("</tr>");
            }
            html.append("</table>");
            return html.toString();
        }
        return "-";
    }

    @Override
    public void processAjaxRequest(HttpServletRequest request, HttpServletResponse response) throws Exception {
    }

}
