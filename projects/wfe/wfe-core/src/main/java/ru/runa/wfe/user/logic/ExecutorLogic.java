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
package ru.runa.wfe.user.logic;

import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Required;

import ru.runa.wfe.InternalApplicationException;
import ru.runa.wfe.commons.logic.CommonLogic;
import ru.runa.wfe.commons.logic.PresentationCompilerHelper;
import ru.runa.wfe.presentation.BatchPresentation;
import ru.runa.wfe.presentation.hibernate.BatchPresentationHibernateCompiler;
import ru.runa.wfe.relation.dao.RelationDAO;
import ru.runa.wfe.security.ASystem;
import ru.runa.wfe.security.AuthorizationException;
import ru.runa.wfe.security.Permission;
import ru.runa.wfe.security.SecuredObjectType;
import ru.runa.wfe.security.SystemPermission;
import ru.runa.wfe.security.WeakPasswordException;
import ru.runa.wfe.user.Actor;
import ru.runa.wfe.user.ActorPermission;
import ru.runa.wfe.user.Executor;
import ru.runa.wfe.user.ExecutorAlreadyExistsException;
import ru.runa.wfe.user.ExecutorAlreadyInGroupException;
import ru.runa.wfe.user.ExecutorDoesNotExistException;
import ru.runa.wfe.user.ExecutorNotInGroupException;
import ru.runa.wfe.user.ExecutorPermission;
import ru.runa.wfe.user.Group;
import ru.runa.wfe.user.GroupPermission;
import ru.runa.wfe.user.User;
import ru.runa.wfe.user.dao.ProfileDAO;

import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

/**
 * Created on 14.03.2005
 */
public class ExecutorLogic extends CommonLogic {
    private static final Log log = LogFactory.getLog(ExecutorLogic.class);
    private Pattern passwordCheckPattern;
    private List<SetStatusHandler> setStatusHandlers;

    @Autowired
    private ProfileDAO profileDAO;
    @Autowired
    private RelationDAO relationDAO;

    @Required
    public void setPasswordCheckPattern(String passwordCheckPattern) {
        try {
            if (!Strings.isNullOrEmpty(passwordCheckPattern)) {
                this.passwordCheckPattern = Pattern.compile(passwordCheckPattern);
            }
        } catch (Throwable th) {
            log.warn("Invalid passwordCheckPattern " + passwordCheckPattern, th);
        }
    }

    @Required
    public void setSetStatusHandlers(List<SetStatusHandler> setStatusHandlers) {
        this.setStatusHandlers = setStatusHandlers;
    }

    public boolean isExecutorExist(User user, String executorName) throws ExecutorDoesNotExistException, AuthorizationException {
        if (!executorDAO.isExecutorExist(executorName)) {
            return false;
        }
        Executor executor = executorDAO.getExecutor(executorName);
        checkPermissionAllowed(user, executor, Permission.READ);
        return true;
    }

    public void update(User user, Executor executor) throws ExecutorAlreadyExistsException, ExecutorDoesNotExistException {
        checkPermissionsOnExecutor(user, executor, ExecutorPermission.UPDATE);
        executorDAO.update(executor);
    }

    public List<Executor> getAll(User user, BatchPresentation batchPresentation) {
        BatchPresentationHibernateCompiler compiler = PresentationCompilerHelper.createAllExecutorsCompiler(user, batchPresentation);
        List<Executor> executorList = compiler.getBatch();
        return executorList;
    }

    public int getAllCount(User user, BatchPresentation batchPresentation) {
        BatchPresentationHibernateCompiler compiler = PresentationCompilerHelper.createAllExecutorsCompiler(user, batchPresentation);
        return compiler.getCount();
    }

    public List<Actor> getActors(User user, BatchPresentation batchPresentation) {
        List<Actor> executorList = getPersistentObjects(user, batchPresentation, Permission.READ,
                new SecuredObjectType[] { SecuredObjectType.ACTOR }, false);
        return executorList;
    }

    public Actor getActor(User user, String name) throws ExecutorDoesNotExistException, AuthorizationException {
        return checkPermissionsOnExecutor(user, executorDAO.getActor(name), Permission.READ);
    }

    public Actor getActorCaseInsensitive(String login) throws ExecutorDoesNotExistException {
        return executorDAO.getActorCaseInsensitive(login);
    }

    public Group getGroup(User user, String name) throws ExecutorDoesNotExistException, AuthorizationException {
        return checkPermissionsOnExecutor(user, executorDAO.getGroup(name), Permission.READ);
    }

    public Executor getExecutor(User user, String name) throws ExecutorDoesNotExistException, AuthorizationException {
        return checkPermissionsOnExecutor(user, executorDAO.getExecutor(name), Permission.READ);
    }

