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
package ru.runa.service.af.impl;

import java.util.List;

import javax.ejb.Stateless;
import javax.ejb.TransactionManagement;
import javax.ejb.TransactionManagementType;
import javax.interceptor.Interceptors;
import javax.security.auth.Subject;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.ejb.interceptor.SpringBeanAutowiringInterceptor;

import ru.runa.service.af.ExecutorServiceLocal;
import ru.runa.service.af.ExecutorServiceRemote;
import ru.runa.service.interceptors.EjbExceptionSupport;
import ru.runa.service.interceptors.EjbTransactionSupport;
import ru.runa.wfe.presentation.BatchPresentation;
import ru.runa.wfe.security.AuthenticationException;
import ru.runa.wfe.security.AuthorizationException;
import ru.runa.wfe.security.WeakPasswordException;
import ru.runa.wfe.user.Actor;
import ru.runa.wfe.user.Executor;
import ru.runa.wfe.user.ExecutorAlreadyExistsException;
import ru.runa.wfe.user.ExecutorAlreadyInGroupException;
import ru.runa.wfe.user.ExecutorDoesNotExistException;
import ru.runa.wfe.user.ExecutorNotInGroupException;
import ru.runa.wfe.user.Group;
import ru.runa.wfe.user.logic.ExecutorLogic;

import com.google.common.base.Preconditions;

/**
 * Implements ExecutorService as bean.
 */
@Stateless
@TransactionManagement(TransactionManagementType.BEAN)
@Interceptors({ EjbExceptionSupport.class, EjbTransactionSupport.class, SpringBeanAutowiringInterceptor.class })
public class ExecutorServiceBean implements ExecutorServiceLocal, ExecutorServiceRemote {
    @Autowired
    private ExecutorLogic executorLogic;

    @Override
    public List<? extends Executor> create(Subject subject, List<? extends Executor> executors) throws ExecutorAlreadyExistsException {
        Preconditions.checkNotNull(subject);
        Preconditions.checkNotNull(executors);
        executorLogic.create(subject, executors);
        return executors;
    }

    @Override
    public Actor update(Subject subject, Actor actor) throws ExecutorAlreadyExistsException, ExecutorDoesNotExistException {
        Preconditions.checkNotNull(subject);
        Preconditions.checkNotNull(actor);
        return executorLogic.update(subject, actor);
    }

    @Override
    public Group update(Subject subject, Group group) throws ExecutorAlreadyExistsException, ExecutorDoesNotExistException {
        Preconditions.checkNotNull(subject);
        Preconditions.checkNotNull(group);
        return executorLogic.update(subject, group);
    }

    @Override
    public List<Executor> getAll(Subject subject, BatchPresentation batchPresentation) {
        Preconditions.checkNotNull(subject);
        Preconditions.checkNotNull(batchPresentation);
        return executorLogic.getAll(subject, batchPresentation);
    }

    @Override
    public int getAllCount(Subject subject, BatchPresentation batchPresentation) {
        Preconditions.checkNotNull(subject);
        Preconditions.checkNotNull(batchPresentation);
        return executorLogic.getAllCount(subject, batchPresentation);
    }

    @Override
    public List<Actor> getActors(Subject subject, BatchPresentation batchPresentation) {
        Preconditions.checkNotNull(subject);
        Preconditions.checkNotNull(batchPresentation);
        return executorLogic.getActors(subject, batchPresentation);
    }

    @Override
    public Actor getActor(Subject subject, String name) throws ExecutorDoesNotExistException {
        Preconditions.checkNotNull(subject);
        return executorLogic.getActor(subject, name);
    }

    @Override
    public Actor getActorCaseInsensitive(String login) throws ExecutorDoesNotExistException {
        Preconditions.checkNotNull(login);
        return executorLogic.getActorCaseInsensitive(login);
    }

