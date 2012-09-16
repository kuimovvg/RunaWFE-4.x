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

import ru.runa.af.ASystem;
import ru.runa.af.Actor;
import ru.runa.af.AuthenticationException;
import ru.runa.af.ExecutorPermission;
import ru.runa.af.Permission;
import ru.runa.af.SystemPermission;
import ru.runa.af.authenticaion.SubjectPrincipalsHelper;
import ru.runa.af.web.action.UpdatePasswordAction;
import ru.runa.af.web.html.PasswordTableBuilder;
import ru.runa.common.web.Messages;

/**
 * Created on 18.08.2004
 * 
 * @jsp.tag name = "updatePasswordForm" body-content = "empty"
 */
public class UpdatePasswordFormTag extends UpdateExecutorBaseFormTag {

    private static final long serialVersionUID = -3273077346043267061L;

    public void fillFormData(TD tdFormElement) throws JspException {
        PasswordTableBuilder builder = new PasswordTableBuilder(!isFormButtonEnabled(), pageContext);
        tdFormElement.addElement(builder.build());
    }

    protected Permission getPermission() {
        return ExecutorPermission.UPDATE;
    }

    public String getFormButtonName() {
        return Messages.getMessage(Messages.BUTTON_APPLY, pageContext);
    }

    protected boolean isVisible() throws JspException {
        boolean result = false;
        if ((getExecutor() instanceof Actor) && isFormButtonEnabled()) {
            result = true;
        }
        return result;
    }

    @Override
    protected boolean isFormButtonEnabled() throws JspException {
        try {
            return super.isFormButtonEnabled()
                    || (SubjectPrincipalsHelper.getActor(getSubject()).equals(getIdentifiable()) && super.isFormButtonEnabled(ASystem.SYSTEM,
                            SystemPermission.CHANGE_SELF_PASSWORD));
        } catch (AuthenticationException e) {
            throw new JspException(e);
        }
    }

    protected String getTitle() {
        return Messages.getMessage(Messages.TITLE_ACTOR_PASSWORD, pageContext);
    }

    public String getAction() {
        return UpdatePasswordAction.ACTION_PATH;
    }
}
