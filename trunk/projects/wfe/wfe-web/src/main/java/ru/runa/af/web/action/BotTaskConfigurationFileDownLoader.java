package ru.runa.af.web.action;

import java.io.OutputStream;

import javax.security.auth.Subject;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.struts.action.Action;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;

import ru.runa.af.web.Native2AsciiHelper;
import ru.runa.af.web.SubjectHttpSessionHelper;
import ru.runa.common.web.HTMLUtils;
import ru.runa.common.web.form.IdForm;
import ru.runa.service.delegate.DelegateFactory;
import ru.runa.service.wf.BotsService;
import ru.runa.wfe.bot.BotTask;

import com.google.common.base.Charsets;

/**
 * User: stan79 Date: 24.01.2009 Time: 13:24:46
 * 
 * @struts:action path="/download_bot_task_configuration" name="idForm" input = "/WEB-INF/wf/bot.jsp"
 */
public class BotTaskConfigurationFileDownLoader extends Action {
    public static final String DOWNLOAD_BOT_TASK_CONFIGURATION_ACTION_PATH = "/download_bot_task_configuration";

    @Override
    public ActionForward execute(ActionMapping actionMapping, ActionForm actionForm, HttpServletRequest request, HttpServletResponse response)
            throws Exception {
        IdForm form = (IdForm) actionForm;
        String parameter = request.getParameter("edit");
        BotsService botsService = DelegateFactory.getBotsService();
        Subject subject = SubjectHttpSessionHelper.getActorSubject(request.getSession());
        BotTask botTask = new BotTask();
        botTask.setId(form.getId());
        botTask = botsService.getBotTask(subject, botTask);
        String fileName = botTask.getName() + "_" + botTask.getId() + ".xml";
        byte[] configuration = botTask.getConfiguration();
        String tempConfiguration = new String(configuration, Charsets.UTF_8);
        if (parameter != null && !Native2AsciiHelper.isXMLfile(tempConfiguration) && Native2AsciiHelper.isNeedConvert(tempConfiguration)) {
            configuration = Native2AsciiHelper.asciiToNative(tempConfiguration).getBytes(Charsets.UTF_8);
            fileName = botTask.getName() + "_" + botTask.getId() + ".properties";
        }
        response.setContentType("text/xml");
        response.setHeader("Pragma", "public");
        response.setHeader("Cache-Control", "max-age=0");
        String encodedFileName = HTMLUtils.encodeFileName(fileName, request.getHeader("User-Agent"));
        response.setHeader("Content-Disposition", "attachment; filename=\"" + encodedFileName + "\"");
        OutputStream os = response.getOutputStream();
        os.write(configuration);
        os.flush();
        return null;
    }
}
