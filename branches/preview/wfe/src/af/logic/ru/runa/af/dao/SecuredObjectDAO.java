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
package ru.runa.af.dao;

import java.util.List;
import java.util.Set;

import ru.runa.af.ASystem;
import ru.runa.af.Actor;
import ru.runa.af.Executor;
import ru.runa.af.Group;
import ru.runa.af.Identifiable;
import ru.runa.af.Permission;
import ru.runa.af.SecuredObject;
import ru.runa.af.SecuredObjectAlreadyExistsException;
import ru.runa.af.SecuredObjectOutOfDateException;
import ru.runa.af.dao.impl.PermissionMapping;
import ru.runa.af.presentation.BatchPresentation;

/**
 * DAO for managing {@link SecuredObject}s.
 */
public interface SecuredObjectDAO {

    /**
     * Check is {@linkplain SecuredObject}} for specified {@linkplain Identifiable} object exist in database.
     * @param identifiable Object to check is {@linkplain SecuredObject} exist.
     * @return Return true, if {@linkplain SecuredObject} for {@linkplain Identifiable} exist and false otherwise.
     */
    public boolean isExist(Identifiable identifiable);

    /**
     * Create {@linkplain SecuredObject} for {@linkplain Identifiable}. Throws exception if {@linkplain SecuredObject} already exists.
     * @param identifiable Object to create {@linkplain SecuredObject}.
     * @return Created {@linkplain SecuredObject}.
     */
    public SecuredObject create(Identifiable identifiable) throws SecuredObjectAlreadyExistsException;

    /**
     * Load {@linkplain SecuredObject} for {@linkplain Identifiable}. Throws exception, if no {@linkplain SecuredObject} found.
     * @param identifiable Object to load {@linkplain SecuredObject}.
     * @return {@linkplain SecuredObject} for {@linkplain Identifiable}.
     */
    public SecuredObject get(Identifiable identifiable) throws SecuredObjectOutOfDateException;

    /**
     * Load {@linkplain SecuredObject}'s for {@linkplain Identifiable}'s. Throws exception, if no {@linkplain SecuredObject} found for one of {@linkplain Identifiable}'s.
     * Result {@linkplain SecuredObject}'s list in the same order, as parameter {@linkplain Identifiable}'s. 
     * @param identifiables Objects to load {@linkplain SecuredObject}'s.
     * @return {@linkplain SecuredObject}'s for {@linkplain Identifiable}'s.
     */
    public List<SecuredObject> get(List<? extends Identifiable> identifiables) throws SecuredObjectOutOfDateException;

    /**
     * Removes {@linkplain SecuredObject} for {@linkplain Identifiable} and all {@linkplain PermissionMapping}'s, associated with it.
     * @param identifiable Object, which {@linkplain SecuredObject} must be removed.
     */
    public void remove(Identifiable identifiable) throws SecuredObjectOutOfDateException;

    /**
     * Create {@linkplain Permission} instance for object given type without any permission set.
     * Throws exception, if {@linkplain Permission} can't be created. 
     * @param type Object type to create {@linkplain Permission} instance. 
     * @return {@linkplain Permission} instance without any permission set.
     */
    public Permission getNoPermission(int type);
    
    /**
     * Returns {@linkplain Permission} without any permission set for given {@linkplain SecuredObject} type.
     * @param identifiable {@linkplain Identifiable} for which you want to get permission object.
     */
    public Permission getNoPermission(Identifiable identifiable);

    /**
     * Return array of privileged {@linkplain Executor}s for given (@linkplain SecuredObject) type 
     * (i.e. executors whose permissions on SecuredObject type can not be changed).
     * @param identifiable {@linkplain Identifiable} for which you want to get privileged executors.
     * @return Privileged {@linkplain Executor}'s array.
     */
    public Set<Executor> getPrivilegedExecutors(Identifiable identifiable);

    /**
     * Return array of all privileged {@linkplain Executor}s for all (@linkplain SecuredObject) type 
     * (i.e. executors whose permissions on any SecuredObject type can not be changed).
     * @return Privileged {@linkplain Executor}'s array.
     */
    public Set<Executor> getPrivilegedExecutors();

