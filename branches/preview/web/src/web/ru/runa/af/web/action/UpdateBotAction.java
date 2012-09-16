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
import ru.runa.af.service.BotsService;
import ru.runa.af.web.SubjectHttpSessionHelper;
import ru.runa.af.web.form.BotForm;
import ru.runa.common.web.ActionExceptionHelper;
import ru.runa.delegate.DelegateFactory;

/**
 * User: petrmikheev
 * 
 * @struts:action path="/update_bot" name="botForm" validate="false" input = "/WEB-INF/wf/bot.jsp"
 */
public class UpdateBotAction extends Action {
    public static final String UPDATE_BOT_ACTION_PATH = "/update_bot";

    @Override
    public ActionForward execute(ActionMapping mapping, ActionForm form, HttpServletRequest request, HttpServletResponse response) {
        ActionMessages errors = new ActionMessages();
        BotForm botForm = (BotForm) form;
        try {
            Subject subject = SubjectHttpSessionHelper.getActorSubject(request.getSession());
            BotsService botsService = DelegateFactory.getInstance().getBotsService();
            Bot bot = new Bot();
            bot.setId(botForm.getBotID());
            bot.setWfeUser(botForm.getWfeUser());
            bot.setWfePass(botForm.getWfePassword());
            bot.setLastInvoked(botForm.getBotTimeout());
            BotStation bs = new BotStation(botForm.getBotStationID());
            bot.setBotStation(botsService.getBotStation(subject, bs));
            botsService.update(subject, bot);
        } catch (Exception e) {
            ActionExceptionHelper.addException(errors, e);
        }

        if (!errors.isEmpty()) {
            saveErrors(request.getSession(), errors);
        }

        return new ActionForward("/bot.do?botID=" + botForm.getBotID());
    }
}
