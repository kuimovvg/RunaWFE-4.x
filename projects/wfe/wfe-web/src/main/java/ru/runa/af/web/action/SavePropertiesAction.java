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
 * @struts:action path="/save_properties" name="propertiesFileForm" validate="false" input =
 *                "/WEB-INF/wf/edit_settings.jsp"
 */
public class SavePropertiesAction extends ActionBase {

    public static final String SAVE_PROPERTIES_ACTION_PATH = "/save_properties";
    private static final Log log = LogFactory.getLog(SavePropertiesAction.class);

    @Override
    public ActionForward execute(ActionMapping mapping, ActionForm form, HttpServletRequest request, HttpServletResponse response) {
    	if (!Delegates.getExecutorService().isAdministrator(Commons.getUser(request.getSession())))
    		throw new AuthorizationException("No permission on this page");
        try {
            PropertiesFileForm pform = (PropertiesFileForm)form;
            String resource = pform.getResource();
            Map<String, String> properties = pform.getModifiedProperties();
            SystemService service = Delegates.getSystemService();
            for (String p : properties.keySet()) {
            	log.info(resource + "[" + p + "] = " + properties.get(p));
            	service.setWfProperty(resource, p, properties.get(p));
            }
        } catch (Exception e) {
            log.error("", e);
        }
        return mapping.findForward(Resources.FORWARD_SUCCESS);
    }
}
