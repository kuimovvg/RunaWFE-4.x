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
import ru.runa.af.web.form.BotForm;
import ru.runa.common.web.ActionExceptionHelper;
import ru.runa.service.delegate.Delegates;
import ru.runa.service.wf.BotService;
import ru.runa.wfe.bot.Bot;

/**
 * User: petrmikheev
 * 
 * @struts:action path="/update_bot" name="botForm" validate="false" input =
 *                "/WEB-INF/wf/bot.jsp"
 */
public class UpdateBotAction extends Action {
    public static final String UPDATE_BOT_ACTION_PATH = "/update_bot";

    @Override
    public ActionForward execute(ActionMapping mapping, ActionForm form, HttpServletRequest request, HttpServletResponse response) {
        ActionMessages errors = new ActionMessages();
        BotForm botForm = (BotForm) form;
        try {
            Subject subject = SubjectHttpSessionHelper.getActorSubject(request.getSession());
            BotService botService = Delegates.getBotService();
            Bot bot = botService.getBot(subject, botForm.getBotID());
            bot.setUsername(botForm.getWfeUser());
            bot.setPassword(botForm.getWfePassword());
            bot.setStartTimeout(botForm.getBotTimeout());
            bot.setBotStation(botService.getBotStation(botForm.getBotStationID()));
            botService.updateBot(subject, bot);
        } catch (Exception e) {
            ActionExceptionHelper.addException(errors, e);
        }

        if (!errors.isEmpty()) {
            saveErrors(request.getSession(), errors);
        }

        return new ActionForward("/bot.do?botID=" + botForm.getBotID());
    }
}
