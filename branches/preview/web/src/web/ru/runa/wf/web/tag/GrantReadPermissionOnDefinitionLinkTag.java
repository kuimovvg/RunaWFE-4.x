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
package ru.runa.wf.web.tag;

import javax.servlet.jsp.JspException;

import ru.runa.af.Permission;
import ru.runa.af.service.AuthorizationService;
import ru.runa.common.web.Messages;
import ru.runa.common.web.tag.IdLinkBaseTag;
import ru.runa.delegate.DelegateFactory;

/**
 * Created on 03.09.2004
 * 
 * @jsp.tag name = "grantReadPermissionOnDefinitionLink" body-content = "empty"
 */
public class GrantReadPermissionOnDefinitionLinkTag extends IdLinkBaseTag {

    private static final long serialVersionUID = -3633536214630213255L;

    @Override
    protected boolean isLinkEnabled() throws JspException {
        try {
            AuthorizationService authorizationService = ru.runa.delegate.DelegateFactory.getInstance()
                    .getAuthorizationService();
            return authorizationService.isAllowed(getSubject(), Permission.UPDATE_PERMISSIONS, DelegateFactory.getInstance()
                    .getDefinitionService().getProcessDefinitionStub(getSubject(), getIdentifiableId()));
        } catch (Exception e) {
            return false;
        }
    }

    @Override
    protected String getLinkText() {
        return Messages.getMessage(Messages.BUTTON_ADD, pageContext);
    }

}
