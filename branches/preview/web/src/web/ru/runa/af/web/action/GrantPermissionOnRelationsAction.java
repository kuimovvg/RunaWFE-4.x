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

import java.util.List;

import javax.security.auth.Subject;

import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.action.ActionMessages;

import ru.runa.af.Identifiable;
import ru.runa.af.Permission;
import ru.runa.af.RelationPermission;
import ru.runa.af.RelationsGroupSecure;
import ru.runa.common.web.Resources;
import ru.runa.common.web.action.GrantPermisionOnIdentifiableAction;

import com.google.common.collect.Lists;

/**
 * Created on 23.08.2004
 * 
 * @struts:action path="/grantPermissionOnRelations" name="idsForm"
 *                validate="false"
 * @struts.action-forward name="success"
 *                        path="/group_of_relations_permission.do" redirect =
 *                        "true"
 * @struts.action-forward name="failure"
 *                        path="/group_of_relations_permission.do" redirect =
 *                        "true"
 */
public class GrantPermissionOnRelationsAction extends GrantPermisionOnIdentifiableAction {

    public static final String ACTION_PATH = "/grantPermissionOnRelations";

    private static List<Permission> PERMISSIONS = Lists.newArrayList(RelationPermission.READ);

    @Override
    protected List<Permission> getIdentifiablePermissions() {
        return PERMISSIONS;
    }

    @Override
    protected Identifiable getIdentifiable(Subject subject, Long identifiableId, ActionMessages errors) {
        return RelationsGroupSecure.INSTANCE;
    }

    @Override
    public ActionForward getErrorForward(ActionMapping mapping, Long identifiableId) {
        return mapping.findForward(Resources.FORWARD_FAILURE);
    }

    @Override
    public ActionForward getSuccessForward(ActionMapping mapping, Long identifiableId) {
        return mapping.findForward(Resources.FORWARD_SUCCESS);
    }
}
