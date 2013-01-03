package ru.runa.wfe.handler.action.var;

import ru.runa.wfe.handler.action.ParamBasedActionHandler;

public class ClearVariableActionHandler extends ParamBasedActionHandler {

    @Override
    protected void executeAction() throws Exception {
        ParamDef paramDef = outputParams.get("object");
        outputVariables.put(paramDef.getVariableName(), null);
    }

}
