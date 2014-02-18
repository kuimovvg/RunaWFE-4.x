package ru.runa.wf.web.ftl.method;

import java.util.Map;

import ru.runa.wf.web.FormSubmissionUtils;
import ru.runa.wfe.commons.ftl.FreemarkerTag;
import ru.runa.wfe.var.dto.WfVariable;
import ru.runa.wfe.var.format.FormatCommons;
import ru.runa.wfe.var.format.VariableFormat;
import freemarker.template.TemplateModelException;

public class DisplayMapElementTag extends FreemarkerTag {
    private static final long serialVersionUID = 1L;

    @Override
    protected Object executeTag() throws TemplateModelException {
        String variableName = getParameterAsString(0);
        WfVariable variable = variableProvider.getVariableNotNull(variableName);
        Map<?, ?> map = (Map<?, ?>) variable.getValue();
        Object key = getParameterAs(Object.class, 1);
        Object object = map.get(key);
        VariableFormat componentFormat = FormatCommons.createComponent(variable, 1);
        String nameSuffix = FormSubmissionUtils.COMPONENT_QUALIFIER_START + key + FormSubmissionUtils.COMPONENT_QUALIFIER_END;
        WfVariable componentVariable = ViewUtil.createComponentVariable(variable, nameSuffix, componentFormat, object);
        return ViewUtil.getOutput(user, webHelper, variableProvider.getProcessId(), componentVariable);
    }

}
