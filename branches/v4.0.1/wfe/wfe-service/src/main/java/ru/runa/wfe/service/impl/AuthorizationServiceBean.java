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
package ru.runa.wfe.service.impl;

import java.util.Collection;
import java.util.List;

import javax.ejb.Stateless;
import javax.ejb.TransactionManagement;
import javax.ejb.TransactionManagementType;
import javax.interceptor.Interceptors;
import javax.jws.WebMethod;
import javax.jws.WebService;
import javax.jws.soap.SOAPBinding;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.ejb.interceptor.SpringBeanAutowiringInterceptor;

import ru.runa.wfe.presentation.BatchPresentation;
import ru.runa.wfe.presentation.BatchPresentationFactory;
import ru.runa.wfe.security.Identifiable;
import ru.runa.wfe.security.Permission;
import ru.runa.wfe.security.SecuredObjectType;
import ru.runa.wfe.security.logic.AuthorizationLogic;
import ru.runa.wfe.service.decl.AuthorizationServiceLocal;
import ru.runa.wfe.service.decl.AuthorizationServiceRemote;
import ru.runa.wfe.service.interceptors.EjbExceptionSupport;
import ru.runa.wfe.service.interceptors.EjbTransactionSupport;
import ru.runa.wfe.user.Executor;
import ru.runa.wfe.user.User;

import com.google.common.base.Preconditions;

/**
 * Implements AuthorizationService as bean. Created on 20.07.2004
 */
@Stateless
@TransactionManagement(TransactionManagementType.BEAN)
@Interceptors({ EjbExceptionSupport.class, EjbTransactionSupport.class, SpringBeanAutowiringInterceptor.class })
@WebService(name = "AuthorizationAPI", serviceName = "AuthorizationWebService")
@SOAPBinding
public class AuthorizationServiceBean implements AuthorizationServiceLocal, AuthorizationServiceRemote {
    @Autowired
    private AuthorizationLogic authorizationLogic;

    @Override
    public boolean isAllowed(User user, Permission permission, Identifiable identifiable) {
        Preconditions.checkNotNull(user);
        Preconditions.checkNotNull(permission);
        Preconditions.checkNotNull(identifiable);
        return authorizationLogic.isAllowed(user, permission, identifiable);
    }

    @WebMethod(exclude = true)
    @Override
    public boolean[] isAllowed(User user, Permission permission, List<? extends Identifiable> identifiables) {
        Preconditions.checkNotNull(user);
        Preconditions.checkNotNull(permission);
        Preconditions.checkNotNull(identifiables);
        return authorizationLogic.isAllowed(user, permission, identifiables);
    }

    @Override
    public List<Permission> getIssuedPermissions(User user, Executor performer, Identifiable identifiable) {
        Preconditions.checkNotNull(user);
        Preconditions.checkNotNull(performer);
        Preconditions.checkNotNull(identifiable);
        return authorizationLogic.getIssuedPermissions(user, performer, identifiable);
    }

    @WebMethod(exclude = true)
    @Override
    public void setPermissions(User user, List<Long> executorIds, List<Collection<Permission>> permissions, Identifiable identifiable) {
        Preconditions.checkNotNull(user);
        Preconditions.checkNotNull(executorIds);
        Preconditions.checkNotNull(permissions);
        Preconditions.checkNotNull(identifiable);
        authorizationLogic.setPermissions(user, executorIds, permissions, identifiable);
    }

    @Override
    public void setPermissions(User user, Long executorId, Collection<Permission> permissions, Identifiable identifiable) {
        Preconditions.checkNotNull(user);
        Preconditions.checkNotNull(executorId);
        Preconditions.checkNotNull(permissions);
        Preconditions.checkNotNull(identifiable);
        authorizationLogic.setPermissions(user, executorId, permissions, identifiable);
    }

    @WebMethod(exclude = true)
    @Override
    public void setPermissions(User user, List<Long> executorsId, Collection<Permission> permissions, Identifiable identifiable) {
        Preconditions.checkNotNull(user);
        Preconditions.checkNotNull(executorsId);
        Preconditions.checkNotNull(permissions);
        Preconditions.checkNotNull(identifiable);
        authorizationLogic.setPermissions(user, executorsId, permissions, identifiable);
    }

    @Override
    public List<Executor> getExecutorsWithPermission(User user, Identifiable identifiable, BatchPresentation batchPresentation, boolean withPermission) {
        Preconditions.checkNotNull(user);
        Preconditions.checkNotNull(identifiable);
        if (batchPresentation == null) {
            batchPresentation = BatchPresentationFactory.EXECUTORS.createDefault();
        }
        return (List<Executor>) authorizationLogic.getExecutorsWithPermission(user, identifiable, batchPresentation, withPermission);
    }

    @Override
    public int getExecutorsWithPermissionCount(User user, Identifiable identifiable, BatchPresentation batchPresentation, boolean withPermission) {
        Preconditions.checkNotNull(user);
        Preconditions.checkNotNull(identifiable);
        if (batchPresentation == null) {
            batchPresentation = BatchPresentationFactory.EXECUTORS.createDefault();
        }
        return authorizationLogic.getExecutorsWithPermissionCount(user, identifiable, batchPresentation, withPermission);
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T extends Object> List<T> getPersistentObjects(User user, BatchPresentation batchPresentation, Class<T> persistentClass,
            Permission permission, SecuredObjectType[] securedObjectTypes, boolean enablePaging) {
        Preconditions.checkNotNull(user, "User");
        Preconditions.checkNotNull(batchPresentation, "Batch presentation");
        Preconditions.checkNotNull(persistentClass, "Persistence class");
        Preconditions.checkNotNull(permission, "Permission");
        Preconditions.checkNotNull(securedObjectTypes, "Secured object class");
        return (List<T>) authorizationLogic.getPersistentObjects(user, batchPresentation, permission, securedObjectTypes, enablePaging);
    }

}
