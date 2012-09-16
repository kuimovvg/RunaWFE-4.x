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
package ru.runa.af.dao;

import java.util.Collection;
import java.util.List;
import java.util.Set;

import ru.runa.af.Actor;
import ru.runa.af.Executor;
import ru.runa.af.ExecutorAlreadyExistsException;
import ru.runa.af.ExecutorAlreadyInGroupException;
import ru.runa.af.ExecutorNotInGroupException;
import ru.runa.af.ExecutorOpenTask;
import ru.runa.af.ExecutorOutOfDateException;
import ru.runa.af.Group;
import ru.runa.af.presentation.BatchPresentation;
import ru.runa.bpm.taskmgmt.exe.TaskInstance;

/**
 * DAO for managing executors. 
 * @author Vitaliy S aka Yilativs
 * @author Gordienko_m
 */
public interface ExecutorDAO {

    /**
     * Check if executor with given name exists.
     * @param executorName Executor name to check. 
     * @return Returns true, if executor with given name exists; false otherwise.
     */
    public boolean isExecutorExist(String executorName);

    /**
     * Check if {@linkplain Actor} with given code exists.
     * @param code {@linkplain Actor} code to check.
     * @return Returns true, if {@linkplain Actor} with given name exists; false otherwise.
     */
    public boolean isActorExist(Long code);

    /**
     * Check executor state with database. If executor state not same, as database state, throws {@linkplain ExecutorOutOfDateException}.  
     * @param <T> Checking executor class.
     * @param executor Executor to check state.
     * @return Returns checked executor if check success.
     */
    public <T extends Executor> T getExecutor(T executor) throws ExecutorOutOfDateException;

    /**
     * Load {@linkplain Executor} by name. Throws exception if load is impossible.
     * @param name Loaded executor name.
     * @return Executor with specified name.
     */
    public Executor getExecutor(String name) throws ExecutorOutOfDateException;

    /**
     * Load {@linkplain Executor} by identity. Throws exception if load is impossible.
     * @param name Loaded executor identity.
     * @return {@linkplain Executor} with specified identity.
     */
    public Executor getExecutor(Long id) throws ExecutorOutOfDateException;

    /**
     * Load {@linkplain Actor} by name. Throws exception if load is impossible, or exist group with same name.
     * @param name Loaded actor name.
     * @return {@linkplain Actor} with specified name.
     */
    public Actor getActor(String name) throws ExecutorOutOfDateException;

    /**
     * Load {@linkplain Actor} by name without case check. This method is a big shame for us. 
     * It should never have its way out of DAO! It only purpose is to use with stupid 
     * Microsoft Active Directory authentication, which is case insensitive. <b>Never use it! </b>
     * @param name Loaded actor name.
     * @return {@linkplain Actor} with specified name (case insensitive).
     */
    public Actor getActorCaseInsensitive(String name) throws ExecutorOutOfDateException;

    /**
     * Load {@linkplain Actor} by identity. Throws exception if load is impossible, or exist group with same identity.
     * @param name Loaded actor identity.
     * @return {@linkplain Actor} with specified identity.
     */
    public Actor getActor(Long id) throws ExecutorOutOfDateException;

    /**
     * Load {@linkplain Actor} by code. Throws exception if load is impossible.
     * @param name Loaded actor code.
     * @return {@linkplain Actor} with specified code.
     */
    public Actor getActorByCode(Long code) throws ExecutorOutOfDateException;

    /**
     * Load {@linkplain Group} by name. Throws exception if load is impossible, or exist actor with same name.
     * @param name Loaded group name.
     * @return {@linkplain Group} with specified name.
     */
    public Group getGroup(String name) throws ExecutorOutOfDateException;

    /**
     * Load {@linkplain Group} by identity. Throws exception if load is impossible, or exist actor with same identity.
     * @param name Loaded group identity.
     * @return {@linkplain Group} with specified identity.
     */
    public Group getGroup(Long id) throws ExecutorOutOfDateException;

