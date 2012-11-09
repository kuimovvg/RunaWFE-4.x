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
package ru.runa.wfe.security.dao;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.hibernate.HibernateException;
import org.hibernate.Query;
import org.hibernate.Session;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.orm.hibernate3.HibernateCallback;

import ru.runa.wfe.commons.PagingCommons;
import ru.runa.wfe.commons.dao.CommonDAO;
import ru.runa.wfe.presentation.BatchPresentation;
import ru.runa.wfe.presentation.hibernate.BatchPresentationHibernateCompiler;
import ru.runa.wfe.security.Identifiable;
import ru.runa.wfe.security.Permission;
import ru.runa.wfe.security.PermissionNotFoundException;
import ru.runa.wfe.security.SecuredObjectType;
import ru.runa.wfe.security.UnapplicablePermissionException;
import ru.runa.wfe.user.Executor;
import ru.runa.wfe.user.dao.ExecutorDAO;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

/**
 * Permission DAO level implementation via Hibernate.
 * 
 * @author Konstantinov Aleksey 19.02.2012
 */
@SuppressWarnings("unchecked")
public class PermissionDAO extends CommonDAO {
    @Autowired
    private ExecutorDAO executorDAO;

    /**
     * Returns an array of Permission that executor has on identifiable. Returns
     * as own permissions on identifiable as inherited group(s) permissions on
     * identifiable.
     * 
     * @param executor
     *            Executor for loading permissions.
     * @param identifiable
     *            Identifiable for loading permissions.
     * @return Array of {@linkplain Permission} on secured object for Executor.
     */
    public List<Permission> getPermissions(final Executor executor, final Identifiable identifiable) {
        final Set<Executor> executorWithGroups = getExecutorWithAllHisGroups(executor);
        for (Executor executor2 : executorWithGroups) {
            if (getPrivilegedExecutors(identifiable).contains(executor2)) {
                return identifiable.getSecuredObjectType().getAllPermissions();
            }
        }
        List<PermissionMapping> list = getHibernateTemplate().executeFind(new HibernateCallback<List<PermissionMapping>>() {

            @Override
            public List<PermissionMapping> doInHibernate(Session session) throws HibernateException, SQLException {
                Query query = session
                        .createQuery("from PermissionMapping where identifiableId=:identifiableId and type=:type and executor in (:executors)");
                query.setParameter("identifiableId", identifiable.getId());
                query.setParameter("type", identifiable.getSecuredObjectType());
                query.setParameterList("executors", executorWithGroups);
                return query.list();
            }
        });
        return getPermissions(list, identifiable.getSecuredObjectType().getNoPermission());
    }

    /**
     * Returns an array of Permission that executor itself has on identifiable.
     * Inherited permissions are not returned.
     * 
     * @param executor
     *            Executor for loading permissions.
     * @param identifiable
     *            Identifiable for loading permissions.
     * @return Array of {@linkplain Permission} on secured object for Executor.
     */
    public List<Permission> getOwnPermissions(Executor executor, Identifiable identifiable) {
        if (getPrivilegedExecutors(identifiable).contains(executor)) {
            return identifiable.getSecuredObjectType().getAllPermissions();
        }
        Set<PermissionMapping> permissionMappingList = getOwnPermissionMappings(executor, identifiable);
        return getPermissions(permissionMappingList, identifiable.getSecuredObjectType().getNoPermission());
    }

    /**
     * Sets permissions for executor on identifiable.
     * 
     * @param executor
     *            Executor, which got permissions.
     * @param permissions
     *            Permissions for executor.
     * @param identifiable
     *            Secured object to set permission on.
     */
    public void setPermissions(Executor executor, Collection<Permission> permissions, Identifiable identifiable) {
        checkArePermissionAllowed(identifiable, permissions);
        setPermissionsInternal(executor, permissions, identifiable);
    }

