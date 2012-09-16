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
package ru.runa.af.logic;

import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;

import javax.security.auth.Subject;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import ru.runa.InternalApplicationException;
import ru.runa.af.ASystem;
import ru.runa.af.Actor;
import ru.runa.af.ActorPermission;
import ru.runa.af.AuthenticationException;
import ru.runa.af.AuthorizationException;
import ru.runa.af.Executor;
import ru.runa.af.ExecutorAlreadyExistsException;
import ru.runa.af.ExecutorAlreadyInGroupException;
import ru.runa.af.ExecutorNotInGroupException;
import ru.runa.af.ExecutorOutOfDateException;
import ru.runa.af.ExecutorPermission;
import ru.runa.af.Group;
import ru.runa.af.GroupPermission;
import ru.runa.af.Permission;
import ru.runa.af.SecuredObject;
import ru.runa.af.SystemPermission;
import ru.runa.af.UnapplicablePermissionException;
import ru.runa.af.WeakPasswordException;
import ru.runa.af.authenticaion.SubjectPrincipalsHelper;
import ru.runa.af.presentation.BatchPresentation;
import ru.runa.af.presentation.BatchPresentationHibernateCompiler;
import ru.runa.commons.Loader;

import com.google.common.base.Strings;
import com.google.common.collect.Lists;

/**
 * Created on 14.03.2005
 */
public class ExecutorLogic extends CommonLogic {
    private static final Log log = LogFactory.getLog(ExecutorLogic.class);
    private Pattern passwordCheckPattern;
    
    public void setPasswordCheckPattern(String passwordCheckPattern) {
        try {
            if (!Strings.isNullOrEmpty(passwordCheckPattern)) {
                this.passwordCheckPattern = Pattern.compile(passwordCheckPattern);
            }
        } catch (Throwable th) {
            log.warn("Invalid passwordCheckPattern " + passwordCheckPattern, th);
        }
    }

    public void create(Subject subject, List<? extends Executor> executors) throws ExecutorAlreadyExistsException, AuthorizationException,
            AuthenticationException {
        checkPermissionAllowed(subject, ASystem.SYSTEM, SystemPermission.CREATE_EXECUTOR);
        executorDAO.create(executors);
        postCreateExecutors(subject, executors, Lists.newArrayList(Permission.READ));
    }

    public boolean isExecutorExist(Subject subject, String executorName) throws ExecutorOutOfDateException, AuthorizationException,
            AuthenticationException {
        if (!executorDAO.isExecutorExist(executorName)) {
            return false;
        }
        Executor executor = executorDAO.getExecutor(executorName);
        checkPermissionAllowed(subject, executor, Permission.READ);
        return true;
    }

    public <T extends Executor> T update(Subject subject, T executor, T newExecutor) throws ExecutorAlreadyExistsException,
            ExecutorOutOfDateException, AuthorizationException, AuthenticationException {
        executor = checkPermissionsOnExecutor(subject, executor, ExecutorPermission.UPDATE);
        T updatedExecutor = executorDAO.update(executor, newExecutor);
        return updatedExecutor;
    }

    public List<Executor> getAll(Subject subject, BatchPresentation batchPresentation) throws AuthenticationException {
        BatchPresentationHibernateCompiler compiler = PresentationCompilerHelper.createAllExecutorsCompiler(subject, batchPresentation);
        List<Executor> executorList = compiler.getBatch();
        return executorList;
    }

    public int getAllCount(Subject subject, BatchPresentation batchPresentation) throws AuthenticationException {
        BatchPresentationHibernateCompiler compiler = PresentationCompilerHelper.createAllExecutorsCompiler(subject, batchPresentation);
        return compiler.getCount();
    }

    public List<Actor> getActors(Subject subject, BatchPresentation batchPresentation) throws AuthenticationException {
        List<Actor> executorList = getPersistentObjects(subject, batchPresentation, Permission.READ,
                new Class[] { Actor.class }, false);
        return executorList;
    }

    public Actor getActor(Subject subject, String name) throws ExecutorOutOfDateException, AuthorizationException, AuthenticationException {
        return checkPermissionsOnExecutor(subject, executorDAO.getActor(name), Permission.READ);
    }
    
