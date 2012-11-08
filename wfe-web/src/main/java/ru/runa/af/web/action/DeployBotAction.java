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
import ru.runa.service.wf.BotService;
import ru.runa.wfe.bot.BotStation;

/**
 * @struts:action path="/deploy_bot" name="deployBotForm" validate="false"
 */
public class DeployBotAction extends Action {

    public static final String ACTION_PATH = "/deploy_bot";

    @Override
    public ActionForward execute(ActionMapping mapping, ActionForm actionForm, final HttpServletRequest request, HttpServletResponse responce) {
        ActionMessages errors = getErrors(request);
        DeployBotForm form = (DeployBotForm) actionForm;
        try {
            Subject subject = SubjectHttpSessionHelper.getActorSubject(request.getSession());
            BotService botService = DelegateFactory.getBotService();
            BotStation station = botService.getBotStation(form.getId());
            botService.importBot(subject, station, form.getFile().getFileData(), form.isReplace());
        } catch (Exception e) {
            ActionExceptionHelper.addException(errors, e);
        }

        if (!errors.isEmpty()) {
            saveErrors(request.getSession(), errors);
        }
        return new ActionForward("/bot_station.do?botStationID=" + form.getId());
    }
}
