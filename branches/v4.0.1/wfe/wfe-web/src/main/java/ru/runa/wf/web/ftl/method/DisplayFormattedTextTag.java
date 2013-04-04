package ru.runa.wf.web.ftl.method;

import ru.runa.wfe.var.dto.WfVariable;
import freemarker.template.TemplateModelException;

/**
 * @deprecated code moved to {@link DisplayVariableTag}.
 * 
 * @author dofs
 * @since 3.3
 */
@Deprecated
public class DisplayFormattedTextTag extends DisplayVariableTag {
    private static final long serialVersionUID = 1L;

    @Override
    protected Object executeTag() throws TemplateModelException {
        String variableName = getParameterAs(String.class, 0);
        WfVariable variable = variableProvider.getVariableNotNull(variableName);
        return variable.getFormatNotNull().format(variable.getValue());
    }

}