    public Actor getActorCaseInsensitive(String login) throws ExecutorOutOfDateException {
        return executorDAO.getActorCaseInsensitive(login);
    }

    public Actor getActor(Subject subject, Long id) throws ExecutorOutOfDateException, AuthorizationException, AuthenticationException {
        return checkPermissionsOnExecutor(subject, executorDAO.getActor(id), Permission.READ);
    }

    public Group getGroup(Subject subject, String name) throws ExecutorOutOfDateException, AuthorizationException, AuthenticationException {
        return checkPermissionsOnExecutor(subject, executorDAO.getGroup(name), Permission.READ);
    }

    public Executor getExecutor(Subject subject, String name) throws ExecutorOutOfDateException, AuthorizationException, AuthenticationException {
        return checkPermissionsOnExecutor(subject, executorDAO.getExecutor(name), Permission.READ);
    }

    public void remove(Subject subject, List<Long> ids) throws ExecutorOutOfDateException, AuthorizationException, AuthenticationException {
        List<Executor> executors = getExecutors(subject, ids);
        removeInternal(subject, executors);
    }

    public void remove(Subject subject, Executor executor) throws ExecutorOutOfDateException, AuthorizationException, AuthenticationException {
        removeInternal(subject, Lists.newArrayList(executor));
    }

    private void removeInternal(Subject subject, List<Executor> executors) throws ExecutorOutOfDateException, AuthorizationException, AuthenticationException {
        executors = checkPermissionsOnExecutors(subject, executors, ExecutorPermission.UPDATE);
        Set<Executor> privelegedExecutorsSet = securedObjectDAO.getPrivilegedExecutors();
        for (Executor executor : executors) {
            if (privelegedExecutorsSet.contains(executor)) {
                throw new AuthorizationException("Executor " + executor.getName() + " can not be removed");
            }
            executorDAO.remove(executor);
        }
    }

    public Actor create(Subject subject, Actor newActor) throws ExecutorAlreadyExistsException, AuthorizationException, AuthenticationException {
        return createExecutor(subject, newActor, Lists.newArrayList(Permission.READ));
    }

    public Group create(Subject subject, Group newGroup) throws ExecutorAlreadyExistsException, AuthorizationException, AuthenticationException {
        return createExecutor(subject, newGroup, Lists.newArrayList(Permission.READ, GroupPermission.LIST_GROUP));
    }

    private <T extends Executor> T createExecutor(Subject subject, T newExecutor, Collection<Permission> selfPermissions) throws AuthorizationException,
            AuthenticationException, ExecutorAlreadyExistsException {
        checkPermissionAllowed(subject, ASystem.SYSTEM, SystemPermission.CREATE_EXECUTOR);
        T executor = executorDAO.create(newExecutor);
        postCreateExecutor(subject, executor, selfPermissions);
        return executor;
    }

    private void postCreateExecutor(Subject subject, Executor executor, Collection<Permission> selfPermissions) throws AuthenticationException {
        try {
            SecuredObject so = securedObjectDAO.get(executor);
            Collection<Permission> p = securedObjectDAO.getNoPermission(executor).getAllPermissions();
            permissionDAO.setPermissions(SubjectPrincipalsHelper.getActor(subject), p, so);
            setPrivelegedExecutorsPermissionsOnIdentifiable(executor);
            permissionDAO.setPermissions(executor, selfPermissions, so);
        } catch (ExecutorOutOfDateException e) {
            throw new InternalApplicationException(e);
        } catch (UnapplicablePermissionException e) {
            throw new InternalApplicationException(e);
        }
    }

    private void postCreateExecutors(Subject subject, List<? extends Executor> executors, Collection<Permission> selfPermissions) throws AuthenticationException {
        try {
            List<SecuredObject> securedObjects = securedObjectDAO.get(executors);
            Actor performer = SubjectPrincipalsHelper.getActor(subject);
            for (int i = 0; i < executors.size(); i++) {
                Collection<Permission> p = securedObjectDAO.getNoPermission(executors.get(i)).getAllPermissions();
                permissionDAO.setPermissions(performer, p, securedObjects.get(i));
                setPrivelegedExecutorsPermissionsOnIdentifiable(executors.get(i));
                permissionDAO.setPermissions(executors.get(i), selfPermissions, securedObjects.get(i));
            }
        } catch (ExecutorOutOfDateException e) {
            throw new InternalApplicationException(e);
        } catch (UnapplicablePermissionException e) {
            throw new InternalApplicationException(e);
        }
    }

