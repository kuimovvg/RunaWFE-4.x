package ru.runa.wf.web.ftl.method;

import ru.runa.wfe.commons.ftl.FreemarkerTag;
import ru.runa.wfe.service.delegate.Delegates;
import ru.runa.wfe.var.dto.WfVariable;
import freemarker.template.TemplateModelException;

public class DisplayProcessVariableTag extends FreemarkerTag {
    private static final long serialVersionUID = 1L;

    @Override
    protected Object executeTag() throws TemplateModelException {
        Long processId = getParameterVariableNotNull(Long.class, 0);
        String variableName = getParameterAs(String.class, 1);
        boolean componentView = getParameterAs(boolean.class, 2);
        WfVariable variable = Delegates.getExecutionService().getVariable(user, processId, variableName);
        if (componentView) {
            return ViewUtil.getComponentOutput(user, variableName, variable.getFormatClassNameNotNull(), variable.getValue());
        } else {
            String html = "<span class=\"displayVariable\">";
            html += ViewUtil.getOutput(user, webHelper, processId, variable);
            html += "</span>";
            return html;
        }
    }
}
