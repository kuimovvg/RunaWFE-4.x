package ru.runa.bp;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import ru.runa.Messages;
import ru.runa.alfresco.AlfSession;
import ru.runa.alfresco.AlfSessionWrapper;
import ru.runa.alfresco.ConnectionException;
import ru.runa.wfe.commons.ftl.AjaxFreemarkerTag;

/**
 * Base class for RunaWFE ajax freemarker tag.
 * 
 * @author dofs
 */
public abstract class AlfAjaxTag extends AjaxFreemarkerTag {
    private static final long serialVersionUID = 1L;
    protected Log log = LogFactory.getLog(getClass());

    protected abstract String renderRequest(AlfSession session) throws Exception;

    protected void processAjaxRequest(AlfSession session, HttpServletRequest request, HttpServletResponse response) throws Exception {
        //
    }

    @Override
    protected String renderRequest() throws Exception {
        try {
            return new AlfSessionWrapper<String>() {
                @Override
                protected String code() throws Exception {
                    return renderRequest(session);
                }
            }.runInSession();
        } catch (Exception e) {
            log.error("Tag execution error", e);
            if (e instanceof ConnectionException) {
                throw new Exception(Messages.getMessage("error.alfresco.unavailable"), e);
            } else {
                throw new Exception(Messages.getMessage("error.alfresco") + ": " + e.getMessage(), e);
            }
        }
    }

    @Override
    public void processAjaxRequest(final HttpServletRequest request, final HttpServletResponse response) throws Exception {
        new AlfSessionWrapper<String>() {
            @Override
            protected String code() throws Exception {
                processAjaxRequest(session, request, response);
                return null;
            }
        }.runInSession();
    }

    protected boolean isValid(String varValue) {
        return varValue != null && varValue.length() > 0;
    }

}
