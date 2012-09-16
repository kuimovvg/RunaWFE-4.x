package ru.runa.af.web.action;

import javax.security.auth.Subject;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.struts.action.Action;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.action.ActionMessages;

import ru.runa.af.Bot;
import ru.runa.af.BotStation;
import ru.runa.af.BotStationDoesNotExistsException;
import ru.runa.af.service.BotsService;
import ru.runa.af.web.SubjectHttpSessionHelper;
import ru.runa.af.web.form.BotForm;
import ru.runa.common.web.ActionExceptionHelper;
import ru.runa.delegate.DelegateFactory;

/**
 * @author petrmikheev
 * 
 * @struts:action path="/create_bot" name="botForm" validate="false" input = "/WEB-INF/wf/add_bot.jsp"
 * @struts.action-forward name="success" path="/bot_station.do" redirect = "true"
 * @struts.action-forward name="failure" path="/create_bot.do" redirect = "true"
 */
public class CreateBotAction extends Action {

    public static final String CREATE_BOT_ACTION_PATH = "/create_bot";

    @Override
    public ActionForward execute(ActionMapping mapping, ActionForm form, HttpServletRequest request, HttpServletResponse response) {
        ActionMessages errors = new ActionMessages();
        BotForm botForm = (BotForm) form;
        try {
            Subject subject = SubjectHttpSessionHelper.getActorSubject(request.getSession());
            BotsService botsService = DelegateFactory.getInstance().getBotsService();
            Bot bot = new Bot();
            bot.setWfeUser(botForm.getWfeUser());
            bot.setWfePass(botForm.getWfePassword());
            BotStation station = new BotStation(botForm.getBotStationID());
            station = botsService.getBotStation(subject, station);
            if (station == null) {
                throw new BotStationDoesNotExistsException("with id " + botForm.getBotStationID());
            }
            bot.setBotStation(station);
            botsService.create(subject, bot);
        } catch (Exception e) {
            ActionExceptionHelper.addException(errors, e);
        }

        if (!errors.isEmpty()) {
            saveErrors(request.getSession(), errors);
        }

        return new ActionForward("/bot_station.do?botStationID=" + botForm.getBotStationID());
    }
}
