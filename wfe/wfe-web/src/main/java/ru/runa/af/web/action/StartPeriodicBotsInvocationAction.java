/*
 * This file is part of the RUNA WFE project.
 * 
 * This program is free software; you can redistribute it and/or 
 * modify it under the terms of the GNU Lesser General Public License 
 * as published by the Free Software Foundation; version 2.1 
 * of the License. 
 * 
 * This program is distributed in the hope that it will be useful, 
 * but WITHOUT ANY WARRANTY; without even the implied warranty of 
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the 
 * GNU Lesser General Public License for more details. 
 * 
 * You should have received a copy of the GNU Lesser General Public License 
 * along with this program; if not, write to the Free Software 
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA.
 */
package ru.runa.af.web.action;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.struts.action.Action;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;

import ru.runa.af.web.form.BotStationForm;
import ru.runa.service.delegate.Delegates;
import ru.runa.service.wf.BotService;
import ru.runa.wfe.bot.BotStation;

/**
 * @author petrmikheev
 * 
 * @struts:action path="/start_periodic_bots_invocation" name="botStationForm"
 *                validate="false" input = "/WEB-INF/wf/bot_station.jsp"
 */
public class StartPeriodicBotsInvocationAction extends Action {
    public static final String START_PERIODIC_BOTS_INVOCATION = "/start_periodic_bots_invocation";

    @Override
    public ActionForward execute(ActionMapping mapping, ActionForm form, HttpServletRequest request, HttpServletResponse response) throws Exception {
        Long id = ((BotStationForm) form).getBotStationId();
        BotService botService = Delegates.getBotService();
        BotStation botStation = botService.getBotStation(id);
        Delegates.getBotInvokerService(botStation.getAddress()).startPeriodicBotsInvocation(botStation);
        return new ActionForward("/bot_station.do?botStationId=" + id);
    }
}
