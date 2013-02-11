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

import ru.runa.common.web.Commons;
import ru.runa.common.web.Messages;
import ru.runa.common.web.tag.LinkTag;
import ru.runa.service.AuthorizationService;
import ru.runa.service.RelationService;
import ru.runa.service.delegate.Delegates;
import ru.runa.wfe.commons.web.PortletUrlType;
import ru.runa.wfe.relation.Relation;
import ru.runa.wfe.relation.RelationPermission;

/**
 * Created on 03.09.2004
 * 
 * @jsp.tag name = "createRelationRightExecutorLink" body-content = "empty"
 */
public class CreateRelationRightExecutorLinkTag extends LinkTag {

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
    protected String getHref() {
        Map<String, Object> params = new HashMap<String, Object>();
        params.put("relationName", relationName);
        params.put("executorId", executorId);
        return Commons.getActionUrl("create_relation_right_executor.do", params, pageContext, PortletUrlType.Action);
    }

    @Override
    protected String getLinkText() {
        return Messages.getMessage(Messages.LINK_CREATE_RELATION, pageContext);
    }

    @Override
    protected boolean isLinkEnabled() {
        try {
            RelationService relationService = Delegates.getRelationService();
            AuthorizationService authorizationService = Delegates.getAuthorizationService();
            Relation relationGroup = relationService.getRelationByName(getUser(), getRelationName());
            return authorizationService.isAllowed(getUser(), RelationPermission.UPDATE_PERMISSIONS, relationGroup);
        } catch (Exception e) {
            return false;
        }
    }
}