    /**
     * Load {@linkplain Executor}'s, which have permission on {@linkplain Identifiable}.
     * <br/><b>Paging is not enabled.</b>
     * @param identifiable {@linkplain Identifiable} to load {@linkplain Executor}'s.
     * @param batchPresentation {@linkplain BatchPresentation} with parameters to load executors. 
     * @return List of {@linkplain Executor}'s with permission on {@linkplain Identifiable}. 
     */
    public List<Executor> getExecutorsWithPermission(Identifiable identifiable, BatchPresentation batchPresentation)
            throws SecuredObjectOutOfDateException;

    /**
     * Load {@linkplain Executor}'s, which have not permission on {@linkplain Identifiable}.
     * <br/><b>Paging is not enabled.</b>
     * @param identifiable {@linkplain Identifiable} to load {@linkplain Executor}'s.
     * @param batchPresentation {@linkplain BatchPresentation} with parameters to load executors. 
     * @return List of {@linkplain Executor}'s with permission on {@linkplain Identifiable}. 
     */
    public List<Executor> getExecutorsWithoutPermission(Identifiable identifiable, BatchPresentation batchPresentation)
            throws SecuredObjectOutOfDateException;

    /**
     * Check if executor is privileged executor for given identifiable. Throw exception, if no {@linkplain SecuredObject} 
     * for {@linkplain Identifiable} found.
     * @param executor {@linkplain Executor}, to check if privileged.
     * @param identifiable {@linkplain Identifiable} object, to check if executor is privileged to it.
     * @return true if executor is privileged for given identifiable and false otherwise.
     */
    public boolean isPrivilegedExecutor(Executor executor, Identifiable identifiable);

    /**
     * Check if executor is privileged executor for given secured object.
     * @param executor {@linkplain Executor}, to check if privileged.
     * @param securedObject {@linkplain SecuredObject} object, to check if executor is privileged to it.
     * @return true if executor is privileged for given {@linkplain SecuredObject} and false otherwise.
     */
    public boolean isPrivilegedExecutor(Executor executor, SecuredObject securedObject);

    /**
     * Adds new record in <i>dictionary</i> tables describing new SecuredObject type.
     * @param targetClass Associated with SecuredObject class which implements {@link Identifiable}(e.g. {@link Actor},{@link Group},{@link ASystem}).
     * @param privelegedExecutors Privileged executors for target class.
     * @param permission Permission class, describes allowed permissions for target class.
     */
    public void addType(Class<? extends Identifiable> targetClass, List<? extends Executor> privelegedExecutors, Class<? extends Permission> permission);

    /**
     * Return type id for specified identifiable class.
     * @param identifiableClass Identifiable class to get type id.
     * @return Type id for specified identifiable class.
     */
    public int getType(Class<? extends Identifiable> identifiableClass);

    /**
     * Load list of {@linkplain Identifiable} for which executors have permission on. 
     * @param executorIds Executors identities, which must have permission on loaded {@linkplain Identifiable} (at least one).
     * @param batchPresentation {@linkplain BatchPresentation} with parameters for loading {@linkplain Identifiable}'s.
     * @param permission {@linkplain Permission}, which executors must has on {@linkplain Identifiable}.
     * @param securedObjectTypes {@linkplain SecuredObject} types, used to check permissions.
     * @param enablePaging Flag, equals true, if paging must be enabled and false otherwise.
     * @return List of {@link Identifiable}'s for which executors have permission on.
     */
    public List<? extends Identifiable> getPersistentObjects(List<Long> executorIds, BatchPresentation batchPresentation, Permission permission,
            int[] securedObjectTypes, boolean enablePaging);

    /**
     * Load count of {@linkplain Identifiable} for which executors have permission on. 
     * @param executorIds Executors identities, which must have permission on loaded {@linkplain Identifiable} (at least one).
     * @param batchPresentation {@linkplain BatchPresentation} with parameters for loading {@linkplain Identifiable}'s.
     * @param permission {@linkplain Permission}, which executors must has on {@linkplain Identifiable}.
     * @param securedObjectTypes {@linkplain SecuredObject} types, used to check permissions.
     * @param enablePaging Flag, equals true, if paging must be enabled and false otherwise.
     * @return Count of {@link Identifiable}'s for which executors have permission on.
     */
    public int getPersistentObjectCount(List<Long> executorIds, BatchPresentation batchPresentation, Permission permission, int[] securedObjectTypes);
}
