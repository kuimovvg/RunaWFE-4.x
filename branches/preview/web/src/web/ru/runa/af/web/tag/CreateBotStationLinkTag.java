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

import javax.servlet.jsp.JspException;

import ru.runa.af.BotStation;
import ru.runa.af.BotStationConfigurePermission;
import ru.runa.af.service.AuthorizationService;
import ru.runa.common.web.Messages;
import ru.runa.common.web.tag.LinkTag;

/**
 * @jsp.tag name = "createBotStationLink" body-content = "empty"
 */
public class CreateBotStationLinkTag extends LinkTag {

    private static final long serialVersionUID = 9038757440517109322L;

    @Override
    protected boolean isLinkEnabled() throws JspException {
        try {
            AuthorizationService authorizationService = ru.runa.delegate.DelegateFactory.getInstance()
                    .getAuthorizationService();
            return authorizationService.isAllowed(getSubject(), BotStationConfigurePermission.BOT_STATION_CONFIGURE, BotStation.SECURED_INSTANCE);
        } catch (Exception e) {
            return false;
        }
    }

    @Override
    protected String getLinkText() {
        return Messages.getMessage(Messages.BUTTON_ADD_BOT_STATION, pageContext);
    }

}
