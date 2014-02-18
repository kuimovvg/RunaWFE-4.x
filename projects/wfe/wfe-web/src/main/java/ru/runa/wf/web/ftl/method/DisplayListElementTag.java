package ru.runa.wf.web.ftl.method;

import java.util.List;

import ru.runa.wfe.commons.ftl.FreemarkerTag;
import ru.runa.wfe.var.dto.WfVariable;
import ru.runa.wfe.var.format.FormatCommons;
import ru.runa.wfe.var.format.VariableFormat;
import freemarker.template.TemplateModelException;

public class DisplayListElementTag extends FreemarkerTag {
    private static final long serialVersionUID = 1L;

    @SuppressWarnings("unchecked")
    @Override
    protected Object executeTag() throws TemplateModelException {
        String variableName = getParameterAsString(0);
        WfVariable variable = variableProvider.getVariableNotNull(variableName);
        List<Object> list = (List<Object>) variable.getValue();
        int index = getParameterAs(int.class, 1);
        Object object = null;
        if (index < list.size()) {
            object = list.get(index);
        }
        VariableFormat componentFormat = FormatCommons.createComponent(variable, 0);
        WfVariable componentVariable = ViewUtil.createListComponentVariable(variable, index, componentFormat, object);
        return ViewUtil.getOutput(user, webHelper, variableProvider.getProcessId(), componentVariable);
    }

}
