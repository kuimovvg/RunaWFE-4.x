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
package ru.runa.service.delegate;

import java.util.List;

import ru.runa.service.af.ExecutorService;
import ru.runa.wfe.presentation.BatchPresentation;
import ru.runa.wfe.user.Actor;
import ru.runa.wfe.user.Executor;
import ru.runa.wfe.user.Group;
import ru.runa.wfe.user.User;

/*
 * Created on 10.08.2004
 */
public class ExecutorServiceDelegate extends EJB3Delegate implements ExecutorService {

    public ExecutorServiceDelegate() {
        super(ExecutorService.class);
    }

    private ExecutorService getExecutorService() {
        return (ExecutorService) getService();
    }

    @Override
    public <T extends Executor> T create(User user, T executor) {
        return getExecutorService().create(user, executor);
    }

    @Override
    public void remove(User user, List<Long> ids) {
        getExecutorService().remove(user, ids);
    }

    @Override
    public void remove(User user, Executor executor) {
        getExecutorService().remove(user, executor);
    }

    @Override
    public void update(User user, Executor executor) {
        getExecutorService().update(user, executor);
    }

    @Override
    public List<Executor> getAll(User user, BatchPresentation batchPresentation) {
        return getExecutorService().getAll(user, batchPresentation);
    }

    @Override
    public int getAllCount(User user, BatchPresentation batchPresentation) {
        return getExecutorService().getAllCount(user, batchPresentation);
    }

    @Override
    public List<Actor> getActors(User user, BatchPresentation batchPresentation) {
        return getExecutorService().getActors(user, batchPresentation);
    }

    @Override
    public Actor getActorCaseInsensitive(String login) {
        return getExecutorService().getActorCaseInsensitive(login);
    }

    @Override
    public List<Group> getGroups(User user, List<Long> ids) {
        return getExecutorService().getGroups(user, ids);
    }

    @Override
    public <T extends Executor> T getExecutor(User user, String name) {
        return (T) getExecutorService().getExecutor(user, name);
    }

    @Override
    public void addExecutorsToGroup(User user, List<? extends Executor> executors, Group group) {
        getExecutorService().addExecutorsToGroup(user, executors, group);
    }

    @Override
    public void addExecutorsToGroup(User user, List<Long> executorIds, Long groupId) {
        getExecutorService().addExecutorsToGroup(user, executorIds, groupId);
    }

    @Override
    public void addExecutorToGroups(User user, Executor executor, List<Group> groups) {
        getExecutorService().addExecutorToGroups(user, executor, groups);
    }

    @Override
    public void addExecutorToGroups(User user, Long executorId, List<Long> groupIds) {
        getExecutorService().addExecutorToGroups(user, executorId, groupIds);
    }

    @Override
    public void removeExecutorsFromGroup(User user, List<? extends Executor> executors, Group group) {
        getExecutorService().removeExecutorsFromGroup(user, executors, group);
    }

    @Override
    public void removeExecutorsFromGroup(User user, List<Long> executorIds, Long groupId) {
        getExecutorService().removeExecutorsFromGroup(user, executorIds, groupId);
    }

    @Override
    public void removeExecutorFromGroups(User user, Executor executor, List<Group> groups) {
        getExecutorService().removeExecutorFromGroups(user, executor, groups);
    }

    @Override
    public void removeExecutorFromGroups(User user, Long executorId, List<Long> groupIds) {
        getExecutorService().removeExecutorFromGroups(user, executorId, groupIds);
    }

    @Override
    public List<Executor> getGroupChildren(User user, Group group, BatchPresentation batchPresentation, boolean isExclude) {
        return getExecutorService().getGroupChildren(user, group, batchPresentation, isExclude);
    }

    @Override
    public int getGroupChildrenCount(User user, Group group, BatchPresentation batchPresentation, boolean isExclude) {
        return getExecutorService().getGroupChildrenCount(user, group, batchPresentation, isExclude);
    }

    @Override
    public List<Actor> getGroupActors(User user, Group group) {
        return getExecutorService().getGroupActors(user, group);
    }

    @Override
    public List<Group> getExecutorGroups(User user, Executor executor, BatchPresentation batchPresentation, boolean isExclude) {
        return getExecutorService().getExecutorGroups(user, executor, batchPresentation, isExclude);
    }

    @Override
    public int getExecutorGroupsCount(User user, Executor executor, BatchPresentation batchPresentation, boolean isExclude) {
        return getExecutorService().getExecutorGroupsCount(user, executor, batchPresentation, isExclude);
    }

    @Override
    public void setPassword(User user, Actor actor, String password) {
        getExecutorService().setPassword(user, actor, password);
    }

    @Override
    public void setStatus(User user, Actor actor, boolean isActive) {
        getExecutorService().setStatus(user, actor, isActive);
    }

    @Override
    public <T extends Executor> T getExecutor(User user, Long id) {
        return (T) getExecutorService().getExecutor(user, id);
    }

    @Override
    public List<Executor> getExecutors(User user, List<Long> ids) {
        return getExecutorService().getExecutors(user, ids);
    }

    @Override
    public Actor getActorByCode(User user, Long code) {
        return getExecutorService().getActorByCode(user, code);
    }

    @Override
    public boolean isExecutorInGroup(User user, Executor executor, Group group) {
        return getExecutorService().isExecutorInGroup(user, executor, group);
    }

    @Override
    public boolean isExecutorExist(User user, String executorName) {
        return getExecutorService().isExecutorExist(user, executorName);
    }

    @Override
    public List<Actor> getActorsByCodes(User user, List<Long> codes) {
        return getExecutorService().getActorsByCodes(user, codes);
    }

    @Override
    public List<Actor> getAvailableActorsByCodes(User user, List<Long> codes) {
        return getExecutorService().getAvailableActorsByCodes(user, codes);
    }

    @Override
    public List<Actor> getActorsByExecutorIds(User user, List<Long> executorIds) {
        return getExecutorService().getActorsByExecutorIds(user, executorIds);
    }

    @Override
    public List<Actor> getActors(User user, List<Long> ids) {
        return getExecutorService().getActors(user, ids);
    }

    @Override
    public List<Executor> getAllExecutorsFromGroup(User user, Group group) {
        return getExecutorService().getAllExecutorsFromGroup(user, group);
    }

}