    public void addExecutorsToGroup(Subject subject, List<? extends Executor> executors, Group group) throws ExecutorAlreadyInGroupException,
            ExecutorOutOfDateException, AuthorizationException, AuthenticationException {
        addExecutorsToGroupInternal(subject, executors, group);
    }

    public void addExecutorsToGroup(Subject subject, List<Long> executorIds, Long groupId) throws ExecutorAlreadyInGroupException,
            ExecutorOutOfDateException, AuthorizationException, AuthenticationException {
        List<Executor> executors = executorDAO.getExecutors(executorIds);
        Group group = executorDAO.getGroup(groupId);
        addExecutorsToGroupInternal(subject, executors, group);
    }

    private void addExecutorsToGroupInternal(Subject subject, List<? extends Executor> executors, Group group) throws AuthorizationException,
            AuthenticationException, ExecutorOutOfDateException, ExecutorAlreadyInGroupException {
        executors = checkPermissionsOnExecutors(subject, executors, Permission.READ);
        group = checkPermissionsOnExecutor(subject, group, GroupPermission.ADD_TO_GROUP);
        executorDAO.addExecutorsToGroup(executors, group);
    }

    public void addExecutorToGroups(Subject subject, Executor executor, List<Group> groups) throws ExecutorAlreadyInGroupException,
            ExecutorOutOfDateException, AuthorizationException, AuthenticationException {
        addExecutorToGroupsInternal(subject, executor, groups);
    }

    public void addExecutorToGroups(Subject subject, Long executorId, List<Long> groupIds) throws ExecutorAlreadyInGroupException,
            ExecutorOutOfDateException, AuthorizationException, AuthenticationException {
        Executor executor = executorDAO.getExecutor(executorId);
        List<Group> groups = executorDAO.getGroups(groupIds);
        addExecutorToGroupsInternal(subject, executor, groups);
    }

    private void addExecutorToGroupsInternal(Subject subject, Executor executor, List<Group> groups) throws AuthorizationException,
            AuthenticationException, ExecutorOutOfDateException, ExecutorAlreadyInGroupException {
        executor = checkPermissionsOnExecutor(subject, executor, Permission.READ);
        groups = checkPermissionsOnExecutors(subject, groups, GroupPermission.ADD_TO_GROUP);
        executorDAO.addExecutorToGroups(executor, groups);
    }

    public List<Executor> getGroupChildren(Subject subject, Group group, BatchPresentation batchPresentation, boolean isExclude)
            throws ExecutorOutOfDateException, AuthorizationException, AuthenticationException {
        group = checkPermissionsOnExecutor(subject, group, isExclude ? GroupPermission.ADD_TO_GROUP : GroupPermission.LIST_GROUP);
        BatchPresentationHibernateCompiler compiler = PresentationCompilerHelper.createGroupChildrenCompiler(subject, group, batchPresentation,
                !isExclude);
        List<Executor> executorList = compiler.getBatch();
        return executorList;
    }

    public int getGroupChildrenCount(Subject subject, Group group, BatchPresentation batchPresentation, boolean isExclude)
            throws ExecutorOutOfDateException, AuthorizationException, AuthenticationException {
        group = checkPermissionsOnExecutor(subject, group, isExclude ? GroupPermission.ADD_TO_GROUP : GroupPermission.LIST_GROUP);
        BatchPresentationHibernateCompiler compiler = PresentationCompilerHelper.createGroupChildrenCompiler(subject, group, batchPresentation,
                !isExclude);
        return compiler.getCount();
    }

