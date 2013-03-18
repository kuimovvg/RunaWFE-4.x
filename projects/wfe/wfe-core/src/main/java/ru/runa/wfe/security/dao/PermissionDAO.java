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

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

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
import ru.runa.wfe.security.SecuredObjectType;
import ru.runa.wfe.security.UnapplicablePermissionException;
import ru.runa.wfe.user.Executor;
import ru.runa.wfe.user.User;
import ru.runa.wfe.user.dao.ExecutorDAO;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
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

    private Map<SecuredObjectType, List<Executor>> privelegedExecutors = Maps.newHashMap();
    private Set<Long> privelegedExecutorIds = Sets.newHashSet();

    @Override
    protected void initDao() throws Exception {
        for (SecuredObjectType type : SecuredObjectType.values()) {
            privelegedExecutors.put(type, new ArrayList<Executor>());
        }
        try {
            List<PrivelegedMapping> list = getHibernateTemplate().find("from PrivelegedMapping m");
            for (PrivelegedMapping mapping : list) {
                privelegedExecutors.get(mapping.getType()).add(mapping.getExecutor());
                privelegedExecutorIds.add(mapping.getExecutor().getId());
            }
        } catch (Exception e) {
            log.error("priveleged executors was not loaded (if this exception occurs in empty DB just ignore it)");
            log.debug("", e);
        }
    }

    public List<Permission> getIssuedPermissions(Executor executor, Identifiable identifiable) {
        List<Permission> permissions = Lists.newArrayList();
        if (!getPrivilegedExecutors(identifiable).contains(executor)) {
            List<PermissionMapping> permissionMappings = getOwnPermissionMappings(executor, identifiable);
            Permission noPermission = identifiable.getSecuredObjectType().getNoPermission();
            for (PermissionMapping pm : permissionMappings) {
                permissions.add(noPermission.getPermission(pm.getMask()));
            }
        }
        return permissions;
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
        if (isPrivilegedExecutor(executor, identifiable)) {
            log.debug("permissions not granted for priveleged executor");
            return;
        }
        checkArePermissionAllowed(identifiable, permissions);
        List<PermissionMapping> permissionMappingToRemove = getOwnPermissionMappings(executor, identifiable);
        for (Permission permission : permissions) {
            PermissionMapping pm = new PermissionMapping(executor, identifiable, permission.getMask());
            if (permissionMappingToRemove.contains(pm)) {
                permissionMappingToRemove.remove(pm);
            } else {
                getHibernateTemplate().save(pm);
            }
        }
        getHibernateTemplate().deleteAll(permissionMappingToRemove);
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
    public boolean isAllowed(final User user, final Permission permission, final Identifiable identifiable) {
        final Set<Executor> executorWithGroups = getExecutorWithAllHisGroups(user.getActor());
        for (Executor executor2 : executorWithGroups) {
            if (getPrivilegedExecutors(identifiable).contains(executor2)) {
                return true;
            }
        }
        return !getHibernateTemplate().executeFind(new HibernateCallback<List<PermissionMapping>>() {

            @Override
            public List<PermissionMapping> doInHibernate(Session session) {
                Query query = session.createQuery("from PermissionMapping where identifiableId=? and type=? and mask=? and executor in (:executors)");
                query.setParameter(0, identifiable.getIdentifiableId());
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
    public boolean[] isAllowed(final User user, final Permission permission, final List<? extends Identifiable> identifiables) {
        if (identifiables.size() == 0) {
            return new boolean[0];
        }
        final Set<Executor> executorWithGroups = getExecutorWithAllHisGroups(user.getActor());
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
            int start = i * 1000; // TODO fails on 2000
            int end = (i + 1) * 1000 > identifiables.size() ? identifiables.size() : (i + 1) * 1000;
            final List<Long> identifiableIds = new ArrayList<Long>(end - start);
            for (int j = start; j < end; j++) {
                identifiableIds.add(identifiables.get(j).getIdentifiableId());
            }
            if (identifiableIds.isEmpty()) {
                break;
            }
            List<PermissionMapping> mappings = getHibernateTemplate().executeFind(new HibernateCallback<List<PermissionMapping>>() {

                @Override
                public List<PermissionMapping> doInHibernate(Session session) {
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
            result[i] = allowedIdentifiableIdsSet.contains(identifiables.get(i).getIdentifiableId());
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
    private List<PermissionMapping> getOwnPermissionMappings(Executor executor, Identifiable identifiable) {
        return getHibernateTemplate().find("from PermissionMapping where identifiableId=? and type=? and executor=?",
                identifiable.getIdentifiableId(), identifiable.getSecuredObjectType(), executor);
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
                "select distinct(pm.executor) from PermissionMapping pm where pm.identifiableId=? and pm.type=?", identifiable.getIdentifiableId(),
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
        return privelegedExecutors.get(identifiable.getSecuredObjectType());
    }

    /**
     * Check if executor is privileged executor for any secured object type.
     */
    public boolean isPrivilegedExecutor(Executor executor) {
        for (List<Executor> executors : privelegedExecutors.values()) {
            if (executors.contains(executor)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Check if executor is privileged executor for any secured object type.
     */
    public boolean hasPrivilegedExecutor(List<Long> executorIds) {
        for (Long executorId : executorIds) {
            if (privelegedExecutorIds.contains(executorId)) {
                return true;
            }
        }
        return false;
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
        return getPrivilegedExecutors(identifiable).contains(executor);
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
            getHibernateTemplate().save(new PrivelegedMapping(type, executor));
            this.privelegedExecutors.get(type).add(executor);
        }
    }

    /**
     * Load list of {@linkplain Identifiable} for which executors have
     * permission on.
     * 
     * @param user
     *            User which must have permission on loaded
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
    public List<? extends Identifiable> getPersistentObjects(User user, BatchPresentation batchPresentation, Permission permission,
            SecuredObjectType[] securedObjectTypes, boolean enablePaging) {
        List<? extends Identifiable> result = new BatchPresentationHibernateCompiler(batchPresentation).getBatch(enablePaging, user, permission,
                securedObjectTypes);
        if (result.size() == 0 && enablePaging && batchPresentation.getPageNumber() > 1) {
            // several objects were removed since we last time created batch
            // presentation
            setLastPageNumber(user, batchPresentation, permission, securedObjectTypes);
            result = getPersistentObjects(user, batchPresentation, permission, securedObjectTypes, enablePaging);
        }
        return result;
    }

    /**
     * Load count of {@linkplain Identifiable} for which executors have
     * permission on.
     * 
     * @param user
     *            User which must have permission on loaded
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
    public int getPersistentObjectCount(User user, BatchPresentation batchPresentation, Permission permission, SecuredObjectType[] securedObjectTypes) {
        return new BatchPresentationHibernateCompiler(batchPresentation).getCount(user, permission, securedObjectTypes);
    }

    private void setLastPageNumber(User user, BatchPresentation batchPresentation, Permission permission, SecuredObjectType[] securedObjectTypes) {
        int objectCount = getPersistentObjectCount(user, batchPresentation, permission, securedObjectTypes);
        int maxPageNumber = PagingCommons.pageCount(objectCount, batchPresentation.getRangeSize());
        batchPresentation.setPageNumber(maxPageNumber);
    }

}
