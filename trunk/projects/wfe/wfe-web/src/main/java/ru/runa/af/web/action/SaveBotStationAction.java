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
import ru.runa.service.wf.BotService;
import ru.runa.wfe.bot.BotStation;

/**
 * @author petrmikheev
 * 
 * @struts:action path="/save_bot_station" name="idForm" validate="false" input
 *                = "/WEB-INF/wf/bot_station.jsp"
 */
public class SaveBotStationAction extends Action {
    public static final String SAVE_BOT_STATION_ACTION_PATH = "/save_bot_station";

    @Override
    public ActionForward execute(ActionMapping mapping, ActionForm actionForm, HttpServletRequest request, HttpServletResponse response) {
        ActionMessages errors = new ActionMessages();
        IdForm form = (IdForm) actionForm;
        try {
            Subject subject = SubjectHttpSessionHelper.getActorSubject(request.getSession());
            BotService botService = DelegateFactory.getBotService();
            BotStation station = botService.getBotStation(form.getId());
            String fileName = station.getName() + ".botstation";
            fileName = HTMLUtils.encodeFileName(fileName, request.getHeader("User-Agent"));
            byte[] archive = botService.exportBotStation(subject, station);
            response.setContentType("application/zip");
            response.setHeader("Content-disposition", "attachment; filename=\"" + fileName + "\"");
            OutputStream out = response.getOutputStream();
            out.write(archive);
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
