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
package ru.runa.af.logic;

import java.util.Collection;
import java.util.List;
import java.util.Set;

import javax.security.auth.Subject;

import ru.runa.af.Actor;
import ru.runa.af.AuthenticationException;
import ru.runa.af.AuthorizationException;
import ru.runa.af.Executor;
import ru.runa.af.ExecutorOutOfDateException;
import ru.runa.af.Identifiable;
import ru.runa.af.Permission;
import ru.runa.af.SecuredObject;
import ru.runa.af.SecuredObjectOutOfDateException;
import ru.runa.af.UnapplicablePermissionException;
import ru.runa.af.authenticaion.SubjectPrincipalsHelper;
import ru.runa.af.presentation.BatchPresentation;
import ru.runa.af.presentation.BatchPresentationHibernateCompiler;

import com.google.common.collect.Sets;

/**
 * Created on 14.03.2005
 * 
 */
public class AuthorizationLogic extends CommonLogic {
    public boolean isAllowed(Subject subject, Permission permission, Identifiable identifiable) throws AuthenticationException {
        Actor actor = SubjectPrincipalsHelper.getActor(subject);
        SecuredObject securedObject = securedObjectDAO.get(identifiable);
        return permissionDAO.isAllowed(actor, permission, securedObject);
    }

    public boolean isPrivelegedExecutor(Subject subject, Executor executor, Identifiable identifiable) throws AuthorizationException,
            AuthenticationException {
        checkPermissionAllowed(subject, executor, Permission.READ);
        checkPermissionAllowed(subject, identifiable, Permission.READ);
        return securedObjectDAO.isPrivilegedExecutor(executor, identifiable);
    }

    public boolean[] isAllowed(Subject subject, Permission permission, List<? extends Identifiable> identifiables) throws AuthenticationException {
        Actor actor = SubjectPrincipalsHelper.getActor(subject);
        List<SecuredObject> securedObjects = securedObjectDAO.get(identifiables);
        return permissionDAO.isAllowed(actor, permission, securedObjects);
    }

    public Collection<Permission> getPermissions(Subject subject, Executor performer, Identifiable identifiable) throws ExecutorOutOfDateException,
            AuthorizationException, AuthenticationException {
        performer = checkPermissionsOnExecutor(subject, performer, Permission.READ);
        SecuredObject securedObject = securedObjectDAO.get(identifiable);
        checkPermissionAllowed(subject, identifiable, Permission.READ);
        return permissionDAO.getPermissions(performer, securedObject);
    }

    public Collection<Permission> getOwnPermissions(Subject subject, Executor performer, Identifiable identifiable) throws ExecutorOutOfDateException,
            AuthorizationException, AuthenticationException {
        performer = checkPermissionsOnExecutor(subject, performer, Permission.READ);
        checkPermissionAllowed(subject, identifiable, Permission.READ);
        SecuredObject securedObject = securedObjectDAO.get(identifiable);
        return permissionDAO.getOwnPermissions(performer, securedObject);
    }

    public void setPermissions(Subject subject, Executor performer, Collection<Permission> permissions, Identifiable identifiable)
            throws UnapplicablePermissionException, ExecutorOutOfDateException, AuthorizationException, AuthenticationException {
        setPermissionOnIdentifiable(subject, performer, permissions, identifiable);
    }

    public void setPermissions(Subject subject, List<Long> executorIds, Collection<Permission> permissions, Identifiable identifiable)
            throws UnapplicablePermissionException, ExecutorOutOfDateException, AuthorizationException, AuthenticationException {
        List<Executor> executors = executorDAO.getExecutors(executorIds);
        checkIsChangingPermissionForPrivilegedExecutors(executors, identifiable, permissions);
        checkPermissionsOnExecutors(subject, executors, Permission.READ);
        checkPermissionAllowed(subject, identifiable, Permission.UPDATE_PERMISSIONS);
        SecuredObject securedObject = securedObjectDAO.get(identifiable);
        permissionDAO.setPermissions(executors, permissions, securedObject);
    }

    public void setPermissions(Subject subject, List<Long> executorIds, List<Collection<Permission>> permissions, Identifiable identifiable)
            throws UnapplicablePermissionException, ExecutorOutOfDateException, AuthorizationException, AuthenticationException {
        List<Executor> executors = executorDAO.getExecutors(executorIds);
        checkIsChangingPermissionForPrivilegedExecutors(executors, identifiable, permissions);
        checkPermissionsOnExecutors(subject, executors, Permission.READ);
        checkPermissionAllowed(subject, identifiable, Permission.UPDATE_PERMISSIONS);
        SecuredObject securedObject = securedObjectDAO.get(identifiable);
        permissionDAO.setPermissions(executors, permissions, securedObject);
    }