    public void remove(User user, List<Long> ids) throws ExecutorDoesNotExistException, AuthorizationException {
        List<Executor> executors = getExecutors(user, ids);
        checkPermissionsOnExecutors(user, executors, ExecutorPermission.UPDATE);
        for (Executor executor : executors) {
            remove(executor);
        }
    }

    public void remove(User user, Executor executor) throws ExecutorDoesNotExistException, AuthorizationException {
        checkPermissionAllowed(user, executor, ExecutorPermission.UPDATE);
        remove(executor);
    }

    public void remove(Executor executor) throws ExecutorDoesNotExistException, AuthorizationException {
        if (permissionDAO.isPrivilegedExecutor(executor)) {
            throw new AuthorizationException("Executor " + executor.getName() + " can not be removed");
        }
        if (executor instanceof Actor) {
            profileDAO.delete((Actor) executor);
        }
        permissionDAO.deleteAllPermissions(executor);
        relationDAO.removeAllRelationPairs(executor);
        executorDAO.remove(executor);
    }

    public <T extends Executor> T create(User user, T executor) throws ExecutorAlreadyExistsException, AuthorizationException {
        checkPermissionAllowed(user, ASystem.INSTANCE, SystemPermission.CREATE_EXECUTOR);
        Collection<Permission> selfPermissions;
        if (executor instanceof Group) {
            selfPermissions = Lists.newArrayList(Permission.READ, GroupPermission.LIST_GROUP);
        } else {
            selfPermissions = Lists.newArrayList(Permission.READ);
        }
        executorDAO.create(executor);
        postCreateExecutor(user, executor, selfPermissions);
        return executor;
    }

    private void postCreateExecutor(User user, Executor executor, Collection<Permission> selfPermissions) {
        Collection<Permission> p = executor.getSecuredObjectType().getNoPermission().getAllPermissions();
        permissionDAO.setPermissions(user.getActor(), p, executor);
        permissionDAO.setPermissions(executor, selfPermissions, executor);
    }

    public void addExecutorsToGroup(User user, List<? extends Executor> executors, Group group) throws ExecutorAlreadyInGroupException,
            ExecutorDoesNotExistException, AuthorizationException {
        addExecutorsToGroupInternal(user, executors, group);
    }

    public void addExecutorsToGroup(User user, List<Long> executorIds, Long groupId) throws ExecutorAlreadyInGroupException,
            ExecutorDoesNotExistException, AuthorizationException {
        List<Executor> executors = executorDAO.getExecutors(executorIds);
        Group group = executorDAO.getGroup(groupId);
        addExecutorsToGroupInternal(user, executors, group);
    }

    private void addExecutorsToGroupInternal(User user, List<? extends Executor> executors, Group group) throws AuthorizationException,
            ExecutorDoesNotExistException, ExecutorAlreadyInGroupException {
        checkPermissionsOnExecutors(user, executors, Permission.READ);
        checkPermissionsOnExecutor(user, group, GroupPermission.ADD_TO_GROUP);
        executorDAO.addExecutorsToGroup(executors, group);
    }

    public void addExecutorToGroups(User user, Executor executor, List<Group> groups) throws ExecutorAlreadyInGroupException,
            ExecutorDoesNotExistException, AuthorizationException {
        addExecutorToGroupsInternal(user, executor, groups);
    }

    public void addExecutorToGroups(User user, Long executorId, List<Long> groupIds) throws ExecutorAlreadyInGroupException,
            ExecutorDoesNotExistException, AuthorizationException {
        Executor executor = executorDAO.getExecutor(executorId);
        List<Group> groups = executorDAO.getGroups(groupIds);
        addExecutorToGroupsInternal(user, executor, groups);
    }

    private void addExecutorToGroupsInternal(User user, Executor executor, List<Group> groups) throws AuthorizationException,
            ExecutorDoesNotExistException, ExecutorAlreadyInGroupException {
        checkPermissionsOnExecutor(user, executor, Permission.READ);
        checkPermissionsOnExecutors(user, groups, GroupPermission.ADD_TO_GROUP);
        executorDAO.addExecutorToGroups(executor, groups);
    }

    public List<Executor> getGroupChildren(User user, Group group, BatchPresentation batchPresentation, boolean isExclude)
            throws ExecutorDoesNotExistException, AuthorizationException {
        checkPermissionsOnExecutor(user, group, isExclude ? GroupPermission.ADD_TO_GROUP : GroupPermission.LIST_GROUP);
        BatchPresentationHibernateCompiler compiler = PresentationCompilerHelper.createGroupChildrenCompiler(user, group, batchPresentation,
                !isExclude);
        List<Executor> executorList = compiler.getBatch();
        return executorList;
    }

