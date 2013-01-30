package ru.runa.wf.web.ftl.method;

import ru.runa.wfe.commons.ftl.FreemarkerTag;
import ru.runa.wfe.var.dto.WfVariable;
import ru.runa.wfe.var.format.FormatCommons;
import ru.runa.wfe.var.format.VariableDisplaySupport;
import ru.runa.wfe.var.format.VariableFormat;
import freemarker.template.TemplateModelException;

public class DisplayVariableTag extends FreemarkerTag {
    private static final long serialVersionUID = 1L;

    @Override
    protected Object executeTag() throws TemplateModelException {
        String variableName = getParameterAs(String.class, 0);
        WfVariable variable = variableProvider.getVariableNotNull(variableName);
        VariableFormat<Object> format = FormatCommons.create(variable);
        String html = "<span class=\"displayVariable\">";
        if (format instanceof VariableDisplaySupport) {
            if (webHelper == null || variableProvider.getProcessId() == null) {
                return "";
            }
            VariableDisplaySupport<Object> displaySupport = (VariableDisplaySupport<Object>) format;
            html += displaySupport.getHtml(user, webHelper, variableProvider.getProcessId(), variableName, variable.getValue());
        } else {
            html += format.format(variable.getValue());
        }
        html += "</span>";
        return html;
    }

}
