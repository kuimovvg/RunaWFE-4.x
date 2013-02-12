package ru.runa.af.web.action;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;

import ru.runa.af.web.form.BotStationForm;
import ru.runa.common.web.Resources;
import ru.runa.common.web.action.ActionBase;
import ru.runa.wfe.bot.BotStation;
import ru.runa.wfe.service.BotService;
import ru.runa.wfe.service.delegate.Delegates;

/**
 * User: stanley Date: 08.06.2008 Time: 19:05:07
 * 
 * @struts:action path="/create_bot_station" name="botStationForm"
 *                validate="false" input = "/WEB-INF/wf/add_bot_station.jsp"
 * @struts.action-forward name="success" path="/configure_bot_station.do"
 *                        redirect = "true"
 * @struts.action-forward name="failure" path="/create_bot_station.do" redirect
 *                        = "true"
 */
public class CreateBotStationAction extends ActionBase {

    public static final String CREATE_BOT_STATION_ACTION_PATH = "/create_bot_station";

    @Override
    public ActionForward execute(ActionMapping mapping, ActionForm actionForm, HttpServletRequest request, HttpServletResponse response) {
        BotStationForm form = (BotStationForm) actionForm;
        try {
            BotService botService = Delegates.getBotService();
            BotStation botStation = new BotStation(form.getBotStationName(), form.getBotStationRMIAddress());
            botService.createBotStation(getLoggedUser(request), botStation);
        } catch (Exception e) {
            addError(request, e);
        }
        return mapping.findForward(Resources.FORWARD_SUCCESS);
    }
}
