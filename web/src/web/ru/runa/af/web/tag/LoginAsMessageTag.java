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

import javax.security.auth.Subject;
import javax.servlet.jsp.JspException;

import org.apache.ecs.html.A;

import ru.runa.af.Actor;
import ru.runa.af.service.AuthenticationService;
import ru.runa.af.web.Resources;
import ru.runa.af.web.SubjectHttpSessionHelper;
import ru.runa.common.web.Commons;
import ru.runa.common.web.Commons.PortletUrl;
import ru.runa.common.web.form.IdForm;
import ru.runa.common.web.tag.MessageTag;
import ru.runa.delegate.DelegateFactory;

/**
 * Created on 06.09.2004
 * 
 * @jsp.tag name = "loginAsMessage" body-content = "empty"
 */
public class LoginAsMessageTag extends MessageTag {

    private static final long serialVersionUID = 1L;

    @Override
    public String getMessage() throws JspException {
        Subject subject = SubjectHttpSessionHelper.getActorSubject(pageContext.getSession());

        Actor actor = null;
        try {
            AuthenticationService authenticationService = DelegateFactory.getInstance().getAuthenticationService();
            actor = authenticationService.getActor(subject);
        } catch (Exception e) {
        }
        String url = Commons.getActionUrl(Resources.ACTION_MAPPING_UPDATE_EXECUTOR, IdForm.ID_INPUT_NAME, String.valueOf(actor.getId()), pageContext,
                PortletUrl.Render);
        A a = new A(url, "<I>" + actor.getName() + "</I>");
        return super.getMessage() + " " + a.toString();
    }
}
