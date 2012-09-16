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
package ru.runa.af.service.impl.ejb;

import java.util.Collection;
import java.util.List;

import javax.ejb.Stateless;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import javax.interceptor.Interceptors;
import javax.security.auth.Subject;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.ejb.interceptor.SpringBeanAutowiringInterceptor;

import ru.runa.af.ArgumentsCommons;
import ru.runa.af.AuthenticationException;
import ru.runa.af.AuthorizationException;
import ru.runa.af.Executor;
import ru.runa.af.ExecutorOutOfDateException;
import ru.runa.af.Identifiable;
import ru.runa.af.Permission;
import ru.runa.af.UnapplicablePermissionException;
import ru.runa.af.logic.AuthorizationLogic;
import ru.runa.af.presentation.BatchPresentation;
import ru.runa.af.service.AuthorizationServiceLocal;
import ru.runa.af.service.AuthorizationServiceRemote;

/**
 * Implements AuthorizationService as bean. Created on 20.07.2004
 */
@Stateless
@TransactionAttribute(TransactionAttributeType.REQUIRED)
@Interceptors({SpringBeanAutowiringInterceptor.class, LoggerInterceptor.class})
public class AuthorizationServiceBean implements AuthorizationServiceLocal, AuthorizationServiceRemote {
    @Autowired
    private AuthorizationLogic authorizationLogic;

    @Override
    public boolean isAllowed(Subject subject, Permission permission, Identifiable identifiable) throws AuthorizationException,
            AuthenticationException {
        ArgumentsCommons.checkNotNull(subject);
        ArgumentsCommons.checkNotNull(permission);
        ArgumentsCommons.checkNotNull(identifiable);
        return authorizationLogic.isAllowed(subject, permission, identifiable);
    }

    @Override
    public boolean[] isAllowed(Subject subject, Permission permission, List<? extends Identifiable> identifiables) throws AuthenticationException {
        ArgumentsCommons.checkNotNull(subject);
        ArgumentsCommons.checkNotNull(permission);
        ArgumentsCommons.checkNotNull(identifiables);
        return authorizationLogic.isAllowed(subject, permission, identifiables);
    }

    @Override
    public Collection<Permission> getPermissions(Subject subject, Executor performer, Identifiable identifiable) throws ExecutorOutOfDateException,
            AuthorizationException, AuthenticationException {
        ArgumentsCommons.checkNotNull(subject);
        ArgumentsCommons.checkNotNull(performer);
        ArgumentsCommons.checkNotNull(identifiable);
        return authorizationLogic.getPermissions(subject, performer, identifiable);
    }

    @Override
    public Collection<Permission> getOwnPermissions(Subject subject, Executor performer, Identifiable identifiable) throws ExecutorOutOfDateException,
            AuthorizationException, AuthenticationException {
        ArgumentsCommons.checkNotNull(subject);
        ArgumentsCommons.checkNotNull(performer);
        ArgumentsCommons.checkNotNull(identifiable);
        return authorizationLogic.getOwnPermissions(subject, performer, identifiable);
    }

    @Override
    public void setPermissions(Subject subject, List<Long> executorIds, List<Collection<Permission>> permissions, Identifiable identifiable)
            throws UnapplicablePermissionException, ExecutorOutOfDateException, AuthorizationException, AuthenticationException {
        ArgumentsCommons.checkNotNull(subject);
        ArgumentsCommons.checkNotNull(executorIds);
        ArgumentsCommons.checkNotNull(permissions);
        ArgumentsCommons.checkNotNull(identifiable);
        authorizationLogic.setPermissions(subject, executorIds, permissions, identifiable);
    }

    @Override
    public void setPermissions(Subject subject, Executor performer, Collection<Permission> permissions, Identifiable identifiable)
            throws AuthorizationException, AuthenticationException, ExecutorOutOfDateException, UnapplicablePermissionException {
        ArgumentsCommons.checkNotNull(subject);
        ArgumentsCommons.checkNotNull(performer);
        ArgumentsCommons.checkNotNull(permissions);
        ArgumentsCommons.checkNotNull(identifiable);
        authorizationLogic.setPermissions(subject, performer, permissions, identifiable);
    }

    @Override
    public void setPermissions(Subject subject, List<Long> executorsId, Collection<Permission> permissions, Identifiable identifiable)
            throws AuthorizationException, AuthenticationException, ExecutorOutOfDateException, UnapplicablePermissionException {
        ArgumentsCommons.checkNotNull(subject);
        ArgumentsCommons.checkNotNull(executorsId);
        ArgumentsCommons.checkNotNull(permissions);
        ArgumentsCommons.checkNotNull(identifiable);
        authorizationLogic.setPermissions(subject, executorsId, permissions, identifiable);
    }

    @Override
    public List<Executor> getExecutorsWithPermission(Subject subject, Identifiable identifiable, BatchPresentation batchPresentation,
            boolean withPermission) throws AuthorizationException, AuthenticationException {
        ArgumentsCommons.checkNotNull(subject);
        ArgumentsCommons.checkNotNull(identifiable);
        ArgumentsCommons.checkNotNull(batchPresentation);
        return authorizationLogic.getExecutorsWithPermission(subject, identifiable, batchPresentation, withPermission);
    }

    @Override
    public int getExecutorsWithPermissionCount(Subject subject, Identifiable identifiable, BatchPresentation batchPresentation, boolean withPermission)
            throws AuthorizationException, AuthenticationException {
        ArgumentsCommons.checkNotNull(subject);
        ArgumentsCommons.checkNotNull(identifiable);
        ArgumentsCommons.checkNotNull(batchPresentation);
        return authorizationLogic.getExecutorsWithPermissionCount(subject, identifiable, batchPresentation, withPermission);
    }

    @Override
    public List<Permission> getAllPermissions(Identifiable identifiable) {
        ArgumentsCommons.checkNotNull(identifiable);
        return authorizationLogic.getAllPermissions(identifiable);
    }

    @Override
    public Permission getNoPermission(Identifiable identifiable) {
        ArgumentsCommons.checkNotNull(identifiable);
        return authorizationLogic.getNoPermission(identifiable);
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T extends Object> List<T> getPersistentObjects(Subject subject, BatchPresentation batchPresentation, Class<T> persistentClass,
            Permission permission, Class<? extends Identifiable>[] securedObjectClasses, boolean enablePaging) throws AuthenticationException {
        ArgumentsCommons.checkNotNull(subject);
        ArgumentsCommons.checkNotNull(batchPresentation);
        ArgumentsCommons.checkNotNull(persistentClass, "Persistence class");
        ArgumentsCommons.checkNotNull(permission);
        ArgumentsCommons.checkNotNull(securedObjectClasses, "Secured object class");
        return (List<T>) authorizationLogic.getPersistentObjects(subject, batchPresentation, permission, securedObjectClasses, enablePaging);
    }

}
