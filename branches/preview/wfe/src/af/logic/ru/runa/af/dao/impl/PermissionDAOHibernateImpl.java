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
package ru.runa.af.dao.impl;

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
import org.springframework.orm.hibernate3.support.HibernateDaoSupport;

import ru.runa.af.Executor;
import ru.runa.af.Permission;
import ru.runa.af.PermissionNotFoundException;
import ru.runa.af.SecuredObject;
import ru.runa.af.UnapplicablePermissionException;
import ru.runa.af.dao.ExecutorDAO;
import ru.runa.af.dao.PermissionDAO;
import ru.runa.af.dao.SecuredObjectDAO;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

/**
  * Permission DAO level implementation via Hibernate.
  *
  * @author Konstantinov Aleksey 19.02.2012
  */
public class PermissionDAOHibernateImpl extends HibernateDaoSupport implements PermissionDAO {
    @Autowired
    private ExecutorDAO executorDAO;
    @Autowired
    private SecuredObjectDAO securedObjectDAO;

    @Override
    public List<Permission> getPermissions(final Executor executor, final SecuredObject securedObject) {
        List<PermissionMapping> list = getHibernateTemplate().executeFind(new HibernateCallback<List<PermissionMapping>>() {

            @Override
            public List<PermissionMapping> doInHibernate(Session session) throws HibernateException, SQLException {
                Query query = session.createQuery("from PermissionMapping where securedObject=:securedObject and executor in (:executors)");
                query.setParameter("securedObject", securedObject);
                query.setParameterList("executors", getExecutorWithAllHisGroups(executor));
                return query.list();
            }
        });
        return getPermissions(list, securedObjectDAO.getNoPermission(securedObject.getType()));
    }

    @Override
    public List<Permission> getOwnPermissions(Executor executor, SecuredObject securedObject) {
        Set<PermissionMapping> permissionMappingList = getOwnPermissionMappings(executor, securedObject);
        return getPermissions(permissionMappingList, securedObjectDAO.getNoPermission(securedObject.getType()));
    }

    @Override
    public void setPermissions(Executor executor, Collection<Permission> permissions, SecuredObject securedObject) throws UnapplicablePermissionException {
        checkArePermissionAllowed(securedObject, permissions);
        setPermissionsInternal(executor, permissions, securedObject);
    }

    @Override
    public void setPermissions(Collection<? extends Executor> executors, Collection<Permission> permissions, SecuredObject securedObject)
            throws UnapplicablePermissionException {
        checkArePermissionAllowed(securedObject, permissions);
        for (Executor executor : executors) {
            setPermissionsInternal(executor, permissions, securedObject);
        }
    }

    @Override
    public void setPermissions(List<Executor> executors, List<Collection<Permission>> permissions, SecuredObject securedObject)
            throws UnapplicablePermissionException {
        if (executors.size() != permissions.size()) {
            throw new IllegalArgumentException("arrays length differs");
        }
        for (int i = 0; i < executors.size(); i++) {
            checkArePermissionAllowed(securedObject, permissions.get(i));
            setPermissionsInternal(executors.get(i), permissions.get(i), securedObject);
        }
    }

    @Override
    public boolean isAllowed(final Executor executor, final Permission permission, final SecuredObject securedObject) {
        return !getHibernateTemplate().executeFind(new HibernateCallback<List<PermissionMapping>>() {

            @Override
            public List<PermissionMapping> doInHibernate(Session session) throws HibernateException, SQLException {
                Query query = session.createQuery("from PermissionMapping where securedObject=? and mask=? and executor in (:executors)");
                query.setParameter(0, securedObject);
                query.setParameter(1, permission.getMask());
                query.setParameterList("executors", getExecutorWithAllHisGroups(executor));
                return query.list();
            }
        }).isEmpty();
    }

