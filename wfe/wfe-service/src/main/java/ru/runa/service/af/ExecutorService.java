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
package ru.runa.service.af;

import java.util.List;

import javax.security.auth.Subject;

import ru.runa.wfe.presentation.BatchPresentation;
import ru.runa.wfe.security.WeakPasswordException;
import ru.runa.wfe.user.Actor;
import ru.runa.wfe.user.Executor;
import ru.runa.wfe.user.ExecutorAlreadyExistsException;
import ru.runa.wfe.user.ExecutorAlreadyInGroupException;
import ru.runa.wfe.user.ExecutorDoesNotExistException;
import ru.runa.wfe.user.ExecutorNotInGroupException;
import ru.runa.wfe.user.Group;
import ru.runa.wfe.user.User;

/**
 * Responsible for managing {@link Executor}s Created on 07.07.2004
 */
public interface ExecutorService {

    public <T extends Executor> T create(User user, T executor) throws ExecutorAlreadyExistsException;

    public List<Executor> getAll(User user, BatchPresentation batchPresentation);

    public int getAllCount(User user, BatchPresentation batchPresentation);

    public List<Executor> getExecutors(User user, List<Long> ids) throws ExecutorDoesNotExistException;

    public List<Actor> getActors(User user, BatchPresentation batchPresentation);

    public <T extends Executor> T getExecutor(User user, Long id) throws ExecutorDoesNotExistException;

    public <T extends Executor> T getExecutor(User user, String name) throws ExecutorDoesNotExistException;

    public void update(User user, Executor executor) throws ExecutorAlreadyExistsException, ExecutorDoesNotExistException;

    public void remove(User user, Executor executor) throws ExecutorDoesNotExistException;

    public void remove(User user, List<Long> ids) throws ExecutorDoesNotExistException;

    public Actor getActorCaseInsensitive(String login) throws ExecutorDoesNotExistException;

    public List<Group> getGroups(User user, List<Long> ids) throws ExecutorDoesNotExistException;

    public List<Actor> getActorsByCodes(User user, List<Long> codes) throws ExecutorDoesNotExistException;

    public List<Actor> getActors(User user, List<Long> ids) throws ExecutorDoesNotExistException;

    public List<Actor> getAvailableActorsByCodes(User user, List<Long> codes) throws ExecutorDoesNotExistException;

    public void addExecutorsToGroup(User user, List<? extends Executor> executors, Group group) throws ExecutorDoesNotExistException,
            ExecutorAlreadyInGroupException;

    public void addExecutorsToGroup(User user, List<Long> executorIds, Long groupId) throws ExecutorDoesNotExistException,
            ExecutorAlreadyInGroupException;

    public void addExecutorToGroups(User user, Executor executor, List<Group> groups) throws ExecutorDoesNotExistException,
            ExecutorAlreadyInGroupException;

    public void addExecutorToGroups(User user, Long executorId, List<Long> groupIds) throws ExecutorDoesNotExistException,
            ExecutorAlreadyInGroupException;

    public void removeExecutorsFromGroup(User user, List<? extends Executor> executors, Group group) throws ExecutorDoesNotExistException,
            ExecutorNotInGroupException;

    public void removeExecutorsFromGroup(User user, List<Long> executorIds, Long groupId) throws ExecutorDoesNotExistException,
            ExecutorNotInGroupException;

    public void removeExecutorFromGroups(User user, Executor executor, List<Group> groups) throws ExecutorDoesNotExistException,
            ExecutorNotInGroupException;

    public void removeExecutorFromGroups(User user, Long executorId, List<Long> groupIds) throws ExecutorDoesNotExistException,
            ExecutorNotInGroupException;

    /**
     * Returns an array of executors from group.
     * <p>
     * For example G1 contains G2 and A0, G2 contins A1 and A2. In this case:
     * Only actor (non-group) executors are returned.
     * </p>
     * <code> getAllNonGroupExecutorsFromGroup(G2) returns {A1, A2}</code>;
     * <code> getAllNonGroupExecutorsFromGroup(G1) returns {A0} </code>
     * 
     * @param group
     *            a group
     * @return an array of executors from group.
     */
    public List<Executor> getAllExecutorsFromGroup(User user, Group group) throws ExecutorDoesNotExistException;

