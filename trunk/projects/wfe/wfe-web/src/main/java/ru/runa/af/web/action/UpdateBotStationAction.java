package ru.runa.af.web.action;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.action.ActionMessages;

import ru.runa.af.web.form.BotStationForm;
import ru.runa.common.web.ActionExceptionHelper;
import ru.runa.common.web.action.ActionBase;
import ru.runa.service.delegate.Delegates;
import ru.runa.service.wf.BotService;
import ru.runa.wfe.bot.BotStation;

/**
 * @author petrmikheev
 * 
 * @struts:action path="/update_bot_station" name="botStationForm"
 *                validate="false" input = "/WEB-INF/wf/bot_station.jsp"
 */
public class UpdateBotStationAction extends ActionBase {
    public static final String UPDATE_BOT_STATION_ACTION_PATH = "/update_bot_station";

    @Override
    public ActionForward execute(ActionMapping mapping, ActionForm form, HttpServletRequest request, HttpServletResponse response) {
        ActionMessages errors = new ActionMessages();
        BotStationForm botStationForm = (BotStationForm) form;
        try {
            BotService botService = Delegates.getBotService();
            BotStation botStation = botService.getBotStation(botStationForm.getBotStationID());
            botStation.setName(botStationForm.getBotStationName());
            botStation.setAddress(botStationForm.getBotStationRMIAddress());
            botService.updateBotStation(getLoggedUser(request), botStation);
        } catch (Exception e) {
            ActionExceptionHelper.addException(errors, e);
        }
        if (!errors.isEmpty()) {
            saveErrors(request.getSession(), errors);
        }
        return new ActionForward("/bot_station.do?botStationID=" + botStationForm.getBotStationID());
    }
}
