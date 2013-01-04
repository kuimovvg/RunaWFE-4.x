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
        WfVariable variable = variableProvider.getVariable(variableName);
        VariableFormat<Object> format = FormatCommons.create(variable);
        if (format instanceof VariableDisplaySupport) {
            if (webHelper == null || variableProvider.getProcessId() == null) {
                return "";
            }
            VariableDisplaySupport<Object> displaySupport = (VariableDisplaySupport<Object>) format;
            return displaySupport.getHtml(subject, webHelper, variableProvider.getProcessId(), variableName, variable.getValue());
        } else {
            return format.format(variable.getValue());
        }
    }

}
