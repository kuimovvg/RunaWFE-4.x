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
package ru.runa.af.web.action;

import java.util.HashMap;
import java.util.Map;

import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.action.ActionMessages;

import ru.runa.common.web.ActionExceptionHelper;
import ru.runa.common.web.Commons;
import ru.runa.common.web.Resources;
import ru.runa.common.web.action.UpdatePermissionOnIdentifiableAction;
import ru.runa.service.delegate.Delegates;
import ru.runa.wfe.relation.Relation;
import ru.runa.wfe.security.Identifiable;
import ru.runa.wfe.user.User;

/**
 * @struts:action path="/updatePermissionOnRelation"
 *                name="updatePermissionsOnIdentifiableForm" validate="true"
 *                input = "/WEB-INF/af/manage_relation_members.jsp"
 * @struts.action-forward name="success" path="/relation_permission.do" redirect
 *                        = "true"
 * @struts.action-forward name="failure" path="/relation_permission.do" redirect
 *                        = "true"
 */
public class UpdatePermissionOnRelation extends UpdatePermissionOnIdentifiableAction {
    public static final String ACTION_PATH_NAME = "/updatePermissionOnRelation";

    @Override
    protected Identifiable getIdentifiable(User user, Long identifiableId, ActionMessages errors) {
        try {
            return Delegates.getRelationService().getRelation(user, identifiableId);
        } catch (Exception e) {
            ActionExceptionHelper.addException(errors, e);
            return null;
        }
    }

    @Override
    public ActionForward getErrorForward(ActionMapping mapping, Long identifiableId) {
        throw new UnsupportedOperationException();
    }

    @Override
    public ActionForward getSuccessForward(ActionMapping mapping, Long identifiableId) {
        throw new UnsupportedOperationException();
    }

    @Override
    protected ActionForward getErrorForward(User user, ActionMapping mapping, Long identifiableId) {
        try {
            Relation relation = Delegates.getRelationService().getRelation(user, identifiableId);
            Map<String, Object> params = new HashMap<String, Object>();
            params.put("relationName", relation.getName());
            params.put("id", identifiableId);
            return Commons.forward(mapping.findForward(Resources.FORWARD_FAILURE), params);
        } catch (Exception e) {
            return null;
        }
    }

    @Override
    protected ActionForward getSuccessForward(User user, ActionMapping mapping, Long identifiableId) {
        try {
            Relation relation = Delegates.getRelationService().getRelation(user, identifiableId);
            Map<String, Object> params = new HashMap<String, Object>();
            params.put("relationName", relation.getName());
            params.put("id", identifiableId);
            return Commons.forward(mapping.findForward(Resources.FORWARD_SUCCESS), params);
        } catch (Exception e) {
            return null;
        }
    }
}
