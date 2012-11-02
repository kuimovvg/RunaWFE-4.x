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

import ru.runa.common.web.Commons;
import ru.runa.common.web.Messages;
import ru.runa.common.web.tag.LinkTag;
import ru.runa.service.af.AuthorizationService;
import ru.runa.service.af.RelationService;
import ru.runa.service.delegate.DelegateFactory;
import ru.runa.wfe.commons.web.PortletUrlType;
import ru.runa.wfe.relation.Relation;
import ru.runa.wfe.relation.RelationPermission;

/**
 * Created on 03.09.2004
 * 
 * @jsp.tag name = "createRelationLink" body-content = "empty"
 */
public class CreateRelationLinkTag extends LinkTag {

    private static final long serialVersionUID = 1L;

    private String relationName;

    /**
     * @jsp.attribute required = "true" rtexprvalue = "true"
     */
    public String getRelationName() {
        return relationName;
    }

    public void setRelationName(String relationName) {
        this.relationName = relationName;
    }

    @Override
    protected String getHref() throws JspException {
        return Commons.getActionUrl("create_relation.do", "relationName", relationName, pageContext, PortletUrlType.Action);
    }

    @Override
    protected String getLinkText() {
        return Messages.getMessage(Messages.LINK_CREATE_RELATION, pageContext);
    }

    @Override
    protected boolean isLinkEnabled() throws JspException {
        try {
            RelationService relationService = DelegateFactory.getRelationService();
            AuthorizationService authorizationService = DelegateFactory.getAuthorizationService();
            Subject subject = getSubject();
            Relation relationGroup = relationService.getRelation(subject, getRelationName());
            return authorizationService.isAllowed(subject, RelationPermission.UPDATE_PERMISSIONS, relationGroup);
        } catch (Exception e) {
            return false;
        }
    }
}
