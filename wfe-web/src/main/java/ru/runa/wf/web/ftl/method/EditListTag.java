package ru.runa.wf.web.ftl.method;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import ru.runa.wfe.commons.ftl.AjaxFreemarkerTag;
import ru.runa.wfe.var.dto.WfVariable;
import freemarker.template.TemplateModelException;

/**
 * shared code with {@link InputVariableTag}.
 * 
 * @author dofs
 * @since 4.0.5
 */
public class EditListTag extends AjaxFreemarkerTag {
    private static final long serialVersionUID = 1L;

    @Override
    protected String renderRequest() throws TemplateModelException {
        String variableName = getParameterAs(String.class, 0);
        WfVariable variable = variableProvider.getVariableNotNull(variableName);
        String elementFormatClassName = ViewUtil.getElementFormatClassName(variable, 0);
        Map<String, String> substitutions = new HashMap<String, String>();
        substitutions.put("VARIABLE", variableName);
        String inputTag = ViewUtil.getComponentInput(user, variableName + "[]", elementFormatClassName, null);
        inputTag = inputTag.replaceAll("\"", "'");
        substitutions.put("COMPONENT_INPUT", inputTag);
        substitutions.put("COMPONENT_JS_HANDLER", ViewUtil.getComponentJSFunction(elementFormatClassName));
        StringBuffer html = new StringBuffer();
        html.append(exportScript("scripts/EditListTag.js", substitutions, false));
        List<Object> list = (List<Object>) variable.getValue();
        if (list == null) {
            list = new ArrayList<Object>();
        }
        // if (list.size() == 0) {
        // list.add("");
        // }
        html.append("<span class=\"editList\">");
        html.append(ViewUtil.getHiddenInput(variableName + ".size", list.size()));
        for (int row = 0; row < list.size(); row++) {
            Object value = list.get(row);
            html.append("<div row=\"").append(row).append("\" style=\"margin-bottom:4px;\" class=\"edit").append(variableName).append("\">");
            html.append(ViewUtil.getComponentInput(user, variableName + "[" + row + "]", elementFormatClassName, value));
            html.append("<input type='button' value=' - ' onclick=\"remove").append(variableName).append("(this);\" />");
            html.append("</div>");
        }
        html.append("<div><input type=\"button\" id=\"btnAdd").append(variableName).append("\" value=\" + \" /></div>");
        html.append("</span>");
        return html.toString();
    }

    @Override
    public void processAjaxRequest(HttpServletRequest request, HttpServletResponse response) throws Exception {
    }

}
