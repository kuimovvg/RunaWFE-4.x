package ru.runa.af.web.action;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.action.ActionMessages;

import ru.runa.af.web.form.BotForm;
import ru.runa.common.web.ActionExceptionHelper;
import ru.runa.common.web.action.ActionBase;
import ru.runa.service.delegate.Delegates;
import ru.runa.service.wf.BotService;
import ru.runa.wfe.bot.Bot;

/**
 * @author petrmikheev
 * 
 * @struts:action path="/create_bot" name="botForm" validate="false" input =
 *                "/WEB-INF/wf/add_bot.jsp"
 * @struts.action-forward name="success" path="/bot_station.do" redirect =
 *                        "true"
 * @struts.action-forward name="failure" path="/create_bot.do" redirect = "true"
 */
public class CreateBotAction extends ActionBase {

    public static final String CREATE_BOT_ACTION_PATH = "/create_bot";

    @Override
    public ActionForward execute(ActionMapping mapping, ActionForm form, HttpServletRequest request, HttpServletResponse response) {
        ActionMessages errors = new ActionMessages();
        BotForm botForm = (BotForm) form;
        try {
            BotService botService = Delegates.getBotService();
            Bot bot = new Bot();
            bot.setUsername(botForm.getWfeUser());
            bot.setPassword(botForm.getWfePassword());
            bot.setBotStation(botService.getBotStation(botForm.getBotStationId()));
            botService.createBot(getLoggedUser(request), bot);
        } catch (Exception e) {
            ActionExceptionHelper.addException(errors, e);
        }
        if (!errors.isEmpty()) {
            saveErrors(request.getSession(), errors);
        }
        return new ActionForward("/bot_station.do?botStationId=" + botForm.getBotStationId());
    }
}