    @Override
    public boolean[] isAllowed(final Executor executor, final Permission permission, List<SecuredObject> securedObjects) {
        if (securedObjects.size() == 0) {
            return new boolean[0];
        }
        List<PermissionMapping> permissions = new ArrayList<PermissionMapping>();
        for (int i = 0; i <= securedObjects.size() / 1000; ++i) {
            int start = i * 1000;
            int end = (i + 1) * 1000 > securedObjects.size() ? securedObjects.size() : (i + 1) * 1000;
            final List<SecuredObject> securedObjectsPart = new ArrayList<SecuredObject>(end - start);
            for (int j = start; j < end; j++) {
                securedObjectsPart.add(securedObjects.get(j));
            }
            List<PermissionMapping> mappings = getHibernateTemplate().executeFind(new HibernateCallback<List<PermissionMapping>>() {

                @Override
                public List<PermissionMapping> doInHibernate(Session session) throws HibernateException, SQLException {
                    Query query = session.createQuery("from PermissionMapping where securedObject in (:securedObjects) and mask=:mask and executor in (:executors)");
                    query.setParameterList("securedObjects", securedObjectsPart);
                    query.setParameter("mask", permission.getMask());
                    query.setParameterList("executors", getExecutorWithAllHisGroups(executor));
                    return query.list();
                }
            });
            permissions.addAll(mappings);
        }
        Set<SecuredObject> allowedSecuredObjectSet = new HashSet<SecuredObject>(permissions.size());
        for (PermissionMapping pm : permissions) {
            allowedSecuredObjectSet.add(pm.getSecuredObject());
        }
        boolean[] result = new boolean[securedObjects.size()];
        for (int i = 0; i < securedObjects.size(); i++) {
            result[i] = allowedSecuredObjectSet.contains(securedObjects.get(i));
        }
        return result;
    }

    /**
     * Check if {@linkplain Permission} is correct e. q. it's allowed for secured object.
     * @param securedObject Secured object (permissions must be for this secured object).
     * @param permissions Permissions to check.
     */
    private void checkArePermissionAllowed(SecuredObject securedObject, Collection<Permission> permissions)
            throws UnapplicablePermissionException {
        List<Permission> applicablePermission = securedObjectDAO.getNoPermission(securedObject.getType()).getAllPermissions();
        Set<Permission> notAllowedPermission = Permission.subtractPermissions(permissions, applicablePermission);
        if (notAllowedPermission.size() > 0) {
            throw new UnapplicablePermissionException(securedObject.getType(), securedObject.getExtId(), permissions);
        }
    }

    /**
     * Save permissions for executor on secured object.
     * @param executor Executor, which got permissions.
     * @param permissions Permissions, set to executor.
     * @param securedObject Secured object to set permission on.
     */
    private void setPermissionsInternal(Executor executor, Collection<Permission> permissions, SecuredObject securedObject) {
        Set<PermissionMapping> permissionMappingToRemoveSet = getOwnPermissionMappings(executor, securedObject);
        for (Permission permission : permissions) {
            long mask = permission.getMask();
            PermissionMapping pm = new PermissionMapping(executor, securedObject, mask);
            if (permissionMappingToRemoveSet.contains(pm)) {
                permissionMappingToRemoveSet.remove(pm);
            } else {
                getHibernateTemplate().save(pm);
            }
        }
        getHibernateTemplate().deleteAll(permissionMappingToRemoveSet);
    }

    /**
     * Loads all permission mappings on specified secured object belongs to specified executor.
     * @param session Session to load permissions.
     * @param executor Executor, which permissions is loading.
     * @param securedObject Secured object, which permissions is loading. 
     * @return Loaded permissions.
     */
    private Set<PermissionMapping> getOwnPermissionMappings(Executor executor, SecuredObject securedObject) {
        List<PermissionMapping> list = getHibernateTemplate().find("from PermissionMapping where securedObject=? and executor=?",
                securedObject, executor);
        return Sets.newHashSet(list);
    }

    /**
     * Converts collection of {@linkplain PermissionMapping} into {@linkplain Permission} array.
     * @param permissionMappings Converted collection of {@linkplain PermissionMapping}.
     * @param permission Template permission to transform {@linkplain PermissionMapping} into {@linkplain Permission}. 
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

    @Override
    public void deleteAllPermissions(Executor executor) {
        List<PermissionMapping> mappings = getHibernateTemplate().find("from PermissionMapping where executor=?", executor);
        getHibernateTemplate().deleteAll(mappings);
        securedObjectDAO.remove(executor);
    }
    
}
