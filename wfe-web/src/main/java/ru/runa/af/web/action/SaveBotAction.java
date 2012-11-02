package ru.runa.af.web.action;

import java.io.OutputStream;

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
import ru.runa.common.web.HTMLUtils;
import ru.runa.common.web.form.IdForm;
import ru.runa.service.delegate.DelegateFactory;
import ru.runa.service.wf.BotsService;
import ru.runa.wfe.bot.Bot;
import ru.runa.wfe.bot.BotDoesNotExistException;

/**
 * @author petrmikheev
 * 
 * @struts:action path="/save_bot" name="idForm" validate="false" input = "/WEB-INF/wf/bot.jsp"
 */
public class SaveBotAction extends Action {

    public static final String SAVE_BOT_ACTION_PATH = "/save_bot";

    @Override
    public ActionForward execute(ActionMapping mapping, ActionForm form, HttpServletRequest request, HttpServletResponse response) {
        ActionMessages errors = new ActionMessages();
        IdForm idForm = (IdForm) form;
        try {
            Subject subject = SubjectHttpSessionHelper.getActorSubject(request.getSession());
            BotsService botsService = DelegateFactory.getBotsService();
            Bot bot = new Bot();
            bot.setId(idForm.getId());
            bot = botsService.getBot(subject, bot);
            if (bot == null) {
                throw new BotDoesNotExistException("with id " + idForm.getId());
            }
            response.setContentType("application/zip");

            String fileName = bot.getWfeUser() + ".bot";
            fileName = HTMLUtils.encodeFileName(fileName, request.getHeader("User-Agent"));
            response.setHeader("Content-disposition", "attachment; filename=\"" + fileName + "\"");
            OutputStream out = response.getOutputStream();
            botsService.saveBot(subject, bot, out);
            out.flush();
        } catch (Exception e) {
            ActionExceptionHelper.addException(errors, e);
        }

        if (!errors.isEmpty()) {
            saveErrors(request.getSession(), errors);
        }

        return null;
    }
}
