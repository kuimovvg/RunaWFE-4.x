package ru.runa.af.web.action;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.action.ActionMessages;

import ru.runa.af.web.form.DeployBotForm;
import ru.runa.common.web.ActionExceptionHelper;
import ru.runa.common.web.action.ActionBase;
import ru.runa.service.delegate.Delegates;
import ru.runa.service.wf.BotService;
import ru.runa.wfe.security.AuthenticationException;

/**
 * @struts:action path="/deploy_bot_station" name="deployBotForm"
 *                validate="false"
 */
public class DeployBotStationAction extends ActionBase {
    public static final String ACTION_PATH = "/deploy_bot_station";

    @Override
    public ActionForward execute(ActionMapping mapping, ActionForm form, final HttpServletRequest request, HttpServletResponse responce)
            throws AuthenticationException {
        ActionMessages errors = getErrors(request);
        DeployBotForm fileForm = (DeployBotForm) form;
        try {
            BotService botService = Delegates.getBotService();
            botService.importBotStation(getLoggedUser(request), fileForm.getFile().getFileData(), fileForm.isReplace());
        } catch (Exception e) {
            ActionExceptionHelper.addException(errors, e);
        }

        if (!errors.isEmpty()) {
            saveErrors(request.getSession(), errors);
        }
        return new ActionForward("/configure_bot_station.do");
    }
}
