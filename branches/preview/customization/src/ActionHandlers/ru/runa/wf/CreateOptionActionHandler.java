package ru.runa.wf;

import ru.runa.wf.web.Option;

public class CreateOptionActionHandler extends ParamBasedActionHandler {
    private static final long serialVersionUID = 1L;

    @Override
    protected void executeAction() throws Exception {
        String value = getInputParam(String.class, "value");
        String text = getInputParam(String.class, "text");
        setOutputVariable("option", new Option(value, text));
    }

}
