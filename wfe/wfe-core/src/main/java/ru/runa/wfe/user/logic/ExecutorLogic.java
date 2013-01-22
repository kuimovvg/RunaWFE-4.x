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

import javax.security.auth.Subject;

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
import ru.runa.wfe.security.auth.SubjectPrincipalsHelper;
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

    public boolean isExecutorExist(Subject subject, String executorName) throws ExecutorDoesNotExistException, AuthorizationException {
        if (!executorDAO.isExecutorExist(executorName)) {
            return false;
        }
        Executor executor = executorDAO.getExecutor(executorName);
        checkPermissionAllowed(subject, executor, Permission.READ);
        return true;
    }

    public void update(Subject subject, Executor executor) throws ExecutorAlreadyExistsException, ExecutorDoesNotExistException {
        executor = checkPermissionsOnExecutor(subject, executor, ExecutorPermission.UPDATE);
        executorDAO.update(executor);
    }

    public List<Executor> getAll(Subject subject, BatchPresentation batchPresentation) {
        BatchPresentationHibernateCompiler compiler = PresentationCompilerHelper.createAllExecutorsCompiler(subject, batchPresentation);
        List<Executor> executorList = compiler.getBatch();
        return executorList;
    }

    public int getAllCount(Subject subject, BatchPresentation batchPresentation) {
        BatchPresentationHibernateCompiler compiler = PresentationCompilerHelper.createAllExecutorsCompiler(subject, batchPresentation);
        return compiler.getCount();
    }

    public List<Actor> getActors(Subject subject, BatchPresentation batchPresentation) {
        List<Actor> executorList = getPersistentObjects(subject, batchPresentation, Permission.READ,
                new SecuredObjectType[] { SecuredObjectType.ACTOR }, false);
        return executorList;
    }

    public Actor getActor(Subject subject, String name) throws ExecutorDoesNotExistException, AuthorizationException {
        return checkPermissionsOnExecutor(subject, executorDAO.getActor(name), Permission.READ);
    }

    public Actor getActorCaseInsensitive(String login) throws ExecutorDoesNotExistException {
        return executorDAO.getActorCaseInsensitive(login);
    }

    public Group getGroup(Subject subject, String name) throws ExecutorDoesNotExistException, AuthorizationException {
        return checkPermissionsOnExecutor(subject, executorDAO.getGroup(name), Permission.READ);
    }

    public Executor getExecutor(Subject subject, String name) throws ExecutorDoesNotExistException, AuthorizationException {
        return checkPermissionsOnExecutor(subject, executorDAO.getExecutor(name), Permission.READ);
    }

    public void remove(Subject subject, List<Long> ids) throws ExecutorDoesNotExistException, AuthorizationException {
        List<Executor> executors = getExecutors(subject, ids);
        executors = checkPermissionsOnExecutors(subject, executors, ExecutorPermission.UPDATE);
        for (Executor executor : executors) {
            remove(executor);
        }
    }

    public void remove(Subject subject, Executor executor) throws ExecutorDoesNotExistException, AuthorizationException {
        checkPermissionAllowed(subject, executor, ExecutorPermission.UPDATE);
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

    public <T extends Executor> T create(Subject subject, T executor) throws ExecutorAlreadyExistsException, AuthorizationException {
        checkPermissionAllowed(subject, ASystem.INSTANCE, SystemPermission.CREATE_EXECUTOR);
        Collection<Permission> selfPermissions;
        if (executor instanceof Group) {
            selfPermissions = Lists.newArrayList(Permission.READ, GroupPermission.LIST_GROUP);
        } else {
            selfPermissions = Lists.newArrayList(Permission.READ);
        }
        executorDAO.create(executor);
        postCreateExecutor(subject, executor, selfPermissions);
        return executor;
    }

    private void postCreateExecutor(Subject subject, Executor executor, Collection<Permission> selfPermissions) {
        Collection<Permission> p = executor.getSecuredObjectType().getNoPermission().getAllPermissions();
        permissionDAO.setPermissions(SubjectPrincipalsHelper.getActor(subject), p, executor);
        permissionDAO.setPermissions(executor, selfPermissions, executor);
    }

    public void addExecutorsToGroup(Subject subject, List<? extends Executor> executors, Group group) throws ExecutorAlreadyInGroupException,
            ExecutorDoesNotExistException, AuthorizationException {
        addExecutorsToGroupInternal(subject, executors, group);
    }

    public void addExecutorsToGroup(Subject subject, List<Long> executorIds, Long groupId) throws ExecutorAlreadyInGroupException,
            ExecutorDoesNotExistException, AuthorizationException {
        List<Executor> executors = executorDAO.getExecutors(executorIds);
        Group group = executorDAO.getGroup(groupId);
        addExecutorsToGroupInternal(subject, executors, group);
    }

    private void addExecutorsToGroupInternal(Subject subject, List<? extends Executor> executors, Group group) throws AuthorizationException,
            ExecutorDoesNotExistException, ExecutorAlreadyInGroupException {
        executors = checkPermissionsOnExecutors(subject, executors, Permission.READ);
        group = checkPermissionsOnExecutor(subject, group, GroupPermission.ADD_TO_GROUP);
        executorDAO.addExecutorsToGroup(executors, group);
    }

    public void addExecutorToGroups(Subject subject, Executor executor, List<Group> groups) throws ExecutorAlreadyInGroupException,
            ExecutorDoesNotExistException, AuthorizationException {
        addExecutorToGroupsInternal(subject, executor, groups);
    }

    public void addExecutorToGroups(Subject subject, Long executorId, List<Long> groupIds) throws ExecutorAlreadyInGroupException,
            ExecutorDoesNotExistException, AuthorizationException {
        Executor executor = executorDAO.getExecutor(executorId);
        List<Group> groups = executorDAO.getGroups(groupIds);
        addExecutorToGroupsInternal(subject, executor, groups);
    }

    private void addExecutorToGroupsInternal(Subject subject, Executor executor, List<Group> groups) throws AuthorizationException,
            ExecutorDoesNotExistException, ExecutorAlreadyInGroupException {
        executor = checkPermissionsOnExecutor(subject, executor, Permission.READ);
        groups = checkPermissionsOnExecutors(subject, groups, GroupPermission.ADD_TO_GROUP);
        executorDAO.addExecutorToGroups(executor, groups);
    }

    public List<Executor> getGroupChildren(Subject subject, Group group, BatchPresentation batchPresentation, boolean isExclude)
            throws ExecutorDoesNotExistException, AuthorizationException {
        group = checkPermissionsOnExecutor(subject, group, isExclude ? GroupPermission.ADD_TO_GROUP : GroupPermission.LIST_GROUP);
        BatchPresentationHibernateCompiler compiler = PresentationCompilerHelper.createGroupChildrenCompiler(subject, group, batchPresentation,
                !isExclude);
        List<Executor> executorList = compiler.getBatch();
        return executorList;
    }

    public int getGroupChildrenCount(Subject subject, Group group, BatchPresentation batchPresentation, boolean isExclude)
            throws ExecutorDoesNotExistException, AuthorizationException {
        group = checkPermissionsOnExecutor(subject, group, isExclude ? GroupPermission.ADD_TO_GROUP : GroupPermission.LIST_GROUP);
        BatchPresentationHibernateCompiler compiler = PresentationCompilerHelper.createGroupChildrenCompiler(subject, group, batchPresentation,
                !isExclude);
        return compiler.getCount();
    }

    public List<Actor> getGroupActors(Subject subject, Group group) throws AuthorizationException, ExecutorDoesNotExistException {
        group = checkPermissionsOnExecutor(subject, group, GroupPermission.LIST_GROUP);
        Set<Actor> groupActors = executorDAO.getGroupActors(group);
        return filterIdentifiable(subject, Lists.newArrayList(groupActors), Permission.READ);
    }

    public List<Executor> getAllExecutorsFromGroup(Subject subject, Group group) throws ExecutorDoesNotExistException, AuthorizationException {
        group = checkPermissionsOnExecutor(subject, group, GroupPermission.LIST_GROUP);
        return filterIdentifiable(subject, executorDAO.getAllNonGroupExecutorsFromGroup(group), Permission.READ);
    }

    public List<Actor> getAvailableActorsByCodes(Subject subject, List<Long> codes) throws AuthorizationException, ExecutorDoesNotExistException {
        return checkPermissionsOnExecutors(subject, executorDAO.getAvailableActorsByCodes(codes), Permission.READ);
    }

    public void removeExecutorsFromGroup(Subject subject, List<? extends Executor> executors, Group group) throws ExecutorNotInGroupException,
            ExecutorDoesNotExistException, AuthorizationException {
        removeExecutorsFromGroupInternal(subject, executors, group);
    }

    public void removeExecutorsFromGroup(Subject subject, List<Long> executorIds, Long groupId) throws ExecutorNotInGroupException,
            ExecutorDoesNotExistException, AuthorizationException {
        List<Executor> executors = executorDAO.getExecutors(executorIds);
        Group group = executorDAO.getGroup(groupId);
        removeExecutorsFromGroupInternal(subject, executors, group);
    }

    private void removeExecutorsFromGroupInternal(Subject subject, List<? extends Executor> executors, Group group) throws AuthorizationException,
            ExecutorDoesNotExistException, ExecutorNotInGroupException {
        // TODO this must be implemented by some mechanism
        // if (executorDAO.getAdministrator().equals(executor) &&
        // executorDAO.getAdministratorsGroup().equals(group)) {
        // throw new AuthorizationException("Executor " + executor.getName()
        // + " can not be removed from group " + group.getName());
        // }
        group = checkPermissionsOnExecutor(subject, group, GroupPermission.REMOVE_FROM_GROUP);
        executors = checkPermissionsOnExecutors(subject, executors, Permission.READ);
        executorDAO.removeExecutorsFromGroup(executors, group);
    }

    public void removeExecutorFromGroups(Subject subject, Executor executor, List<Group> groups) throws ExecutorNotInGroupException,
            ExecutorDoesNotExistException, AuthorizationException {
        executor = checkPermissionsOnExecutor(subject, executor, Permission.READ);
        groups = checkPermissionsOnExecutors(subject, groups, GroupPermission.REMOVE_FROM_GROUP);
        executorDAO.removeExecutorFromGroups(executor, groups);
    }

    public void removeExecutorFromGroups(Subject subject, Long executorId, List<Long> groupIds) throws ExecutorNotInGroupException,
            ExecutorDoesNotExistException, AuthorizationException {
        Executor executor = executorDAO.getExecutor(executorId);
        List<Group> groups = executorDAO.getGroups(groupIds);
        executor = checkPermissionsOnExecutor(subject, executor, Permission.READ);
        groups = checkPermissionsOnExecutors(subject, groups, GroupPermission.REMOVE_FROM_GROUP);
        executorDAO.removeExecutorFromGroups(executor, groups);
    }

    public void setPassword(Subject subject, Actor actor, String password) throws ExecutorDoesNotExistException, AuthorizationException,
            WeakPasswordException {
        if (passwordCheckPattern != null && !passwordCheckPattern.matcher(password).matches()) {
            throw new WeakPasswordException();
        }
        if (!isPermissionAllowed(subject, actor, ExecutorPermission.UPDATE)) {
            if (SubjectPrincipalsHelper.getActor(subject).equals(actor)) {
                checkPermissionAllowed(subject, ASystem.INSTANCE, SystemPermission.CHANGE_SELF_PASSWORD);
            } else {
                throw new AuthorizationException("Executor " + SubjectPrincipalsHelper.getActor(subject).getFullName()
                        + " hasn't permission to change password for actor " + actor.getFullName());
            }
        }
        executorDAO.setPassword(actor, password);
    }

    public void setStatus(Subject subject, Long actorId, boolean isActive) throws AuthorizationException, ExecutorDoesNotExistException {
        Actor actor = executorDAO.getActor(actorId);
        actor = checkPermissionsOnExecutor(subject, actor, ActorPermission.UPDATE_STATUS);
        executorDAO.setStatus(actor, isActive);
        callSetStatusHandlers(actor, isActive);
    }

    public List<Group> getExecutorGroups(Subject subject, Executor executor, BatchPresentation batchPresentation, boolean isExclude)
            throws ExecutorDoesNotExistException, AuthorizationException {
        executor = checkPermissionsOnExecutor(subject, executor, Permission.READ);
        BatchPresentationHibernateCompiler compiler = PresentationCompilerHelper.createExecutorGroupsCompiler(subject, executor, batchPresentation,
                !isExclude);
        List<Group> executorList = compiler.getBatch();
        return executorList;
    }

    public int getExecutorGroupsCount(Subject subject, Executor executor, BatchPresentation batchPresentation, boolean isExclude)
            throws ExecutorDoesNotExistException, AuthorizationException {
        executor = checkPermissionsOnExecutor(subject, executor, Permission.READ);
        BatchPresentationHibernateCompiler compiler = PresentationCompilerHelper.createExecutorGroupsCompiler(subject, executor, batchPresentation,
                !isExclude);
        return compiler.getCount();
    }

    public boolean isExecutorInGroup(Subject subject, Executor executor, Group group) throws ExecutorDoesNotExistException, AuthorizationException {
        executor = checkPermissionsOnExecutor(subject, executor, Permission.READ);
        group = checkPermissionsOnExecutor(subject, group, Permission.READ);
        return executorDAO.isExecutorInGroup(executor, group);
    }

    public Group getGroup(Subject subject, Long id) throws ExecutorDoesNotExistException, AuthorizationException {
        return checkPermissionsOnExecutor(subject, executorDAO.getGroup(id), Permission.READ);
    }

    public Executor getExecutor(Subject subject, Long id) throws ExecutorDoesNotExistException, AuthorizationException {
        return checkPermissionsOnExecutor(subject, executorDAO.getExecutor(id), Permission.READ);
    }

    public List<Executor> getExecutors(Subject subject, List<Long> ids) throws ExecutorDoesNotExistException, AuthorizationException {
        return checkPermissionsOnExecutors(subject, executorDAO.getExecutors(ids), Permission.READ);
    }

    public List<Actor> getActors(Subject subject, List<Long> ids) throws ExecutorDoesNotExistException, AuthorizationException {
        return checkPermissionsOnExecutors(subject, executorDAO.getActors(ids), Permission.READ);
    }

    public List<Actor> getActorsByCodes(Subject subject, List<Long> codes) throws ExecutorDoesNotExistException, AuthorizationException {
        return checkPermissionsOnExecutors(subject, executorDAO.getActorsByCodes(codes), Permission.READ);
    }

    public List<Actor> getActorsByExecutorIds(Subject subject, List<Long> executorIds) throws ExecutorDoesNotExistException, AuthorizationException {
        return checkPermissionsOnExecutors(subject, executorDAO.getActorsByExecutorIds(executorIds), Permission.READ);
    }

    public List<Group> getGroups(Subject subject, List<Long> ids) throws ExecutorDoesNotExistException, AuthorizationException {
        return checkPermissionsOnExecutors(subject, executorDAO.getGroups(ids), Permission.READ);
    }

    public Actor getActorByCode(Subject subject, Long code) throws AuthorizationException, ExecutorDoesNotExistException {
        return checkPermissionsOnExecutor(subject, executorDAO.getActorByCode(code), Permission.READ);
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
