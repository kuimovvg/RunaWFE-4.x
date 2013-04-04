package ru.runa.wf.web.ftl.method;

import ru.runa.wfe.commons.ftl.FreemarkerTag;
import ru.runa.wfe.var.dto.WfVariable;
import freemarker.template.TemplateModelException;

public class DisplayVariableTag extends FreemarkerTag {
    private static final long serialVersionUID = 1L;

    @Override
    protected Object executeTag() throws TemplateModelException {
        String variableName = getParameterAs(String.class, 0);
        WfVariable variable = variableProvider.getVariableNotNull(variableName);
        String html = "<span class=\"displayVariable\">";
        html += ViewUtil.getVariableValueHtml(user, webHelper, variableProvider.getProcessId(), variable);
        html += "</span>";
        return html;
    }

}
