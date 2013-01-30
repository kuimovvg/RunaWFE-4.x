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

import java.util.HashMap;
import java.util.Map;

import javax.servlet.jsp.JspException;

import ru.runa.common.web.Commons;
import ru.runa.common.web.Messages;
import ru.runa.common.web.tag.LinkTag;
import ru.runa.service.af.AuthorizationService;
import ru.runa.service.af.RelationService;
import ru.runa.service.delegate.Delegates;
import ru.runa.wfe.commons.web.PortletUrlType;
import ru.runa.wfe.relation.Relation;
import ru.runa.wfe.relation.RelationPermission;

/**
 * @jsp.tag name = "grantPermissionOnRelationLink" body-content = "empty"
 */
public class GrantPermissionOnRelationLinkTag extends LinkTag {

    private static final long serialVersionUID = 1L;

    private String relationName;

    private static final String HREF = "/grant_permission_on_relation.do";

    @Override
    protected boolean isLinkEnabled() throws JspException {
        try {
            RelationService relationService = Delegates.getRelationService();
            AuthorizationService authorizationService = Delegates.getAuthorizationService();
            Relation relationGroup = relationService.getRelation(getUser(), getRelationName());
            return authorizationService.isAllowed(getUser(), RelationPermission.UPDATE_PERMISSIONS, relationGroup);
        } catch (Exception e) {
            return false;
        }
    }

    @Override
    protected String getHref() {
        try {
            RelationService relationService = Delegates.getRelationService();
            Relation relationGroup;
            relationGroup = relationService.getRelation(getUser(), getRelationName());
            Map<String, Object> params = new HashMap<String, Object>();
            params.put("relationName", getRelationName());
            params.put("id", relationGroup.getId());
            return Commons.getActionUrl(HREF, params, pageContext, PortletUrlType.Action);
        } catch (Exception e) {
            return "";
        }
    }

    @Override
    protected String getLinkText() {
        return Messages.getMessage(Messages.BUTTON_ADD, pageContext);
    }

    /**
     * @jsp.attribute required = "false" rtexprvalue = "true"
     */
    public void setRelationName(String relationName) {
        this.relationName = relationName;
    }

    public String getRelationName() {
        return relationName;
    }
}
