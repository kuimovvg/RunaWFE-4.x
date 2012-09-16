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
import ru.runa.af.BotDoesNotExistsException;
import ru.runa.af.BotTask;
import ru.runa.af.service.BotsService;
import ru.runa.af.web.SubjectHttpSessionHelper;
import ru.runa.common.web.ActionExceptionHelper;
import ru.runa.common.web.form.IdForm;
import ru.runa.delegate.DelegateFactory;

/**
 * @author petrmikheev
 * 
 * @struts:action path="/create_bot_task" name="idForm" validate="false" input = "/WEB-INF/wf/bot.jsp"
 */
public class CreateBotTaskAction extends Action {

    public static final String CREATE_BOT_TASK_ACTION_PATH = "/create_bot_task";

    @Override
    public ActionForward execute(ActionMapping mapping, ActionForm form, HttpServletRequest request, HttpServletResponse response) {
        ActionMessages errors = new ActionMessages();
        IdForm idForm = (IdForm) form;
        try {
            Subject subject = SubjectHttpSessionHelper.getActorSubject(request.getSession());
            BotsService botsService = DelegateFactory.getInstance().getBotsService();
            Bot bot = new Bot();
            bot.setId(idForm.getId());
            bot = botsService.getBot(subject, bot);
            if (bot == null) {
                throw new BotDoesNotExistsException("with id " + idForm.getId());
            }
            BotTask task = new BotTask();
            task.setBot(bot);
            task.setClazz("");
            task.setName("");
            task.setConfig("");
            task.setConfiguration(new byte[0]);
            botsService.create(subject, task);
        } catch (Exception e) {
            ActionExceptionHelper.addException(errors, e);
        }

        if (!errors.isEmpty()) {
            saveErrors(request.getSession(), errors);
        }

        return new ActionForward("/bot.do?botID=" + idForm.getId());
    }
}
