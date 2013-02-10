package ru.runa.af.web.action;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;

import ru.runa.af.web.form.BotForm;
import ru.runa.common.web.action.ActionBase;
import ru.runa.service.BotService;
import ru.runa.service.delegate.Delegates;
import ru.runa.wfe.bot.Bot;

/**
 * User: petrmikheev
 * 
 * @struts:action path="/update_bot" name="botForm" validate="false" input =
 *                "/WEB-INF/wf/bot.jsp"
 */
public class UpdateBotAction extends ActionBase {
    public static final String UPDATE_BOT_ACTION_PATH = "/update_bot";

    @Override
    public ActionForward execute(ActionMapping mapping, ActionForm form, HttpServletRequest request, HttpServletResponse response) {
        BotForm botForm = (BotForm) form;
        try {
            BotService botService = Delegates.getBotService();
            Bot bot = botService.getBot(getLoggedUser(request), botForm.getBotId());
            bot.setUsername(botForm.getWfeUser());
            bot.setPassword(botForm.getWfePassword());
            bot.setStartTimeout(botForm.getBotTimeout());
            bot.setBotStation(botService.getBotStation(botForm.getBotStationId()));
            botService.updateBot(getLoggedUser(request), bot);
        } catch (Exception e) {
            addError(request, e);
        }
        return new ActionForward("/bot.do?botId=" + botForm.getBotId());
    }
}