    public int getGroupChildrenCount(User user, Group group, BatchPresentation batchPresentation, boolean isExclude)
            throws ExecutorDoesNotExistException, AuthorizationException {
        checkPermissionsOnExecutor(user, group, isExclude ? GroupPermission.ADD_TO_GROUP : GroupPermission.LIST_GROUP);
        BatchPresentationHibernateCompiler compiler = PresentationCompilerHelper.createGroupChildrenCompiler(user, group, batchPresentation,
                !isExclude);
        return compiler.getCount();
    }

    public List<Actor> getGroupActors(User user, Group group) throws AuthorizationException, ExecutorDoesNotExistException {
        checkPermissionsOnExecutor(user, group, GroupPermission.LIST_GROUP);
        Set<Actor> groupActors = executorDAO.getGroupActors(group);
        return filterIdentifiable(user, Lists.newArrayList(groupActors), Permission.READ);
    }

    public List<Executor> getAllExecutorsFromGroup(User user, Group group) throws ExecutorDoesNotExistException, AuthorizationException {
        checkPermissionsOnExecutor(user, group, GroupPermission.LIST_GROUP);
        return filterIdentifiable(user, executorDAO.getAllNonGroupExecutorsFromGroup(group), Permission.READ);
    }

    public List<Actor> getAvailableActorsByCodes(User user, List<Long> codes) throws AuthorizationException, ExecutorDoesNotExistException {
        return checkPermissionsOnExecutors(user, executorDAO.getAvailableActorsByCodes(codes), Permission.READ);
    }

    public void removeExecutorsFromGroup(User user, List<? extends Executor> executors, Group group) throws ExecutorNotInGroupException,
            ExecutorDoesNotExistException, AuthorizationException {
        removeExecutorsFromGroupInternal(user, executors, group);
    }

    public void removeExecutorsFromGroup(User user, List<Long> executorIds, Long groupId) throws ExecutorNotInGroupException,
            ExecutorDoesNotExistException, AuthorizationException {
        List<Executor> executors = executorDAO.getExecutors(executorIds);
        Group group = executorDAO.getGroup(groupId);
        removeExecutorsFromGroupInternal(user, executors, group);
    }

    private void removeExecutorsFromGroupInternal(User user, List<? extends Executor> executors, Group group) throws AuthorizationException,
            ExecutorDoesNotExistException, ExecutorNotInGroupException {
        // TODO this must be implemented by some mechanism
        // if (executorDAO.getAdministrator().equals(executor) &&
        // executorDAO.getAdministratorsGroup().equals(group)) {
        // throw new AuthorizationException("Executor " + executor.getName()
        // + " can not be removed from group " + group.getName());
        // }
        checkPermissionsOnExecutor(user, group, GroupPermission.REMOVE_FROM_GROUP);
        checkPermissionsOnExecutors(user, executors, Permission.READ);
        executorDAO.removeExecutorsFromGroup(executors, group);
    }

    public void removeExecutorFromGroups(User user, Executor executor, List<Group> groups) throws ExecutorNotInGroupException,
            ExecutorDoesNotExistException, AuthorizationException {
        checkPermissionsOnExecutor(user, executor, Permission.READ);
        checkPermissionsOnExecutors(user, groups, GroupPermission.REMOVE_FROM_GROUP);
        executorDAO.removeExecutorFromGroups(executor, groups);
    }

    public void removeExecutorFromGroups(User user, Long executorId, List<Long> groupIds) throws ExecutorNotInGroupException,
            ExecutorDoesNotExistException, AuthorizationException {
        Executor executor = executorDAO.getExecutor(executorId);
        List<Group> groups = executorDAO.getGroups(groupIds);
        checkPermissionsOnExecutor(user, executor, Permission.READ);
        checkPermissionsOnExecutors(user, groups, GroupPermission.REMOVE_FROM_GROUP);
        executorDAO.removeExecutorFromGroups(executor, groups);
    }

    public void setPassword(User user, Actor actor, String password) throws ExecutorDoesNotExistException, AuthorizationException,
            WeakPasswordException {
        if (passwordCheckPattern != null && !passwordCheckPattern.matcher(password).matches()) {
            throw new WeakPasswordException();
        }
        if (!isPermissionAllowed(user, actor, ExecutorPermission.UPDATE)) {
            if (user.equals(actor)) {
                checkPermissionAllowed(user, ASystem.INSTANCE, SystemPermission.CHANGE_SELF_PASSWORD);
            } else {
                throw new AuthorizationException("Executor " + user + " hasn't permission to change password for actor " + actor.getFullName());
            }
        }
        executorDAO.setPassword(actor, password);
    }

    public void setStatus(User user, Long actorId, boolean isActive) throws AuthorizationException, ExecutorDoesNotExistException {
        Actor actor = executorDAO.getActor(actorId);
        checkPermissionsOnExecutor(user, actor, ActorPermission.UPDATE_STATUS);
        executorDAO.setStatus(actor, isActive);
        callSetStatusHandlers(actor, isActive);
    }

