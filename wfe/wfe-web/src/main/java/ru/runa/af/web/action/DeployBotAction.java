package ru.runa.af.web.action;

import javax.security.auth.Subject;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.struts.action.Action;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.action.ActionMessages;

import ru.runa.af.web.SubjectHttpSessionHelper;
import ru.runa.af.web.form.DeployBotForm;
import ru.runa.common.web.ActionExceptionHelper;
import ru.runa.service.delegate.DelegateFactory;
import ru.runa.service.wf.BotsService;
import ru.runa.wfe.bot.BotStation;
import ru.runa.wfe.bot.BotStationDoesNotExistException;
import ru.runa.wfe.security.AuthenticationException;

/**
 * @struts:action path="/deploy_bot" name="deployBotForm" validate="false"
 */
public class DeployBotAction extends Action {

    public static final String ACTION_PATH = "/deploy_bot";

    @Override
    public ActionForward execute(ActionMapping mapping, ActionForm form, final HttpServletRequest request, HttpServletResponse responce)
            throws AuthenticationException {
        ActionMessages errors = getErrors(request);
        DeployBotForm fiForm = (DeployBotForm) form;
        try {
            Subject subject = SubjectHttpSessionHelper.getActorSubject(request.getSession());
            BotsService botsService = DelegateFactory.getBotsService();
            BotStation station = new BotStation(fiForm.getId());
            station = botsService.getBotStation(subject, station);
            if (station == null) {
                throw new BotStationDoesNotExistException("with id " + fiForm.getId());
            }
            botsService.deployBot(subject, station, fiForm.getFile().getInputStream(), fiForm.isReplace());
        } catch (Exception e) {
            ActionExceptionHelper.addException(errors, e);
        }

        if (!errors.isEmpty()) {
            saveErrors(request.getSession(), errors);
        }
        return new ActionForward("/bot_station.do?botStationID=" + fiForm.getId());
    }
}