    /**
     * Load {@linkplain Executor}'s with given identities.
     * @param ids Loading {@linkplain Executor}'s identities. 
     * @return Loaded executors in same order, as identities.
     */
    public List<Executor> getExecutors(List<Long> ids) throws ExecutorOutOfDateException;

    /**
     * Load {@linkplain Actor}'s with given identities.
     * @param executorIds Loading {@linkplain Actor}'s identities. 
     * @return Loaded actors in same order, as identities.
     */
    public List<Actor> getActors(List<Long> ids) throws ExecutorOutOfDateException;

    /**
     * Returns Actors by array of executor identities. If id element belongs to group it is replaced by all actors in group recursively.
     * @param ids Executors identities, to load actors.
     * @return Loaded actors, belongs to executor identities.
     */
    public List<Actor> getActorsByExecutorIds(List<Long> ids) throws ExecutorOutOfDateException;

    /**
     * Load identities for given {@linkplain Actor} codes. Loaded identities order is not specified. 
     * @param codes {@linkplain Actor} codes to load identities.
     * @return {@linkplain Actor} identities without order specified.
     */
    public List<Long> getActorIdsByCodes(List<Long> codes) throws ExecutorOutOfDateException;

    /**
     * Load {@linkplain Actor}'s with given codes.
     * @param executorIds Loading {@linkplain Actor}'s codes. 
     * @return Loaded actors in same order, as codes.
     */
    public List<Actor> getActorsByCodes(List<Long> codes) throws ExecutorOutOfDateException;

    /**
     * Returns identities of {@linkplain Actor} and all his groups recursively.
     * Actor identity is always result[0], but groups identities order is not specified.
     * </br>
     * For example G1 contains A1 and G2 contains G1. In this case:</br>
     * <code>getActorAndGroupsIds(A1) == {A1.id, G1.id, G2.id}.</code>
     * @param actor {@linkplain Actor}, which identity and groups must be loaded.
     * @return Returns identities of {@linkplain Actor} and all his groups recursively.
     */
    public List<Long> getActorAndGroupsIds(Actor actor);

    /**
     * Load all not active {@linkplain Actor}s codes. 
     * @return Not active actors codes.
     */
    public List<Long> getNotActiveActorCodes();

    /**
     * Load available {@linkplain Actor}'s with given codes. If actor with some code not available, it will be ignored.
     * Result order is not specified.
     * @param executorIds Loading {@linkplain Actor}'s codes. 
     * @return Loaded actors.
     */
    public List<Actor> getAvailableActorsByCodes(List<Long> codes);

    /**
     * Load {@linkplain Group}'s with given identities.
     * @param executorIds Loading {@linkplain Group}'s identities. 
     * @return Loaded groups in same order, as identities.
     */
    public List<Group> getGroups(List<Long> ids) throws ExecutorOutOfDateException;

    /**
     * Create executor (save it to database). Generate code property for {@linkplain Actor} with code == 0. 
     * @param <T> Creating executor class.
     * @param executor Creating executor.
     * @return Returns created executor.
     */
    public <T extends Executor> T create(T executor) throws ExecutorAlreadyExistsException;

    /**
     * Create executors (save it to database). Generate code property for {@linkplain Actor} with code == 0.
     * @param executors Creating executors
     * @return Returns created executors.
     */
    public void create(List<? extends Executor> executors) throws ExecutorAlreadyExistsException;

    /**
     * Removes executors with given id's.
     * @param ids Removing executors id's.
     */
    public void remove(List<Long> ids) throws ExecutorOutOfDateException;

    public void remove(Executor executor) throws ExecutorOutOfDateException;

    /**
     * Updates password for {@linkplain Actor}.
     * @param actor {@linkplain Actor} to update password.
     * @param password New actor password.
     */
    public void setPassword(Actor actor, String password) throws ExecutorOutOfDateException;

    /**
     * Check if password is valid for user.
     * @param actor {@linkplain Actor}, which password is checking.
     * @param password Checking password.
     * @return Returns true, if password is correct for actor and false otherwise. 
     */
    public boolean isPasswordValid(Actor actor, String password) throws ExecutorOutOfDateException;