    public List<Actor> getGroupActors(Subject subject, Group group) throws AuthenticationException, AuthorizationException, ExecutorOutOfDateException {
        group = checkPermissionsOnExecutor(subject, group, GroupPermission.LIST_GROUP);
        Set<Actor> groupActors = executorDAO.getGroupActors(group);
        return filterIdentifiable(subject, Lists.newArrayList(groupActors), Permission.READ);
    }

    public List<Executor> getAllExecutorsFromGroup(Subject subject, Group group) throws ExecutorOutOfDateException, AuthorizationException,
            AuthenticationException {
        group = checkPermissionsOnExecutor(subject, group, GroupPermission.LIST_GROUP);
        return filterIdentifiable(subject, executorDAO.getAllNonGroupExecutorsFromGroup(group), Permission.READ);
    }

    public List<Actor> getAvailableActorsByCodes(Subject subject, List<Long> codes) throws AuthorizationException, AuthenticationException,
            ExecutorOutOfDateException {
        return checkPermissionsOnExecutors(subject, executorDAO.getAvailableActorsByCodes(codes), Permission.READ);
    }

    public void removeExecutorsFromGroup(Subject subject, List<? extends Executor> executors, Group group) throws ExecutorNotInGroupException,
            ExecutorOutOfDateException, AuthorizationException, AuthenticationException {
        removeExecutorsFromGroupInternal(subject, executors, group);
    }

    public void removeExecutorsFromGroup(Subject subject, List<Long> executorIds, Long groupId) throws ExecutorNotInGroupException,
            ExecutorOutOfDateException, AuthorizationException, AuthenticationException {
        List<Executor> executors = executorDAO.getExecutors(executorIds);
        Group group = executorDAO.getGroup(groupId);
        removeExecutorsFromGroupInternal(subject, executors, group);
    }

