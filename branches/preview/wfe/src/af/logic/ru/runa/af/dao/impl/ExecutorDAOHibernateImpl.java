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
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.hibernate.Criteria;
import org.hibernate.HibernateException;
import org.hibernate.LockMode;
import org.hibernate.NonUniqueObjectException;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.StaleObjectStateException;
import org.hibernate.criterion.Criterion;
import org.hibernate.criterion.MatchMode;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Restrictions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.orm.hibernate3.HibernateCallback;
import org.springframework.orm.hibernate3.support.HibernateDaoSupport;

import ru.runa.af.Actor;
import ru.runa.af.Executor;
import ru.runa.af.ExecutorAlreadyExistsException;
import ru.runa.af.ExecutorAlreadyInGroupException;
import ru.runa.af.ExecutorNotInGroupException;
import ru.runa.af.ExecutorOpenTask;
import ru.runa.af.ExecutorOutOfDateException;
import ru.runa.af.Group;
import ru.runa.af.caches.ExecutorCache;
import ru.runa.af.caches.ExecutorCacheCtrl;
import ru.runa.af.dao.ExecutorDAO;
import ru.runa.af.dao.PermissionDAO;
import ru.runa.af.dao.RelationDAO;
import ru.runa.af.dao.SecuredObjectDAO;
import ru.runa.af.presentation.AFProfileStrategy;
import ru.runa.af.presentation.BatchPresentation;
import ru.runa.af.presentation.BatchPresentationConsts;
import ru.runa.af.presentation.BatchPresentationHibernateCompiler;
import ru.runa.bpm.taskmgmt.exe.TaskInstance;
import ru.runa.commons.hibernate.HibernateSessionFactory;

import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

/**
 * DAO for managing executors implemented via hibernate. 
 * @author Vitaliy S aka Yilativs
 * @author Gordienko_m
 */
public class ExecutorDAOHibernateImpl extends HibernateDaoSupport implements ExecutorDAO {
    private final ExecutorCache executorCache = ExecutorCacheCtrl.getInstance();
    private static final String NAME_PROPERTY_NAME = "name";
    private static final String ID_PROPERTY_NAME = "id";
    private static final String CODE_PROPERTY_NAME = "code";
    @Autowired
    private SecuredObjectDAO securedObjectDAO;
    @Autowired
    private RelationDAO relationDAO;
    @Autowired
    private PermissionDAO permissionDAO;

    @Override
    public boolean isExecutorExist(String executorName) {
        return getExecutorByName(Executor.class, executorName) != null;
    }

    @Override
    public boolean isActorExist(Long code) {
        return getActorByCodeInternal(code) != null;
    }

    @Override
    public <T extends Executor> T getExecutor(T executor) throws ExecutorOutOfDateException {
        try {
            Session session = HibernateSessionFactory.getSession();
            try {
                if (!session.contains(executor)) {
                    session.lock(executor, LockMode.READ);
                }
                return executor;
            } catch (NonUniqueObjectException e) {
                return (T) session.get(e.getEntityName(), e.getIdentifier());
            } catch (StaleObjectStateException e) {
                return (T) session.get(e.getEntityName(), e.getIdentifier());
            }
        } catch (HibernateException e) {
            throw new ExecutorOutOfDateException(executor.getName(), executor.getClass());
        }
    }

    @Override
    public Executor getExecutor(String name) throws ExecutorOutOfDateException {
        return getExecutor(Executor.class, name);
    }

    @Override
    public Executor getExecutor(Long id) throws ExecutorOutOfDateException {
        return getExecutor(Executor.class, id);
    }

    @Override
    public Actor getActor(String name) throws ExecutorOutOfDateException {
        return getExecutor(Actor.class, name);
    }

    @Override
    public Actor getActorCaseInsensitive(String name) throws ExecutorOutOfDateException {
        Criteria criteria = HibernateSessionFactory.getSession().createCriteria(Actor.class);
        criteria.add(Restrictions.ilike(NAME_PROPERTY_NAME, name, MatchMode.EXACT));
        return checkExecutorNotNull((Actor) criteria.uniqueResult(), name, Actor.class);
    }

