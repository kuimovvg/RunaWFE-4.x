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
package ru.runa.af.web;

import java.util.HashSet;
import java.util.List;

import ru.runa.service.AuthorizationService;
import ru.runa.service.delegate.Delegates;
import ru.runa.wfe.presentation.BatchPresentation;
import ru.runa.wfe.security.Permission;
import ru.runa.wfe.security.SecuredObjectType;
import ru.runa.wfe.user.Executor;
import ru.runa.wfe.user.User;

public class BatchExecutorPermissionHelper {
    private static final SecuredObjectType[] ACTOR_GROUP_CLASSESS = { SecuredObjectType.ACTOR, SecuredObjectType.GROUP };

    public static boolean[] getEnabledCheckboxes(User user, List<? extends Executor> executors, BatchPresentation batchPresentation,
            Permission permission) {
        AuthorizationService authorizationService = Delegates.getAuthorizationService();
        // boolean[] enabledCheckboxed =
        // authorizationServiceDelegate.isAllowed(subject,
        // ExecutorPermission.UPDATE, executors);
        HashSet<Executor> executorsWithUpdatePermissionSet = new HashSet<Executor>(authorizationService.getPersistentObjects(user, batchPresentation,
                Executor.class, permission, ACTOR_GROUP_CLASSESS, false));
        boolean[] enabledCheckboxed = new boolean[executors.size()];
        for (int i = 0; i < executors.size(); i++) {
            if (executorsWithUpdatePermissionSet.contains(executors.get(i))) {
                enabledCheckboxed[i] = true;
            }
        }
        return enabledCheckboxed;
    }
}
