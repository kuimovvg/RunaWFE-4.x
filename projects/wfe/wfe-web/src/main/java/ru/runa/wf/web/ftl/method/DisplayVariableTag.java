package ru.runa.wf.web.ftl.method;

import ru.runa.wfe.commons.ftl.FreemarkerTag;
import ru.runa.wfe.var.dto.WfVariable;
import ru.runa.wfe.var.format.ListFormat;
import freemarker.template.TemplateModelException;

public class DisplayVariableTag extends FreemarkerTag {
    private static final long serialVersionUID = 1L;

    @Override
    protected Object executeTag() throws TemplateModelException {
        String variableName = getParameterAs(String.class, 0);
        boolean componentView = getParameterAs(boolean.class, 1);
        WfVariable variable = variableProvider.getVariableNotNull(variableName);
        if (componentView) {
            if (ListFormat.class.getName().equals(variable.getFormatClassNameNotNull())) {
                ViewListTag tag = new ViewListTag();
                tag.initChained(this);
                return tag.executeTag();
            }
            return ViewUtil.getComponentOutput(user, webHelper, variableProvider.getProcessId(), variableName, 
                       variable.getFormatClassNameNotNull(), variable.getValue());
        } else {
            String html = "<span class=\"displayVariable\">";
            html += ViewUtil.getOutput(user, webHelper, variableProvider.getProcessId(), variable);
            html += "</span>";
            return html;
        }
    }
}
