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
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.hibernate.Criteria;
import org.hibernate.HibernateException;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.criterion.Restrictions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.orm.hibernate3.HibernateCallback;
import org.springframework.orm.hibernate3.support.HibernateDaoSupport;

import ru.runa.InternalApplicationException;
import ru.runa.af.Executor;
import ru.runa.af.Identifiable;
import ru.runa.af.Permission;
import ru.runa.af.SecuredObject;
import ru.runa.af.SecuredObjectAlreadyExistsException;
import ru.runa.af.SecuredObjectOutOfDateException;
import ru.runa.af.dao.ExecutorDAO;
import ru.runa.af.dao.SecuredObjectDAO;
import ru.runa.af.presentation.BatchPresentation;
import ru.runa.af.presentation.BatchPresentationHibernateCompiler;
import ru.runa.commons.PagingCommons;
import ru.runa.commons.hibernate.HibernateSessionFactory;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

/**
 * Created on 17.12.2004
 * 
 */
public class SecuredObjectDAOHibernateImpl extends HibernateDaoSupport implements SecuredObjectDAO {
    private static final String TYPE_PROPERTY_NAME = "type";
    private static final String SECURED_OBJECT_PROPERTY_NAME = "securedObject";
    private final static String TYPE_PARAMETER_PLACEHOLDER = "type";
    private final static String GET_ALL_PRIVELEGED_EXECUTORS_BY_TYPE = "select distinct ex from ru.runa.af.dao.impl.SecuredObjectType sot join sot.privelegedExecutors ex where sot.type = :"
            + TYPE_PARAMETER_PLACEHOLDER;
    private final static String ID_PARAMETER_PLACEHOLDER = "id";
    private final static String GET_PRIVELEGED_EXECUTORS_BY_TYPE_AND_ID = "select count(ex) from ru.runa.af.dao.impl.SecuredObjectType sot join sot.privelegedExecutors ex where sot.type = :"
            + TYPE_PARAMETER_PLACEHOLDER + " and ex.id = :" + ID_PARAMETER_PLACEHOLDER;
    private final static String GET_ALL_PRIVELEGED_EXECUTORS = "select distinct ex from ru.runa.af.dao.impl.SecuredObjectType sot join sot.privelegedExecutors ex";
    @Autowired
    private ExecutorDAO executorDAO;

    @Override
    public boolean isExist(Identifiable identifiable) {
        return getSecuredObject(identifiable.getId(), getType(identifiable)) != null;
    }

    @Override
    public SecuredObject create(Identifiable identifiable) throws SecuredObjectAlreadyExistsException {
        int type = identifiable.identifiableType();
        if (getSecuredObject(identifiable.getId(), type) != null) {
            throw new SecuredObjectAlreadyExistsException(identifiable.getId(), type);
        }
        SecuredObject securedObject = new SecuredObject(identifiable.getId(), type);
        getHibernateTemplate().save(securedObject);
        return securedObject;
    }

    @Override
    public SecuredObject get(Identifiable identifiable) throws SecuredObjectOutOfDateException {
        int type = identifiable.identifiableType();
        SecuredObject securedObject = getSecuredObject(identifiable.getId(), type);
        if (securedObject == null) {
            throw new SecuredObjectOutOfDateException(identifiable.getId(), type);
        }
        return securedObject;
    }

    @Override
    public List<SecuredObject> get(List<? extends Identifiable> identifiables) throws SecuredObjectOutOfDateException {
        List<SecuredObject> securedObjects = Lists.newArrayListWithExpectedSize(identifiables.size());
        if (identifiables.size() == 0) {
            return securedObjects;
        }
        List<SecuredObject> result = new ArrayList<SecuredObject>();
        for (int i = 0; i <= identifiables.size() / 1000; ++i) {
            int start = i * 1000;
            int end = (i + 1) * 1000 > identifiables.size() ? identifiables.size() : (i + 1) * 1000;
            final List<Long> longIdentifiers = Lists.newArrayListWithExpectedSize(end - start);
            for (int j = start; j < end; j++) {
                longIdentifiers.add(identifiables.get(j).getId());
            }
            List<SecuredObject> list = getHibernateTemplate().executeFind(new HibernateCallback<List<SecuredObject>>() {

                @Override
                public List<SecuredObject> doInHibernate(Session session) throws HibernateException, SQLException {
                    Query query = session.createQuery("from SecuredObject where extId in (:ids)");
                    query.setParameterList("ids", longIdentifiers);
                    return query.list();
                }
            });
            result.addAll(list);
        }
        Map<SecuredObject, SecuredObject> securedObjectMap = new HashMap<SecuredObject, SecuredObject>(result.size());
        for (SecuredObject so : result) {
            securedObjectMap.put(so, so);
        }
        for (Identifiable identifiable : identifiables) {
            SecuredObject dummySo = new SecuredObject(identifiable.getId(), getType(identifiable));
            SecuredObject so = securedObjectMap.get(dummySo);
            if (so == null) {
                throw new SecuredObjectOutOfDateException(dummySo.getExtId(), dummySo.getType());
            }
            securedObjects.add(so);
        }
        return securedObjects;
    }