    @Override
    public Actor getActor(Long id) throws ExecutorOutOfDateException {
        return getExecutor(Actor.class, id);
    }

    @Override
    public Actor getActorByCode(Long code) throws ExecutorOutOfDateException {
        Actor actor = getActorByCodeInternal(code);
        return checkExecutorNotNull(actor, "with code " + code, Actor.class);
    }

    @Override
    public Group getGroup(String name) throws ExecutorOutOfDateException {
        return getExecutor(Group.class, name);
    }

    @Override
    public Group getGroup(Long id) throws ExecutorOutOfDateException {
        return getExecutor(Group.class, id);
    }

    @Override
    public List<Executor> getExecutors(List<Long> ids) throws ExecutorOutOfDateException {
        return getExecutors(Executor.class, ids, false);
    }

    @Override
    public List<Actor> getActors(List<Long> ids) throws ExecutorOutOfDateException {
        return getExecutors(Actor.class, ids, false);
    }

    @Override
    public List<Actor> getActorsByExecutorIds(List<Long> executorIds) throws ExecutorOutOfDateException {
        Set<Actor> actorSet = new HashSet<Actor>();
        for (Executor executor : getExecutors(executorIds)) {
            if (executor instanceof Actor) {
                actorSet.add((Actor) executor);
            } else {
                actorSet.addAll(getGroupActors((Group) executor));
            }
        }
        return Lists.newArrayList(actorSet);
    }

    @Override
    public List<Long> getActorIdsByCodes(List<Long> codes) throws ExecutorOutOfDateException {
        String query = "select actor.id from Actor as actor where actor.code in ?";
        List<Long> codeList = getHibernateTemplate().find(query, codes);
        if (codeList.size() != codes.size()) {
            throw new ExecutorOutOfDateException("unknown actor", Actor.class);
        }
        return codeList;
    }

    @Override
    public List<Actor> getActorsByCodes(List<Long> codes) throws ExecutorOutOfDateException {
        return getExecutors(Actor.class, codes, true);
    }

    @Override
    public List<Long> getActorAndGroupsIds(Actor actor) {
        Set<Group> groupSet = getExecutorParentsAll(actor);
        List<Long> ids = Lists.newArrayListWithExpectedSize(groupSet.size() + 1);
        ids.add(actor.getId());
        for (Group group : groupSet) {
            ids.add(group.getId());
        }
        return ids;
    }

    @Override
    public List<Long> getNotActiveActorCodes() {
        return getHibernateTemplate().find("select actor.code from Actor as actor where actor.active = 0");
    }

    @Override
    public List<Actor> getAvailableActorsByCodes(List<Long> codes) {
        return getHibernateTemplate().find("select actor from Actor as actor where actor.code in ?", codes);
    }

    @Override
    public List<Group> getGroups(List<Long> ids) throws ExecutorOutOfDateException {
        return getExecutors(Group.class, ids, false);
    }

    @Override
    public <T extends Executor> T create(T executor) throws ExecutorAlreadyExistsException {
        Session session = HibernateSessionFactory.getSession();
        if (isExecutorExist(executor.getName())) {
            throw new ExecutorAlreadyExistsException(executor.getName());
        }
        if (executor instanceof Actor) {
            checkActorCode((Actor) executor, session);
        }
        session.save(executor);
        securedObjectDAO.create(executor);
        return executor;
    }

    @Override
    public void create(List<? extends Executor> executors) throws ExecutorAlreadyExistsException {
        for (Executor executor : executors) {
            create(executor);
        }
    }

    @Override
    public void remove(List<Long> ids) throws ExecutorOutOfDateException {
        List<Executor> executors = getExecutors(ids);
        for (Executor executor : executors) {
            remove(executor);
        }
    }

    @Override
    public void setPassword(Actor actor, String password) throws ExecutorOutOfDateException {
        Preconditions.checkNotNull(password, "Password must be specified.");
        ActorPassword actorPassword = getActorPassword(actor.getId());
        if (actorPassword == null) {
            actorPassword = new ActorPassword(actor, password);
            getHibernateTemplate().save(actorPassword);
        } else {
            actorPassword.setPassword(password);
            getHibernateTemplate().update(actorPassword);
        }
    }

