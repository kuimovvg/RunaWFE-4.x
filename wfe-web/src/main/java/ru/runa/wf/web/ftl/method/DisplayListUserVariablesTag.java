package ru.runa.wf.web.ftl.method;

import ru.runa.wfe.var.dto.WfVariable;

public class DisplayListUserVariablesTag extends AbstractListUserVariables {
    private static final long serialVersionUID = 1L;

    @Override
    protected Object executeTag() throws Exception {
        initFields();
        WfVariable variable = variableProvider.getVariableNotNull(variableName);
        return ViewUtil.getActiveJsonTable(user, webHelper, variable, sortField, displayMode == DisplayMode.MULTI_DIMENTIONAL_TABLE, false);
    }

}
