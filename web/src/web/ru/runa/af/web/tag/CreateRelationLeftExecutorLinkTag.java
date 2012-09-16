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
 * @jsp.tag name = "createRelationLeftExecutorLink" body-content = "empty"
 */
public class CreateRelationLeftExecutorLinkTag extends LinkTag {

    private static final long serialVersionUID = 1L;

    private String relationName;

    private Long executorId;

    /**
     * @jsp.attribute required = "true" rtexprvalue = "true"
     */
    public String getRelationName() {
        return relationName;
    }

    public void setRelationName(String relationName) {
        this.relationName = relationName;
    }

    /**
     * @jsp.attribute required = "true" rtexprvalue = "true"
     */
    public Long getExecutorId() {
        return executorId;
    }

    public void setExecutorId(Long executorId) {
        this.executorId = executorId;
    }

    @Override
    protected String getHref() throws JspException {
        Map<String, Object> params = new HashMap<String, Object>();
        params.put("relationName", relationName);
        params.put("executorId", executorId);
        return Commons.getActionUrl("create_relation_left_executor.do", params, pageContext, PortletUrl.Action);
    }

    @Override
    protected String getLinkText() {
        return Messages.getMessage(Messages.LINK_CREATE_RELATION, pageContext);
    }

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
}
