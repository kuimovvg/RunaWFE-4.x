package ru.runa.wf.web.ftl.method;

import ru.runa.wfe.user.User;

public class RkDemoRegisterInOutsTag extends EditLinkedListsTag {
    private static final long serialVersionUID = 1L;

    @Override
    protected String getComponentInput(User user, String variableName, String formatClassName, Object value, boolean enabled) {
        if (value != null || (!"время реального входа".equals(variableName) && !"время реального выхода".equals(variableName))) {
            enabled = false;
        }
        return super.getComponentInput(user, variableName, formatClassName, value, enabled);
    }
}
