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
import ru.runa.common.web.ActionExceptionHelper;
import ru.runa.common.web.form.IdForm;
import ru.runa.service.delegate.DelegateFactory;
import ru.runa.service.wf.BotService;
import ru.runa.wfe.bot.Bot;
import ru.runa.wfe.bot.BotTask;

/**
 * @author petrmikheev
 * 
 * @struts:action path="/create_bot_task" name="idForm" validate="false" input =
 *                "/WEB-INF/wf/bot.jsp"
 */
public class CreateBotTaskAction extends Action {

    public static final String CREATE_BOT_TASK_ACTION_PATH = "/create_bot_task";

    @Override
    public ActionForward execute(ActionMapping mapping, ActionForm actionForm, HttpServletRequest request, HttpServletResponse response) {
        ActionMessages errors = new ActionMessages();
        IdForm form = (IdForm) actionForm;
        try {
            Subject subject = SubjectHttpSessionHelper.getActorSubject(request.getSession());
            BotService botService = DelegateFactory.getBotService();
            Bot bot = botService.getBot(subject, form.getId());
            BotTask task = new BotTask();
            task.setBot(bot);
            task.setName("");
            botService.createBotTask(subject, task);
        } catch (Exception e) {
            ActionExceptionHelper.addException(errors, e);
        }
        if (!errors.isEmpty()) {
            saveErrors(request.getSession(), errors);
        }
        return new ActionForward("/bot.do?botID=" + form.getId());
    }
}
