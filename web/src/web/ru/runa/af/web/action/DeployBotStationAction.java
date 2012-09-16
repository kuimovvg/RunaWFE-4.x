package ru.runa.af.web.action;

import javax.security.auth.Subject;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.struts.action.Action;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.action.ActionMessages;

import ru.runa.af.AuthenticationException;
import ru.runa.af.service.BotsService;
import ru.runa.af.web.SubjectHttpSessionHelper;
import ru.runa.af.web.form.DeployBotForm;
import ru.runa.common.web.ActionExceptionHelper;
import ru.runa.delegate.DelegateFactory;

/**
 * @struts:action path="/deploy_bot_station" name="deployBotForm"
 *                validate="false"
 */
public class DeployBotStationAction extends Action {
    public static final String ACTION_PATH = "/deploy_bot_station";

    @Override
    public ActionForward execute(ActionMapping mapping, ActionForm form, final HttpServletRequest request, HttpServletResponse responce)
            throws AuthenticationException {
        ActionMessages errors = getErrors(request);
        DeployBotForm fileForm = (DeployBotForm) form;
        try {
            Subject subject = SubjectHttpSessionHelper.getActorSubject(request.getSession());
            BotsService botsService = DelegateFactory.getInstance().getBotsService();
            botsService.deployBotStation(subject, fileForm.getFile().getInputStream(), fileForm.isReplace());
        } catch (Exception e) {
            ActionExceptionHelper.addException(errors, e);
        }

        if (!errors.isEmpty()) {
            saveErrors(request.getSession(), errors);
        }
        return new ActionForward("/configure_bot_station.do");
    }
}