    /**
     * Loads first level group children's (not recursive). <b>Paging is enabled
     * on executors loading.</b>
     * 
     * @param subject
     *            Current actor {@linkplain Subject}.
     * @param group
     *            {@linkplain Group} to load children's from.
     * @param batchPresentation
     *            {@linkplain BatchPresentation} for loading executors.
     * @param isExclude
     *            Flag, equals true, if must be loaded executors, which not yet
     *            group children's; false to load group children's.
     * @return Array of loaded executors.
     */
    public List<Executor> getGroupChildren(User user, Group group, BatchPresentation batchPresentation, boolean isExclude)
            throws ExecutorDoesNotExistException;

    /**
     * Loads first level group children's count.
     * 
     * @param subject
     *            Current actor {@linkplain Subject}.
     * @param group
     *            {@linkplain Group} to load children's from.
     * @param batchPresentation
     *            {@linkplain BatchPresentation} for loading executors.
     * @param isExclude
     *            Flag, equals true, if must be loaded executors, which not yet
     *            group children's; false to load group children's.
     * @return Executors count.
     */
    public int getGroupChildrenCount(User user, Group group, BatchPresentation batchPresentation, boolean isExclude)
            throws ExecutorDoesNotExistException;

    /**
     * Returns actors from group and all subgroups recursive.
     * <p>
     * For example G1 contains G2, A3, G2 contains A1 and A2. In this case:
     * </p>
     * <code> getGroupActors(G2) returns {A1, A2}</code>;
     * <code> getGroupActors(G1) returns {A1, A2, A3} </code>
     * 
     * @param subject
     *            Actor subject, performing action.
     * @param group
     *            Group, to load actors from.
     * @return All actors from group.
     */
    public List<Actor> getGroupActors(User user, Group group) throws ExecutorDoesNotExistException;

    /**
     * Load first level executor groups (not recursive). <b>Paging is enabled on
     * executors loading.</b>
     * 
     * @param subject
     *            Current actor {@linkplain Subject}.
     * @param executor
     *            {@linkplain Executor} to load groups.
     * @param batchPresentation
     *            {@linkplain BatchPresentation} to load groups.
     * @param isExclude
     *            Flag, equals true, if must be loaded groups, which not yet
     *            contains executor; false to load groups, which contains
     *            executor.
     * @return Array of loaded groups.
     */
    public List<Group> getExecutorGroups(User user, Executor executor, BatchPresentation batchPresentation, boolean isExclude)
            throws ExecutorDoesNotExistException;

    /**
     * Load first level executor groups (not recursive) count.
     * 
     * @param subject
     *            Current actor {@linkplain Subject}.
     * @param executor
     *            {@linkplain Executor} to load groups.
     * @param batchPresentation
     *            {@linkplain BatchPresentation} to load groups.
     * @param isExclude
     *            Flag, equals true, if must be loaded groups, which not yet
     *            contains executor; false to load groups, which contains
     *            executor.
     * @return Groups count.
     */
    public int getExecutorGroupsCount(User user, Executor executor, BatchPresentation batchPresentation, boolean isExclude)
            throws ExecutorDoesNotExistException;

    public boolean isExecutorInGroup(User user, Executor executor, Group group) throws ExecutorDoesNotExistException;

    public boolean isExecutorExist(User user, String executorName) throws ExecutorDoesNotExistException;

    public void setPassword(User user, Actor actor, String password) throws ExecutorDoesNotExistException, WeakPasswordException;

    public void setStatus(User user, Long actorId, boolean isActive) throws ExecutorDoesNotExistException;

    public Actor getActorByCode(User user, Long code) throws ExecutorDoesNotExistException;

    public List<Actor> getActorsByExecutorIds(User user, List<Long> executorIds) throws ExecutorDoesNotExistException;
}