    private void removeExecutorsFromGroupInternal(Subject subject, List<? extends Executor> executors, Group group) throws AuthorizationException,
            AuthenticationException, ExecutorOutOfDateException, ExecutorNotInGroupException {
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
            ExecutorOutOfDateException, AuthorizationException, AuthenticationException {
        executor = checkPermissionsOnExecutor(subject, executor, Permission.READ);
        groups = checkPermissionsOnExecutors(subject, groups, GroupPermission.REMOVE_FROM_GROUP);
        executorDAO.removeExecutorFromGroups(executor, groups);
    }

    public void removeExecutorFromGroups(Subject subject, Long executorId, List<Long> groupIds) throws ExecutorNotInGroupException,
            ExecutorOutOfDateException, AuthorizationException, AuthenticationException {
        Executor executor = executorDAO.getExecutor(executorId);
        List<Group> groups = executorDAO.getGroups(groupIds);
        executor = checkPermissionsOnExecutor(subject, executor, Permission.READ);
        groups = checkPermissionsOnExecutors(subject, groups, GroupPermission.REMOVE_FROM_GROUP);
        executorDAO.removeExecutorFromGroups(executor, groups);
    }

    public void setPassword(Subject subject, Actor actor, String password) throws ExecutorOutOfDateException, AuthorizationException,
            AuthenticationException, WeakPasswordException {
        if (passwordCheckPattern != null && !passwordCheckPattern.matcher(password).matches()) {
            throw new WeakPasswordException();
        }
        if (!executorDAO.isExecutorExist(actor.getName())) {
            throw new ExecutorOutOfDateException(actor.getName(), actor.getClass());
        }
        if (!isPermissionAllowed(subject, actor, ExecutorPermission.UPDATE)) {
            if (SubjectPrincipalsHelper.getActor(subject).equals(actor)) {
                checkPermissionAllowed(subject, ASystem.SYSTEM, SystemPermission.CHANGE_SELF_PASSWORD);
            } else {
                throw new AuthorizationException("Executor " + SubjectPrincipalsHelper.getActor(subject).getFullName()
                        + " hasn't permission to change password for actor " + actor.getFullName());
            }
        }
        executorDAO.setPassword(actor, password);
    }

    public void setStatus(Subject subject, Long actorId, boolean isActive) throws AuthorizationException, AuthenticationException,
            ExecutorOutOfDateException {
        Actor actor = executorDAO.getActor(actorId);
        actor = checkPermissionsOnExecutor(subject, actor, ActorPermission.UPDATE_STATUS);
        executorDAO.setStatus(actor, isActive);
        callSetStatusHandlers(actor, isActive);
    }

    public List<Group> getExecutorGroups(Subject subject, Executor executor, BatchPresentation batchPresentation, boolean isExclude)
            throws ExecutorOutOfDateException, AuthorizationException, AuthenticationException {
        executor = checkPermissionsOnExecutor(subject, executor, Permission.READ);
        BatchPresentationHibernateCompiler compiler = PresentationCompilerHelper.createExecutorGroupsCompiler(subject, executor, batchPresentation,
                !isExclude);
        List<Group> executorList = compiler.getBatch();
        return executorList;
    }

    public int getExecutorGroupsCount(Subject subject, Executor executor, BatchPresentation batchPresentation, boolean isExclude)
            throws ExecutorOutOfDateException, AuthorizationException, AuthenticationException {
        executor = checkPermissionsOnExecutor(subject, executor, Permission.READ);
        BatchPresentationHibernateCompiler compiler = PresentationCompilerHelper.createExecutorGroupsCompiler(subject, executor, batchPresentation,
                !isExclude);
        return compiler.getCount();
    }

    public boolean isExecutorInGroup(Subject subject, Executor executor, Group group) throws ExecutorOutOfDateException, AuthorizationException,
            AuthenticationException {
        executor = checkPermissionsOnExecutor(subject, executor, Permission.READ);
        group = checkPermissionsOnExecutor(subject, group, Permission.READ);
        return executorDAO.isExecutorInGroup(executor, group);
    }

    public Group getGroup(Subject subject, Long id) throws ExecutorOutOfDateException, AuthorizationException, AuthenticationException {
        return checkPermissionsOnExecutor(subject, executorDAO.getGroup(id), Permission.READ);
    }

    public Executor getExecutor(Subject subject, Long id) throws ExecutorOutOfDateException, AuthorizationException, AuthenticationException {
        return checkPermissionsOnExecutor(subject, executorDAO.getExecutor(id), Permission.READ);
    }

    public List<Executor> getExecutors(Subject subject, List<Long> ids) throws ExecutorOutOfDateException, AuthorizationException, AuthenticationException {
        return checkPermissionsOnExecutors(subject, executorDAO.getExecutors(ids), Permission.READ);
    }

    public List<Actor> getActors(Subject subject, List<Long> ids) throws ExecutorOutOfDateException, AuthorizationException, AuthenticationException {
        return checkPermissionsOnExecutors(subject, executorDAO.getActors(ids), Permission.READ);
    }

    public List<Actor> getActorsByCodes(Subject subject, List<Long> codes) throws ExecutorOutOfDateException, AuthorizationException, AuthenticationException {
        return checkPermissionsOnExecutors(subject, executorDAO.getActorsByCodes(codes), Permission.READ);
    }

    public List<Actor> getActorsByExecutorIds(Subject subject, List<Long> executorIds) throws ExecutorOutOfDateException, AuthorizationException,
            AuthenticationException {
        return checkPermissionsOnExecutors(subject, executorDAO.getActorsByExecutorIds(executorIds), Permission.READ);
    }

    public List<Group> getGroups(Subject subject, List<Long> ids) throws ExecutorOutOfDateException, AuthorizationException, AuthenticationException {
        return checkPermissionsOnExecutors(subject, executorDAO.getGroups(ids), Permission.READ);
    }

    public Actor getActorByCode(Subject subject, Long code) throws AuthorizationException, AuthenticationException, ExecutorOutOfDateException {
        return checkPermissionsOnExecutor(subject, executorDAO.getActorByCode(code), Permission.READ);
    }

    private void callSetStatusHandlers(Actor actor, boolean isActive) {
        for (String handlerClass : LogicResources.getOnStatusChangeHandlers()) {
            try {
                SetStatusHandler handler = (SetStatusHandler) (Loader.loadObject(handlerClass, null));
                handler.onStatusChange(actor, isActive);
            } catch (Throwable e) {
                log.warn("Exception while calling loginHandler " + handlerClass, e);
            }
        }
    }
}
