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
package ru.runa.delegate.impl;

import java.util.Collection;
import java.util.List;

import javax.security.auth.Subject;

import ru.runa.af.AuthenticationException;
import ru.runa.af.AuthorizationException;
import ru.runa.af.Executor;
import ru.runa.af.ExecutorOutOfDateException;
import ru.runa.af.Identifiable;
import ru.runa.af.Permission;
import ru.runa.af.UnapplicablePermissionException;
import ru.runa.af.presentation.BatchPresentation;
import ru.runa.af.service.AuthorizationService;

/**
 * 
 * Created 14.10.2005
 */
public class AuthorizationServiceDelegateImpl extends EJB3Delegate implements AuthorizationService {

    @Override
    protected String getBeanName() {
        return "AuthorizationServiceBean";
    }

    private AuthorizationService getAuthorizationService() {
        return (AuthorizationService) getService();
    }

    @Override
    public boolean isAllowed(Subject subject, Permission permission, Identifiable identifiable) throws AuthorizationException,
            AuthenticationException {
        return getAuthorizationService().isAllowed(subject, permission, identifiable);
    }

    @Override
    public boolean[] isAllowed(Subject subject, Permission permission, List<? extends Identifiable> identifiables) throws AuthenticationException {
        return getAuthorizationService().isAllowed(subject, permission, identifiables);
    }

    @Override
    public void setPermissions(Subject subject, Executor performer, Collection<Permission> permissions, Identifiable identifiable)
            throws UnapplicablePermissionException, ExecutorOutOfDateException, AuthorizationException, AuthenticationException {
        getAuthorizationService().setPermissions(subject, performer, permissions, identifiable);
    }

    @Override
    public void setPermissions(Subject subject, List<Long> executorsId, List<Collection<Permission>> permissions, Identifiable identifiable)
            throws UnapplicablePermissionException, ExecutorOutOfDateException, AuthorizationException, AuthenticationException {
        getAuthorizationService().setPermissions(subject, executorsId, permissions, identifiable);
    }

    @Override
    public void setPermissions(Subject subject, List<Long> executorsId, Collection<Permission> permissions, Identifiable identifiable)
            throws UnapplicablePermissionException, ExecutorOutOfDateException, AuthorizationException, AuthenticationException {
        getAuthorizationService().setPermissions(subject, executorsId, permissions, identifiable);
    }

    @Override
    public Collection<Permission> getPermissions(Subject subject, Executor performer, Identifiable identifiable) throws ExecutorOutOfDateException,
            AuthorizationException, AuthenticationException {
        return getAuthorizationService().getPermissions(subject, performer, identifiable);
    }

    @Override
    public Collection<Permission> getOwnPermissions(Subject subject, Executor performer, Identifiable identifiable) throws ExecutorOutOfDateException,
            AuthorizationException, AuthenticationException {
        return getAuthorizationService().getOwnPermissions(subject, performer, identifiable);
    }

    @Override
    public List<Executor> getExecutorsWithPermission(Subject subject, Identifiable identifiable, BatchPresentation batchPresentation,
            boolean hasPermission) throws AuthorizationException, AuthenticationException {
        return getAuthorizationService().getExecutorsWithPermission(subject, identifiable, batchPresentation, hasPermission);
    }

    @Override
    public int getExecutorsWithPermissionCount(Subject subject, Identifiable identifiable, BatchPresentation batchPresentation, boolean hasPermission)
            throws AuthorizationException, AuthenticationException {
        return getAuthorizationService().getExecutorsWithPermissionCount(subject, identifiable, batchPresentation, hasPermission);
    }

    @Override
    public List<Permission> getAllPermissions(Identifiable identifiable) {
        return getAuthorizationService().getAllPermissions(identifiable);
    }

    @Override
    public Permission getNoPermission(Identifiable identifiable) {
        return getAuthorizationService().getNoPermission(identifiable);
    }

    @Override
    public <T extends Object> List<T> getPersistentObjects(Subject subject, BatchPresentation batchPresentation, Class<T> persistentClass,
            Permission permission, Class<? extends Identifiable>[] securedObjectClasses, boolean enablePaging) throws AuthenticationException {
        return getAuthorizationService().getPersistentObjects(subject, batchPresentation, persistentClass, permission, securedObjectClasses,
                enablePaging);
    }

}
