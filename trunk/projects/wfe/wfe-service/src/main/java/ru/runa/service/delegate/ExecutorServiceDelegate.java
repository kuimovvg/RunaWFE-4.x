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

import javax.security.auth.Subject;

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
    public List<? extends Executor> create(Subject subject, List<? extends Executor> executors) throws ExecutorAlreadyExistsException,
            AuthorizationException, AuthenticationException {
        return getExecutorService().create(subject, executors);
    }

    @Override
    public Actor create(Subject subject, Actor actor) throws ExecutorAlreadyExistsException, AuthorizationException, AuthenticationException {
        return getExecutorService().create(subject, actor);
    }

    @Override
    public Group create(Subject subject, Group group) throws ExecutorAlreadyExistsException, AuthorizationException, AuthenticationException {
        return getExecutorService().create(subject, group);
    }

    @Override
    public void remove(Subject subject, List<Long> ids) throws ExecutorDoesNotExistException, AuthorizationException, AuthenticationException {
        getExecutorService().remove(subject, ids);
    }

    @Override
    public void remove(Subject subject, Executor executor) throws ExecutorDoesNotExistException, AuthorizationException, AuthenticationException {
        getExecutorService().remove(subject, executor);
    }

    @Override
    public Actor update(Subject subject, Actor actor) throws ExecutorAlreadyExistsException, ExecutorDoesNotExistException, AuthorizationException,
            AuthenticationException {
        return getExecutorService().update(subject, actor);
    }

    @Override
    public Group update(Subject subject, Group group) throws ExecutorAlreadyExistsException, ExecutorDoesNotExistException, AuthorizationException,
            AuthenticationException {
        return getExecutorService().update(subject, group);
    }

    @Override
    public List<Executor> getAll(Subject subject, BatchPresentation batchPresentation) throws AuthorizationException, AuthenticationException {
        return getExecutorService().getAll(subject, batchPresentation);
    }

    @Override
    public int getAllCount(Subject subject, BatchPresentation batchPresentation) throws AuthorizationException, AuthenticationException {
        return getExecutorService().getAllCount(subject, batchPresentation);
    }

    @Override
    public List<Actor> getActors(Subject subject, BatchPresentation batchPresentation) throws AuthorizationException, AuthenticationException {
        return getExecutorService().getActors(subject, batchPresentation);
    }

    @Override
    public Actor getActor(Subject subject, String name) throws ExecutorDoesNotExistException, AuthorizationException, AuthenticationException {
        return getExecutorService().getActor(subject, name);
    }

    @Override
    public Actor getActorCaseInsensitive(String login) throws ExecutorDoesNotExistException {
        return getExecutorService().getActorCaseInsensitive(login);
    }

    @Override
    public Group getGroup(Subject subject, String name) throws ExecutorDoesNotExistException, AuthorizationException, AuthenticationException {
        return getExecutorService().getGroup(subject, name);
    }

    @Override
    public List<Group> getGroups(Subject subject, List<Long> ids) throws ExecutorDoesNotExistException, AuthorizationException,
            AuthenticationException {
        return getExecutorService().getGroups(subject, ids);
    }

    @Override
    public Executor getExecutor(Subject subject, String name) throws ExecutorDoesNotExistException, AuthorizationException, AuthenticationException {
        return getExecutorService().getExecutor(subject, name);
    }

    @Override
    public void addExecutorsToGroup(Subject subject, List<? extends Executor> executors, Group group) throws ExecutorAlreadyInGroupException,
            ExecutorDoesNotExistException, AuthorizationException, AuthenticationException {
        getExecutorService().addExecutorsToGroup(subject, executors, group);
    }

    @Override
    public void addExecutorsToGroup(Subject subject, List<Long> executorIds, Long groupId) throws ExecutorAlreadyInGroupException,
            ExecutorDoesNotExistException, AuthorizationException, AuthenticationException {
        getExecutorService().addExecutorsToGroup(subject, executorIds, groupId);
    }

    @Override
    public void addExecutorToGroups(Subject subject, Executor executor, List<Group> groups) throws ExecutorAlreadyInGroupException,
            ExecutorDoesNotExistException, AuthorizationException, AuthenticationException {
        getExecutorService().addExecutorToGroups(subject, executor, groups);
    }

    @Override
    public void addExecutorToGroups(Subject subject, Long executorId, List<Long> groupIds) throws ExecutorAlreadyInGroupException,
            ExecutorDoesNotExistException, AuthorizationException, AuthenticationException {
        getExecutorService().addExecutorToGroups(subject, executorId, groupIds);
    }

    @Override
    public void removeExecutorsFromGroup(Subject subject, List<? extends Executor> executors, Group group) throws ExecutorNotInGroupException,
            ExecutorDoesNotExistException, AuthorizationException, AuthenticationException {
        getExecutorService().removeExecutorsFromGroup(subject, executors, group);
    }

    @Override
    public void removeExecutorsFromGroup(Subject subject, List<Long> executorIds, Long groupId) throws ExecutorNotInGroupException,
            ExecutorDoesNotExistException, AuthorizationException, AuthenticationException {
        getExecutorService().removeExecutorsFromGroup(subject, executorIds, groupId);
    }

    @Override
    public void removeExecutorFromGroups(Subject subject, Executor executor, List<Group> groups) throws ExecutorNotInGroupException,
            ExecutorDoesNotExistException, AuthorizationException, AuthenticationException {
        getExecutorService().removeExecutorFromGroups(subject, executor, groups);
    }

    @Override
    public void removeExecutorFromGroups(Subject subject, Long executorId, List<Long> groupIds) throws ExecutorNotInGroupException,
            ExecutorDoesNotExistException, AuthorizationException, AuthenticationException {
        getExecutorService().removeExecutorFromGroups(subject, executorId, groupIds);
    }

    @Override
    public List<Executor> getGroupChildren(Subject subject, Group group, BatchPresentation batchPresentation, boolean isExclude)
            throws ExecutorDoesNotExistException, AuthorizationException, AuthenticationException {
        return getExecutorService().getGroupChildren(subject, group, batchPresentation, isExclude);
    }

    @Override
    public int getGroupChildrenCount(Subject subject, Group group, BatchPresentation batchPresentation, boolean isExclude)
            throws ExecutorDoesNotExistException, AuthorizationException, AuthenticationException {
        return getExecutorService().getGroupChildrenCount(subject, group, batchPresentation, isExclude);
    }

    @Override
    public List<Actor> getGroupActors(Subject subject, Group group) throws AuthenticationException, AuthorizationException,
            ExecutorDoesNotExistException {
        return getExecutorService().getGroupActors(subject, group);
    }

    @Override
    public List<Group> getExecutorGroups(Subject subject, Executor executor, BatchPresentation batchPresentation, boolean isExclude)
            throws ExecutorDoesNotExistException, AuthorizationException, AuthenticationException {
        return getExecutorService().getExecutorGroups(subject, executor, batchPresentation, isExclude);
    }

    @Override
    public int getExecutorGroupsCount(Subject subject, Executor executor, BatchPresentation batchPresentation, boolean isExclude)
            throws ExecutorDoesNotExistException, AuthorizationException, AuthenticationException {
        return getExecutorService().getExecutorGroupsCount(subject, executor, batchPresentation, isExclude);
    }

    @Override
    public void setPassword(Subject subject, Actor actor, String password) throws ExecutorDoesNotExistException, AuthorizationException,
            AuthenticationException, WeakPasswordException {
        getExecutorService().setPassword(subject, actor, password);
    }

    @Override
    public void setStatus(Subject subject, Long actorId, boolean isActive) throws AuthorizationException, AuthenticationException,
            ExecutorDoesNotExistException {
        getExecutorService().setStatus(subject, actorId, isActive);
    }

    @Override
    public Actor getActor(Subject subject, Long id) throws ExecutorDoesNotExistException, AuthorizationException, AuthenticationException {
        return getExecutorService().getActor(subject, id);
    }

    @Override
    public Group getGroup(Subject subject, Long id) throws ExecutorDoesNotExistException, AuthorizationException, AuthenticationException {
        return getExecutorService().getGroup(subject, id);
    }

    @Override
    public Executor getExecutor(Subject subject, Long id) throws ExecutorDoesNotExistException, AuthorizationException, AuthenticationException {
        return getExecutorService().getExecutor(subject, id);
    }

    @Override
    public List<Executor> getExecutors(Subject subject, List<Long> ids) throws ExecutorDoesNotExistException, AuthorizationException,
            AuthenticationException {
        return getExecutorService().getExecutors(subject, ids);
    }

    @Override
    public Actor getActorByCode(Subject subject, Long code) throws AuthorizationException, AuthenticationException, ExecutorDoesNotExistException {
        return getExecutorService().getActorByCode(subject, code);
    }

    @Override
    public boolean isExecutorInGroup(Subject subject, Executor executor, Group group) throws AuthorizationException, AuthenticationException,
            ExecutorDoesNotExistException {
        return getExecutorService().isExecutorInGroup(subject, executor, group);
    }

    @Override
    public boolean isExecutorExist(Subject subject, String executorName) throws AuthorizationException, AuthenticationException,
            ExecutorDoesNotExistException {
        return getExecutorService().isExecutorExist(subject, executorName);
    }

    @Override
    public List<Actor> getActorsByCodes(Subject subject, List<Long> codes) throws AuthorizationException, AuthenticationException,
            ExecutorDoesNotExistException {
        return getExecutorService().getActorsByCodes(subject, codes);
    }

    @Override
    public List<Actor> getAvailableActorsByCodes(Subject subject, List<Long> codes) throws AuthorizationException, AuthenticationException,
            ExecutorDoesNotExistException {
        return getExecutorService().getAvailableActorsByCodes(subject, codes);
    }

    @Override
    public List<Actor> getActorsByExecutorIds(Subject subject, List<Long> executorIds) throws ExecutorDoesNotExistException, AuthorizationException,
            AuthenticationException {
        return getExecutorService().getActorsByExecutorIds(subject, executorIds);
    }

    @Override
    public List<Actor> getActors(Subject subject, List<Long> ids) throws AuthorizationException, AuthenticationException,
            ExecutorDoesNotExistException {
        return getExecutorService().getActors(subject, ids);
    }

    @Override
    public List<Executor> getAllExecutorsFromGroup(Subject subject, Group group) throws ExecutorDoesNotExistException, AuthorizationException,
            AuthenticationException {
        return getExecutorService().getAllExecutorsFromGroup(subject, group);
    }

}