    /**
     * Checks whether executor has permission on identifiable.
     * 
     * @param executor
     *            Executor, which permission must be check.
     * @param permission
     *            Checking permission.
     * @param identifiable
     *            Secured object to check permission on.
     * @return true if executor has requested permission on secuedObject; false
     *         otherwise.
     */
    public boolean isAllowed(final Executor executor, final Permission permission, final Identifiable identifiable) {
        final Set<Executor> executorWithGroups = getExecutorWithAllHisGroups(executor);
        for (Executor executor2 : executorWithGroups) {
            if (getPrivilegedExecutors(identifiable).contains(executor2)) {
                return true;
            }
        }
        return !getHibernateTemplate().executeFind(new HibernateCallback<List<PermissionMapping>>() {

            @Override
            public List<PermissionMapping> doInHibernate(Session session) throws HibernateException, SQLException {
                Query query = session.createQuery("from PermissionMapping where identifiableId=? and type=? and mask=? and executor in (:executors)");
                query.setParameter(0, identifiable.getId());
                query.setParameter(1, identifiable.getSecuredObjectType());
                query.setParameter(2, permission.getMask());
                query.setParameterList("executors", executorWithGroups);
                return query.list();
            }
        }).isEmpty();
    }

    /**
     * Checks whether executor has permission on identifiable's. Create result
     * array in same order, as identifiable's.
     * 
     * @param executor
     *            Executor, which permission must be check.
     * @param permission
     *            Checking permission.
     * @param identifiable
     *            Secured objects to check permission on.
     * @return Array of: true if executor has requested permission on
     *         secuedObject; false otherwise.
     */
    public boolean[] isAllowed(final Executor executor, final Permission permission, final List<? extends Identifiable> identifiables) {
        if (identifiables.size() == 0) {
            return new boolean[0];
        }
        final Set<Executor> executorWithGroups = getExecutorWithAllHisGroups(executor);
        for (Executor potentialPrivilegedExecutor : executorWithGroups) {
            if (getPrivilegedExecutors(identifiables.get(0)).contains(potentialPrivilegedExecutor)) {
                boolean[] result = new boolean[identifiables.size()];
                for (int i = 0; i < identifiables.size(); i++) {
                    result[i] = true;
                }
                return result;
            }
        }
        List<PermissionMapping> permissions = new ArrayList<PermissionMapping>();
        for (int i = 0; i <= identifiables.size() / 1000; ++i) {
            int start = i * 1000;
            int end = (i + 1) * 1000 > identifiables.size() ? identifiables.size() : (i + 1) * 1000;
            final List<Long> identifiableIds = new ArrayList<Long>(end - start);
            for (int j = start; j < end; j++) {
                identifiableIds.add(identifiables.get(j).getId());
            }
            if (identifiableIds.isEmpty()) {
                break;
            }
            List<PermissionMapping> mappings = getHibernateTemplate().executeFind(new HibernateCallback<List<PermissionMapping>>() {

                @Override
                public List<PermissionMapping> doInHibernate(Session session) throws HibernateException, SQLException {
                    Query query = session
                            .createQuery("from PermissionMapping where identifiableId in (:identifiableIds) and type=:type and mask=:mask and executor in (:executors)");
                    query.setParameterList("identifiableIds", identifiableIds);
                    query.setParameter("type", identifiables.get(0).getSecuredObjectType());
                    query.setParameter("mask", permission.getMask());
                    query.setParameterList("executors", executorWithGroups);
                    return query.list();
                }
            });
            permissions.addAll(mappings);
        }
        Set<Long> allowedIdentifiableIdsSet = new HashSet<Long>(permissions.size());
        for (PermissionMapping pm : permissions) {
            allowedIdentifiableIdsSet.add(pm.getIdentifiableId());
        }
        boolean[] result = new boolean[identifiables.size()];
        for (int i = 0; i < identifiables.size(); i++) {
            result[i] = allowedIdentifiableIdsSet.contains(identifiables.get(i).getId());
        }
        return result;
    }

    /**
     * Check if {@linkplain Permission} is correct e. q. it's allowed for
     * secured object.
     * 
     * @param identifiable
     *            Secured object (permissions must be for this secured object).
     * @param permissions
     *            Permissions to check.
     */
    private void checkArePermissionAllowed(Identifiable identifiable, Collection<Permission> permissions) throws UnapplicablePermissionException {
        List<Permission> applicablePermission = identifiable.getSecuredObjectType().getAllPermissions();
        Set<Permission> notAllowedPermission = Permission.subtractPermissions(permissions, applicablePermission);
        if (notAllowedPermission.size() > 0) {
            throw new UnapplicablePermissionException(identifiable, permissions);
        }
    }

