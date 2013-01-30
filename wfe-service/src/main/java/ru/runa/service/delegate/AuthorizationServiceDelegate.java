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
import ru.runa.wfe.user.User;

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
    public boolean isAllowed(User user, Permission permission, Identifiable identifiable) throws AuthorizationException, AuthenticationException {
        return getAuthorizationService().isAllowed(user, permission, identifiable);
    }

    @Override
    public boolean[] isAllowed(User user, Permission permission, List<? extends Identifiable> identifiables) throws AuthenticationException {
        return getAuthorizationService().isAllowed(user, permission, identifiables);
    }

    @Override
    public void setPermissions(User user, Executor performer, Collection<Permission> permissions, Identifiable identifiable)
            throws UnapplicablePermissionException, ExecutorDoesNotExistException, AuthorizationException, AuthenticationException {
        getAuthorizationService().setPermissions(user, performer, permissions, identifiable);
    }

    @Override
    public void setPermissions(User user, List<Long> executorsId, List<Collection<Permission>> permissions, Identifiable identifiable)
            throws UnapplicablePermissionException, ExecutorDoesNotExistException, AuthorizationException, AuthenticationException {
        getAuthorizationService().setPermissions(user, executorsId, permissions, identifiable);
    }

    @Override
    public void setPermissions(User user, List<Long> executorsId, Collection<Permission> permissions, Identifiable identifiable)
            throws UnapplicablePermissionException, ExecutorDoesNotExistException, AuthorizationException, AuthenticationException {
        getAuthorizationService().setPermissions(user, executorsId, permissions, identifiable);
    }

    @Override
    public Collection<Permission> getPermissions(User user, Executor performer, Identifiable identifiable) throws ExecutorDoesNotExistException,
            AuthorizationException, AuthenticationException {
        return getAuthorizationService().getPermissions(user, performer, identifiable);
    }

    @Override
    public Collection<Permission> getOwnPermissions(User user, Executor performer, Identifiable identifiable) throws ExecutorDoesNotExistException,
            AuthorizationException, AuthenticationException {
        return getAuthorizationService().getOwnPermissions(user, performer, identifiable);
    }

    @Override
    public List<Executor> getExecutorsWithPermission(User user, Identifiable identifiable, BatchPresentation batchPresentation, boolean hasPermission)
            throws AuthorizationException, AuthenticationException {
        return getAuthorizationService().getExecutorsWithPermission(user, identifiable, batchPresentation, hasPermission);
    }

    @Override
    public int getExecutorsWithPermissionCount(User user, Identifiable identifiable, BatchPresentation batchPresentation, boolean hasPermission)
            throws AuthorizationException, AuthenticationException {
        return getAuthorizationService().getExecutorsWithPermissionCount(user, identifiable, batchPresentation, hasPermission);
    }

    @Override
    public <T extends Object> List<T> getPersistentObjects(User user, BatchPresentation batchPresentation, Class<T> persistentClass,
            Permission permission, SecuredObjectType[] securedObjectClasses, boolean enablePaging) throws AuthenticationException {
        return getAuthorizationService().getPersistentObjects(user, batchPresentation, persistentClass, permission, securedObjectClasses,
                enablePaging);
    }

}
