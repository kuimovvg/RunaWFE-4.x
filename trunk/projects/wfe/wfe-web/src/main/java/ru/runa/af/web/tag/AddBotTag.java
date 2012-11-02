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

import org.apache.ecs.html.Input;
import org.apache.ecs.html.TD;
import org.apache.ecs.html.TR;
import org.apache.ecs.html.Table;

import ru.runa.af.web.action.CreateBotAction;
import ru.runa.af.web.form.BotForm;
import ru.runa.common.web.Messages;
import ru.runa.common.web.tag.TitledFormTag;

/**
 * @author: petrmikheev
 * @jsp.tag name = "addBotTag" body-content = "JSP"
 */
public class AddBotTag extends TitledFormTag {
    private static final long serialVersionUID = 1920713038009470026L;

    private Long botStationID;

    public void setBotStationID(Long botStationID) {
        this.botStationID = botStationID;
    }

    /**
     * @jsp.attribute required = "false" rtexprvalue = "true"
     */
    public Long getBotStationID() {
        return botStationID;
    }

    protected void fillFormElement(TD tdFormElement) throws JspException {
        Table table = new Table();
        ActorSelectTD actorSelect = new ActorSelectTD(getSubject(), BotForm.USER_NAME);
        Input botPasswordInput = new Input(Input.TEXT, BotForm.PASSWORD);

        Input hiddenBotStationID = new Input(Input.HIDDEN, BotForm.BOT_STATION_ID, botStationID);
        tdFormElement.addElement(hiddenBotStationID);

        TR tr = new TR();
        tr.addElement(new TD(Messages.getMessage(Messages.LABEL_BOT_NAME, pageContext)));
        tr.addElement(actorSelect);
        table.addElement(tr);
        tr = new TR();
        tr.addElement(new TD(Messages.getMessage(Messages.LABEL_BOT_PASSWORD, pageContext)));
        tr.addElement(new TD(botPasswordInput));
        table.addElement(tr);
        tdFormElement.addElement(table);
    }

    protected String getTitle() {
        return Messages.getMessage(Messages.TITLE_ADD_BOT, pageContext);
    }

    public String getButtonAlignment() {
        return "right";
    }

    protected String getFormButtonName() {
        return Messages.getMessage(Messages.BUTTON_ADD_BOT, pageContext);
    }

    public String getAction() {
        return CreateBotAction.CREATE_BOT_ACTION_PATH;
    }
}