    @Override
    public boolean isPasswordValid(Actor actor, String password) throws ExecutorOutOfDateException {
        Preconditions.checkNotNull(password, "Password must be specified.");
        ActorPassword actorPassword = new ActorPassword(actor, password);
        ActorPassword result = getActorPassword(actor.getId());
        return actorPassword.equals(result);
    }

    @Override
    public void setStatus(Actor actor, boolean isActive) throws ExecutorOutOfDateException {
        actor.setActive(isActive);
        getHibernateTemplate().update(actor);
    }

    @Override
    public boolean isActorActive(Long code) throws ExecutorOutOfDateException {
        Actor cachedActor = executorCache.getActor(code);
        if (cachedActor != null) {
            return cachedActor.isActive();
        }
        String queryString = "select actor.active from Actor as actor where actor.code = :" + CODE_PROPERTY_NAME;
        Query query = HibernateSessionFactory.getSession().createQuery(queryString);
        query.setParameter(CODE_PROPERTY_NAME, code);
        return ((Actor) getHibernateTemplate().find(queryString, code).get(0)).isActive();
//        Boolean isActive = (Boolean) query.uniqueResult();
//        if (isActive == null) {
//            throw new ExecutorOutOfDateException("unknown actor with code " + code, Actor.class);
//        }
//        return isActive;
    }

    @Override
    public <T extends Executor> T update(T oldExecutor, T newExecutor) throws ExecutorAlreadyExistsException, ExecutorOutOfDateException {
        Session session = HibernateSessionFactory.getSession();
        if (isExecutorExistExcludeId(session, Executor.class, Restrictions.eq(NAME_PROPERTY_NAME, newExecutor.getName()), oldExecutor.getId())) {
            throw new ExecutorAlreadyExistsException(newExecutor.getName());
        }
        if (newExecutor instanceof Actor) {
            Actor newActor = (Actor) newExecutor;
            if (isExecutorExistExcludeId(session, Actor.class, Restrictions.eq(CODE_PROPERTY_NAME, newActor.getCode()), oldExecutor.getId())) {
                throw new ExecutorAlreadyExistsException(newActor.getCode());
            }
        }
        oldExecutor.update(newExecutor);
        session.update(oldExecutor);
        return oldExecutor;
    }

    @Override
    public void clearGroup(Long groupId) throws ExecutorOutOfDateException {
        Group group = getGroup(groupId);
        List<ExecutorGroupRelation> list = getRelationGroupWithExecutors(group);
        getHibernateTemplate().deleteAll(list);
    }

    @Override
    public List<Executor> getAll(BatchPresentation batchPresentation) {
        return getAll(Executor.class, batchPresentation);
    }

    @Override
    public List<Actor> getAllActors(BatchPresentation batchPresentation) {
        return getAll(Actor.class, batchPresentation);
    }

    @Override
    public List<Group> getAllGroups() {
        BatchPresentation batchPresentation = AFProfileStrategy.EXECUTOR_DEAFAULT_BATCH_PRESENTATOIN_FACTORY.getDefaultBatchPresentation();
        batchPresentation.setRangeSize(BatchPresentationConsts.MAX_UNPAGED_REQUEST_SIZE);
        return getAll(Group.class, batchPresentation);
    }

    @Override
    public void addExecutorsToGroup(Collection<? extends Executor> executors, Group group) throws ExecutorOutOfDateException, ExecutorAlreadyInGroupException {
        for (Executor executor : executors) {
            checkAndAddExecutorToGroup(executor, group);
        }
    }

    @Override
    public void addExecutorToGroups(Executor executor, List<Group> groups) throws ExecutorOutOfDateException, ExecutorAlreadyInGroupException {
        for (Group group : groups) {
            checkAndAddExecutorToGroup(executor, group);
        }
    }

