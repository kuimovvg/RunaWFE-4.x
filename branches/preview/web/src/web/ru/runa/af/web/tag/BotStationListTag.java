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
package ru.runa.af.web.tag;

import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.jsp.JspException;

import org.apache.ecs.html.Input;
import org.apache.ecs.html.TD;

import ru.runa.af.AuthenticationException;
import ru.runa.af.AuthorizationException;
import ru.runa.af.BotStation;
import ru.runa.af.BotStationConfigurePermission;
import ru.runa.af.Identifiable;
import ru.runa.af.Permission;
import ru.runa.af.service.AuthorizationService;
import ru.runa.af.service.BotsService;
import ru.runa.af.web.action.DeleteBotStationAction;
import ru.runa.af.web.html.BotStationTableBuilder;
import ru.runa.common.web.ConfirmationPopupHelper;
import ru.runa.common.web.Messages;
import ru.runa.common.web.form.IdsForm;
import ru.runa.common.web.tag.TitledFormTag;
import ru.runa.delegate.DelegateFactory;

/**
 * @author: stan79 Date: 25.05.2008 Time: 20:04:19
 * @jsp.tag name = "botStationList" body-content = "JSP"
 */

public class BotStationListTag extends TitledFormTag {
    private static final long serialVersionUID = -4263750161023575386L;

    protected Permission getPermission() throws JspException {
        return BotStationConfigurePermission.BOT_STATION_CONFIGURE;
    }

    protected Identifiable getIdentifiable() throws JspException {
        return BotStation.SECURED_INSTANCE;
    }

    @Override
    protected void fillFormElement(TD tdFormElement) throws JspException {
        getForm().setName("botStationList");
        getForm().setID("botStationList");
        getForm().setAction(
                ((HttpServletRequest) pageContext.getRequest()).getContextPath() + DeleteBotStationAction.DELETE_BOT_STATION_ACTION_PATH + ".do");
        getForm().setMethod("post");
        tdFormElement.addElement(new Input(Input.hidden, IdsForm.ID_INPUT_NAME, "1"));
        BotsService botsService = DelegateFactory.getInstance().getBotsService();
        try {
            List<BotStation> botStations = botsService.getBotStationList(getSubject());
            tdFormElement.addElement(new BotStationTableBuilder(pageContext).buildBotStationTable(botStations));
        } catch (AuthorizationException e) {
            throw new JspException(e);
        } catch (AuthenticationException e) {
            throw new JspException(e);
        }
    }

    @Override
    protected String getTitle() {
        return Messages.getMessage(Messages.TITLE_BOT_STATIONS, pageContext);
    }

    @Override
    protected String getFormButtonName() {
        return Messages.getMessage(Messages.BUTTON_REMOVE, pageContext);
    }

    @Override
    public String getAction() {
        return DeleteBotStationAction.DELETE_BOT_STATION_ACTION_PATH;
    }

    @Override
    public boolean isFormButtonEnabled() throws JspException {
        boolean result = false;
        try {
            AuthorizationService authorizationService = DelegateFactory.getInstance().getAuthorizationService();
            result = authorizationService.isAllowed(getSubject(), BotStationConfigurePermission.BOT_STATION_CONFIGURE, BotStation.SECURED_INSTANCE);
        } catch (AuthorizationException e) {
            throw new JspException(e);
        } catch (AuthenticationException e) {
            throw new JspException(e);
        }
        return result;
    }

    @Override
    public String getConfirmationPopupParameter() {
        return ConfirmationPopupHelper.REMOVE_BOT_STATION_PARAMETER;
    }
}