    @Override
    public Actor getActor(Subject subject, Long id) throws ExecutorDoesNotExistException {
        Preconditions.checkNotNull(subject);
        return executorLogic.getActor(subject, id);
    }

    @Override
    public Group getGroup(Subject subject, String name) throws ExecutorDoesNotExistException, AuthorizationException, AuthenticationException {
        Preconditions.checkNotNull(subject);
        return executorLogic.getGroup(subject, name);
    }

    @Override
    public Executor getExecutor(Subject subject, String name) throws ExecutorDoesNotExistException, AuthorizationException, AuthenticationException {
        Preconditions.checkNotNull(subject);
        return executorLogic.getExecutor(subject, name);
    }

    @Override
    public void remove(Subject subject, List<Long> ids) throws ExecutorDoesNotExistException, AuthorizationException, AuthenticationException {
        Preconditions.checkNotNull(subject);
        Preconditions.checkNotNull(ids);
        executorLogic.remove(subject, ids);
    }

    @Override
    public void remove(Subject subject, Executor executor) throws ExecutorDoesNotExistException, AuthorizationException, AuthenticationException {
        Preconditions.checkNotNull(subject);
        Preconditions.checkNotNull(executor);
        executorLogic.remove(subject, executor);
    }

    @Override
    public Actor create(Subject subject, Actor newActor) throws ExecutorAlreadyExistsException, AuthorizationException, AuthenticationException {
        Preconditions.checkNotNull(subject);
        Preconditions.checkNotNull(newActor);
        return executorLogic.create(subject, newActor);
    }

    @Override
    public Group create(Subject subject, Group newGroup) throws ExecutorAlreadyExistsException, AuthorizationException, AuthenticationException {
        Preconditions.checkNotNull(subject);
        Preconditions.checkNotNull(newGroup);
        return executorLogic.create(subject, newGroup);
    }

    @Override
    public void addExecutorsToGroup(Subject subject, List<? extends Executor> executors, Group group) throws ExecutorAlreadyInGroupException,
            ExecutorDoesNotExistException, AuthorizationException, AuthenticationException {
        Preconditions.checkNotNull(subject);
        Preconditions.checkNotNull(executors);
        Preconditions.checkNotNull(group);
        executorLogic.addExecutorsToGroup(subject, executors, group);
    }

    @Override
    public void addExecutorsToGroup(Subject subject, List<Long> executorIds, Long groupId) throws ExecutorAlreadyInGroupException,
            ExecutorDoesNotExistException, AuthorizationException, AuthenticationException {
        Preconditions.checkNotNull(subject);
        Preconditions.checkNotNull(executorIds);
        executorLogic.addExecutorsToGroup(subject, executorIds, groupId);
    }

    @Override
    public void addExecutorToGroups(Subject subject, Executor executor, List<Group> groups) throws ExecutorAlreadyInGroupException,
            ExecutorDoesNotExistException, AuthorizationException, AuthenticationException {
        Preconditions.checkNotNull(subject);
        Preconditions.checkNotNull(executor);
        Preconditions.checkNotNull(groups);
        executorLogic.addExecutorToGroups(subject, executor, groups);
    }

    @Override
    public void addExecutorToGroups(Subject subject, Long executorId, List<Long> groupIds) throws ExecutorAlreadyInGroupException,
            ExecutorDoesNotExistException, AuthorizationException, AuthenticationException {
        Preconditions.checkNotNull(subject);
        Preconditions.checkNotNull(groupIds);
        executorLogic.addExecutorToGroups(subject, executorId, groupIds);
    }

    @Override
    public List<Executor> getGroupChildren(Subject subject, Group group, BatchPresentation batchPresentation, boolean isExclude)
            throws ExecutorDoesNotExistException, AuthorizationException, AuthenticationException {
        Preconditions.checkNotNull(subject);
        Preconditions.checkNotNull(group);
        Preconditions.checkNotNull(batchPresentation);
        return executorLogic.getGroupChildren(subject, group, batchPresentation, isExclude);
    }

