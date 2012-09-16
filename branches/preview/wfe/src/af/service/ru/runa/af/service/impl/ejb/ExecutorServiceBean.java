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
package ru.runa.af.service.impl.ejb;

import java.util.List;

import javax.ejb.Stateless;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import javax.interceptor.Interceptors;
import javax.security.auth.Subject;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.ejb.interceptor.SpringBeanAutowiringInterceptor;

import ru.runa.af.Actor;
import ru.runa.af.ArgumentsCommons;
import ru.runa.af.AuthenticationException;
import ru.runa.af.AuthorizationException;
import ru.runa.af.Executor;
import ru.runa.af.ExecutorAlreadyExistsException;
import ru.runa.af.ExecutorAlreadyInGroupException;
import ru.runa.af.ExecutorNotInGroupException;
import ru.runa.af.ExecutorOutOfDateException;
import ru.runa.af.Group;
import ru.runa.af.WeakPasswordException;
import ru.runa.af.logic.ExecutorLogic;
import ru.runa.af.presentation.BatchPresentation;
import ru.runa.af.service.ExecutorServiceLocal;
import ru.runa.af.service.ExecutorServiceRemote;

/**
 * Implements ExecutorService as bean.
 */
@Stateless
@TransactionAttribute(TransactionAttributeType.REQUIRED)
@Interceptors({SpringBeanAutowiringInterceptor.class, LoggerInterceptor.class})
public class ExecutorServiceBean implements ExecutorServiceLocal, ExecutorServiceRemote {
    @Autowired
    private ExecutorLogic executorLogic;

    @Override
    public List<? extends Executor> create(Subject subject, List<? extends Executor> executors) throws ExecutorAlreadyExistsException, AuthorizationException,
            AuthenticationException {
        ArgumentsCommons.checkNotNull(subject);
        ArgumentsCommons.checkNotNull(executors);
        executorLogic.create(subject, executors);
        return executors;
    }

    @Override
    public Actor update(Subject subject, Actor actor, Actor newActor) throws ExecutorAlreadyExistsException, ExecutorOutOfDateException,
            AuthorizationException, AuthenticationException {
        ArgumentsCommons.checkNotNull(subject);
        ArgumentsCommons.checkNotNull(actor);
        ArgumentsCommons.checkNotNull(newActor);
        return executorLogic.update(subject, actor, newActor);
    }

    @Override
    public Group update(Subject subject, Group group, Group newGroup) throws ExecutorAlreadyExistsException, ExecutorOutOfDateException,
            AuthorizationException, AuthenticationException {
        ArgumentsCommons.checkNotNull(subject);
        ArgumentsCommons.checkNotNull(group);
        ArgumentsCommons.checkNotNull(newGroup);
        return executorLogic.update(subject, group, newGroup);
    }

    @Override
    public List<Executor> getAll(Subject subject, BatchPresentation batchPresentation) throws AuthorizationException, AuthenticationException {
        ArgumentsCommons.checkNotNull(subject);
        ArgumentsCommons.checkNotNull(batchPresentation);
        return executorLogic.getAll(subject, batchPresentation);
    }

    @Override
    public int getAllCount(Subject subject, BatchPresentation batchPresentation) throws AuthorizationException, AuthenticationException {
        ArgumentsCommons.checkNotNull(subject);
        ArgumentsCommons.checkNotNull(batchPresentation);
        return executorLogic.getAllCount(subject, batchPresentation);
    }

    @Override
    public List<Actor> getActors(Subject subject, BatchPresentation batchPresentation) throws AuthorizationException, AuthenticationException {
        ArgumentsCommons.checkNotNull(subject);
        ArgumentsCommons.checkNotNull(batchPresentation);
        return executorLogic.getActors(subject, batchPresentation);
    }

    @Override
    public Actor getActor(Subject subject, String name) throws ExecutorOutOfDateException, AuthorizationException, AuthenticationException {
        ArgumentsCommons.checkNotNull(subject);
        ArgumentsCommons.checkNotEmpty(name, "Actor name");
        return executorLogic.getActor(subject, name);
    }

    @Override
    public Actor getActorCaseInsensitive(String login) throws ExecutorOutOfDateException {
        ArgumentsCommons.checkNotNull(login);
        return executorLogic.getActorCaseInsensitive(login);
    }