    public List<Group> getExecutorGroups(User user, Executor executor, BatchPresentation batchPresentation, boolean isExclude)
            throws ExecutorDoesNotExistException, AuthorizationException {
        checkPermissionsOnExecutor(user, executor, Permission.READ);
        BatchPresentationHibernateCompiler compiler = PresentationCompilerHelper.createExecutorGroupsCompiler(user, executor, batchPresentation,
                !isExclude);
        List<Group> executorList = compiler.getBatch();
        return executorList;
    }

    public int getExecutorGroupsCount(User user, Executor executor, BatchPresentation batchPresentation, boolean isExclude)
            throws ExecutorDoesNotExistException, AuthorizationException {
        checkPermissionsOnExecutor(user, executor, Permission.READ);
        BatchPresentationHibernateCompiler compiler = PresentationCompilerHelper.createExecutorGroupsCompiler(user, executor, batchPresentation,
                !isExclude);
        return compiler.getCount();
    }

    public boolean isExecutorInGroup(User user, Executor executor, Group group) throws ExecutorDoesNotExistException, AuthorizationException {
        checkPermissionsOnExecutor(user, executor, Permission.READ);
        checkPermissionsOnExecutor(user, group, Permission.READ);
        return executorDAO.isExecutorInGroup(executor, group);
    }

    public Group getGroup(User user, Long id) throws ExecutorDoesNotExistException, AuthorizationException {
        return checkPermissionsOnExecutor(user, executorDAO.getGroup(id), Permission.READ);
    }

    public Executor getExecutor(User user, Long id) throws ExecutorDoesNotExistException, AuthorizationException {
        return checkPermissionsOnExecutor(user, executorDAO.getExecutor(id), Permission.READ);
    }

    public List<Executor> getExecutors(User user, List<Long> ids) throws ExecutorDoesNotExistException, AuthorizationException {
        return checkPermissionsOnExecutors(user, executorDAO.getExecutors(ids), Permission.READ);
    }

    public List<Actor> getActors(User user, List<Long> ids) throws ExecutorDoesNotExistException, AuthorizationException {
        return checkPermissionsOnExecutors(user, executorDAO.getActors(ids), Permission.READ);
    }

    public List<Actor> getActorsByCodes(User user, List<Long> codes) throws ExecutorDoesNotExistException, AuthorizationException {
        return checkPermissionsOnExecutors(user, executorDAO.getActorsByCodes(codes), Permission.READ);
    }

    public List<Actor> getActorsByExecutorIds(User user, List<Long> executorIds) throws ExecutorDoesNotExistException, AuthorizationException {
        return checkPermissionsOnExecutors(user, executorDAO.getActorsByExecutorIds(executorIds), Permission.READ);
    }

    public List<Group> getGroups(User user, List<Long> ids) throws ExecutorDoesNotExistException, AuthorizationException {
        return checkPermissionsOnExecutors(user, executorDAO.getGroups(ids), Permission.READ);
    }

    public Actor getActorByCode(User user, Long code) throws AuthorizationException, ExecutorDoesNotExistException {
        return checkPermissionsOnExecutor(user, executorDAO.getActorByCode(code), Permission.READ);
    }

    public Group saveTemporaryGroup(Group temporaryGroup, Collection<? extends Executor> groupExecutors) throws ExecutorAlreadyExistsException,
            ExecutorDoesNotExistException, ExecutorAlreadyInGroupException {
        if (executorDAO.isExecutorExist(temporaryGroup.getName())) {
            temporaryGroup = (Group) executorDAO.getExecutor(temporaryGroup.getName());
            executorDAO.clearGroup(temporaryGroup.getId());
        } else {
            temporaryGroup = executorDAO.create(temporaryGroup);
        }
        executorDAO.addExecutorsToGroup(groupExecutors, temporaryGroup);
        try {
            Set<Executor> grantedExecutors = Sets.newHashSet();
            grantedExecutors.addAll(groupExecutors);
            for (Executor executor : groupExecutors) {
                grantedExecutors.addAll(permissionDAO.getExecutorsWithPermission(executor));
            }
            for (Executor executor : grantedExecutors) {
                permissionDAO.setPermissions(executor, Lists.newArrayList(GroupPermission.LIST_GROUP, GroupPermission.READ), temporaryGroup);
            }
        } catch (Exception e) {
            throw new InternalApplicationException("Can't set permission to dynamic group", e);
        }
        return temporaryGroup;
    }

    private void callSetStatusHandlers(Actor actor, boolean isActive) {
        for (SetStatusHandler handler : setStatusHandlers) {
            try {
                handler.onStatusChange(actor, isActive);
            } catch (Throwable e) {
                log.warn("Exception while calling loginHandler " + handler, e);
            }
        }
    }
}
