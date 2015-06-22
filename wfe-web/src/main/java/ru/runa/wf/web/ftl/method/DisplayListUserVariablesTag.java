package ru.runa.wf.web.ftl.method;


public class DisplayListUserVariablesTag extends AbstractListUserVariables {
    private static final long serialVersionUID = 1L;

    @Override
    protected Object executeTag() throws Exception {
        initFields();
        return ViewUtil.getUserTypeListTable(user, webHelper, variableProvider.getVariableNotNull(variableName), null, variableProvider.getProcessId(),
                sortField, displayMode == DisplayMode.MULTI_DIMENTIONAL_TABLE);
    }

}