    /**
     * Save permissions for executor on secured object.
     * 
     * @param executor
     *            Executor, which got permissions.
     * @param permissions
     *            Permissions, set to executor.
     * @param identifiable
     *            Secured object to set permission on.
     */
    private void setPermissionsInternal(Executor executor, Collection<Permission> permissions, Identifiable identifiable) {
        Set<PermissionMapping> permissionMappingToRemoveSet = getOwnPermissionMappings(executor, identifiable);
        for (Permission permission : permissions) {
            PermissionMapping pm = new PermissionMapping(executor, identifiable, permission.getMask());
            if (permissionMappingToRemoveSet.contains(pm)) {
                permissionMappingToRemoveSet.remove(pm);
            } else {
                getHibernateTemplate().save(pm);
            }
        }
        getHibernateTemplate().deleteAll(permissionMappingToRemoveSet);
    }

    /**
     * Loads all permission mappings on specified secured object belongs to
     * specified executor.
     * 
     * @param session
     *            Session to load permissions.
     * @param executor
     *            Executor, which permissions is loading.
     * @param identifiable
     *            Secured object, which permissions is loading.
     * @return Loaded permissions.
     */
    private Set<PermissionMapping> getOwnPermissionMappings(Executor executor, Identifiable identifiable) {
        List<PermissionMapping> list = getHibernateTemplate().find("from PermissionMapping where identifiableId=? and type=? and executor=?",
                identifiable.getId(), identifiable.getSecuredObjectType(), executor);
        return Sets.newHashSet(list);
    }

    /**
     * Converts collection of {@linkplain PermissionMapping} into
     * {@linkplain Permission} array.
     * 
     * @param permissionMappings
     *            Converted collection of {@linkplain PermissionMapping}.
     * @param permission
     *            Template permission to transform
     *            {@linkplain PermissionMapping} into {@linkplain Permission}.
     * @return {@linkplain Permission} array.
     */
    private List<Permission> getPermissions(Collection<PermissionMapping> permissionMappings, Permission permission)
            throws PermissionNotFoundException {
        List<Permission> permissions = Lists.newArrayList();
        for (PermissionMapping pm : permissionMappings) {
            permissions.add(permission.getPermission(pm.getMask()));
        }
        return permissions;
    }

    private Set<Executor> getExecutorWithAllHisGroups(Executor executor) {
        Set<Executor> set = new HashSet<Executor>(executorDAO.getExecutorParentsAll(executor));
        set.add(executor);
        return set;
    }

    /**
     * Deletes all permissions for executor.
     * 
     * @param executor
     */
    public void deleteAllPermissions(Executor executor) {
        List<PermissionMapping> mappings = getHibernateTemplate().find("from PermissionMapping where executor=?", executor);
        getHibernateTemplate().deleteAll(mappings);
    }

    /**
     * Load {@linkplain Executor}'s, which have permission on
     * {@linkplain Identifiable}. <br/>
     * <b>Paging is not enabled.</b>
     * 
     * @param identifiable
     *            {@linkplain Identifiable} to load {@linkplain Executor}'s.
     * @return List of {@linkplain Executor}'s with permission on
     *         {@linkplain Identifiable}.
     */
    public Set<Executor> getExecutorsWithPermission(Identifiable identifiable) {
        List<Executor> list = getHibernateTemplate().find(
                "select distinct(pm.executor) from PermissionMapping pm where pm.identifiableId=? and pm.type=?", identifiable.getId(),
                identifiable.getSecuredObjectType());
        Set<Executor> result = Sets.newHashSet(list);
        result.addAll(getPrivilegedExecutors(identifiable));
        return result;
    }

    /**
     * Return array of privileged {@linkplain Executor}s for given (@linkplain
     * SecuredObject) type (i.e. executors whose permissions on SecuredObject
     * type can not be changed).
     * 
     * @param identifiable
     *            {@linkplain Identifiable} for which you want to get privileged
     *            executors.
     * @return Privileged {@linkplain Executor}'s array.
     */
    public List<Executor> getPrivilegedExecutors(Identifiable identifiable) {
        return getHibernateTemplate().find("select distinct m.executor from PrivelegedMapping m where m.type=?", identifiable.getSecuredObjectType());
    }

    /**
     * Return array of all privileged {@linkplain Executor}s for all (@linkplain
     * SecuredObject) type (i.e. executors whose permissions on any
     * SecuredObject type can not be changed).
     * 
     * @return Privileged {@linkplain Executor}'s array.
     */
    public List<Executor> getPrivilegedExecutors() {
        return getHibernateTemplate().find("select distinct m.executor from PrivelegedMapping m");
    }

