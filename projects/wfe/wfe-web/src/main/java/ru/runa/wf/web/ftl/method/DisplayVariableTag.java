package ru.runa.wf.web.ftl.method;

import ru.runa.wfe.commons.ftl.FreemarkerTag;
import ru.runa.wfe.var.dto.WfVariable;
import ru.runa.wfe.var.format.ListFormat;
import ru.runa.wfe.var.format.VariableFormat;
import freemarker.template.TemplateModelException;

public class DisplayVariableTag extends FreemarkerTag {
    private static final long serialVersionUID = 1L;

    @Override
    protected Object executeTag() throws TemplateModelException {
        String variableName = getParameterAsString(0);
        boolean componentView = getParameterAs(boolean.class, 1);
        WfVariable variable = variableProvider.getVariableNotNull(variableName);
        if (componentView) {
            VariableFormat variableFormat = variable.getFormatNotNull();
            if (variableFormat instanceof ListFormat) {
                ViewListTag tag = new ViewListTag();
                tag.initChained(this);
                return tag.executeTag();
            }
            return ViewUtil.getComponentOutput(user, webHelper, variableProvider.getProcessId(), variableName, variableFormat, variable.getValue());
        } else {
            String html = "<span class=\"displayVariable\">";
            html += ViewUtil.getOutput(user, webHelper, variableProvider.getProcessId(), variable);
            html += "</span>";
            return html;
        }
    }
}