    @Override
    public Actor getActor(Subject subject, Long id) throws ExecutorOutOfDateException, AuthorizationException, AuthenticationException {
        ArgumentsCommons.checkNotNull(subject);
        return executorLogic.getActor(subject, id);
    }

    @Override
    public Group getGroup(Subject subject, String name) throws ExecutorOutOfDateException, AuthorizationException, AuthenticationException {
        ArgumentsCommons.checkNotNull(subject);
        ArgumentsCommons.checkNotEmpty(name, "Group name");
        return executorLogic.getGroup(subject, name);
    }

    @Override
    public Executor getExecutor(Subject subject, String name) throws ExecutorOutOfDateException, AuthorizationException, AuthenticationException {
        ArgumentsCommons.checkNotNull(subject);
        ArgumentsCommons.checkNotEmpty(name, "Executor name");
        return executorLogic.getExecutor(subject, name);
    }

    @Override
    public void remove(Subject subject, List<Long> ids) throws ExecutorOutOfDateException, AuthorizationException, AuthenticationException {
        ArgumentsCommons.checkNotNull(subject);
        ArgumentsCommons.checkNotNull(ids);
        executorLogic.remove(subject, ids);
    }

    @Override
    public void remove(Subject subject, Executor executor) throws ExecutorOutOfDateException, AuthorizationException, AuthenticationException {
        ArgumentsCommons.checkNotNull(subject);
        ArgumentsCommons.checkNotNull(executor);
        executorLogic.remove(subject, executor);
    }

    @Override
    public Actor create(Subject subject, Actor newActor) throws ExecutorAlreadyExistsException, AuthorizationException, AuthenticationException {
        ArgumentsCommons.checkNotNull(subject);
        ArgumentsCommons.checkNotNull(newActor);
        return executorLogic.create(subject, newActor);
    }

    @Override
    public Group create(Subject subject, Group newGroup) throws ExecutorAlreadyExistsException, AuthorizationException, AuthenticationException {
        ArgumentsCommons.checkNotNull(subject);
        ArgumentsCommons.checkNotNull(newGroup);
        return executorLogic.create(subject, newGroup);
    }

    @Override
    public void addExecutorsToGroup(Subject subject, List<? extends Executor> executors, Group group) throws ExecutorAlreadyInGroupException,
            ExecutorOutOfDateException, AuthorizationException, AuthenticationException {
        ArgumentsCommons.checkNotNull(subject);
        ArgumentsCommons.checkNotNull(executors);
        ArgumentsCommons.checkNotNull(group);
        executorLogic.addExecutorsToGroup(subject, executors, group);
    }

    @Override
    public void addExecutorsToGroup(Subject subject, List<Long> executorIds, Long groupId) throws ExecutorAlreadyInGroupException,
            ExecutorOutOfDateException, AuthorizationException, AuthenticationException {
        ArgumentsCommons.checkNotNull(subject);
        ArgumentsCommons.checkNotNull(executorIds);
        executorLogic.addExecutorsToGroup(subject, executorIds, groupId);
    }

    @Override
    public void addExecutorToGroups(Subject subject, Executor executor, List<Group> groups) throws ExecutorAlreadyInGroupException,
            ExecutorOutOfDateException, AuthorizationException, AuthenticationException {
        ArgumentsCommons.checkNotNull(subject);
        ArgumentsCommons.checkNotNull(executor);
        ArgumentsCommons.checkNotNull(groups);
        executorLogic.addExecutorToGroups(subject, executor, groups);
    }

    @Override
    public void addExecutorToGroups(Subject subject, Long executorId, List<Long> groupIds) throws ExecutorAlreadyInGroupException,
            ExecutorOutOfDateException, AuthorizationException, AuthenticationException {
        ArgumentsCommons.checkNotNull(subject);
        ArgumentsCommons.checkNotNull(groupIds);
        executorLogic.addExecutorToGroups(subject, executorId, groupIds);
    }

    @Override
    public List<Executor> getGroupChildren(Subject subject, Group group, BatchPresentation batchPresentation, boolean isExclude)
            throws ExecutorOutOfDateException, AuthorizationException, AuthenticationException {
        ArgumentsCommons.checkNotNull(subject);
        ArgumentsCommons.checkNotNull(group);
        ArgumentsCommons.checkNotNull(batchPresentation);
        return executorLogic.getGroupChildren(subject, group, batchPresentation, isExclude);
    }