    /**
     * Check if executor is privileged executor for given identifiable. Throw
     * exception, if no {@linkplain SecuredObject} for {@linkplain Identifiable}
     * found.
     * 
     * @param executor
     *            {@linkplain Executor}, to check if privileged.
     * @param identifiable
     *            {@linkplain Identifiable} object, to check if executor is
     *            privileged to it.
     * @return true if executor is privileged for given identifiable and false
     *         otherwise.
     */
    public boolean isPrivilegedExecutor(Executor executor, Identifiable identifiable) {
        return getHibernateTemplate().find("from PrivelegedMapping where type=? and executor=?", identifiable.getSecuredObjectType(), executor)
                .size() > 0;
    }

    /**
     * Adds new record in <i>dictionary</i> tables describing new SecuredObject
     * type.
     * 
     * @param type
     *            Type of SecuredObject.
     * @param privelegedExecutors
     *            Privileged executors for target class.
     * @param permission
     *            Permission class, describes allowed permissions for target
     *            class.
     */
    public void addType(SecuredObjectType type, List<? extends Executor> privelegedExecutors) {
        for (Executor executor : privelegedExecutors) {
            getHibernateTemplate().save(new PrivelegedMapping(type, executor, Permission.READ));
        }
    }

    /**
     * Load list of {@linkplain Identifiable} for which executors have
     * permission on.
     * 
     * @param executorIds
     *            Executors identities, which must have permission on loaded
     *            {@linkplain Identifiable} (at least one).
     * @param batchPresentation
     *            {@linkplain BatchPresentation} with parameters for loading
     *            {@linkplain Identifiable}'s.
     * @param permission
     *            {@linkplain Permission}, which executors must has on
     *            {@linkplain Identifiable}.
     * @param securedObjectTypes
     *            {@linkplain SecuredObject} types, used to check permissions.
     * @param enablePaging
     *            Flag, equals true, if paging must be enabled and false
     *            otherwise.
     * @return List of {@link Identifiable}'s for which executors have
     *         permission on.
     */
    public List<? extends Identifiable> getPersistentObjects(List<Long> executorIds, BatchPresentation batchPresentation, Permission permission,
            SecuredObjectType[] securedObjectTypes, boolean enablePaging) {
        List<? extends Identifiable> result = new BatchPresentationHibernateCompiler(batchPresentation).getBatch(enablePaging, executorIds,
                permission, securedObjectTypes);
        if (result.size() == 0 && enablePaging && batchPresentation.getPageNumber() > 1) {
            // several objects were removed since we last time created batch
            // presentation
            setLastPageNumber(executorIds, batchPresentation, permission, securedObjectTypes);
            result = getPersistentObjects(executorIds, batchPresentation, permission, securedObjectTypes, enablePaging);
        }
        return result;
    }

    /**
     * Load count of {@linkplain Identifiable} for which executors have
     * permission on.
     * 
     * @param executorIds
     *            Executors identities, which must have permission on loaded
     *            {@linkplain Identifiable} (at least one).
     * @param batchPresentation
     *            {@linkplain BatchPresentation} with parameters for loading
     *            {@linkplain Identifiable}'s.
     * @param permission
     *            {@linkplain Permission}, which executors must has on
     *            {@linkplain Identifiable}.
     * @param securedObjectTypes
     *            {@linkplain SecuredObject} types, used to check permissions.
     * @param enablePaging
     *            Flag, equals true, if paging must be enabled and false
     *            otherwise.
     * @return Count of {@link Identifiable}'s for which executors have
     *         permission on.
     */
    public int getPersistentObjectCount(List<Long> executorIds, BatchPresentation batchPresentation, Permission permission,
            SecuredObjectType[] securedObjectTypes) {
        return new BatchPresentationHibernateCompiler(batchPresentation).getCount(executorIds, permission, securedObjectTypes);
    }

    private void setLastPageNumber(List<Long> executorIds, BatchPresentation batchPresentation, Permission permission,
            SecuredObjectType[] securedObjectTypes) {
        int objectCount = getPersistentObjectCount(executorIds, batchPresentation, permission, securedObjectTypes);
        int maxPageNumber = PagingCommons.pageCount(objectCount, batchPresentation.getRangeSize());
        batchPresentation.setPageNumber(maxPageNumber);
    }

}