    @Override
    public void remove(Identifiable identifiable) throws SecuredObjectOutOfDateException {
        Session session = HibernateSessionFactory.getSession();
        SecuredObject securedObject = get(identifiable);
        // TODO use hql to remove PM
        Criteria pmCriteria = session.createCriteria(PermissionMapping.class);
        pmCriteria.add(Restrictions.eq(SECURED_OBJECT_PROPERTY_NAME, securedObject));
        List<PermissionMapping> permissionMappingSet = pmCriteria.list();
        for (PermissionMapping mapping : permissionMappingSet) {
            session.delete(mapping);
        }
        getHibernateTemplate().delete(securedObject);
    }

    @Override
    public Permission getNoPermission(int type) {
        try {
            Criteria criteria = HibernateSessionFactory.getSession().createCriteria(SecuredObjectType.class);
            criteria.add(Restrictions.eq(TYPE_PROPERTY_NAME, new Integer(type)));
            SecuredObjectType securedObjectType = (SecuredObjectType) criteria.uniqueResult();
            if (securedObjectType != null) {
                return (Permission) Class.forName(securedObjectType.getPermissionClassName()).newInstance();
            }
            throw new InternalApplicationException("No Permission was found for secured object type " + type);
        } catch (Exception e) {
            throw new InternalApplicationException(e);
        }
    }

    @Override
    public Permission getNoPermission(Identifiable identifiable) {
        return getNoPermission(getType(identifiable));
    }

    @Override
    public Set<Executor> getPrivilegedExecutors(Identifiable identifiable) {
        Query query = HibernateSessionFactory.getSession().createQuery(GET_ALL_PRIVELEGED_EXECUTORS_BY_TYPE);
        query.setInteger(TYPE_PARAMETER_PLACEHOLDER, getType(identifiable));
        query.setCacheable(true);
        return Sets.newHashSet(query.list());
    }

    @Override
    public Set<Executor> getPrivilegedExecutors() {
        Query query = HibernateSessionFactory.getSession().createQuery(GET_ALL_PRIVELEGED_EXECUTORS);
        query.setCacheable(true);
        // TODO query.setCacheable(true); "select distinct ex from ru.runa.af.dao.impl.SecuredObjectType sot join sot.privelegedExecutors ex";
        return new HashSet<Executor>(query.list());
    }

    @Override
    public List<Executor> getExecutorsWithPermission(Identifiable identifiable, BatchPresentation batchPresentation)
            throws SecuredObjectOutOfDateException {
        return getExecutorsWithPermissionInternal(identifiable, batchPresentation);
    }

    @Override
    public List<Executor> getExecutorsWithoutPermission(Identifiable identifiable, BatchPresentation batchPresentation)
            throws SecuredObjectOutOfDateException {
        List<Executor> executorsWithPermissionsList = getExecutorsWithPermissionInternal(identifiable, batchPresentation);
        List<Executor> allExecutorsList = executorDAO.getAll(batchPresentation);
        allExecutorsList.removeAll(executorsWithPermissionsList);
        return allExecutorsList;
    }

    @Override
    public boolean isPrivilegedExecutor(Executor executor, Identifiable identifiable) {
        return isPrivilegedExecutor(executor, get(identifiable));
    }

