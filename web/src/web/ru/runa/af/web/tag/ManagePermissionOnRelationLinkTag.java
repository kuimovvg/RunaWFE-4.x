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

import javax.security.auth.Subject;
import javax.servlet.jsp.JspException;

import ru.runa.af.Relation;
import ru.runa.af.RelationPermission;
import ru.runa.af.service.AuthorizationService;
import ru.runa.af.service.RelationService;
import ru.runa.common.web.Commons;
import ru.runa.common.web.Commons.PortletUrl;
import ru.runa.common.web.Messages;
import ru.runa.common.web.tag.LinkTag;
import ru.runa.delegate.DelegateFactory;

/**
 * @jsp.tag name = "managePermissionOnRelationLink" body-content = "empty"
 */
public class ManagePermissionOnRelationLinkTag extends LinkTag {

    private static final long serialVersionUID = 1L;

    private String relationName;

    private static final String HREF = "/relation_permission.do";

    @Override
    protected boolean isLinkEnabled() throws JspException {
        try {
            RelationService relationService = DelegateFactory.getInstance().getRelationService();
            AuthorizationService authorizationService = DelegateFactory.getInstance().getAuthorizationService();
            Subject subject = getSubject();
            Relation relationGroup = relationService.getRelation(subject, getRelationName());
            return authorizationService.isAllowed(subject, RelationPermission.UPDATE_PERMISSIONS, relationGroup);
        } catch (Exception e) {
            return false;
        }
    }

    @Override
    protected String getHref() {
        try {
            RelationService relationService = DelegateFactory.getInstance().getRelationService();
            Relation relationGroup;
            relationGroup = relationService.getRelation(getSubject(), getRelationName());
            Map<String, Object> params = new HashMap<String, Object>();
            params.put("relationName", getRelationName());
            params.put("id", relationGroup.getId());
            return Commons.getActionUrl(HREF, params, pageContext, PortletUrl.Action);
        } catch (Exception e) {
            return "";
        }
    }

    @Override
    protected String getLinkText() {
        return Messages.getMessage(Messages.TITLE_PERMISSION_OWNERS, pageContext);
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