    @Override
    public int getGroupChildrenCount(Subject subject, Group group, BatchPresentation batchPresentation, boolean isExclude)
            throws ExecutorOutOfDateException, AuthorizationException, AuthenticationException {
        ArgumentsCommons.checkNotNull(subject);
        ArgumentsCommons.checkNotNull(group);
        ArgumentsCommons.checkNotNull(batchPresentation);
        return executorLogic.getGroupChildrenCount(subject, group, batchPresentation, isExclude);
    }

    @Override
    public List<Actor> getGroupActors(Subject subject, Group group) throws AuthenticationException, AuthorizationException, ExecutorOutOfDateException {
        ArgumentsCommons.checkNotNull(subject);
        ArgumentsCommons.checkNotNull(group);
        return executorLogic.getGroupActors(subject, group);
    }

    @Override
    public void removeExecutorsFromGroup(Subject subject, List<? extends Executor> executors, Group group) throws ExecutorNotInGroupException,
            ExecutorOutOfDateException, AuthorizationException, AuthenticationException {
        ArgumentsCommons.checkNotNull(subject);
        ArgumentsCommons.checkNotNull(executors);
        ArgumentsCommons.checkNotNull(group);
        executorLogic.removeExecutorsFromGroup(subject, executors, group);
    }

    @Override
    public void removeExecutorsFromGroup(Subject subject, List<Long> executorIds, Long groupId) throws ExecutorNotInGroupException,
            ExecutorOutOfDateException, AuthorizationException, AuthenticationException {
        ArgumentsCommons.checkNotNull(subject);
        ArgumentsCommons.checkNotNull(executorIds);
        executorLogic.removeExecutorsFromGroup(subject, executorIds, groupId);
    }

    @Override
    public void removeExecutorFromGroups(Subject subject, Executor executor, List<Group> groups) throws ExecutorNotInGroupException,
            ExecutorOutOfDateException, AuthorizationException, AuthenticationException {
        ArgumentsCommons.checkNotNull(subject);
        ArgumentsCommons.checkNotNull(executor);
        ArgumentsCommons.checkNotNull(groups);
        executorLogic.removeExecutorFromGroups(subject, executor, groups);
    }

    @Override
    public void removeExecutorFromGroups(Subject subject, Long executorId, List<Long> groupIds) throws ExecutorNotInGroupException,
            ExecutorOutOfDateException, AuthorizationException, AuthenticationException {
        ArgumentsCommons.checkNotNull(subject);
        ArgumentsCommons.checkNotNull(groupIds);
        executorLogic.removeExecutorFromGroups(subject, executorId, groupIds);
    }

    @Override
    public void setPassword(Subject subject, Actor actor, String password) throws ExecutorOutOfDateException, AuthorizationException,
            AuthenticationException, WeakPasswordException {
        ArgumentsCommons.checkNotNull(subject);
        ArgumentsCommons.checkNotNull(actor);
        executorLogic.setPassword(subject, actor, password);
    }

    @Override
    public void setStatus(Subject subject, Long actorId, boolean isActive) throws AuthorizationException, AuthenticationException,
            ExecutorOutOfDateException {
        ArgumentsCommons.checkNotNull(subject);
        executorLogic.setStatus(subject, actorId, isActive);
    }

    @Override
    public List<Group> getExecutorGroups(Subject subject, Executor executor, BatchPresentation batchPresentation, boolean isExclude)
            throws ExecutorOutOfDateException, AuthorizationException, AuthenticationException {
        ArgumentsCommons.checkNotNull(subject);
        ArgumentsCommons.checkNotNull(executor);
        ArgumentsCommons.checkNotNull(batchPresentation);
        return executorLogic.getExecutorGroups(subject, executor, batchPresentation, isExclude);
    }

    @Override
    public int getExecutorGroupsCount(Subject subject, Executor executor, BatchPresentation batchPresentation, boolean isExclude)
            throws ExecutorOutOfDateException, AuthorizationException, AuthenticationException {
        ArgumentsCommons.checkNotNull(subject);
        ArgumentsCommons.checkNotNull(executor);
        ArgumentsCommons.checkNotNull(batchPresentation);
        return executorLogic.getExecutorGroupsCount(subject, executor, batchPresentation, isExclude);
    }