    @Override
    public void removeExecutorsFromGroup(List<? extends Executor> executors, Group group) throws ExecutorOutOfDateException, ExecutorNotInGroupException {
        for (Executor executor : executors) {
            checkAndRemoveExecutorFromGroup(executor, group);
        }
    }

    @Override
    public void removeExecutorFromGroups(Executor executor, List<Group> groups) throws ExecutorOutOfDateException, ExecutorNotInGroupException {
        for (Group group : groups) {
            checkAndRemoveExecutorFromGroup(executor, group);
        }
    }

    @Override
    public boolean isExecutorInGroup(Executor executor, Group group) throws ExecutorOutOfDateException {
        return getExecutorParentsAll(executor).contains(group);
    }

    @Override
    public List<Executor> getGroupChildren(Group group, BatchPresentation batchPresentation) throws ExecutorOutOfDateException {
        Set<Executor> childrenSet = getGroupChildren(group);
        List<Executor> unmodify = getAll(Executor.class, batchPresentation);
        List<Executor> allExecutorList = new ArrayList<Executor>();
        allExecutorList.addAll(unmodify);
        allExecutorList.retainAll(childrenSet);
        return allExecutorList;
    }

    @Override
    public Set<Executor> getGroupChildren(Group group) {
        Set<Executor> result = executorCache.getGroupMembers(group);
        if (result != null) {
            return result;
        }
        result = new HashSet<Executor>();
        for (ExecutorGroupRelation relation : getRelationGroupWithExecutors(group)) {
            result.add(relation.getExecutor());
        }
        return result;
    }
    
    private List<ExecutorGroupRelation> getRelationGroupWithExecutors(Group group) {
        return getHibernateTemplate().find("from ExecutorGroupRelation where group=?", group);
    }

    private List<ExecutorGroupRelation> getRelationExecutorWithGroups(Executor executor) {
        return getHibernateTemplate().find("from ExecutorGroupRelation where executor=?", executor);
    }

    private ExecutorGroupRelation getRelationExecutorWithGroup(Group group, Executor executor) {
        List<ExecutorGroupRelation> list = getHibernateTemplate().find("from ExecutorGroupRelation where group=? and executor=?", group, executor);
        if (list.size() > 0) {
            return list.get(0);
        }
        return null;
    }

    @Override
    public Set<Actor> getGroupActors(Group group) throws ExecutorOutOfDateException {
        Set<Actor> result = executorCache.getGroupActorsAll(group);
        if (result == null) {
            result = getGroupActors(group, new HashSet<Group>());
        }
        return result;
    }

    @Override
    public Set<Group> getExecutorParentsAll(Executor executor) {
        return getExecutorGroupsAll(executor, new HashSet<Executor>());
    }

    @Override
    public List<Executor> getAllNonGroupExecutorsFromGroup(Group group) throws ExecutorOutOfDateException {
        Set<Executor> childrenSet = getGroupChildren(group);
        List<Executor> retVal = new ArrayList<Executor>();
        for (Executor executor : childrenSet) {
            if (!(executor instanceof Group)) {
                retVal.add(executor);
            }
        }
        return retVal;
    }

    @Override
    public List<Group> getExecutorGroups(Executor executor, BatchPresentation batchPresentation) throws ExecutorOutOfDateException {
        Set<Group> executorGroupSet = getExecutorGroups(executor);
        List<Group> tmp = getAll(Group.class, batchPresentation);
        List<Group> allGroupList = new ArrayList<Group>();
        allGroupList.addAll(tmp);
        allGroupList.retainAll(executorGroupSet);
        allGroupList.remove(executor);
        return allGroupList;
    }

    @Override
    public ExecutorOpenTask createOpenTask(ExecutorOpenTask executorOpenTask) {
        HibernateSessionFactory.getSession().save(executorOpenTask);
        return executorOpenTask;
    }

    @Override
    public void removeOpenTask(TaskInstance taskInstance) {
        String q = "select eot from " + ExecutorOpenTask.class.getName() + " as eot where eot.taskInstance=?";
        List<ExecutorOpenTask> list = getHibernateTemplate().find(q, taskInstance);
        removeOpenTasks(list);
    }

