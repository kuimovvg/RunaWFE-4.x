package ru.runa.wf.web.ftl.method;


public class RkDemoEditGuestsTag extends EditLinkedListsTag {
    private static final long serialVersionUID = 1L;

    @Override
    protected String getComponentInput(String variableName, String formatClassName, Object value, boolean enabled) {
        if (variableName.startsWith("сроки действия пропусков")) {
            String html = "<select name=\"" + variableName + "\"";
            if (!enabled) {
                html += " disabled=\"true\"";
            }
            html += ">";
            html += "<option>разовый</option>";
            html += "<option>временный</option>";
            html += "</select>";
            if (!enabled) {
                html += ViewUtil.getHiddenInput(variableName, formatClassName, value);
            }
            return html;
        }
        return super.getComponentInput(variableName, formatClassName, value, enabled);
    }
}
