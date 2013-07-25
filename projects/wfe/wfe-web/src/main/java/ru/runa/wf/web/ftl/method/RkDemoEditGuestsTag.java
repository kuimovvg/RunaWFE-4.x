package ru.runa.wf.web.ftl.method;

import ru.runa.wfe.user.User;

public class RkDemoEditGuestsTag extends EditLinkedListsTag {
    private static final long serialVersionUID = 1L;

    @Override
    protected String getComponentInput(User user, String variableName, String formatClassName, Object value, boolean enabled) {
        if ("сроки действия пропусков".equals(variableName)) {
            String html = "<select name=\"" + variableName + "\"";
            if (!enabled) {
                html += " disabled=\"true\"";
            }
            html += ">";
            html += "<option>разовый</option>";
            html += "<option>временный</option>";
            html += "</select>";
            return html;
        }
        return super.getComponentInput(user, variableName, formatClassName, value, enabled);
    }
}
