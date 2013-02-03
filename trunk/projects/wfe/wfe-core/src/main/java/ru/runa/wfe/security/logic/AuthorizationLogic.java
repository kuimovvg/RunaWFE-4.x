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
package ru.runa.wfe.security.logic;

import java.util.Collection;
import java.util.List;
import java.util.Set;

import ru.runa.wfe.commons.logic.CommonLogic;
import ru.runa.wfe.commons.logic.PresentationCompilerHelper;
import ru.runa.wfe.presentation.BatchPresentation;
import ru.runa.wfe.presentation.hibernate.BatchPresentationHibernateCompiler;
import ru.runa.wfe.security.AuthorizationException;
import ru.runa.wfe.security.Identifiable;
import ru.runa.wfe.security.Permission;
import ru.runa.wfe.user.Executor;
import ru.runa.wfe.user.User;

import com.google.common.base.Preconditions;
import com.google.common.collect.Sets;

/**
 * Created on 14.03.2005
 * 
 */
public class AuthorizationLogic extends CommonLogic {
    public boolean isAllowed(User user, Permission permission, Identifiable identifiable) {
        return permissionDAO.isAllowed(user, permission, identifiable);
    }

    public boolean isPrivelegedExecutor(User user, Executor executor, Identifiable identifiable) {
        checkPermissionAllowed(user, executor, Permission.READ);
        checkPermissionAllowed(user, identifiable, Permission.READ);
        return permissionDAO.isPrivilegedExecutor(executor, identifiable);
    }

    public boolean[] isAllowed(User user, Permission permission, List<? extends Identifiable> identifiables) {
        return permissionDAO.isAllowed(user, permission, identifiables);
    }

    public Collection<Permission> getOwnPermissions(User user, Executor performer, Identifiable identifiable) {
        checkPermissionsOnExecutor(user, performer, Permission.READ);
        checkPermissionAllowed(user, identifiable, Permission.READ);
        return permissionDAO.getOwnPermissions(performer, identifiable);
    }

    public void setPermissions(User user, Executor performer, Collection<Permission> permissions, Identifiable identifiable) {
        setPermissionOnIdentifiable(user, performer, permissions, identifiable);
    }

    public void setPermissions(User user, List<Long> executorIds, Collection<Permission> permissions, Identifiable identifiable) {
        List<Executor> executors = executorDAO.getExecutors(executorIds);
        checkIsChangingPermissionForPrivilegedExecutors(executors, identifiable, permissions);
        checkPermissionsOnExecutors(user, executors, Permission.READ);
        checkPermissionAllowed(user, identifiable, Permission.UPDATE_PERMISSIONS);
        for (Executor executor : executors) {
            permissionDAO.setPermissions(executor, permissions, identifiable);
        }
    }

    public void setPermissions(User user, List<Long> executorIds, List<Collection<Permission>> permissions, Identifiable identifiable) {
        List<Executor> executors = executorDAO.getExecutors(executorIds);
        checkIsChangingPermissionForPrivilegedExecutors(executors, identifiable, permissions);
        checkPermissionAllowed(user, identifiable, Permission.UPDATE_PERMISSIONS);
        Preconditions.checkArgument(executors.size() == permissions.size(), "arrays length differs");
        for (int i = 0; i < executors.size(); i++) {
            permissionDAO.setPermissions(executors.get(i), permissions.get(i), identifiable);
        }
    }

    public void setPermissions(User user, Executor performer, Collection<Permission> permissions, List<? extends Identifiable> identifiables) {
        for (Identifiable identifiable : identifiables) {
            performer = setPermissionOnIdentifiable(user, performer, permissions, identifiable);
        }
    }

    private Executor setPermissionOnIdentifiable(User user, Executor performer, Collection<Permission> permissions, Identifiable identifiable) {
        checkIsChangingPermissionForPrivilegedExecutors(performer, identifiable, permissions);
        checkPermissionsOnExecutor(user, performer, Permission.READ);
        checkPermissionAllowed(user, identifiable, Permission.UPDATE_PERMISSIONS);
        permissionDAO.setPermissions(performer, permissions, identifiable);
        return performer;
    }

