package ru.runa.wfe.extension.handler.var;

import ru.runa.wfe.extension.handler.CommonParamBasedHandler;
import ru.runa.wfe.extension.handler.HandlerData;

// TODO test this
/**
 * Set process variable to 'null' value.
 * 
 * @author dofs
 * @since 3.4
 */
public class ClearVariableActionHandler extends CommonParamBasedHandler {

    @Override
    protected void executeAction(HandlerData handlerData) throws Exception {
        handlerData.setOutputParam("object", null);
    }

}