    @Override
    public void removeOpenTasks(Long processInstanceId) {
        String q = "select eot from " + ExecutorOpenTask.class.getName() + " as eot where eot.taskInstance.processInstance.id=?";
        List<ExecutorOpenTask> list = getHibernateTemplate().find(q, processInstanceId);
        removeOpenTasks(list);
    }

    @Override
    public void removeOpenTasks(String processDefinitionName) {
        String q = "select eot from " + ExecutorOpenTask.class.getName()
                + " as eot where eot.taskInstance.processInstance.processDefinition.name=?";
        List<ExecutorOpenTask> list = getHibernateTemplate().find(q, processDefinitionName);
        removeOpenTasks(list);
    }

    @Override
    public List<ExecutorOpenTask> getOpenedTasksByExecutor(Executor executor) {
        String q = "select eot from " + ExecutorOpenTask.class.getName()
                + " as eot where eot.executor=?";
        return getHibernateTemplate().find(q, executor);
    }

    private void removeOpenTasks(List<ExecutorOpenTask> executorOpenTasks) {
        getHibernateTemplate().deleteAll(executorOpenTasks);
    }

    @Override
    public void remove(Executor executor) {
        getHibernateTemplate().deleteAll(getRelationExecutorWithGroups(executor));
        if (executor instanceof Group) {
            getHibernateTemplate().deleteAll(getRelationGroupWithExecutors((Group) executor));
        } else {
            ActorPassword actorPassword = getActorPassword(executor.getId());
            if (actorPassword != null) {
                getHibernateTemplate().delete(actorPassword);
            }
        }
        // TODO to ...DAO
        getHibernateTemplate().deleteAll(getOpenedTasksByExecutor(executor));
        permissionDAO.deleteAllPermissions(executor);
        
        relationDAO.removeAllRelationPairs(executor);

        getHibernateTemplate().delete(executor);
    }

    /**
     * Generates code for actor, if code not set (equals 0).
     * If code is already set, when throws {@linkplain ExecutorAlreadyExistsException} if executor with what code exists in database. 
     * @param actor Actor to generate code if not set.
     * @param session Hibernate session.
     */
    private void checkActorCode(Actor actor, Session session) throws ExecutorAlreadyExistsException {
        if (actor.getCode() == null) {
            Long nextCode = getHibernateTemplate().execute(new HibernateCallback<Long>() {
                @Override
                public Long doInHibernate(Session session) throws HibernateException, SQLException {
                    Criteria criteria = session.createCriteria(Actor.class);
                    criteria.setMaxResults(1);
                    criteria.addOrder(Order.asc(CODE_PROPERTY_NAME));
                    List<Actor> actors = criteria.list();
                    if (actors.size() > 0) {
                        return new Long(actors.get(0).getCode().longValue() - 1);
                    }
                    return -1L;
                }
            });
            actor.setCode(nextCode);
        }
        if (isActorExist(actor.getCode())) {
            throw new ExecutorAlreadyExistsException(actor.getCode());
        }
    }

    private <T extends Executor> List<T> getAll(Class<T> clazz, BatchPresentation batchPresentation) {
        List<T> retVal = executorCache.getAllExecutor(clazz, batchPresentation);
        if (retVal != null) {
            return retVal;
        }
        int cacheVersion = executorCache.getCacheVersion();
        retVal = new BatchPresentationHibernateCompiler(batchPresentation).getBatch(clazz, false);
        executorCache.addAllExecutor(cacheVersion, clazz, batchPresentation, retVal);
        return retVal;
    }

    private void checkAndAddExecutorToGroup(Executor executor, Group group) throws ExecutorAlreadyInGroupException {
        if (getRelationExecutorWithGroup(group, executor) != null) {
            throw new ExecutorAlreadyInGroupException(executor.getName(), group.getName());
        }
        getHibernateTemplate().save(new ExecutorGroupRelation(group, executor));
    }

