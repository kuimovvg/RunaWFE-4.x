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
import ru.runa.wfe.bot.BotStation;
import ru.runa.wfe.bot.BotStationDoesNotExistException;

/**
 * @author petrmikheev
 * 
 * @struts:action path="/save_bot_station" name="idForm" validate="false" input = "/WEB-INF/wf/bot_station.jsp"
 */
public class SaveBotStationAction extends Action {
    public static final String SAVE_BOT_STATION_ACTION_PATH = "/save_bot_station";

    @Override
    public ActionForward execute(ActionMapping mapping, ActionForm form, HttpServletRequest request, HttpServletResponse response) {
        ActionMessages errors = new ActionMessages();
        IdForm idForm = (IdForm) form;
        try {
            Subject subject = SubjectHttpSessionHelper.getActorSubject(request.getSession());
            BotsService botsService = DelegateFactory.getBotsService();
            BotStation station = new BotStation(idForm.getId());
            station = botsService.getBotStation(subject, station);
            if (station == null) {
                throw new BotStationDoesNotExistException("with id " + idForm.getId());
            }
            response.setContentType("application/zip");
            String fileName = station.getName() + ".botstation";
            fileName = HTMLUtils.encodeFileName(fileName, request.getHeader("User-Agent"));
            response.setHeader("Content-disposition", "attachment; filename=\"" + fileName + "\"");
            OutputStream out = response.getOutputStream();
            botsService.saveBotStation(subject, station, out);
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
