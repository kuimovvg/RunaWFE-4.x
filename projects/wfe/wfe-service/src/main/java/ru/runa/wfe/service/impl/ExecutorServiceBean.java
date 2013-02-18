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
package ru.runa.wfe.service.impl;

import java.util.List;

import javax.ejb.Stateless;
import javax.ejb.TransactionManagement;
import javax.ejb.TransactionManagementType;
import javax.interceptor.Interceptors;
import javax.jws.WebService;
import javax.jws.soap.SOAPBinding;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.ejb.interceptor.SpringBeanAutowiringInterceptor;

import ru.runa.wfe.presentation.BatchPresentation;
import ru.runa.wfe.presentation.BatchPresentationFactory;
import ru.runa.wfe.service.decl.ExecutorServiceLocal;
import ru.runa.wfe.service.decl.ExecutorServiceRemote;
import ru.runa.wfe.service.interceptors.EjbExceptionSupport;
import ru.runa.wfe.service.interceptors.EjbTransactionSupport;
import ru.runa.wfe.user.Actor;
import ru.runa.wfe.user.Executor;
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
@WebService(name = "ExecutorAPI", serviceName = "ExecutorWebService")
@SOAPBinding
public class ExecutorServiceBean implements ExecutorServiceLocal, ExecutorServiceRemote {
    @Autowired
    private ExecutorLogic executorLogic;

    @Override
    public void update(User user, Executor executor) {
        Preconditions.checkNotNull(user);
        Preconditions.checkNotNull(executor);
        executorLogic.update(user, executor);
    }

    @Override
    public List<? extends Executor> getExecutors(User user, BatchPresentation batchPresentation) {
        Preconditions.checkNotNull(user);
        if (batchPresentation == null) {
            batchPresentation = BatchPresentationFactory.EXECUTORS.createDefault();
        }
        return executorLogic.getExecutors(user, batchPresentation);
    }

    @Override
    public int getExecutorsCount(User user, BatchPresentation batchPresentation) {
        Preconditions.checkNotNull(user);
        if (batchPresentation == null) {
            batchPresentation = BatchPresentationFactory.EXECUTORS.createDefault();
        }
        return executorLogic.getExecutorsCount(user, batchPresentation);
    }

    @Override
    public Actor getActorCaseInsensitive(String login) {
        Preconditions.checkNotNull(login);
        return executorLogic.getActorCaseInsensitive(login);
    }

    @Override
    public Executor getExecutorByName(User user, String name) {
        Preconditions.checkNotNull(user);
        return executorLogic.getExecutor(user, name);
    }

    @Override
    public void remove(User user, List<Long> ids) {
        Preconditions.checkNotNull(user);
        Preconditions.checkNotNull(ids);
        executorLogic.remove(user, ids);
    }

    @Override
    public <T extends Executor> T create(User user, T executor) {
        Preconditions.checkNotNull(user);
        Preconditions.checkNotNull(executor);
        return executorLogic.create(user, executor);
    }

    @Override
    public void addExecutorsToGroup(User user, List<Long> executorIds, Long groupId) {
        Preconditions.checkNotNull(user);
        Preconditions.checkNotNull(executorIds);
        executorLogic.addExecutorsToGroup(user, executorIds, groupId);
    }

    @Override
    public void addExecutorToGroups(User user, Long executorId, List<Long> groupIds) {
        Preconditions.checkNotNull(user);
        Preconditions.checkNotNull(groupIds);
        executorLogic.addExecutorToGroups(user, executorId, groupIds);
    }

    @Override
    public List<Executor> getGroupChildren(User user, Group group, BatchPresentation batchPresentation, boolean isExclude) {
        Preconditions.checkNotNull(user);
        Preconditions.checkNotNull(group);
        if (batchPresentation == null) {
            batchPresentation = BatchPresentationFactory.EXECUTORS.createDefault();
        }
        return executorLogic.getGroupChildren(user, group, batchPresentation, isExclude);
    }

    @Override
    public int getGroupChildrenCount(User user, Group group, BatchPresentation batchPresentation, boolean isExclude) {
        Preconditions.checkNotNull(user);
        Preconditions.checkNotNull(group);
        if (batchPresentation == null) {
            batchPresentation = BatchPresentationFactory.EXECUTORS.createDefault();
        }
        return executorLogic.getGroupChildrenCount(user, group, batchPresentation, isExclude);
    }

    @Override
    public List<Actor> getGroupActors(User user, Group group) {
        Preconditions.checkNotNull(user);
        Preconditions.checkNotNull(group);
        return executorLogic.getGroupActors(user, group);
    }

    @Override
    public void removeExecutorsFromGroup(User user, List<Long> executorIds, Long groupId) {
        Preconditions.checkNotNull(user);
        Preconditions.checkNotNull(executorIds);
        executorLogic.removeExecutorsFromGroup(user, executorIds, groupId);
    }

    @Override
    public void removeExecutorFromGroups(User user, Long executorId, List<Long> groupIds) {
        Preconditions.checkNotNull(user);
        Preconditions.checkNotNull(groupIds);
        executorLogic.removeExecutorFromGroups(user, executorId, groupIds);
    }

    @Override
    public void setPassword(User user, Actor actor, String password) {
        Preconditions.checkNotNull(user);
        Preconditions.checkNotNull(actor);
        executorLogic.setPassword(user, actor, password);
    }

    @Override
    public void setStatus(User user, Actor actor, boolean isActive) {
        Preconditions.checkNotNull(user);
        executorLogic.setStatus(user, actor, isActive);
    }

    @Override
    public List<Group> getExecutorGroups(User user, Executor executor, BatchPresentation batchPresentation, boolean isExclude) {
        Preconditions.checkNotNull(user);
        Preconditions.checkNotNull(executor);
        if (batchPresentation == null) {
            batchPresentation = BatchPresentationFactory.GROUPS.createDefault();
        }
        return executorLogic.getExecutorGroups(user, executor, batchPresentation, isExclude);
    }

    @Override
    public int getExecutorGroupsCount(User user, Executor executor, BatchPresentation batchPresentation, boolean isExclude) {
        Preconditions.checkNotNull(user);
        Preconditions.checkNotNull(executor);
        if (batchPresentation == null) {
            batchPresentation = BatchPresentationFactory.GROUPS.createDefault();
        }
        return executorLogic.getExecutorGroupsCount(user, executor, batchPresentation, isExclude);
    }

    @Override
    public List<Executor> getAllExecutorsFromGroup(User user, Group group) {
        Preconditions.checkNotNull(user);
        Preconditions.checkNotNull(group);
        return executorLogic.getAllExecutorsFromGroup(user, group);
    }

    @Override
    public boolean isExecutorInGroup(User user, Executor executor, Group group) {
        Preconditions.checkNotNull(user);
        Preconditions.checkNotNull(executor);
        Preconditions.checkNotNull(group);
        return executorLogic.isExecutorInGroup(user, executor, group);
    }

    @Override
    public boolean isExecutorExist(User user, String executorName) {
        Preconditions.checkNotNull(user);
        return executorLogic.isExecutorExist(user, executorName);
    }

    @Override
    public Executor getExecutor(User user, Long id) {
        Preconditions.checkNotNull(user);
        return executorLogic.getExecutor(user, id);
    }

    @Override
    public Actor getActorByCode(User user, Long code) {
        Preconditions.checkNotNull(user);
        return executorLogic.getActorByCode(user, code);
    }

}