    /**
     * Set {@linkplain Actor} active state.
     * @param actor {@linkplain Actor}, which active state is set.
     * @param isActive Flag, equals true to set actor active and false, to set actor inactive.
     */
    public void setStatus(Actor actor, boolean isActive) throws ExecutorOutOfDateException;

    /**
     * Check if actor is active.
     * @param code Checking actor code.
     * @return Returns true, if actor is active and false otherwise.
     */
    public boolean isActorActive(Long code) throws ExecutorOutOfDateException;

    /**
     * Update executor.
     * @param <T> Updated executor class.
     * @param oldExecutor Updated executor old state.
     * @param newExecutor Updated executor new state.
     * @return Returns updated executor state after update.
     */
    public <T extends Executor> T update(T oldExecutor, T newExecutor) throws ExecutorAlreadyExistsException, ExecutorOutOfDateException;

    /**
     * Clear group, i. e. removes all children's from group. 
     * @param groupId Clearing group id.
     * @throws ExecutorOutOfDateException 
     */
    public void clearGroup(Long groupId) throws ExecutorOutOfDateException;

    /**
     * Load all {@linkplain Executor}s according to {@linkplain BatchPresentation}.</br>
     * <b>Paging is not enabled. Really ALL executors is loading.</b>
     * @param batchPresentation {@linkplain BatchPresentation} to load executors.
     * @return {@linkplain Executor}s, loaded according to {@linkplain BatchPresentation}.
     */
    public List<Executor> getAll(BatchPresentation batchPresentation);

    /**
     * Load all {@linkplain Actor}s according to {@linkplain BatchPresentation}.</br>
     * <b>Paging is not enabled. Really ALL actors is loading.</b>
     * @param batchPresentation {@linkplain BatchPresentation} to load actors.
     * @return {@linkplain Actor}s, loaded according to {@linkplain BatchPresentation}.
     */
    public List<Actor> getAllActors(BatchPresentation batchPresentation);

    /**
     * Load all {@linkplain Group}s.</br>
     * <b>Paging is not enabled. Really ALL groups is loading.</b>
     * @return {@linkplain Group}s.
     */
    public List<Group> getAllGroups();

    /**
     * Add {@linkplain Executor}'s to {@linkplain Group}. 
     * @param executors {@linkplain Executor}'s, added to {@linkplain Group}. 
     * @param group {@linkplain Group}, to add executors in.
     */
    public void addExecutorsToGroup(Collection<? extends Executor> executors, Group group) throws ExecutorOutOfDateException, ExecutorAlreadyInGroupException;

    /**
     * Add {@linkplain Executor} to {@linkplain Group}'s. 
     * @param executors {@linkplain Executor}, added to {@linkplain Group}'s. 
     * @param group {@linkplain Group}s, to add executors in.
     */
    public void addExecutorToGroups(Executor executor, List<Group> groups) throws ExecutorOutOfDateException, ExecutorAlreadyInGroupException;

    /**
     * Remove {@linkplain Executor}'s from {@linkplain Group}. 
     * @param executors {@linkplain Executor}'s, removed from {@linkplain Group}. 
     * @param group {@linkplain Group}, to remove executors from.
     */
    public void removeExecutorsFromGroup(List<? extends Executor> executors, Group group) throws ExecutorOutOfDateException, ExecutorNotInGroupException;

    /**
     * Remove {@linkplain Executor} from {@linkplain Group}'s. 
     * @param executors {@linkplain Executor}, removed from {@linkplain Group}'s. 
     * @param group {@linkplain Group}s, to remove executors from.
     */
    public void removeExecutorFromGroups(Executor executor, List<Group> groups) throws ExecutorOutOfDateException, ExecutorNotInGroupException;

    /**
     * Returns true if executor belongs to group recursively or false in any other case.</br>
     * For example G1 contains G2, G2 contains A1. In this case:</br>
     * <code>isExecutorInGroup(A1,G2) == true;</code>
     * @param executor An executor to check if it in group.
     * @param group A group to check if it contains executor.
     * @return true if executor belongs to group recursively; false in any other case.
     */
    public boolean isExecutorInGroup(Executor executor, Group group) throws ExecutorOutOfDateException;