    private void checkAndRemoveExecutorFromGroup(Executor executor, Group group) throws ExecutorNotInGroupException {
        ExecutorGroupRelation mapping = getRelationExecutorWithGroup(group, executor);
        if (mapping == null) {
            throw new ExecutorNotInGroupException(executor.getName(), group.getName());
        }
        getHibernateTemplate().delete(mapping);
    }

    private Set<Actor> getGroupActors(Group group, Set<Group> visited) throws ExecutorOutOfDateException {
        Set<Actor> result = executorCache.getGroupActorsAll(group);
        if (result != null) {
            return result;
        }
        result = new HashSet<Actor>();
        if (visited.contains(group)) {
            return result;
        }
        visited.add(group);
        for (Executor executor : getGroupChildren(group)) {
            if (executor instanceof Group) {
                result.addAll(getGroupActors((Group) executor, visited));
            } else {
                result.add((Actor) executor);
            }
        }
        return result;
    }

    private Set<Group> getExecutorGroups(Executor executor) {
        Set<Group> result = executorCache.getExecutorParents(executor);
        if (result == null) {
            result = new HashSet<Group>();
            Session session = HibernateSessionFactory.getSession();
            Criteria criteria = session.createCriteria(ExecutorGroupRelation.class);
            criteria.add(Restrictions.eq("executor", executor));
            List<ExecutorGroupRelation> relations = criteria.list();
            for (ExecutorGroupRelation relation : relations) {
                result.add(relation.getGroup());
            }
            return result;
        }
        return result;
    }

    private Set<Group> getExecutorGroupsAll(Executor executor, Set<Executor> visited) {
        Set<Group> result = executorCache.getExecutorParentsAll(executor);
        if (result == null) {
            result = new HashSet<Group>();
            if (visited.contains(executor)) {
                return result;
            }
            visited.add(executor);
            for (Group group : getExecutorGroups(executor)) {
                result.add(group);
                result.addAll(getExecutorGroupsAll(group, visited));
            }
        }
        return result;
    }

    /**
     * Loads executors by id or code (for {@link Actor}).
     * @param clazz Loaded executors class.
     * @param identifiers Loaded executors identities or codes.
     * @param loadByCodes Flag, equals true, to loading actors by codes; false to load executors by identity. 
     * @return Loaded executors.
     */
    private <T extends Executor> List<T> getExecutors(final Class<T> clazz, final List<Long> identifiers, boolean loadByCodes) throws ExecutorOutOfDateException {
        final String propertyName = loadByCodes ? CODE_PROPERTY_NAME : ID_PROPERTY_NAME;
        List<T> executors = getExecutorsFromCache(clazz, identifiers, loadByCodes);
        if (executors != null) {
            return executors;
        }
        List<T> list = getHibernateTemplate().executeFind(new HibernateCallback<List<T>>() {

            @Override
            public List<T> doInHibernate(Session session) throws HibernateException, SQLException {
                Query query = session.createQuery("from " + clazz.getName() + " where " + propertyName + " in (:ids)");
                query.setParameterList("ids", identifiers);
                return query.list();
            }
        });
        HashMap<Long, Executor> idExecutorMap = Maps.newHashMapWithExpectedSize(list.size());
        for (Executor executor : list) {
            idExecutorMap.put(loadByCodes ? ((Actor) executor).getCode() : executor.getId(), executor);
        }
        executors = Lists.newArrayListWithExpectedSize(identifiers.size());
        for (Long id : identifiers) {
            Executor executor = idExecutorMap.get(id);
            if (executor == null) {
                throw new ExecutorOutOfDateException("with identifier " + id + " for property " + propertyName, clazz);
            }
            executors.add((T) executor);
        }
        return executors;
    }

