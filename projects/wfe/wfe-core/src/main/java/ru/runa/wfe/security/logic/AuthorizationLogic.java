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

import javax.security.auth.Subject;

import ru.runa.wfe.commons.logic.CommonLogic;
import ru.runa.wfe.commons.logic.PresentationCompilerHelper;
import ru.runa.wfe.presentation.BatchPresentation;
import ru.runa.wfe.presentation.hibernate.BatchPresentationHibernateCompiler;
import ru.runa.wfe.security.AuthenticationException;
import ru.runa.wfe.security.AuthorizationException;
import ru.runa.wfe.security.Identifiable;
import ru.runa.wfe.security.Permission;
import ru.runa.wfe.security.UnapplicablePermissionException;
import ru.runa.wfe.security.auth.SubjectPrincipalsHelper;
import ru.runa.wfe.user.Actor;
import ru.runa.wfe.user.Executor;
import ru.runa.wfe.user.ExecutorDoesNotExistException;

import com.google.common.base.Preconditions;
import com.google.common.collect.Sets;

/**
 * Created on 14.03.2005
 * 
 */
public class AuthorizationLogic extends CommonLogic {
    public boolean isAllowed(Subject subject, Permission permission, Identifiable identifiable) throws AuthenticationException {
        Actor actor = SubjectPrincipalsHelper.getActor(subject);
        return permissionDAO.isAllowed(actor, permission, identifiable);
    }

    public boolean isPrivelegedExecutor(Subject subject, Executor executor, Identifiable identifiable) throws AuthorizationException,
            AuthenticationException {
        checkPermissionAllowed(subject, executor, Permission.READ);
        checkPermissionAllowed(subject, identifiable, Permission.READ);
        return permissionDAO.isPrivilegedExecutor(executor, identifiable);
    }

    public boolean[] isAllowed(Subject subject, Permission permission, List<? extends Identifiable> identifiables) throws AuthenticationException {
        Actor actor = SubjectPrincipalsHelper.getActor(subject);
        return permissionDAO.isAllowed(actor, permission, identifiables);
    }

    public Collection<Permission> getPermissions(Subject subject, Executor performer, Identifiable identifiable)
            throws ExecutorDoesNotExistException, AuthorizationException, AuthenticationException {
        performer = checkPermissionsOnExecutor(subject, performer, Permission.READ);
        checkPermissionAllowed(subject, identifiable, Permission.READ);
        return permissionDAO.getPermissions(performer, identifiable);
    }

    public Collection<Permission> getOwnPermissions(Subject subject, Executor performer, Identifiable identifiable)
            throws ExecutorDoesNotExistException, AuthorizationException, AuthenticationException {
        performer = checkPermissionsOnExecutor(subject, performer, Permission.READ);
        checkPermissionAllowed(subject, identifiable, Permission.READ);
        return permissionDAO.getOwnPermissions(performer, identifiable);
    }

    public void setPermissions(Subject subject, Executor performer, Collection<Permission> permissions, Identifiable identifiable)
            throws UnapplicablePermissionException, ExecutorDoesNotExistException, AuthorizationException, AuthenticationException {
        setPermissionOnIdentifiable(subject, performer, permissions, identifiable);
    }

    public void setPermissions(Subject subject, List<Long> executorIds, Collection<Permission> permissions, Identifiable identifiable)
            throws UnapplicablePermissionException, ExecutorDoesNotExistException, AuthorizationException, AuthenticationException {
        List<Executor> executors = executorDAO.getExecutors(executorIds);
        checkIsChangingPermissionForPrivilegedExecutors(executors, identifiable, permissions);
        checkPermissionsOnExecutors(subject, executors, Permission.READ);
        checkPermissionAllowed(subject, identifiable, Permission.UPDATE_PERMISSIONS);
        for (Executor executor : executors) {
            permissionDAO.setPermissions(executor, permissions, identifiable);
        }
    }

    public void setPermissions(Subject subject, List<Long> executorIds, List<Collection<Permission>> permissions, Identifiable identifiable)
            throws UnapplicablePermissionException, ExecutorDoesNotExistException, AuthorizationException, AuthenticationException {
        List<Executor> executors = executorDAO.getExecutors(executorIds);
        checkIsChangingPermissionForPrivilegedExecutors(executors, identifiable, permissions);
        checkPermissionAllowed(subject, identifiable, Permission.UPDATE_PERMISSIONS);
        Preconditions.checkArgument(executors.size() == permissions.size(), "arrays length differs");
        for (int i = 0; i < executors.size(); i++) {
            permissionDAO.setPermissions(executors.get(i), permissions.get(i), identifiable);
        }
    }