    /**
     * Load executor's which already has (or not has) some permission on
     * specified identifiable. This query using paging.
     * 
     * @param user
     *            Current actor {@linkplain user}.
     * @param identifiable
     *            {@linkplain Identifiable} to load executors, which has (or
     *            not) permission on this identifiable.
     * @param batchPresentation
     *            {@linkplain BatchPresentation} for loading executors.
     * @param hasPermission
     *            Flag equals true to load executors with permissions on
     *            {@linkplain Identifiable}; false to load executors without
     *            permissions.
     * @return Executors with or without permission on {@linkplain Identifiable}
     *         .
     */
    public List<Executor> getExecutorsWithPermission(User user, Identifiable identifiable, BatchPresentation batchPresentation, boolean hasPermission) {
        checkPermissionAllowed(user, identifiable, Permission.READ);
        BatchPresentationHibernateCompiler compiler = PresentationCompilerHelper.createExecutorWithPermissionCompiler(user, identifiable,
                batchPresentation, hasPermission);
        if (hasPermission) {
            List<Executor> executors = compiler.getBatch();
            executors.addAll(0, permissionDAO.getPrivilegedExecutors(identifiable));
            return executors;
        } else {
            return compiler.getBatch();
        }
    }

    /**
     * Load executor's count which already has (or not has) some permission on
     * specified identifiable.
     * 
     * @param user
     *            Current actor {@linkplain user}.
     * @param identifiable
     *            {@linkplain Identifiable} to load executors, which has (or
     *            not) permission on this identifiable.
     * @param batchPresentation
     *            {@linkplain BatchPresentation} for loading executors.
     * @param hasPermission
     *            Flag equals true to load executors with permissions on
     *            {@linkplain Identifiable}; false to load executors without
     *            permissions.
     * @return Count of executors with or without permission on
     *         {@linkplain Identifiable}.
     */
    public int getExecutorsWithPermissionCount(User user, Identifiable identifiable, BatchPresentation batchPresentation, boolean hasPermission) {
        checkPermissionAllowed(user, identifiable, Permission.READ);
        BatchPresentationHibernateCompiler compiler = PresentationCompilerHelper.createExecutorWithPermissionCompiler(user, identifiable,
                batchPresentation, hasPermission);
        return compiler.getCount();
    }

    private void checkIsChangingPermissionForPrivilegedExecutors(List<Executor> executors, Identifiable identifiable,
            List<Collection<Permission>> permissions) {
        for (int i = 0; i < executors.size(); i++) {
            checkIsChangingPermissionForPrivilegedExecutors(executors.get(i), identifiable, permissions.get(i));
        }
    }

    private void checkIsChangingPermissionForPrivilegedExecutors(List<Executor> executors, Identifiable identifiable,
            Collection<Permission> permissions) {
        for (Executor executor : executors) {
            checkIsChangingPermissionForPrivilegedExecutors(executor, identifiable, permissions);
        }
    }

    private void checkIsChangingPermissionForPrivilegedExecutors(Executor executor, Identifiable identifiable, Collection<Permission> permissions) {
        if (permissionDAO.isPrivilegedExecutor(executor, identifiable)) {
            checkIsPermissionChanged(executor, identifiable, permissions);
        }
    }

    private void checkIsPermissionChanged(Executor executor, Identifiable identifiable, Collection<Permission> permissions) {
        Set<Permission> currentPermissions = Sets.newHashSet(permissionDAO.getOwnPermissions(executor, identifiable));
        Set<Permission> newPermissions = Sets.newHashSet(permissions);
        if (!currentPermissions.equals(newPermissions)) {
            throw new AuthorizationException("Can not change permissions on priveleged executor " + executor);
        }
    }
}
