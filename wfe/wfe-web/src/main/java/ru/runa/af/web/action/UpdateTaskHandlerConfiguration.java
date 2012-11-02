package ru.runa.af.web.action;

import java.io.PrintWriter;

import javax.security.auth.Subject;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.struts.action.Action;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.action.ActionMessages;

import ru.runa.af.web.Native2AsciiHelper;
import ru.runa.af.web.SubjectHttpSessionHelper;
import ru.runa.common.web.ActionExceptionHelper;
import ru.runa.common.web.form.IdForm;
import ru.runa.service.delegate.DelegateFactory;
import ru.runa.service.wf.BotsService;
import ru.runa.wfe.bot.BotTask;

import com.google.common.base.Charsets;

/**
 * 
 * @author riven
 * 
 * @struts:action path="/update_tasks_handler_conf" name="botTasksForm" validate="true" input = "/WEB-INF/wf/bot.jsp"
 * 
 */
public class UpdateTaskHandlerConfiguration extends Action {

    public static final String UPDATE_TASK_HANDLER_CONF_ACTION_PATH = "/update_tasks_handler_conf";

    @Override
    public ActionForward execute(ActionMapping mapping, ActionForm form, HttpServletRequest request, HttpServletResponse response) {
        ActionMessages errors = new ActionMessages();
        IdForm idForm = (IdForm) form;

        BotsService botsService = DelegateFactory.getBotsService();
        Subject subject = SubjectHttpSessionHelper.getActorSubject(request.getSession());

        try {
            Long botTaskId = idForm.getId();
            String parameter = request.getParameter("conf");
            byte[] configuration = parameter.getBytes(Charsets.UTF_8);
            if (!Native2AsciiHelper.isXMLfile(parameter)) {
                configuration = Native2AsciiHelper.nativeToAscii(parameter).getBytes(Charsets.UTF_8);
            }

            BotTask botTask = new BotTask();
            botTask.setId(botTaskId);
            botTask = botsService.getBotTask(subject, botTask);
            botTask.setConfiguration(configuration);
            botsService.update(subject, botTask);

            PrintWriter out = response.getWriter();
            response.setContentType("text/xml");
            response.setHeader("Cache-Control", "no-cache");
            out.println("<response>");
            out.println("</response>");
            out.close();
        } catch (Exception e) {
            ActionExceptionHelper.addException(errors, e);
        }
        return null;
    }
}
