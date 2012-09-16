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
import ru.runa.wf.ProcessInstanceStub;
import ru.runa.wf.service.ExecutionService;

/**
 * @jsp.tag name = "grantReadPermissionOnProcessInstanceLink" body-content = "empty"
 */
public class GrantReadPermissionOnProcessInstanceLinkTag extends IdLinkBaseTag {

    private static final long serialVersionUID = -8445857392805848169L;

    @Override
    protected boolean isLinkEnabled() throws JspException {
        try {
            AuthorizationService authorizationService = ru.runa.delegate.DelegateFactory.getInstance()
                    .getAuthorizationService();
            ExecutionService executionService = DelegateFactory.getInstance().getExecutionService();
            ProcessInstanceStub instanceStub = executionService.getProcessInstanceStub(getSubject(), getIdentifiableId());
            return authorizationService.isAllowed(getSubject(), Permission.UPDATE_PERMISSIONS, instanceStub);
        } catch (Exception e) {
            return false;
        }
    }

    @Override
    protected String getLinkText() {
        return Messages.getMessage(Messages.BUTTON_ADD, pageContext);
    }

}