    public void setPermissions(Subject subject, Executor performer, Collection<Permission> permissions, List<? extends Identifiable> identifiables)
            throws UnapplicablePermissionException, ExecutorOutOfDateException, AuthorizationException, AuthenticationException {
        for (Identifiable identifiable : identifiables) {
            performer = setPermissionOnIdentifiable(subject, performer, permissions, identifiable);
        }
    }

    private Executor setPermissionOnIdentifiable(Subject subject, Executor performer, Collection<Permission> permissions, Identifiable identifiable)
            throws AuthorizationException, ExecutorOutOfDateException, AuthenticationException, UnapplicablePermissionException {
        checkIsChangingPermissionForPrivilegedExecutors(performer, identifiable, permissions);
        performer = checkPermissionsOnExecutor(subject, performer, Permission.READ);
        checkPermissionAllowed(subject, identifiable, Permission.UPDATE_PERMISSIONS);
        SecuredObject securedObject = securedObjectDAO.get(identifiable);
        permissionDAO.setPermissions(performer, permissions, securedObject);
        return performer;
    }

    /**
     * Load executor's which already has (or not has) some permission on specified identifiable.
     * This query using paging.
     * @param subject Current actor {@linkplain Subject}.
     * @param identifiable {@linkplain Identifiable} to load executors, which has (or not) permission on this identifiable.
     * @param batchPresentation {@linkplain BatchPresentation} for loading executors.
     * @param hasPermission Flag equals true to load executors with permissions on {@linkplain Identifiable}; false to load executors without permissions. 
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
     * @param subject Current actor {@linkplain Subject}.
     * @param identifiable {@linkplain Identifiable} to load executors, which has (or not) permission on this identifiable.
     * @param batchPresentation {@linkplain BatchPresentation} for loading executors.
     * @param hasPermission Flag equals true to load executors with permissions on {@linkplain Identifiable}; false to load executors without permissions. 
     * @return Count of executors with or without permission on {@linkplain Identifiable}.
     */
    public int getExecutorsWithPermissionCount(Subject subject, Identifiable identifiable, BatchPresentation batchPresentation, boolean hasPermission)
            throws AuthorizationException, AuthenticationException {
        checkPermissionAllowed(subject, identifiable, Permission.READ);
        BatchPresentationHibernateCompiler compiler = PresentationCompilerHelper.createExecutorWithPermissionCompiler(subject, identifiable,
                batchPresentation, hasPermission);
        return compiler.getCount();
    }

    public List<Permission> getAllPermissions(Identifiable identifiable) {
        return getNoPermission(identifiable).getAllPermissions();
    }

    public Permission getNoPermission(Identifiable identifiable) {
        return securedObjectDAO.getNoPermission(identifiable);
    }

    private void checkIsChangingPermissionForPrivilegedExecutors(List<Executor> executors, Identifiable identifiable, List<Collection<Permission>> permissions)
            throws AuthorizationException, ExecutorOutOfDateException, SecuredObjectOutOfDateException {
        Set<Executor> privelegedExecutorsSet = getPrivelegeExecutors(identifiable);
        for (int i = 0; i < executors.size(); i++) {
            if (privelegedExecutorsSet.contains(executors.get(i))) {
                checkIsPermissionChanged(executors.get(i), identifiable, permissions.get(i));
            }
        }
    }

    private void checkIsChangingPermissionForPrivilegedExecutors(List<Executor> executors, Identifiable identifiable, Collection<Permission> permissions)
            throws AuthorizationException, ExecutorOutOfDateException, SecuredObjectOutOfDateException {
        Set<Executor> privelegedExecutorsSet = getPrivelegeExecutors(identifiable);
        for (Executor executor : executors) {
            if (privelegedExecutorsSet.contains(executor)) {
                checkIsPermissionChanged(executor, identifiable, permissions);
            }
        }
    }

    private void checkIsChangingPermissionForPrivilegedExecutors(Executor executor, Identifiable identifiable, Collection<Permission> permissions)
            throws AuthorizationException, ExecutorOutOfDateException, SecuredObjectOutOfDateException {
        Set<Executor> privelegedExecutorsSet = getPrivelegeExecutors(identifiable);
        if (privelegedExecutorsSet.contains(executor)) {
            checkIsPermissionChanged(executor, identifiable, permissions);
        }
    }

    private Set<Executor> getPrivelegeExecutors(Identifiable identifiable) {
        return securedObjectDAO.getPrivilegedExecutors(identifiable);
    }

    private void checkIsPermissionChanged(Executor executor, Identifiable identifiable, Collection<Permission> permissions) throws AuthorizationException,
            ExecutorOutOfDateException, SecuredObjectOutOfDateException {
        Set<Permission> currentPermissions = Sets.newHashSet(permissionDAO.getOwnPermissions(executor, securedObjectDAO.get(identifiable)));
        Set<Permission> newPermissions = Sets.newHashSet(permissions);
        if (!currentPermissions.equals(newPermissions)) {
            throw new AuthorizationException("Can not change permissions on priveleged executor " + executor.getName());
        }
    }
}