    @Override
    public int getGroupChildrenCount(Subject subject, Group group, BatchPresentation batchPresentation, boolean isExclude)
            throws ExecutorDoesNotExistException, AuthorizationException, AuthenticationException {
        Preconditions.checkNotNull(subject);
        Preconditions.checkNotNull(group);
        Preconditions.checkNotNull(batchPresentation);
        return executorLogic.getGroupChildrenCount(subject, group, batchPresentation, isExclude);
    }

    @Override
    public List<Actor> getGroupActors(Subject subject, Group group) throws AuthenticationException, AuthorizationException,
            ExecutorDoesNotExistException {
        Preconditions.checkNotNull(subject);
        Preconditions.checkNotNull(group);
        return executorLogic.getGroupActors(subject, group);
    }

    @Override
    public void removeExecutorsFromGroup(Subject subject, List<? extends Executor> executors, Group group) throws ExecutorNotInGroupException,
            ExecutorDoesNotExistException, AuthorizationException, AuthenticationException {
        Preconditions.checkNotNull(subject);
        Preconditions.checkNotNull(executors);
        Preconditions.checkNotNull(group);
        executorLogic.removeExecutorsFromGroup(subject, executors, group);
    }

    @Override
    public void removeExecutorsFromGroup(Subject subject, List<Long> executorIds, Long groupId) throws ExecutorNotInGroupException,
            ExecutorDoesNotExistException, AuthorizationException, AuthenticationException {
        Preconditions.checkNotNull(subject);
        Preconditions.checkNotNull(executorIds);
        executorLogic.removeExecutorsFromGroup(subject, executorIds, groupId);
    }

    @Override
    public void removeExecutorFromGroups(Subject subject, Executor executor, List<Group> groups) throws ExecutorNotInGroupException,
            ExecutorDoesNotExistException, AuthorizationException, AuthenticationException {
        Preconditions.checkNotNull(subject);
        Preconditions.checkNotNull(executor);
        Preconditions.checkNotNull(groups);
        executorLogic.removeExecutorFromGroups(subject, executor, groups);
    }

    @Override
    public void removeExecutorFromGroups(Subject subject, Long executorId, List<Long> groupIds) throws ExecutorNotInGroupException,
            ExecutorDoesNotExistException, AuthorizationException, AuthenticationException {
        Preconditions.checkNotNull(subject);
        Preconditions.checkNotNull(groupIds);
        executorLogic.removeExecutorFromGroups(subject, executorId, groupIds);
    }

    @Override
    public void setPassword(Subject subject, Actor actor, String password) throws ExecutorDoesNotExistException, AuthorizationException,
            AuthenticationException, WeakPasswordException {
        Preconditions.checkNotNull(subject);
        Preconditions.checkNotNull(actor);
        executorLogic.setPassword(subject, actor, password);
    }

    @Override
    public void setStatus(Subject subject, Long actorId, boolean isActive) throws AuthorizationException, AuthenticationException,
            ExecutorDoesNotExistException {
        Preconditions.checkNotNull(subject);
        executorLogic.setStatus(subject, actorId, isActive);
    }

    @Override
    public List<Group> getExecutorGroups(Subject subject, Executor executor, BatchPresentation batchPresentation, boolean isExclude)
            throws ExecutorDoesNotExistException, AuthorizationException, AuthenticationException {
        Preconditions.checkNotNull(subject);
        Preconditions.checkNotNull(executor);
        Preconditions.checkNotNull(batchPresentation);
        return executorLogic.getExecutorGroups(subject, executor, batchPresentation, isExclude);
    }

    @Override
    public int getExecutorGroupsCount(Subject subject, Executor executor, BatchPresentation batchPresentation, boolean isExclude)
            throws ExecutorDoesNotExistException, AuthorizationException, AuthenticationException {
        Preconditions.checkNotNull(subject);
        Preconditions.checkNotNull(executor);
        Preconditions.checkNotNull(batchPresentation);
        return executorLogic.getExecutorGroupsCount(subject, executor, batchPresentation, isExclude);
    }

