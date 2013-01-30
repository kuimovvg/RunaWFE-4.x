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
    public <T extends Executor> T create(User user, T executor) throws ExecutorAlreadyExistsException, AuthorizationException,
            AuthenticationException {
        return getExecutorService().create(user, executor);
    }

    @Override
    public void remove(User user, List<Long> ids) throws ExecutorDoesNotExistException, AuthorizationException, AuthenticationException {
        getExecutorService().remove(user, ids);
    }

    @Override
    public void remove(User user, Executor executor) throws ExecutorDoesNotExistException, AuthorizationException, AuthenticationException {
        getExecutorService().remove(user, executor);
    }

    @Override
    public void update(User user, Executor executor) throws ExecutorAlreadyExistsException, ExecutorDoesNotExistException, AuthorizationException {
        getExecutorService().update(user, executor);
    }

    @Override
    public List<Executor> getAll(User user, BatchPresentation batchPresentation) throws AuthorizationException, AuthenticationException {
        return getExecutorService().getAll(user, batchPresentation);
    }

    @Override
    public int getAllCount(User user, BatchPresentation batchPresentation) throws AuthorizationException, AuthenticationException {
        return getExecutorService().getAllCount(user, batchPresentation);
    }

    @Override
    public List<Actor> getActors(User user, BatchPresentation batchPresentation) throws AuthorizationException, AuthenticationException {
        return getExecutorService().getActors(user, batchPresentation);
    }

    @Override
    public Actor getActorCaseInsensitive(String login) throws ExecutorDoesNotExistException {
        return getExecutorService().getActorCaseInsensitive(login);
    }

    @Override
    public List<Group> getGroups(User user, List<Long> ids) throws ExecutorDoesNotExistException, AuthorizationException, AuthenticationException {
        return getExecutorService().getGroups(user, ids);
    }

    @Override
    public <T extends Executor> T getExecutor(User user, String name) throws ExecutorDoesNotExistException, AuthorizationException,
            AuthenticationException {
        return (T) getExecutorService().getExecutor(user, name);
    }

    @Override
    public void addExecutorsToGroup(User user, List<? extends Executor> executors, Group group) throws ExecutorAlreadyInGroupException,
            ExecutorDoesNotExistException, AuthorizationException, AuthenticationException {
        getExecutorService().addExecutorsToGroup(user, executors, group);
    }

    @Override
    public void addExecutorsToGroup(User user, List<Long> executorIds, Long groupId) throws ExecutorAlreadyInGroupException,
            ExecutorDoesNotExistException, AuthorizationException, AuthenticationException {
        getExecutorService().addExecutorsToGroup(user, executorIds, groupId);
    }

    @Override
    public void addExecutorToGroups(User user, Executor executor, List<Group> groups) throws ExecutorAlreadyInGroupException,
            ExecutorDoesNotExistException, AuthorizationException, AuthenticationException {
        getExecutorService().addExecutorToGroups(user, executor, groups);
    }

    @Override
    public void addExecutorToGroups(User user, Long executorId, List<Long> groupIds) throws ExecutorAlreadyInGroupException,
            ExecutorDoesNotExistException, AuthorizationException, AuthenticationException {
        getExecutorService().addExecutorToGroups(user, executorId, groupIds);
    }

    @Override
    public void removeExecutorsFromGroup(User user, List<? extends Executor> executors, Group group) throws ExecutorNotInGroupException,
            ExecutorDoesNotExistException, AuthorizationException, AuthenticationException {
        getExecutorService().removeExecutorsFromGroup(user, executors, group);
    }

    @Override
    public void removeExecutorsFromGroup(User user, List<Long> executorIds, Long groupId) throws ExecutorNotInGroupException,
            ExecutorDoesNotExistException, AuthorizationException, AuthenticationException {
        getExecutorService().removeExecutorsFromGroup(user, executorIds, groupId);
    }

    @Override
    public void removeExecutorFromGroups(User user, Executor executor, List<Group> groups) throws ExecutorNotInGroupException,
            ExecutorDoesNotExistException, AuthorizationException, AuthenticationException {
        getExecutorService().removeExecutorFromGroups(user, executor, groups);
    }

    @Override
    public void removeExecutorFromGroups(User user, Long executorId, List<Long> groupIds) throws ExecutorNotInGroupException,
            ExecutorDoesNotExistException, AuthorizationException, AuthenticationException {
        getExecutorService().removeExecutorFromGroups(user, executorId, groupIds);
    }

    @Override
    public List<Executor> getGroupChildren(User user, Group group, BatchPresentation batchPresentation, boolean isExclude)
            throws ExecutorDoesNotExistException, AuthorizationException, AuthenticationException {
        return getExecutorService().getGroupChildren(user, group, batchPresentation, isExclude);
    }

    @Override
    public int getGroupChildrenCount(User user, Group group, BatchPresentation batchPresentation, boolean isExclude)
            throws ExecutorDoesNotExistException, AuthorizationException, AuthenticationException {
        return getExecutorService().getGroupChildrenCount(user, group, batchPresentation, isExclude);
    }

    @Override
    public List<Actor> getGroupActors(User user, Group group) throws AuthenticationException, AuthorizationException, ExecutorDoesNotExistException {
        return getExecutorService().getGroupActors(user, group);
    }

    @Override
    public List<Group> getExecutorGroups(User user, Executor executor, BatchPresentation batchPresentation, boolean isExclude)
            throws ExecutorDoesNotExistException, AuthorizationException, AuthenticationException {
        return getExecutorService().getExecutorGroups(user, executor, batchPresentation, isExclude);
    }

    @Override
    public int getExecutorGroupsCount(User user, Executor executor, BatchPresentation batchPresentation, boolean isExclude)
            throws ExecutorDoesNotExistException, AuthorizationException, AuthenticationException {
        return getExecutorService().getExecutorGroupsCount(user, executor, batchPresentation, isExclude);
    }

    @Override
    public void setPassword(User user, Actor actor, String password) throws ExecutorDoesNotExistException, AuthorizationException,
            AuthenticationException, WeakPasswordException {
        getExecutorService().setPassword(user, actor, password);
    }

    @Override
    public void setStatus(User user, Long actorId, boolean isActive) throws AuthorizationException, AuthenticationException,
            ExecutorDoesNotExistException {
        getExecutorService().setStatus(user, actorId, isActive);
    }

    @Override
    public <T extends Executor> T getExecutor(User user, Long id) throws ExecutorDoesNotExistException, AuthorizationException,
            AuthenticationException {
        return (T) getExecutorService().getExecutor(user, id);
    }

    @Override
    public List<Executor> getExecutors(User user, List<Long> ids) throws ExecutorDoesNotExistException, AuthorizationException,
            AuthenticationException {
        return getExecutorService().getExecutors(user, ids);
    }

    @Override
    public Actor getActorByCode(User user, Long code) throws AuthorizationException, AuthenticationException, ExecutorDoesNotExistException {
        return getExecutorService().getActorByCode(user, code);
    }

    @Override
    public boolean isExecutorInGroup(User user, Executor executor, Group group) throws AuthorizationException, AuthenticationException,
            ExecutorDoesNotExistException {
        return getExecutorService().isExecutorInGroup(user, executor, group);
    }

    @Override
    public boolean isExecutorExist(User user, String executorName) throws AuthorizationException, AuthenticationException,
            ExecutorDoesNotExistException {
        return getExecutorService().isExecutorExist(user, executorName);
    }

    @Override
    public List<Actor> getActorsByCodes(User user, List<Long> codes) throws AuthorizationException, AuthenticationException,
            ExecutorDoesNotExistException {
        return getExecutorService().getActorsByCodes(user, codes);
    }

    @Override
    public List<Actor> getAvailableActorsByCodes(User user, List<Long> codes) throws AuthorizationException, AuthenticationException,
            ExecutorDoesNotExistException {
        return getExecutorService().getAvailableActorsByCodes(user, codes);
    }

    @Override
    public List<Actor> getActorsByExecutorIds(User user, List<Long> executorIds) throws ExecutorDoesNotExistException, AuthorizationException,
            AuthenticationException {
        return getExecutorService().getActorsByExecutorIds(user, executorIds);
    }

    @Override
    public List<Actor> getActors(User user, List<Long> ids) throws AuthorizationException, AuthenticationException, ExecutorDoesNotExistException {
        return getExecutorService().getActors(user, ids);
    }

    @Override
    public List<Executor> getAllExecutorsFromGroup(User user, Group group) throws ExecutorDoesNotExistException, AuthorizationException,
            AuthenticationException {
        return getExecutorService().getAllExecutorsFromGroup(user, group);
    }

}
