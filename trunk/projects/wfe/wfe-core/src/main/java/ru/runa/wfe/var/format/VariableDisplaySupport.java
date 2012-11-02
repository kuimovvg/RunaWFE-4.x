package ru.runa.wfe.var.format;

import javax.security.auth.Subject;
import javax.servlet.jsp.PageContext;

import ru.runa.wfe.commons.web.WebHelper;

/**
 * Provides a way to customize a variable display.
 * 
 * @author Dofs
 */
public interface VariableDisplaySupport {

    /**
     * Generates HTML for variable value display.
     */
    public String getHtml(Subject subject, PageContext pageContext, WebHelper webHelper, Long processId, String name, Object value);

}