    @Override
    public List<Executor> getAllExecutorsFromGroup(Subject subject, Group group) throws ExecutorOutOfDateException, AuthorizationException,
            AuthenticationException {
        ArgumentsCommons.checkNotNull(subject);
        ArgumentsCommons.checkNotNull(group);
        return executorLogic.getAllExecutorsFromGroup(subject, group);
    }

    @Override
    public boolean isExecutorInGroup(Subject subject, Executor executor, Group group) throws ExecutorOutOfDateException, AuthorizationException,
            AuthenticationException {
        ArgumentsCommons.checkNotNull(subject);
        ArgumentsCommons.checkNotNull(executor);
        ArgumentsCommons.checkNotNull(group);
        return executorLogic.isExecutorInGroup(subject, executor, group);
    }

    @Override
    public boolean isExecutorExist(Subject subject, String executorName) throws ExecutorOutOfDateException, AuthorizationException,
            AuthenticationException {
        ArgumentsCommons.checkNotNull(subject);
        ArgumentsCommons.checkNotEmpty(executorName, "Executor name");
        return executorLogic.isExecutorExist(subject, executorName);
    }

    @Override
    public Group getGroup(Subject subject, Long id) throws ExecutorOutOfDateException, AuthorizationException, AuthenticationException {
        ArgumentsCommons.checkNotNull(subject);
        return executorLogic.getGroup(subject, id);
    }

    @Override
    public Executor getExecutor(Subject subject, Long id) throws ExecutorOutOfDateException, AuthorizationException, AuthenticationException {
        ArgumentsCommons.checkNotNull(subject);
        return executorLogic.getExecutor(subject, id);
    }

    @Override
    public List<Executor> getExecutors(Subject subject, List<Long> ids) throws ExecutorOutOfDateException, AuthorizationException, AuthenticationException {
        ArgumentsCommons.checkNotNull(subject);
        ArgumentsCommons.checkNotNull(ids);
        return executorLogic.getExecutors(subject, ids);
    }

    @Override
    public List<Group> getGroups(Subject subject, List<Long> ids) throws ExecutorOutOfDateException, AuthorizationException, AuthenticationException {
        ArgumentsCommons.checkNotNull(subject);
        ArgumentsCommons.checkNotNull(ids);
        return executorLogic.getGroups(subject, ids);
    }

    @Override
    public Actor getActorByCode(Subject subject, Long code) throws AuthorizationException, AuthenticationException, ExecutorOutOfDateException {
        ArgumentsCommons.checkNotNull(subject);
        return executorLogic.getActorByCode(subject, code);
    }

    @Override
    public List<Actor> getActorsByCodes(Subject subject, List<Long> codes) throws AuthorizationException, AuthenticationException, ExecutorOutOfDateException {
        ArgumentsCommons.checkNotNull(subject);
        ArgumentsCommons.checkNotNull(codes);
        return executorLogic.getActorsByCodes(subject, codes);
    }

    @Override
    public List<Actor> getActorsByExecutorIds(Subject subject, List<Long> executorIds) throws ExecutorOutOfDateException, AuthorizationException,
            AuthenticationException {
        ArgumentsCommons.checkNotNull(subject);
        ArgumentsCommons.checkNotNull(executorIds);
        return executorLogic.getActorsByExecutorIds(subject, executorIds);
    }

    @Override
    public List<Actor> getAvailableActorsByCodes(Subject subject, List<Long> codes) throws AuthorizationException, AuthenticationException,
            ExecutorOutOfDateException {
        ArgumentsCommons.checkNotNull(subject);
        ArgumentsCommons.checkNotNull(codes);
        return executorLogic.getAvailableActorsByCodes(subject, codes);
    }

    @Override
    public List<Actor> getActors(Subject subject, List<Long> ids) throws AuthorizationException, AuthenticationException, ExecutorOutOfDateException {
        ArgumentsCommons.checkNotNull(subject);
        ArgumentsCommons.checkNotNull(ids);
        return executorLogic.getActors(subject, ids);
    }
}
