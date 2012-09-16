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
package ru.runa.wf.logic;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import ru.runa.af.Executor;
import ru.runa.af.ExecutorOutOfDateException;
import ru.runa.af.Permission;
import ru.runa.af.SecuredObject;
import ru.runa.af.SecuredObjectOutOfDateException;
import ru.runa.af.dao.PermissionDAO;
import ru.runa.wf.ProcessDefinitionPermission;
import ru.runa.wf.ProcessInstancePermission;

/**
 */
public class InstancePermissionsHelper {

    private static final Map<Permission, Permission> DEFINITION_TO_INSTANCE_PERMISSION_MAP;
    static {
        DEFINITION_TO_INSTANCE_PERMISSION_MAP = new HashMap<Permission, Permission>();
        DEFINITION_TO_INSTANCE_PERMISSION_MAP.put(ProcessDefinitionPermission.READ_STARTED_INSTANCE, ProcessInstancePermission.READ);
        DEFINITION_TO_INSTANCE_PERMISSION_MAP.put(ProcessDefinitionPermission.CANCEL_STARTED_INSTANCE, ProcessInstancePermission.CANCEL_INSTANCE);
    }

    private InstancePermissionsHelper() {
    }

    public static Set<Permission> getInstancePermissions(Executor executor, SecuredObject definitionSecuredObject, PermissionDAO permissionDAO)
            throws ExecutorOutOfDateException, SecuredObjectOutOfDateException {
        List<Permission> definitionPermissions = permissionDAO.getOwnPermissions(executor, definitionSecuredObject);
        Set<Permission> result = new HashSet<Permission>();
        for (Permission permission : definitionPermissions) {
            if (DEFINITION_TO_INSTANCE_PERMISSION_MAP.containsKey(permission)) {
                result.add(DEFINITION_TO_INSTANCE_PERMISSION_MAP.get(permission));
            }
        }
        return result;
    }
}