    @Override
    public List<Executor> getAllExecutorsFromGroup(Subject subject, Group group) throws ExecutorDoesNotExistException, AuthorizationException,
            AuthenticationException {
        Preconditions.checkNotNull(subject);
        Preconditions.checkNotNull(group);
        return executorLogic.getAllExecutorsFromGroup(subject, group);
    }

    @Override
    public boolean isExecutorInGroup(Subject subject, Executor executor, Group group) throws ExecutorDoesNotExistException, AuthorizationException,
            AuthenticationException {
        Preconditions.checkNotNull(subject);
        Preconditions.checkNotNull(executor);
        Preconditions.checkNotNull(group);
        return executorLogic.isExecutorInGroup(subject, executor, group);
    }

    @Override
    public boolean isExecutorExist(Subject subject, String executorName) throws ExecutorDoesNotExistException, AuthorizationException,
            AuthenticationException {
        Preconditions.checkNotNull(subject);
        return executorLogic.isExecutorExist(subject, executorName);
    }

    @Override
    public Group getGroup(Subject subject, Long id) throws ExecutorDoesNotExistException, AuthorizationException, AuthenticationException {
        Preconditions.checkNotNull(subject);
        return executorLogic.getGroup(subject, id);
    }

    @Override
    public Executor getExecutor(Subject subject, Long id) throws ExecutorDoesNotExistException, AuthorizationException, AuthenticationException {
        Preconditions.checkNotNull(subject);
        return executorLogic.getExecutor(subject, id);
    }

    @Override
    public List<Executor> getExecutors(Subject subject, List<Long> ids) throws ExecutorDoesNotExistException, AuthorizationException,
            AuthenticationException {
        Preconditions.checkNotNull(subject);
        Preconditions.checkNotNull(ids);
        return executorLogic.getExecutors(subject, ids);
    }

    @Override
    public List<Group> getGroups(Subject subject, List<Long> ids) throws ExecutorDoesNotExistException, AuthorizationException,
            AuthenticationException {
        Preconditions.checkNotNull(subject);
        Preconditions.checkNotNull(ids);
        return executorLogic.getGroups(subject, ids);
    }

    @Override
    public Actor getActorByCode(Subject subject, Long code) throws AuthorizationException, AuthenticationException, ExecutorDoesNotExistException {
        Preconditions.checkNotNull(subject);
        return executorLogic.getActorByCode(subject, code);
    }

    @Override
    public List<Actor> getActorsByCodes(Subject subject, List<Long> codes) throws AuthorizationException, AuthenticationException,
            ExecutorDoesNotExistException {
        Preconditions.checkNotNull(subject);
        Preconditions.checkNotNull(codes);
        return executorLogic.getActorsByCodes(subject, codes);
    }

    @Override
    public List<Actor> getActorsByExecutorIds(Subject subject, List<Long> executorIds) throws ExecutorDoesNotExistException, AuthorizationException,
            AuthenticationException {
        Preconditions.checkNotNull(subject);
        Preconditions.checkNotNull(executorIds);
        return executorLogic.getActorsByExecutorIds(subject, executorIds);
    }

    @Override
    public List<Actor> getAvailableActorsByCodes(Subject subject, List<Long> codes) throws AuthorizationException, AuthenticationException,
            ExecutorDoesNotExistException {
        Preconditions.checkNotNull(subject);
        Preconditions.checkNotNull(codes);
        return executorLogic.getAvailableActorsByCodes(subject, codes);
    }

    @Override
    public List<Actor> getActors(Subject subject, List<Long> ids) throws AuthorizationException, AuthenticationException,
            ExecutorDoesNotExistException {
        Preconditions.checkNotNull(subject);
        Preconditions.checkNotNull(ids);
        return executorLogic.getActors(subject, ids);
    }
}
