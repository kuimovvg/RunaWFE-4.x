package ru.runa.wf.web.ftl.method;

public class RkDemoEditAccompanyingTag extends RkDemoEditGuestsTag {
    private static final long serialVersionUID = 1L;

    @Override
    protected String getComponentInput(String variableName, String formatClassName, Object value, boolean enabled) {
        if (!variableName.startsWith("сопровождающие")) {
            enabled = false;
        }
        return super.getComponentInput(variableName, formatClassName, value, enabled);
    }
}
