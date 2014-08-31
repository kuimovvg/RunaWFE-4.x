package ru.runa.af.web.action;

import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;

import ru.runa.common.web.Commons;
import ru.runa.common.web.Resources;
import ru.runa.common.web.action.ActionBase;
import ru.runa.common.web.form.PropertiesFileForm;
import ru.runa.wfe.security.AuthorizationException;
import ru.runa.wfe.service.SystemService;
import ru.runa.wfe.service.delegate.Delegates;

/**
 * @author petrmikheev
 * 
 * @struts:action path="/restore_default_properties" input = "/WEB-INF/wf/manage_settings.jsp"
 */
public class RestoreDefaultPropertiesAction extends ActionBase {

    public static final String RESTORE_DEFAULT_PROPERTIES_ACTION_PATH = "/restore_default_properties";

    @Override
    public ActionForward execute(ActionMapping mapping, ActionForm form, HttpServletRequest request, HttpServletResponse response) {
    	if (!Delegates.getExecutorService().isAdministrator(Commons.getUser(request.getSession())))
    		throw new AuthorizationException("No permission on this page");
        try {
            SystemService service = Delegates.getSystemService();
            service.clearSettings();
        } catch (Exception e) {
            log.error("", e);
        }
        return mapping.findForward(Resources.FORWARD_SUCCESS);
    }
}
