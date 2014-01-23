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
import freemarker.template.TemplateModelException;

/**
 * shared code with {@link InputVariableTag}.
 * 
 * @author dofs
 * @since 4.0.5
 */
@SuppressWarnings("unchecked")
public class EditListTag extends AjaxFreemarkerTag {
    private static final long serialVersionUID = 1L;

    @Override
    protected String renderRequest() throws TemplateModelException {
        String variableName = getParameterAsString(0);
        WfVariable variable = variableProvider.getVariableNotNull(variableName);
        String scriptingVariableName = variable.getDefinition().getScriptingNameWithoutDots();
        VariableFormat componentFormat = FormatCommons.createComponent(variable, 0);
        Map<String, String> substitutions = new HashMap<String, String>();
        substitutions.put("VARIABLE", variableName);
        substitutions.put("UNIQUENAME", scriptingVariableName);
        String inputTag = ViewUtil.getComponentInput(user, webHelper, variableName + "[]", componentFormat, null);
        inputTag = inputTag.replaceAll("\"", "'").replaceAll("\t", "").replaceAll("\n", "");
        substitutions.put("COMPONENT_INPUT", inputTag);
        substitutions.put("COMPONENT_JS_HANDLER", ViewUtil.getComponentJSFunction(componentFormat));
        StringBuffer html = new StringBuffer();
        html.append(exportScript("scripts/EditListTag.js", substitutions, false));
        List<Object> list = variableProvider.getValue(List.class, variableName);
        if (list == null) {
            list = new ArrayList<Object>();
        }
        html.append("<span class=\"editList\" id=\"").append(scriptingVariableName).append("\">");
        html.append(ViewUtil.getHiddenInput(variableName + ".size", StringFormat.class, list.size()));
        for (int row = 0; row < list.size(); row++) {
            Object value = list.get(row);
            html.append("<div row=\"").append(row).append("\">");
            html.append(ViewUtil.getComponentInput(user, webHelper, variableName + "[" + row + "]", componentFormat, value));
            html.append("<input type='button' value=' - ' onclick=\"remove").append(scriptingVariableName).append("(this);\" style=\"width: 30px;\" />");
            html.append("</div>");
        }
        html.append("<div><input type=\"button\" id=\"btnAdd").append(scriptingVariableName).append("\" value=\" + \" style=\"width: 30px;\" /></div>");
        html.append("</span>");
        return html.toString();
    }

    @Override
    public void processAjaxRequest(HttpServletRequest request, HttpServletResponse response) throws Exception {
    }

}