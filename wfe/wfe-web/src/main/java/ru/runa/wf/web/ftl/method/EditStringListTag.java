package ru.runa.wf.web.ftl.method;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import ru.runa.wfe.commons.ftl.AjaxFreemarkerTag;
import freemarker.template.TemplateModelException;

/**
 * shared code with {@link InputVariableTag}.
 * 
 * @author dofs
 * @since 3.5
 */
public class EditStringListTag extends AjaxFreemarkerTag {
    private static final long serialVersionUID = 1L;

    @Override
    protected String renderRequest() throws TemplateModelException {
        String variableName = getParameterAs(String.class, 0);
        Map<String, String> substitutions = new HashMap<String, String>();
        substitutions.put("VARIABLE", variableName);
        StringBuffer html = new StringBuffer();
        html.append(exportScript("scripts/EditStringListTag.js", substitutions, false));
        List<Object> list = variableProvider.getValue(List.class, variableName);
        if (list == null) {
            list = new ArrayList<Object>();
        }
        if (list.size() == 0) {
            list.add("");
        }
        for (int i = 0; i < list.size(); i++) {
            Object object = list.get(i);
            String value = object.toString();
            String inputId = variableName + (i + 1);
            String divId = "div" + inputId;
            html.append("<div id=\"").append(divId).append("\" style=\"margin-bottom:4px;\" class=\"cloned").append(variableName).append("\">");
            // TODO css classes in FTL tags
            html.append("<input type=\"text\" name=\"").append(variableName).append("\" id=\"").append(inputId).append("\" class=\"")
                    .append(variableName).append("\" value=\"").append(value).append("\"/>");
            html.append("<input type='button' value=' - ' onclick=\"$('#").append(divId).append("').remove();\" />");
            html.append("</div>");
        }
        html.append("<div><input type=\"button\" id=\"btnAdd").append(variableName).append("\" value=\" + \" /></div>");
        return html.toString();
    }

    @Override
    public void processAjaxRequest(HttpServletRequest request, HttpServletResponse response) throws Exception {
    }

}