    /**
     * Loads executors by id or code (for {@link Actor}) from caches.
     * @param clazz Loaded executors class.
     * @param identifiers Loaded executors identities or codes.
     * @param loadByCodes Flag, equals true, to loading actors by codes; false to load executors by identity. 
     * @return Loaded executors or null, if executors couldn't load from cache.
     */
    private <T extends Executor> List<T> getExecutorsFromCache(Class<T> clazz, List<Long> identifiers, boolean loadByCodes) throws ExecutorOutOfDateException {
        List<T> executors = Lists.newArrayListWithExpectedSize(identifiers.size());
        for (Long id  : identifiers) {
            Preconditions.checkArgument(id != null, "id == null");
            Executor ex = !loadByCodes ? executorCache.getExecutor(id) : executorCache.getActor(id);
            if (ex == null) {
                return null;
            }
            if (!clazz.isAssignableFrom(ex.getClass())) {
                String propertyName = loadByCodes ? CODE_PROPERTY_NAME : ID_PROPERTY_NAME;
                throw new ExecutorOutOfDateException("with identifier " + id + " for property " + propertyName, clazz);
            }
            executors.add((T) ex);
        }
        return executors;
    }

    private <T extends Executor> T getExecutorById(Class<T> clazz, Long id) {
        Executor executor = executorCache.getExecutor(id);
        if (executor != null) {
            return clazz.isAssignableFrom(executor.getClass()) ? (T) executor : null;
        } else {
            // we do not use session.get() here since hibernate 2.1 does not support it for
            // table per-class hierarchy
            Criteria criteria = HibernateSessionFactory.getSession().createCriteria(clazz);
            criteria.add(Restrictions.eq(ID_PROPERTY_NAME, id));
            return (T) criteria.uniqueResult();
        }
    }

    private <T extends Executor> T getExecutorByName(Class<T> clazz, String name) {
        Executor executor = executorCache.getExecutor(name);
        if (executor != null) {
            return (T) (clazz.isAssignableFrom(executor.getClass()) ? executor : null);
        } else {
            // we do not use session.get() here since hibernate 2.1 does not support it for
            // table per-class hierarchy
            Criteria criteria = HibernateSessionFactory.getSession().createCriteria(clazz);
            criteria.add(Restrictions.eq(NAME_PROPERTY_NAME, name));
            return (T) criteria.uniqueResult();
        }
    }

    private <T extends Executor> T getExecutor(Class<T> clazz, Long id) throws ExecutorOutOfDateException {
        return checkExecutorNotNull(getExecutorById(clazz, new Long(id)), id, clazz);
    }

    private <T extends Executor> T getExecutor(Class<T> clazz, String name) throws ExecutorOutOfDateException {
        if (Strings.isNullOrEmpty(name)) {
            throw new NullPointerException("Executor name must be specified");
        }
        return checkExecutorNotNull(getExecutorByName(clazz, name), name, clazz);
    }

    private ActorPassword getActorPassword(Long id) throws HibernateException {
        List<ActorPassword> list = getHibernateTemplate().find("from ActorPassword where actorId=?", id);
        if (list.size() > 0) {
            return list.get(0);
        }
        return null;
    }

    private boolean isExecutorExistExcludeId(Session session, Class<? extends Executor> clazz, Criterion executorCriterion, Long id) {
        Criteria criteria = session.createCriteria(clazz);
        criteria.add(executorCriterion);
        criteria.add(Restrictions.not(Restrictions.eq(ID_PROPERTY_NAME, new Long(id))));
        return !criteria.list().isEmpty();
    }

    private Actor getActorByCodeInternal(Long code) {
        Actor actor = executorCache.getActor(code);
        if (actor != null) {
            return actor;
        }
        Criteria criteria = HibernateSessionFactory.getSession().createCriteria(Actor.class);
        criteria.add(Restrictions.eq(CODE_PROPERTY_NAME, new Long(code)));
        return (Actor) criteria.uniqueResult();
    }

    private <T extends Executor> T checkExecutorNotNull(T executor, Long id, Class<T> clazz) throws ExecutorOutOfDateException {
        if (executor == null) {
            throw new ExecutorOutOfDateException(id, clazz);
        }
        return executor;
    }

    private <T extends Executor> T checkExecutorNotNull(T executor, String name, Class<T> clazz) throws ExecutorOutOfDateException {
        if (executor == null) {
            throw new ExecutorOutOfDateException(name, clazz);
        }
        return executor;
    }
}
