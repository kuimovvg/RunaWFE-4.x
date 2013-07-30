package ru.runa.bp;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.json.simple.JSONAware;

import ru.runa.Messages;
import ru.runa.alfresco.AlfSession;
import ru.runa.alfresco.AlfSessionWrapper;
import ru.runa.alfresco.ConnectionException;
import ru.runa.wfe.commons.ftl.AjaxJsonFreemarkerTag;

/**
 * Base class for RunaWFE ajax freemarker tag to work with JSON format.
 * 
 * @author dofs
 */
public abstract class AlfAjaxJsonTag extends AjaxJsonFreemarkerTag {
    private static final long serialVersionUID = 1L;
    protected Log log = LogFactory.getLog(getClass());

    protected abstract String renderRequest(AlfSession session) throws Exception;

    protected abstract JSONAware processAjaxRequest(AlfSession session, HttpServletRequest request) throws Exception;

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
            if (e instanceof ConnectionException) {
                throw new Exception(Messages.getMessage("error.alfresco.unavailable"), e);
            } else {
                throw new Exception(Messages.getMessage("error.alfresco") + ": " + e.getMessage(), e);
            }
        }
    }

    @Override
    public JSONAware processAjaxRequest(final HttpServletRequest request) throws Exception {
        return new AlfSessionWrapper<JSONAware>() {
            @Override
            protected JSONAware code() throws Exception {
                return processAjaxRequest(session, request);
            }
        }.runInSession();
    }

    protected boolean isValid(String varValue) {
        return varValue != null && varValue.length() > 0;
    }

}