    @Override
    public boolean isPrivilegedExecutor(Executor executor, SecuredObject securedObject) {
        Query query = HibernateSessionFactory.getSession().createQuery(GET_PRIVELEGED_EXECUTORS_BY_TYPE_AND_ID);
        query.setInteger(TYPE_PARAMETER_PLACEHOLDER, securedObject.getType());
        query.setLong(ID_PARAMETER_PLACEHOLDER, executor.getId());
        return ((Number) query.list().get(0)).intValue() != 0;
    }

    @Override
    public void addType(Class<? extends Identifiable> targetClass, List<? extends Executor> privelegedExecutors, Class<? extends Permission> permission) {
        Set<Executor> privelegedExecutorsSet = new HashSet<Executor>(privelegedExecutors);
        SecuredObjectType securedObjectType = new SecuredObjectType(getType(targetClass), permission.getName(), privelegedExecutorsSet);
        HibernateSessionFactory.getSession().save(securedObjectType);
    }

    @Override
    public int getType(Class<? extends Identifiable> identifiableClass) {
        return identifiableClass.getName().hashCode();
    }

    @Override
    public List<? extends Identifiable> getPersistentObjects(List<Long> executorIds, BatchPresentation batchPresentation, Permission permission,
            int[] securedObjectTypes, boolean enablePaging) {
        List<? extends Identifiable> result = new BatchPresentationHibernateCompiler(batchPresentation).getBatch(enablePaging, executorIds,
                permission, securedObjectTypes);
        if (result.size() == 0 && enablePaging && batchPresentation.getPageNumber() > 1) {
            // several objects were removed since we last time created batch presentation
            setLastPageNumber(executorIds, batchPresentation, permission, securedObjectTypes, HibernateSessionFactory.getSession());
            result = getPersistentObjects(executorIds, batchPresentation, permission, securedObjectTypes, enablePaging);
        }
        return result;
    }

    @Override
    public int getPersistentObjectCount(List<Long> executorIds, BatchPresentation batchPresentation, Permission permission, int[] securedObjectTypes) {
        return new BatchPresentationHibernateCompiler(batchPresentation).getCount(executorIds, permission, securedObjectTypes);
    }

    /**
     * Load {@linkplain Executor}'s, which have permission on {@linkplain Identifiable}.
     * @param identifiable {@linkplain Identifiable} to load {@linkplain Executor}'s.
     * @param batchPresentation {@linkplain BatchPresentation} with parameters to load executors. 
     * @return List of {@linkplain Executor}'s with permission on {@linkplain Identifiable}. 
     */
    private List<Executor> getExecutorsWithPermissionInternal(Identifiable identifiable, BatchPresentation batchPresentation)
            throws SecuredObjectOutOfDateException {
        Session session = HibernateSessionFactory.getSession();
        Criteria criteria = session.createCriteria(PermissionMapping.class);
        criteria.add(Restrictions.eq("securedObject", get(identifiable)));
        List<PermissionMapping> result = criteria.list();
        if (result.size() == 0) {
            return new ArrayList<Executor>();
        }
        Set<Long> executorsIds = new HashSet<Long>();
        for (PermissionMapping mapping : result) {
            executorsIds.add(mapping.getExecutor().getId());
        }
        return new BatchPresentationHibernateCompiler(batchPresentation).getBatch(executorsIds, "id", false);
    }

    /**
     * Check if {@linkplain SecuredObject} for object with specified identity and type exists in database.
     * @param extId Object identity.
     * @param type Object type.
     * @return Returns true, if {@linkplain SecuredObject} exists and false otherwise.
     */
    private SecuredObject getSecuredObject(Long extId, int type) {
        List<SecuredObject> list = getHibernateTemplate().find("from SecuredObject where extId=? and type=?", extId, type);
        if (list.size() > 0) {
            return list.get(0);
        }
        return null;
    }

    private int getType(Identifiable identifiable) {
        return identifiable.identifiableType();
    }

    private void setLastPageNumber(List<Long> executorIds, BatchPresentation batchPresentation, Permission permission, int[] securedObjectTypes,
            Session session) throws HibernateException {
        int objectCount = getPersistentObjectCount(executorIds, batchPresentation, permission, securedObjectTypes);
        int maxPageNumber = PagingCommons.pageCount(objectCount, batchPresentation.getRangeSize());
        batchPresentation.setPageNumber(maxPageNumber);
        session.saveOrUpdate(batchPresentation);
    }
}
