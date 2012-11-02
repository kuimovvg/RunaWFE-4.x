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
import ru.runa.af.web.form.BotStationForm;
import ru.runa.common.web.ActionExceptionHelper;
import ru.runa.common.web.Resources;
import ru.runa.service.delegate.DelegateFactory;
import ru.runa.service.wf.BotsService;
import ru.runa.wfe.bot.BotStation;

/**
 * User: stanley Date: 08.06.2008 Time: 19:05:07
 * 
 * @struts:action path="/create_bot_station" name="botStationForm" validate="false" input = "/WEB-INF/wf/add_bot_station.jsp"
 * @struts.action-forward name="success" path="/configure_bot_station.do" redirect = "true"
 * @struts.action-forward name="failure" path="/create_bot_station.do" redirect = "true"
 */
public class CreateBotStationAction extends Action {

    public static final String CREATE_BOT_STATION_ACTION_PATH = "/create_bot_station";

    @Override
    public ActionForward execute(ActionMapping mapping, ActionForm actionForm, HttpServletRequest request, HttpServletResponse response) {
        ActionMessages errors = new ActionMessages();
        BotStationForm form = (BotStationForm) actionForm;
        try {
            Subject subject = SubjectHttpSessionHelper.getActorSubject(request.getSession());
            BotsService botsService = DelegateFactory.getBotsService();
            BotStation botStation = new BotStation(form.getBotStationName(), form.getBotStationRMIAddress());
            botsService.create(subject, botStation);
        } catch (Exception e) {
            ActionExceptionHelper.addException(errors, e);
        }

        if (!errors.isEmpty()) {
            saveErrors(request.getSession(), errors);
        }

        return mapping.findForward(Resources.FORWARD_SUCCESS);
    }
}
