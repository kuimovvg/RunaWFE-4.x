package ru.runa.wfe.handler.action;

import ru.runa.wfe.commons.web.Option;

public class CreateOptionActionHandler extends ParamBasedActionHandler {

    @Override
    protected void executeAction() throws Exception {
        String value = getInputParam(String.class, "value");
        String text = getInputParam(String.class, "text");
        setOutputVariable("option", new Option(value, text));
    }

}
