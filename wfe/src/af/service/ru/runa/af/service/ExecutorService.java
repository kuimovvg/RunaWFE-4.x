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
package ru.runa.af.service;

import java.util.List;

import javax.security.auth.Subject;

import ru.runa.af.Actor;
import ru.runa.af.AuthenticationException;
import ru.runa.af.AuthorizationException;
import ru.runa.af.Executor;
import ru.runa.af.ExecutorAlreadyExistsException;
import ru.runa.af.ExecutorAlreadyInGroupException;
import ru.runa.af.ExecutorNotInGroupException;
import ru.runa.af.ExecutorOutOfDateException;
import ru.runa.af.Group;
import ru.runa.af.WeakPasswordException;
import ru.runa.af.presentation.BatchPresentation;

/**
 * Responsible for managing {@link Executor}s Created on 07.07.2004
 */
public interface ExecutorService {

    public List<? extends Executor> create(Subject subject, List<? extends Executor> executors) throws ExecutorAlreadyExistsException, AuthorizationException,
            AuthenticationException;

    public Actor create(Subject subject, Actor actor) throws AuthorizationException, AuthenticationException, ExecutorAlreadyExistsException;

    public Group create(Subject subject, Group group) throws AuthorizationException, AuthenticationException, ExecutorAlreadyExistsException;

    public void remove(Subject subject, Executor executor) throws AuthorizationException, AuthenticationException, ExecutorOutOfDateException;

    public void remove(Subject subject, List<Long> ids) throws AuthorizationException, AuthenticationException, ExecutorOutOfDateException;

    public Actor update(Subject subject, Actor actor, Actor newActor) throws AuthorizationException, AuthenticationException,
            ExecutorAlreadyExistsException, ExecutorOutOfDateException;

    public Group update(Subject subject, Group group, Group newGroup) throws AuthorizationException, AuthenticationException,
            ExecutorAlreadyExistsException, ExecutorOutOfDateException;

    public List<Executor> getAll(Subject subject, BatchPresentation batchPresentation) throws AuthorizationException, AuthenticationException;

    public int getAllCount(Subject subject, BatchPresentation batchPresentation) throws AuthorizationException, AuthenticationException;

    public List<Actor> getActors(Subject subject, BatchPresentation batchPresentation) throws AuthorizationException, AuthenticationException;

    public Actor getActor(Subject subject, String name) throws AuthorizationException, AuthenticationException, ExecutorOutOfDateException;

    public Group getGroup(Subject subject, String name) throws AuthorizationException, AuthenticationException, ExecutorOutOfDateException;

    public Executor getExecutor(Subject subject, String name) throws AuthorizationException, AuthenticationException, ExecutorOutOfDateException;

    public Executor getExecutor(Subject subject, Long id) throws AuthorizationException, AuthenticationException, ExecutorOutOfDateException;

    public List<Executor> getExecutors(Subject subject, List<Long> ids) throws AuthorizationException, AuthenticationException, ExecutorOutOfDateException;

    public Actor getActor(Subject subject, Long id) throws AuthorizationException, AuthenticationException, ExecutorOutOfDateException;

    public Actor getActorCaseInsensitive(String login) throws ExecutorOutOfDateException;
    
    public Group getGroup(Subject subject, Long id) throws AuthorizationException, AuthenticationException, ExecutorOutOfDateException;

    public List<Group> getGroups(Subject subject, List<Long> ids) throws AuthorizationException, AuthenticationException, ExecutorOutOfDateException;

    public List<Actor> getActorsByCodes(Subject subject, List<Long> codes) throws AuthorizationException, AuthenticationException, ExecutorOutOfDateException;

    public List<Actor> getActors(Subject subject, List<Long> ids) throws AuthorizationException, AuthenticationException, ExecutorOutOfDateException;

    public List<Actor> getAvailableActorsByCodes(Subject subject, List<Long> codes) throws AuthorizationException, AuthenticationException,
            ExecutorOutOfDateException;

    public void addExecutorsToGroup(Subject subject, List<? extends Executor> executors, Group group) throws AuthorizationException, AuthenticationException,
            ExecutorOutOfDateException, ExecutorAlreadyInGroupException;

    public void addExecutorsToGroup(Subject subject, List<Long> executorIds, Long groupId) throws AuthorizationException, AuthenticationException,
            ExecutorOutOfDateException, ExecutorAlreadyInGroupException;

    public void addExecutorToGroups(Subject subject, Executor executor, List<Group> groups) throws AuthorizationException, AuthenticationException,
            ExecutorOutOfDateException, ExecutorAlreadyInGroupException;

    public void addExecutorToGroups(Subject subject, Long executorId, List<Long> groupIds) throws AuthorizationException, AuthenticationException,
            ExecutorOutOfDateException, ExecutorAlreadyInGroupException;

    public void removeExecutorsFromGroup(Subject subject, List<? extends Executor> executors, Group group) throws AuthorizationException, AuthenticationException,
            ExecutorOutOfDateException, ExecutorNotInGroupException;

    public void removeExecutorsFromGroup(Subject subject, List<Long> executorIds, Long groupId) throws AuthorizationException, AuthenticationException,
            ExecutorOutOfDateException, ExecutorNotInGroupException;

