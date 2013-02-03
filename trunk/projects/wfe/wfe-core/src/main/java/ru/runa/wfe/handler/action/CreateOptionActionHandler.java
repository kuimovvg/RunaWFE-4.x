package ru.runa.wfe.handler.action;

import ru.runa.wfe.commons.web.Option;
import ru.runa.wfe.handler.CommonParamBasedHandler;
import ru.runa.wfe.handler.HandlerData;

public class CreateOptionActionHandler extends CommonParamBasedHandler {

    @Override
    protected void executeAction(HandlerData handlerData) throws Exception {
        String value = handlerData.getInputParam(String.class, "value");
        String text = handlerData.getInputParam(String.class, "text");
        handlerData.setOutputVariable("option", new Option(value, text));
    }

}