    public void setPermissions(Subject subject, Executor performer, Collection<Permission> permissions, List<? extends Identifiable> identifiables)
            throws UnapplicablePermissionException, ExecutorDoesNotExistException, AuthorizationException, AuthenticationException {
        for (Identifiable identifiable : identifiables) {
            performer = setPermissionOnIdentifiable(subject, performer, permissions, identifiable);
        }
    }

    private Executor setPermissionOnIdentifiable(Subject subject, Executor performer, Collection<Permission> permissions, Identifiable identifiable)
            throws AuthorizationException, ExecutorDoesNotExistException, AuthenticationException, UnapplicablePermissionException {
        checkIsChangingPermissionForPrivilegedExecutors(performer, identifiable, permissions);
        performer = checkPermissionsOnExecutor(subject, performer, Permission.READ);
        checkPermissionAllowed(subject, identifiable, Permission.UPDATE_PERMISSIONS);
        permissionDAO.setPermissions(performer, permissions, identifiable);
        return performer;
    }

    /**
     * Load executor's which already has (or not has) some permission on specified identifiable. This query using paging.
     * 
     * @param subject
     *            Current actor {@linkplain Subject}.
     * @param identifiable
     *            {@linkplain Identifiable} to load executors, which has (or not) permission on this identifiable.
     * @param batchPresentation
     *            {@linkplain BatchPresentation} for loading executors.
     * @param hasPermission
     *            Flag equals true to load executors with permissions on {@linkplain Identifiable}; false to load executors without permissions.
     * @return Executors with or without permission on {@linkplain Identifiable}.
     */
    public List<Executor> getExecutorsWithPermission(Subject subject, Identifiable identifiable, BatchPresentation batchPresentation,
            boolean hasPermission) throws AuthorizationException, AuthenticationException {
        checkPermissionAllowed(subject, identifiable, Permission.READ);
        BatchPresentationHibernateCompiler compiler = PresentationCompilerHelper.createExecutorWithPermissionCompiler(subject, identifiable,
                batchPresentation, hasPermission);
        return compiler.getBatch();
    }

    /**
     * Load executor's count which already has (or not has) some permission on specified identifiable.
     * 
     * @param subject
     *            Current actor {@linkplain Subject}.
     * @param identifiable
     *            {@linkplain Identifiable} to load executors, which has (or not) permission on this identifiable.
     * @param batchPresentation
     *            {@linkplain BatchPresentation} for loading executors.
     * @param hasPermission
     *            Flag equals true to load executors with permissions on {@linkplain Identifiable}; false to load executors without permissions.
     * @return Count of executors with or without permission on {@linkplain Identifiable}.
     */
    public int getExecutorsWithPermissionCount(Subject subject, Identifiable identifiable, BatchPresentation batchPresentation, boolean hasPermission)
            throws AuthorizationException, AuthenticationException {
        checkPermissionAllowed(subject, identifiable, Permission.READ);
        BatchPresentationHibernateCompiler compiler = PresentationCompilerHelper.createExecutorWithPermissionCompiler(subject, identifiable,
                batchPresentation, hasPermission);
        return compiler.getCount();
    }

    private void checkIsChangingPermissionForPrivilegedExecutors(List<Executor> executors, Identifiable identifiable,
            List<Collection<Permission>> permissions) throws AuthorizationException, ExecutorDoesNotExistException {
        for (int i = 0; i < executors.size(); i++) {
            checkIsChangingPermissionForPrivilegedExecutors(executors.get(i), identifiable, permissions.get(i));
        }
    }

    private void checkIsChangingPermissionForPrivilegedExecutors(List<Executor> executors, Identifiable identifiable,
            Collection<Permission> permissions) throws AuthorizationException, ExecutorDoesNotExistException {
        for (Executor executor : executors) {
            checkIsChangingPermissionForPrivilegedExecutors(executor, identifiable, permissions);
        }
    }

    private void checkIsChangingPermissionForPrivilegedExecutors(Executor executor, Identifiable identifiable, Collection<Permission> permissions)
            throws AuthorizationException, ExecutorDoesNotExistException {
        if (permissionDAO.isPrivilegedExecutor(executor, identifiable)) {
            checkIsPermissionChanged(executor, identifiable, permissions);
        }
    }

    private void checkIsPermissionChanged(Executor executor, Identifiable identifiable, Collection<Permission> permissions)
            throws AuthorizationException, ExecutorDoesNotExistException {
        Set<Permission> currentPermissions = Sets.newHashSet(permissionDAO.getOwnPermissions(executor, identifiable));
        Set<Permission> newPermissions = Sets.newHashSet(permissions);
        if (!currentPermissions.equals(newPermissions)) {
            throw new AuthorizationException("Can not change permissions on priveleged executor " + executor.getName());
        }
    }
}
