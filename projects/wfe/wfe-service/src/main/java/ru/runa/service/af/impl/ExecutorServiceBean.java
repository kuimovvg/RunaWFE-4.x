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
import ru.runa.wfe.user.User;
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
    public void update(User user, Executor executor) throws ExecutorAlreadyExistsException, ExecutorDoesNotExistException {
        Preconditions.checkNotNull(user);
        Preconditions.checkNotNull(executor);
        executorLogic.update(user, executor);
    }

    @Override
    public List<Executor> getAll(User user, BatchPresentation batchPresentation) {
        Preconditions.checkNotNull(user);
        Preconditions.checkNotNull(batchPresentation);
        return executorLogic.getAll(user, batchPresentation);
    }

    @Override
    public int getAllCount(User user, BatchPresentation batchPresentation) {
        Preconditions.checkNotNull(user);
        Preconditions.checkNotNull(batchPresentation);
        return executorLogic.getAllCount(user, batchPresentation);
    }

    @Override
    public List<Actor> getActors(User user, BatchPresentation batchPresentation) {
        Preconditions.checkNotNull(user);
        Preconditions.checkNotNull(batchPresentation);
        return executorLogic.getActors(user, batchPresentation);
    }

    @Override
    public Actor getActorCaseInsensitive(String login) throws ExecutorDoesNotExistException {
        Preconditions.checkNotNull(login);
        return executorLogic.getActorCaseInsensitive(login);
    }

    @Override
    public Executor getExecutor(User user, String name) throws ExecutorDoesNotExistException, AuthorizationException, AuthenticationException {
        Preconditions.checkNotNull(user);
        return executorLogic.getExecutor(user, name);
    }

    @Override
    public void remove(User user, List<Long> ids) throws ExecutorDoesNotExistException, AuthorizationException, AuthenticationException {
        Preconditions.checkNotNull(user);
        Preconditions.checkNotNull(ids);
        executorLogic.remove(user, ids);
    }

    @Override
    public void remove(User user, Executor executor) throws ExecutorDoesNotExistException, AuthorizationException, AuthenticationException {
        Preconditions.checkNotNull(user);
        Preconditions.checkNotNull(executor);
        executorLogic.remove(user, executor);
    }

    @Override
    public <T extends Executor> T create(User user, T executor) throws ExecutorAlreadyExistsException, AuthorizationException {
        Preconditions.checkNotNull(user);
        Preconditions.checkNotNull(executor);
        return executorLogic.create(user, executor);
    }

    @Override
    public void addExecutorsToGroup(User user, List<? extends Executor> executors, Group group) throws ExecutorAlreadyInGroupException,
            ExecutorDoesNotExistException, AuthorizationException, AuthenticationException {
        Preconditions.checkNotNull(user);
        Preconditions.checkNotNull(executors);
        Preconditions.checkNotNull(group);
        executorLogic.addExecutorsToGroup(user, executors, group);
    }

    @Override
    public void addExecutorsToGroup(User user, List<Long> executorIds, Long groupId) throws ExecutorAlreadyInGroupException,
            ExecutorDoesNotExistException, AuthorizationException, AuthenticationException {
        Preconditions.checkNotNull(user);
        Preconditions.checkNotNull(executorIds);
        executorLogic.addExecutorsToGroup(user, executorIds, groupId);
    }

    @Override
    public void addExecutorToGroups(User user, Executor executor, List<Group> groups) throws ExecutorAlreadyInGroupException,
            ExecutorDoesNotExistException, AuthorizationException, AuthenticationException {
        Preconditions.checkNotNull(user);
        Preconditions.checkNotNull(executor);
        Preconditions.checkNotNull(groups);
        executorLogic.addExecutorToGroups(user, executor, groups);
    }

    @Override
    public void addExecutorToGroups(User user, Long executorId, List<Long> groupIds) throws ExecutorAlreadyInGroupException,
            ExecutorDoesNotExistException, AuthorizationException, AuthenticationException {
        Preconditions.checkNotNull(user);
        Preconditions.checkNotNull(groupIds);
        executorLogic.addExecutorToGroups(user, executorId, groupIds);
    }

    @Override
    public List<Executor> getGroupChildren(User user, Group group, BatchPresentation batchPresentation, boolean isExclude)
            throws ExecutorDoesNotExistException, AuthorizationException, AuthenticationException {
        Preconditions.checkNotNull(user);
        Preconditions.checkNotNull(group);
        Preconditions.checkNotNull(batchPresentation);
        return executorLogic.getGroupChildren(user, group, batchPresentation, isExclude);
    }

    @Override
    public int getGroupChildrenCount(User user, Group group, BatchPresentation batchPresentation, boolean isExclude)
            throws ExecutorDoesNotExistException, AuthorizationException, AuthenticationException {
        Preconditions.checkNotNull(user);
        Preconditions.checkNotNull(group);
        Preconditions.checkNotNull(batchPresentation);
        return executorLogic.getGroupChildrenCount(user, group, batchPresentation, isExclude);
    }

    @Override
    public List<Actor> getGroupActors(User user, Group group) throws AuthenticationException, AuthorizationException, ExecutorDoesNotExistException {
        Preconditions.checkNotNull(user);
        Preconditions.checkNotNull(group);
        return executorLogic.getGroupActors(user, group);
    }

    @Override
    public void removeExecutorsFromGroup(User user, List<? extends Executor> executors, Group group) throws ExecutorNotInGroupException,
            ExecutorDoesNotExistException, AuthorizationException, AuthenticationException {
        Preconditions.checkNotNull(user);
        Preconditions.checkNotNull(executors);
        Preconditions.checkNotNull(group);
        executorLogic.removeExecutorsFromGroup(user, executors, group);
    }

    @Override
    public void removeExecutorsFromGroup(User user, List<Long> executorIds, Long groupId) throws ExecutorNotInGroupException,
            ExecutorDoesNotExistException, AuthorizationException, AuthenticationException {
        Preconditions.checkNotNull(user);
        Preconditions.checkNotNull(executorIds);
        executorLogic.removeExecutorsFromGroup(user, executorIds, groupId);
    }

    @Override
    public void removeExecutorFromGroups(User user, Executor executor, List<Group> groups) throws ExecutorNotInGroupException,
            ExecutorDoesNotExistException, AuthorizationException, AuthenticationException {
        Preconditions.checkNotNull(user);
        Preconditions.checkNotNull(executor);
        Preconditions.checkNotNull(groups);
        executorLogic.removeExecutorFromGroups(user, executor, groups);
    }

    @Override
    public void removeExecutorFromGroups(User user, Long executorId, List<Long> groupIds) throws ExecutorNotInGroupException,
            ExecutorDoesNotExistException, AuthorizationException, AuthenticationException {
        Preconditions.checkNotNull(user);
        Preconditions.checkNotNull(groupIds);
        executorLogic.removeExecutorFromGroups(user, executorId, groupIds);
    }

    @Override
    public void setPassword(User user, Actor actor, String password) throws ExecutorDoesNotExistException, AuthorizationException,
            AuthenticationException, WeakPasswordException {
        Preconditions.checkNotNull(user);
        Preconditions.checkNotNull(actor);
        executorLogic.setPassword(user, actor, password);
    }

    @Override
    public void setStatus(User user, Long actorId, boolean isActive) throws AuthorizationException, AuthenticationException,
            ExecutorDoesNotExistException {
        Preconditions.checkNotNull(user);
        executorLogic.setStatus(user, actorId, isActive);
    }

    @Override
    public List<Group> getExecutorGroups(User user, Executor executor, BatchPresentation batchPresentation, boolean isExclude)
            throws ExecutorDoesNotExistException, AuthorizationException, AuthenticationException {
        Preconditions.checkNotNull(user);
        Preconditions.checkNotNull(executor);
        Preconditions.checkNotNull(batchPresentation);
        return executorLogic.getExecutorGroups(user, executor, batchPresentation, isExclude);
    }

    @Override
    public int getExecutorGroupsCount(User user, Executor executor, BatchPresentation batchPresentation, boolean isExclude)
            throws ExecutorDoesNotExistException, AuthorizationException, AuthenticationException {
        Preconditions.checkNotNull(user);
        Preconditions.checkNotNull(executor);
        Preconditions.checkNotNull(batchPresentation);
        return executorLogic.getExecutorGroupsCount(user, executor, batchPresentation, isExclude);
    }

    @Override
    public List<Executor> getAllExecutorsFromGroup(User user, Group group) throws ExecutorDoesNotExistException, AuthorizationException,
            AuthenticationException {
        Preconditions.checkNotNull(user);
        Preconditions.checkNotNull(group);
        return executorLogic.getAllExecutorsFromGroup(user, group);
    }

    @Override
    public boolean isExecutorInGroup(User user, Executor executor, Group group) throws ExecutorDoesNotExistException, AuthorizationException,
            AuthenticationException {
        Preconditions.checkNotNull(user);
        Preconditions.checkNotNull(executor);
        Preconditions.checkNotNull(group);
        return executorLogic.isExecutorInGroup(user, executor, group);
    }

    @Override
    public boolean isExecutorExist(User user, String executorName) throws ExecutorDoesNotExistException, AuthorizationException,
            AuthenticationException {
        Preconditions.checkNotNull(user);
        return executorLogic.isExecutorExist(user, executorName);
    }

    @Override
    public Executor getExecutor(User user, Long id) throws ExecutorDoesNotExistException, AuthorizationException, AuthenticationException {
        Preconditions.checkNotNull(user);
        return executorLogic.getExecutor(user, id);
    }

    @Override
    public List<Executor> getExecutors(User user, List<Long> ids) throws ExecutorDoesNotExistException, AuthorizationException,
            AuthenticationException {
        Preconditions.checkNotNull(user);
        Preconditions.checkNotNull(ids);
        return executorLogic.getExecutors(user, ids);
    }

    @Override
    public List<Group> getGroups(User user, List<Long> ids) throws ExecutorDoesNotExistException, AuthorizationException, AuthenticationException {
        Preconditions.checkNotNull(user);
        Preconditions.checkNotNull(ids);
        return executorLogic.getGroups(user, ids);
    }

    @Override
    public Actor getActorByCode(User user, Long code) throws AuthorizationException, AuthenticationException, ExecutorDoesNotExistException {
        Preconditions.checkNotNull(user);
        return executorLogic.getActorByCode(user, code);
    }

    @Override
    public List<Actor> getActorsByCodes(User user, List<Long> codes) throws AuthorizationException, AuthenticationException,
            ExecutorDoesNotExistException {
        Preconditions.checkNotNull(user);
        Preconditions.checkNotNull(codes);
        return executorLogic.getActorsByCodes(user, codes);
    }

    @Override
    public List<Actor> getActorsByExecutorIds(User user, List<Long> executorIds) throws ExecutorDoesNotExistException, AuthorizationException,
            AuthenticationException {
        Preconditions.checkNotNull(user);
        Preconditions.checkNotNull(executorIds);
        return executorLogic.getActorsByExecutorIds(user, executorIds);
    }

    @Override
    public List<Actor> getAvailableActorsByCodes(User user, List<Long> codes) throws AuthorizationException, AuthenticationException,
            ExecutorDoesNotExistException {
        Preconditions.checkNotNull(user);
        Preconditions.checkNotNull(codes);
        return executorLogic.getAvailableActorsByCodes(user, codes);
    }

    @Override
    public List<Actor> getActors(User user, List<Long> ids) throws AuthorizationException, AuthenticationException, ExecutorDoesNotExistException {
        Preconditions.checkNotNull(user);
        Preconditions.checkNotNull(ids);
        return executorLogic.getActors(user, ids);
    }
}
