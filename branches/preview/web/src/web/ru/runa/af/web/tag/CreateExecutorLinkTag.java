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

import ru.runa.af.ASystem;
import ru.runa.af.SystemPermission;
import ru.runa.af.service.AuthorizationService;
import ru.runa.common.web.tag.LinkTag;
import ru.runa.delegate.DelegateFactory;

/**
 * Created on 03.09.2004
 */
abstract public class CreateExecutorLinkTag extends LinkTag {
    private static final long serialVersionUID = 1L;

    @Override
    protected boolean isLinkEnabled() throws JspException {
        try {
            AuthorizationService authorizationService = DelegateFactory.getInstance().getAuthorizationService();
            return authorizationService.isAllowed(getSubject(), SystemPermission.CREATE_EXECUTOR, ASystem.SYSTEM);
        } catch (Exception e) {
            return false;
        }
    }
}
