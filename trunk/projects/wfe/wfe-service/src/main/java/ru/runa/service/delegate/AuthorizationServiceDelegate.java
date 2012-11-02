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
package ru.runa.service.delegate;

import java.util.Collection;
import java.util.List;

import javax.security.auth.Subject;

import ru.runa.service.af.AuthorizationService;
import ru.runa.wfe.presentation.BatchPresentation;
import ru.runa.wfe.security.AuthenticationException;
import ru.runa.wfe.security.AuthorizationException;
import ru.runa.wfe.security.Identifiable;
import ru.runa.wfe.security.Permission;
import ru.runa.wfe.security.SecuredObjectType;
import ru.runa.wfe.security.UnapplicablePermissionException;
import ru.runa.wfe.user.Executor;
import ru.runa.wfe.user.ExecutorDoesNotExistException;

/**
 * 
 * Created 14.10.2005
 */
public class AuthorizationServiceDelegate extends EJB3Delegate implements AuthorizationService {

    public AuthorizationServiceDelegate() {
        super(AuthorizationService.class);
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
            throws UnapplicablePermissionException, ExecutorDoesNotExistException, AuthorizationException, AuthenticationException {
        getAuthorizationService().setPermissions(subject, performer, permissions, identifiable);
    }

    @Override
    public void setPermissions(Subject subject, List<Long> executorsId, List<Collection<Permission>> permissions, Identifiable identifiable)
            throws UnapplicablePermissionException, ExecutorDoesNotExistException, AuthorizationException, AuthenticationException {
        getAuthorizationService().setPermissions(subject, executorsId, permissions, identifiable);
    }

    @Override
    public void setPermissions(Subject subject, List<Long> executorsId, Collection<Permission> permissions, Identifiable identifiable)
            throws UnapplicablePermissionException, ExecutorDoesNotExistException, AuthorizationException, AuthenticationException {
        getAuthorizationService().setPermissions(subject, executorsId, permissions, identifiable);
    }

    @Override
    public Collection<Permission> getPermissions(Subject subject, Executor performer, Identifiable identifiable)
            throws ExecutorDoesNotExistException, AuthorizationException, AuthenticationException {
        return getAuthorizationService().getPermissions(subject, performer, identifiable);
    }

    @Override
    public Collection<Permission> getOwnPermissions(Subject subject, Executor performer, Identifiable identifiable)
            throws ExecutorDoesNotExistException, AuthorizationException, AuthenticationException {
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
    public <T extends Object> List<T> getPersistentObjects(Subject subject, BatchPresentation batchPresentation, Class<T> persistentClass,
            Permission permission, SecuredObjectType[] securedObjectClasses, boolean enablePaging) throws AuthenticationException {
        return getAuthorizationService().getPersistentObjects(subject, batchPresentation, persistentClass, permission, securedObjectClasses,
                enablePaging);
    }

}
