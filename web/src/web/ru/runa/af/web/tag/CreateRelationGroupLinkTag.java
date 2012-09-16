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

import ru.runa.af.RelationPermission;
import ru.runa.af.RelationsGroupSecure;
import ru.runa.af.service.AuthorizationService;
import ru.runa.common.web.Commons;
import ru.runa.common.web.Commons.PortletUrl;
import ru.runa.common.web.Messages;
import ru.runa.common.web.tag.LinkTag;
import ru.runa.delegate.DelegateFactory;

/**
 * Created on 03.09.2004
 * 
 * @jsp.tag name = "createRelationGroupLink" body-content = "empty"
 */
public class CreateRelationGroupLinkTag extends LinkTag {

    private static final long serialVersionUID = 1L;

    @Override
    protected String getHref() throws JspException {
        return Commons.getActionUrl("create_relation_group.do", pageContext, PortletUrl.Action);
    }

    @Override
    protected String getLinkText() {
        return Messages.getMessage(Messages.LINK_CREATE_RELATION_GROUP, pageContext);
    }

    @Override
    protected boolean isLinkEnabled() throws JspException {
        try {
            AuthorizationService authorizationService = DelegateFactory.getInstance().getAuthorizationService();
            return authorizationService.isAllowed(getSubject(), RelationPermission.UPDATE_RELATION, RelationsGroupSecure.INSTANCE);
        } catch (Exception e) {
            return false;
        }
    }
}