    /**
     * Returns an array of group children (first level children, not recursively).</br>
     * For example G1 contains G2, G2 contains A1 and A2. In this case:</br>
     * <code> getGroupChildren(G2, defaultPresentation) == {A1, A2}</code><br/>
     * <code> getGroupChildren(G1, defaultPresentation) == {G2} </code> 
     * @param group A group to load children's from.
     * @param batchPresentation As {@linkplain BatchPresentation} of array returned.
     * @return Array of group children.
     */
    public List<Executor> getGroupChildren(Group group, BatchPresentation batchPresentation) throws ExecutorOutOfDateException;

    /**
     * Returns group children (first level children, not recursively).</br>
     * For example G1 contains G2, G2 contains A1 and A2. In this case:</br>
     * <code> getGroupChildren(G2) == {A1, A2}</code><br/><code> getGroupChildren(G1) == {G2} </code> 
     * @param group A group to load children's from.
     * @param batchPresentation As {@linkplain BatchPresentation} of array returned.
     * @return Array of group children.
     */
    public Set<Executor> getGroupChildren(Group group) throws ExecutorOutOfDateException;

    /**
     * Returns all {@linkplain Actor}s from {@linkplain Group} recursively. All actors from subgroups is also added to result.
     * For example G1 contains G2 and A3, G2 contains A1 and A2. In this case:</br>
     * <code> getGroupActors(G2) == {A1, A2}</code><br/><code> getGroupActors(G1) == {A1, A2, A3} </code> 
     * @param group {@linkplain Group} to load {@linkplain Actor} children's
     * @return Set of actor children's.
     */
    public Set<Actor> getGroupActors(Group group) throws ExecutorOutOfDateException;

    /**
     * Returns all executor parent {@linkplain Groups}s recursively.
     * For example G1 contains G2 and A3, G2 contains A1 and A2. In this case:</br>
     * <code> getExecutorParentsAll(A1) == {G1, G2}</code><br/><code> getExecutorParentsAll(A3) == {G1} </code> 
     * @param executor {@linkplain Executor} to load parent groups.
     * @return Set of executor parents.
     */
    public Set<Group> getExecutorParentsAll(Executor executor);

    /**
     * Returns an array of actors from group (first level children, not recursively).</br>
     * For example G1 contains G2 and A0, G2 contains A1 and A2. In this case: Only actor (non-group) executors are returned.</br>
     * <code> getAllNonGroupExecutorsFromGroup(G2) returns {A1, A2}</code>;<code> getAllNonGroupExecutorsFromGroup(G1) returns {A0} </code>
     * @param group {@linkplain Group}, to load actor children's.
     * @return Array of executors from group.
     */
    public List<Executor> getAllNonGroupExecutorsFromGroup(Group group) throws ExecutorOutOfDateException;

    /**
     * Returns an array of executor groups (first level, not recursively).
     * @param executor Executor, which groups is loaded.
     * @return Array of executor groups.
     */
    public List<Group> getExecutorGroups(Executor executor, BatchPresentation batchPresentation) throws ExecutorOutOfDateException;

    /**
     * Saves open task DTO to database. Saving open task must not be saved before. 
     * @param executorOpenTask Saving open task DTO
     * @return Saved open task DTO.
     */
    public ExecutorOpenTask createOpenTask(ExecutorOpenTask executorOpenTask);

    /**
     * Removes all open tasks for task instance.
     * @param taskInstance Task instance to remove open tasks.
     */
    public void removeOpenTask(TaskInstance taskInstance);

    /**
     * Removes all open tasks for process instance.
     * @param processInstanceId Process instance to remove open tasks.
     */
    public void removeOpenTasks(Long processInstanceId);

    public void removeOpenTasks(String processDefinitionName);

    public List<ExecutorOpenTask> getOpenedTasksByExecutor(Executor executor);
}
