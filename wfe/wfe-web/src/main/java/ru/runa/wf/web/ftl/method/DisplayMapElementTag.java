package ru.runa.wf.web.ftl.method;

import ru.runa.wfe.commons.ftl.FreemarkerTag;
import ru.runa.wfe.var.dto.WfVariable;

public class DisplayMapElementTag extends FreemarkerTag {
    private static final long serialVersionUID = 1L;

    @Override
    protected Object executeTag() {
        String variableName = getParameterAsString(0);
        WfVariable variable = variableProvider.getVariableNotNull(variableName);
        Object key = getRichComboParameterAs(Object.class, 1);
        WfVariable componentVariable = ViewUtil.createMapComponentVariable(variable, key);
        return ViewUtil.getOutput(user, webHelper, variableProvider.getProcessId(), componentVariable);
    }

}
