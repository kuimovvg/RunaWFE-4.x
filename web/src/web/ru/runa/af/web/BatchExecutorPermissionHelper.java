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

import javax.security.auth.Subject;

import ru.runa.af.Actor;
import ru.runa.af.AuthenticationException;
import ru.runa.af.Executor;
import ru.runa.af.Group;
import ru.runa.af.Identifiable;
import ru.runa.af.Permission;
import ru.runa.af.presentation.BatchPresentation;
import ru.runa.af.service.AuthorizationService;
import ru.runa.delegate.DelegateFactory;

public class BatchExecutorPermissionHelper {
    private static final Class<? extends Identifiable>[] ACTOR_GROUP_CLASSESS = new Class[] { Actor.class, Group.class };

    public static boolean[] getEnabledCheckboxes(Subject subject, List<? extends Executor> executors, BatchPresentation batchPresentation, Permission permission)
            throws AuthenticationException {
        AuthorizationService authorizationService = DelegateFactory.getInstance().getAuthorizationService();
        // boolean[] enabledCheckboxed = authorizationServiceDelegate.isAllowed(subject, ExecutorPermission.UPDATE, executors);
        HashSet<Executor> executorsWithUpdatePermissionSet = new HashSet<Executor>(authorizationService.getPersistentObjects(subject,
                batchPresentation, Executor.class, permission, ACTOR_GROUP_CLASSESS, false));
        boolean[] enabledCheckboxed = new boolean[executors.size()];
        for (int i = 0; i < executors.size(); i++) {
            if (executorsWithUpdatePermissionSet.contains(executors.get(i))) {
                enabledCheckboxed[i] = true;
            }
        }
        return enabledCheckboxed;
    }
}
