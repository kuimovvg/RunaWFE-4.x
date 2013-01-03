package ru.runa.wfe.var.format;

import javax.security.auth.Subject;

import ru.runa.wfe.commons.web.WebHelper;

/**
 * Provides a way to customize a variable display.
 * 
 * @author Dofs
 */
public interface VariableDisplaySupport<T> {

    /**
     * Generates HTML for variable value display.
     */
    public String getHtml(Subject subject, WebHelper webHelper, Long processId, String name, T value);

}
