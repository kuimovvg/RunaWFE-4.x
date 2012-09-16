package ru.runa.commons.format;

import javax.security.auth.Subject;
import javax.servlet.jsp.PageContext;

/**
 * Provides a way to customize a variable display.
 * 
 * @author Dofs
 */
public interface VariableDisplaySupport {

    /**
     * Generates HTML for variable value display.
     */
    public String getHtml(Subject subject, PageContext pageContext, Long instanceId, String name, Object value);

}
