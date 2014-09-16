package ru.runa.wf.web.ftl.method;

import ru.runa.wfe.commons.ftl.FreemarkerTag;
import ru.runa.wfe.var.dto.WfVariable;

public class DisplayVariableTag extends FreemarkerTag {
    private static final long serialVersionUID = 1L;

    @Override
    protected Object executeTag() {
        String variableName = getParameterAsString(0);
        boolean componentView = getParameterAs(boolean.class, 1);
        WfVariable variable = variableProvider.getVariableNotNull(variableName);
        String html = "<span class=\"displayVariable " + variable.getDefinition().getScriptingNameWithoutDots() + "\">";
        if (componentView) {
            html += ViewUtil.getComponentOutput(user, webHelper, variableProvider.getProcessId(), variable);
        } else {
            html += ViewUtil.getOutput(user, webHelper, variableProvider.getProcessId(), variable);
        }
        html += "</span>";
        return html;
    }
}
