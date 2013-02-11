package ru.runa.wfe.extension.handler;

import ru.runa.wfe.commons.web.Option;

public class CreateOptionActionHandler extends CommonParamBasedHandler {

    @Override
    protected void executeAction(HandlerData handlerData) throws Exception {
        String value = handlerData.getInputParam(String.class, "value");
        String text = handlerData.getInputParam(String.class, "text");
        handlerData.setOutputVariable("option", new Option(value, text));
    }

}
