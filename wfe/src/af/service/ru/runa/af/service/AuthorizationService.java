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
package ru.runa.af.service;

import java.util.Collection;
import java.util.List;

import javax.security.auth.Subject;

import ru.runa.af.Actor;
import ru.runa.af.AuthenticationException;
import ru.runa.af.AuthorizationException;
import ru.runa.af.Executor;
import ru.runa.af.ExecutorOutOfDateException;
import ru.runa.af.Identifiable;
import ru.runa.af.Permission;
import ru.runa.af.UnapplicablePermissionException;
import ru.runa.af.presentation.BatchPresentation;

/**
 * Provides methods for authorization mechanism.
 * <p>
 * Created on 20.07.2004
 * </p>
 */
public interface AuthorizationService {

    /**
     * Checks whether {@link Actor}from subject has permission on {@link Identifiable}.
     */
    public boolean isAllowed(Subject subject, Permission permission, Identifiable identifiable) throws AuthorizationException,
            AuthenticationException;

    public boolean[] isAllowed(Subject subject, Permission permission, List<? extends Identifiable> identifiables) throws AuthenticationException;

    /**
     * Sets permissions for performer on {@link Identifiable}.
     */
    public void setPermissions(Subject subject, Executor performer, Collection<Permission> permissions, Identifiable identifiable)
            throws AuthorizationException, AuthenticationException, ExecutorOutOfDateException, UnapplicablePermissionException;

    public void setPermissions(Subject subject, List<Long> executorsId, List<Collection<Permission>> permissions, Identifiable identifiable)
            throws AuthorizationException, AuthenticationException, ExecutorOutOfDateException, UnapplicablePermissionException;

    public void setPermissions(Subject subject, List<Long> executorsId, Collection<Permission> permissions, Identifiable identifiable)
            throws AuthorizationException, AuthenticationException, ExecutorOutOfDateException, UnapplicablePermissionException;

    /**
     * Returns an array of Permission that executor has on {@link Identifiable}.
     */
    public Collection<Permission> getPermissions(Subject subject, Executor performer, Identifiable identifiable) throws AuthorizationException,
            AuthenticationException, ExecutorOutOfDateException;

    /**
     * Returns an array of Permission that executor himself has on {@link Identifiable}.
     */
    public Collection<Permission> getOwnPermissions(Subject subject, Executor performer, Identifiable identifiable) throws AuthorizationException,
            AuthenticationException, ExecutorOutOfDateException;

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
            boolean hasPermission) throws AuthenticationException, AuthorizationException;

    /**
     * Load executor's count which already has (or not has) some permission on specified identifiable.
     * @param subject Current actor {@linkplain Subject}.
     * @param identifiable {@linkplain Identifiable} to load executors, which has (or not) permission on this identifiable.
     * @param batchPresentation {@linkplain BatchPresentation} for loading executors.
     * @param hasPermission Flag equals true to load executors with permissions on {@linkplain Identifiable}; false to load executors without permissions. 
     * @return Count of executors with or without permission on {@linkplain Identifiable}.
     */
    public int getExecutorsWithPermissionCount(Subject subject, Identifiable identifiable, BatchPresentation batchPresentation, boolean hasPermission)
            throws AuthenticationException, AuthorizationException;

    public List<Permission> getAllPermissions(Identifiable identifiable);

    /**
     * @return NO_PERMISSION which corresponds to given identifiable
     */
    public Permission getNoPermission(Identifiable identifiable);

    public <T extends Object> List<T> getPersistentObjects(Subject subject, BatchPresentation batchPresentation, Class<T> persistentClass,
            Permission permission, Class<? extends Identifiable>[] securedObjectClasses, boolean enablePaging) throws AuthenticationException;

}
