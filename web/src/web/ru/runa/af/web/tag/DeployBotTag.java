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

import org.apache.ecs.html.Form;
import org.apache.ecs.html.Input;
import org.apache.ecs.html.TD;

import ru.runa.af.BotStation;
import ru.runa.af.BotStationConfigurePermission;
import ru.runa.af.service.AuthorizationService;
import ru.runa.af.web.action.DeployBotAction;
import ru.runa.af.web.form.DeployBotForm;
import ru.runa.common.web.Messages;
import ru.runa.common.web.form.FileForm;
import ru.runa.common.web.tag.TitledFormTag;

/**
 * @author petrmikheev
 * @jsp.tag name = "deployBot" body-content = "empty"
 */
public class DeployBotTag extends TitledFormTag {

    private static final long serialVersionUID = 9038757445617109322L;

    private Long ID;

    public void setID(Long ID) {
        this.ID = ID;
    }

    /**
     * @jsp.attribute required = "false" rtexprvalue = "true"
     */
    public Long getID() {
        return ID;
    }

    @Override
    protected boolean isFormButtonEnabled() throws JspException {
        try {
            AuthorizationService authorizationService = ru.runa.delegate.DelegateFactory.getInstance()
                    .getAuthorizationService();
            return authorizationService.isAllowed(getSubject(), BotStationConfigurePermission.BOT_STATION_CONFIGURE, BotStation.SECURED_INSTANCE);
        } catch (Exception e) {
            return false;
        }
    }

    @Override
    protected String getFormButtonName() {
        return Messages.getMessage(Messages.BUTTON_DEPLOY_BOT, pageContext);
    }

    @Override
    protected String getTitle() {
        return Messages.getMessage(Messages.BUTTON_DEPLOY_BOT, pageContext);
    }

    @Override
    public String getAction() {
        return DeployBotAction.ACTION_PATH;
    }

    @Override
    protected void fillFormElement(TD tdFormElement) throws JspException {
        getForm().setEncType(Form.ENC_UPLOAD);
        tdFormElement.addElement(new Input(Input.hidden, FileForm.ID_INPUT_NAME, Long.toString(ID)));
        Input boolInput = new Input(Input.CHECKBOX, DeployBotForm.REPLACE_OPTION_NAME);
        tdFormElement.addElement(boolInput);
        tdFormElement.addElement(Messages.getMessage(Messages.LABEL_REPLACE_BOT_TASKS, pageContext) + "<br>");
        Input fileUploadInput = new Input(Input.FILE, FileForm.FILE_INPUT_NAME);
        tdFormElement.addElement(fileUploadInput);
    }
}
