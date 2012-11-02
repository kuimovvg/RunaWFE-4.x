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

import org.apache.ecs.html.TD;

import ru.runa.af.web.action.UpdateBotStationPermissionAction;
import ru.runa.common.web.Messages;
import ru.runa.common.web.html.PermissionTableBuilder;
import ru.runa.common.web.tag.IdentifiableFormTag;
import ru.runa.wfe.bot.BotStation;
import ru.runa.wfe.bot.BotStationPermission;
import ru.runa.wfe.security.Identifiable;
import ru.runa.wfe.security.Permission;

/**
 * @author: stan79 Date: 25.05.2008 Time: 20:04:19
 * @jsp.tag name = "botStationConfigurationTag" body-content = "JSP"
 */
public class BotStationConfigurationTag extends IdentifiableFormTag {
    private static final long serialVersionUID = -1187003724875968614L;

    protected void fillFormData(TD tdFormElement) throws JspException {
        PermissionTableBuilder tableBuilder = new PermissionTableBuilder(getIdentifiable(), getSubject(), pageContext);
        tdFormElement.addElement(tableBuilder.buildTable());
    }

    protected Permission getPermission() throws JspException {
        return BotStationPermission.READ;
    }

    protected Identifiable getIdentifiable() throws JspException {
        return BotStation.INSTANCE;
    }

    public String getAction() {
        return UpdateBotStationPermissionAction.ACTION_PATH_NAME;
    }

    protected String getFormButtonName() {
        return Messages.getMessage(Messages.BUTTON_APPLY, pageContext);
    }

    protected String getTitle() {
        return Messages.getMessage(Messages.TITLE_PERMISSION_OWNERS, pageContext);
    }
}