    public void removeExecutorFromGroups(Subject subject, Executor executor, List<Group> groups) throws AuthorizationException, AuthenticationException,
            ExecutorOutOfDateException, ExecutorNotInGroupException;

    public void removeExecutorFromGroups(Subject subject, Long executorId, List<Long> groupIds) throws AuthorizationException, AuthenticationException,
            ExecutorOutOfDateException, ExecutorNotInGroupException;

    /**
     * Returns an array of executors from group.
     * <p>
     * For example G1 contains G2 and A0, G2 contins A1 and A2. In this case: Only actor (non-group) executors are returned.
     * </p>
     * <code> getAllNonGroupExecutorsFromGroup(G2) returns {A1, A2}</code>;<code> getAllNonGroupExecutorsFromGroup(G1) returns {A0} </code>
     * 
     * @param group
     *            a group
     * @return an array of executors from group.
     */
    public List<Executor> getAllExecutorsFromGroup(Subject subject, Group group) throws ExecutorOutOfDateException, AuthorizationException,
            AuthenticationException;

    /**
     * Loads first level group children's (not recursive).
     * <b>Paging is enabled on executors loading.</b>
     * @param subject Current actor {@linkplain Subject}. 
     * @param group {@linkplain Group} to load children's from.
     * @param batchPresentation {@linkplain BatchPresentation} for loading executors.
     * @param isExclude Flag, equals true, if must be loaded executors, which not yet group children's; false to load group children's. 
     * @return Array of loaded executors.
     */
    public List<Executor> getGroupChildren(Subject subject, Group group, BatchPresentation batchPresentation, boolean isExclude)
            throws AuthorizationException, AuthenticationException, ExecutorOutOfDateException;

    /**
     * Loads first level group children's count.
     * @param subject Current actor {@linkplain Subject}. 
     * @param group {@linkplain Group} to load children's from.
     * @param batchPresentation {@linkplain BatchPresentation} for loading executors.
     * @param isExclude Flag, equals true, if must be loaded executors, which not yet group children's; false to load group children's. 
     * @return Executors count.
     */
    public int getGroupChildrenCount(Subject subject, Group group, BatchPresentation batchPresentation, boolean isExclude)
            throws AuthorizationException, AuthenticationException, ExecutorOutOfDateException;

    /**
     * Returns actors from group and all subgroups recursive.
     * <p>
     * For example G1 contains G2, A3, G2 contains A1 and A2. In this case:
     * </p>
     * <code> getGroupActors(G2) returns {A1, A2}</code>;<code> getGroupActors(G1) returns {A1, A2, A3} </code>
     * @param subject Actor subject, performing action.
     * @param group Group, to load actors from.
     * @return All actors from group.
     */
    public List<Actor> getGroupActors(Subject subject, Group group) throws AuthenticationException, AuthorizationException, ExecutorOutOfDateException;

    /**
     * Load first level executor groups (not recursive).
     * <b>Paging is enabled on executors loading.</b>
     * @param subject Current actor {@linkplain Subject}.
     * @param executor {@linkplain Executor} to load groups.
     * @param batchPresentation {@linkplain BatchPresentation} to load groups.
     * @param isExclude Flag, equals true, if must be loaded groups, which not yet contains executor; false to load groups, which contains executor.  
     * @return Array of loaded groups.
     */
    public List<Group> getExecutorGroups(Subject subject, Executor executor, BatchPresentation batchPresentation, boolean isExclude)
            throws AuthorizationException, AuthenticationException, ExecutorOutOfDateException;

    /**
     * Load first level executor groups (not recursive) count.
     * @param subject Current actor {@linkplain Subject}.
     * @param executor {@linkplain Executor} to load groups.
     * @param batchPresentation {@linkplain BatchPresentation} to load groups.
     * @param isExclude Flag, equals true, if must be loaded groups, which not yet contains executor; false to load groups, which contains executor.  
     * @return Groups count.
     */
    public int getExecutorGroupsCount(Subject subject, Executor executor, BatchPresentation batchPresentation, boolean isExclude)
            throws AuthorizationException, AuthenticationException, ExecutorOutOfDateException;

    public boolean isExecutorInGroup(Subject subject, Executor executor, Group group) throws AuthorizationException, AuthenticationException,
            ExecutorOutOfDateException;

    public boolean isExecutorExist(Subject subject, String executorName) throws ExecutorOutOfDateException, AuthorizationException,
            AuthenticationException;

    public void setPassword(Subject subject, Actor actor, String password) throws AuthorizationException, AuthenticationException,
            ExecutorOutOfDateException, WeakPasswordException;

    public void setStatus(Subject subject, Long actorId, boolean isActive) throws AuthorizationException, AuthenticationException,
            ExecutorOutOfDateException;

    public Actor getActorByCode(Subject subject, Long code) throws AuthorizationException, AuthenticationException, ExecutorOutOfDateException;

    public List<Actor> getActorsByExecutorIds(Subject subject, List<Long> executorIds) throws ExecutorOutOfDateException, AuthorizationException,
            AuthenticationException;
}
