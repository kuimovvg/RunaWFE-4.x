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
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;

import ru.runa.af.web.form.RelationIdsForm;
import ru.runa.common.web.Commons;
import ru.runa.common.web.Resources;
import ru.runa.common.web.action.IdentifiableAction;
import ru.runa.wfe.relation.RelationPermission;
import ru.runa.wfe.relation.RelationsGroupSecure;
import ru.runa.wfe.security.Identifiable;
import ru.runa.wfe.security.Permission;
import ru.runa.wfe.service.AuthorizationService;
import ru.runa.wfe.service.RelationService;
import ru.runa.wfe.service.delegate.Delegates;
import ru.runa.wfe.user.User;

import com.google.common.collect.Lists;

/**
 * Created on 23.08.2004
 * 
 * @struts:action path="/grantPermissionOnRelation" name="relationIdsForm"
 *                validate="false" input="/WEB-INF/af/relation_permission.jsp"
 * @struts.action-forward name="success" path="/relation_permission.do" redirect
 *                        = "true"
 * @struts.action-forward name="failure" path="/relation_permission.do" redirect
 *                        = "true"
 */
public class GrantPermissionOnRelationAction extends IdentifiableAction {

    public static final String ACTION_PATH = "/grantPermissionOnRelation";

    private static List<Permission> PERMISSIONS = Lists.newArrayList(RelationPermission.READ);

    @Override
    protected List<Permission> getIdentifiablePermissions() {
        return PERMISSIONS;
    }

    @Override
    public ActionForward execute(ActionMapping mapping, ActionForm form, HttpServletRequest request, HttpServletResponse response) {
        RelationIdsForm relationForm = (RelationIdsForm) form;
        List<Long> selectedIds = Lists.newArrayList(relationForm.getIds());
        Map<String, Object> params = new HashMap<String, Object>();
        params.put("relationName", relationForm.getRelationName());
        params.put("id", relationForm.getId());
        try {
            AuthorizationService authorizationService = Delegates.getAuthorizationService();
            RelationService relationService = Delegates.getRelationService();
            Identifiable identifiable = relationService.getRelationByName(getLoggedUser(request), relationForm.getRelationName());
            if (identifiable != null) {
                authorizationService.setPermissions(getLoggedUser(request), selectedIds, getIdentifiablePermissions(), identifiable);
            }
        } catch (Exception e) {
            addError(request, e);
            return Commons.forward(mapping.findForward(Resources.FORWARD_FAILURE), params);
        }
        return Commons.forward(mapping.findForward(Resources.FORWARD_SUCCESS), params);
    }

    @Override
    protected Identifiable getIdentifiable(User user, Long identifiableId) {
        return RelationsGroupSecure.INSTANCE;
    }
}
