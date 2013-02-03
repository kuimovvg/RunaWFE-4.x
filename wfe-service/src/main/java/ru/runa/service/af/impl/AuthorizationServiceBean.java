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
package ru.runa.service.af.impl;

import java.util.Collection;
import java.util.List;
import java.util.Map;

import javax.ejb.Stateless;
import javax.ejb.TransactionManagement;
import javax.ejb.TransactionManagementType;
import javax.interceptor.Interceptors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.ejb.interceptor.SpringBeanAutowiringInterceptor;

import ru.runa.service.af.AuthorizationServiceLocal;
import ru.runa.service.af.AuthorizationServiceRemote;
import ru.runa.service.interceptors.EjbExceptionSupport;
import ru.runa.service.interceptors.EjbTransactionSupport;
import ru.runa.wfe.presentation.BatchPresentation;
import ru.runa.wfe.security.AuthenticationException;
import ru.runa.wfe.security.AuthorizationException;
import ru.runa.wfe.security.Identifiable;
import ru.runa.wfe.security.Permission;
import ru.runa.wfe.security.SecuredObjectType;
import ru.runa.wfe.security.UnapplicablePermissionException;
import ru.runa.wfe.security.logic.AuthorizationLogic;
import ru.runa.wfe.user.Executor;
import ru.runa.wfe.user.ExecutorDoesNotExistException;
import ru.runa.wfe.user.User;

import com.google.common.base.Preconditions;

/**
 * Implements AuthorizationService as bean. Created on 20.07.2004
 */
@Stateless
@TransactionManagement(TransactionManagementType.BEAN)
@Interceptors({ EjbExceptionSupport.class, EjbTransactionSupport.class, SpringBeanAutowiringInterceptor.class })
public class AuthorizationServiceBean implements AuthorizationServiceLocal, AuthorizationServiceRemote {
    @Autowired
    private AuthorizationLogic authorizationLogic;

    @Override
    public boolean isAllowed(User user, Permission permission, Identifiable identifiable) throws AuthorizationException, AuthenticationException {
        Preconditions.checkNotNull(user);
        Preconditions.checkNotNull(permission);
        Preconditions.checkNotNull(identifiable);
        return authorizationLogic.isAllowed(user, permission, identifiable);
    }

    @Override
    public boolean[] isAllowed(User user, Permission permission, List<? extends Identifiable> identifiables) throws AuthenticationException {
        Preconditions.checkNotNull(user);
        Preconditions.checkNotNull(permission);
        Preconditions.checkNotNull(identifiables);
        return authorizationLogic.isAllowed(user, permission, identifiables);
    }

    @Override
    public Map<Permission, Boolean> getOwnPermissions(User user, Executor performer, Identifiable identifiable) throws ExecutorDoesNotExistException,
            AuthorizationException, AuthenticationException {
        Preconditions.checkNotNull(user);
        Preconditions.checkNotNull(performer);
        Preconditions.checkNotNull(identifiable);
        return authorizationLogic.getOwnPermissions(user, performer, identifiable);
    }

    @Override
    public void setPermissions(User user, List<Long> executorIds, List<Collection<Permission>> permissions, Identifiable identifiable)
            throws UnapplicablePermissionException, ExecutorDoesNotExistException, AuthorizationException, AuthenticationException {
        Preconditions.checkNotNull(user);
        Preconditions.checkNotNull(executorIds);
        Preconditions.checkNotNull(permissions);
        Preconditions.checkNotNull(identifiable);
        authorizationLogic.setPermissions(user, executorIds, permissions, identifiable);
    }

    @Override
    public void setPermissions(User user, Executor performer, Collection<Permission> permissions, Identifiable identifiable)
            throws AuthorizationException, AuthenticationException, ExecutorDoesNotExistException, UnapplicablePermissionException {
        Preconditions.checkNotNull(user);
        Preconditions.checkNotNull(performer);
        Preconditions.checkNotNull(permissions);
        Preconditions.checkNotNull(identifiable);
        authorizationLogic.setPermissions(user, performer, permissions, identifiable);
    }

    @Override
    public void setPermissions(User user, List<Long> executorsId, Collection<Permission> permissions, Identifiable identifiable)
            throws AuthorizationException, AuthenticationException, ExecutorDoesNotExistException, UnapplicablePermissionException {
        Preconditions.checkNotNull(user);
        Preconditions.checkNotNull(executorsId);
        Preconditions.checkNotNull(permissions);
        Preconditions.checkNotNull(identifiable);
        authorizationLogic.setPermissions(user, executorsId, permissions, identifiable);
    }

    @Override
    public List<Executor> getExecutorsWithPermission(User user, Identifiable identifiable, BatchPresentation batchPresentation, boolean withPermission)
            throws AuthorizationException, AuthenticationException {
        Preconditions.checkNotNull(user);
        Preconditions.checkNotNull(identifiable);
        Preconditions.checkNotNull(batchPresentation);
        return authorizationLogic.getExecutorsWithPermission(user, identifiable, batchPresentation, withPermission);
    }

    @Override
    public int getExecutorsWithPermissionCount(User user, Identifiable identifiable, BatchPresentation batchPresentation, boolean withPermission)
            throws AuthorizationException, AuthenticationException {
        Preconditions.checkNotNull(user);
        Preconditions.checkNotNull(identifiable);
        Preconditions.checkNotNull(batchPresentation);
        return authorizationLogic.getExecutorsWithPermissionCount(user, identifiable, batchPresentation, withPermission);
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T extends Object> List<T> getPersistentObjects(User user, BatchPresentation batchPresentation, Class<T> persistentClass,
            Permission permission, SecuredObjectType[] securedObjectTypes, boolean enablePaging) throws AuthenticationException {
        Preconditions.checkNotNull(user);
        Preconditions.checkNotNull(batchPresentation);
        Preconditions.checkNotNull(persistentClass, "Persistence class");
        Preconditions.checkNotNull(permission);
        Preconditions.checkNotNull(securedObjectTypes, "Secured object class");
        return (List<T>) authorizationLogic.getPersistentObjects(user, batchPresentation, permission, securedObjectTypes, enablePaging);
    }

}
