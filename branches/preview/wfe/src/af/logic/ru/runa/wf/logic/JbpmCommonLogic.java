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
package ru.runa.wf.logic;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.security.auth.Subject;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;

import ru.runa.InternalApplicationException;
import ru.runa.af.Actor;
import ru.runa.af.AuthenticationException;
import ru.runa.af.AuthorizationException;
import ru.runa.af.ExecutorOutOfDateException;
import ru.runa.af.Group;
import ru.runa.af.authenticaion.SubjectPrincipalsHelper;
import ru.runa.af.logic.CommonLogic;
import ru.runa.af.logic.SubstitutionLogic;
import ru.runa.bpm.db.ProcessDefinitionDAO;
import ru.runa.bpm.db.ProcessExecutionDAO;
import ru.runa.bpm.db.TaskDAO;
import ru.runa.bpm.graph.def.ArchievedProcessDefinition;
import ru.runa.bpm.graph.def.ExecutableProcessDefinition;
import ru.runa.bpm.graph.exe.ProcessInstance;
import ru.runa.bpm.taskmgmt.exe.TaskInstance;
import ru.runa.commons.InfoHolder;
import ru.runa.commons.hibernate.HibernateSessionFactory;
import ru.runa.wf.ProcessDefinition;
import ru.runa.wf.ProcessDefinitionDoesNotExistException;
import ru.runa.wf.ProcessDefinitionPermission;
import ru.runa.wf.ProcessInstancePermission;
import ru.runa.wf.ProcessInstanceStub;
import ru.runa.wf.TaskDoesNotExistException;
import ru.runa.wf.caches.ProcessDefCacheCtrl;
import ru.runa.wf.dao.TmpDAO;
import ru.runa.wf.graph.GraphConverter;
import ru.runa.wf.graph.GraphElementPresentation;
import ru.runa.wf.graph.GraphElementPresentationVisitor;

import com.google.common.base.Objects;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

/**
 * Created on 15.03.2005
 */
@SuppressWarnings({ "unchecked" })
public class JbpmCommonLogic extends CommonLogic {
    private static final Log log = LogFactory.getLog(JbpmCommonLogic.class);

    @Autowired
    protected ProcessDefCacheCtrl processDefCache;
    @Autowired
    protected SubstitutionLogic substitutionLogic;

    @Autowired
    protected ProcessDefinitionDAO processDefinitionDAO;
    @Autowired
    protected ProcessExecutionDAO processExecutionDAO;
    @Autowired
    protected TaskDAO taskDAO;
    @Autowired
    protected TmpDAO tmpDAO;

    public ExecutableProcessDefinition getDefinition(Long processDefinitionId) {
        return processDefCache.getDefinition(processDefinitionId);
    }

    public ExecutableProcessDefinition getDefinition(ProcessInstance processInstance) {
        return getDefinition(processInstance.getProcessDefinition().getId());
    }

    public ExecutableProcessDefinition getDefinition(TaskInstance taskInstance) {
        return getDefinition(taskInstance.getProcessInstance());
    }

    protected ExecutableProcessDefinition getLatestDefinition(String definitionName) throws ProcessDefinitionDoesNotExistException {
        return processDefCache.getLatestDefinition(definitionName);
    }

    protected ProcessInstanceStub createProcessInstanceStub(ProcessInstance processInstance) {
        return new ProcessInstanceStub(processInstance);
    }

    protected ProcessDefinition createProcessDefinitionStub(ExecutableProcessDefinition definition) {
        ProcessDefinition stub = new ProcessDefinition(definition);
        stub.setType(processDefCache.getDefinitionType(definition.getName()));
        return stub;
    }

    protected ProcessDefinition createProcessDefinitionStub(ArchievedProcessDefinition definition) {
        return new ProcessDefinition(definition);
    }

    protected TaskInstance getTaskWithName(Long tokenId, String taskName) throws TaskDoesNotExistException {
        TaskInstance taskInstance = taskDAO.getTaskInstanceNotNull(tokenId);
        if (taskInstance.getTask() == null || !taskInstance.getName().equals(taskName)) {
            throw new TaskDoesNotExistException(taskName);
        }
        return taskInstance;
    }

    protected boolean hasActorWithCode(Long actorCode, Set<Actor> actorsSet) {
        for (Actor actor : actorsSet) {
            if (Objects.equal(actor.getCode(), actorCode)) {
                return true;
            }
        }
        return false;
    }

    protected boolean hasActiveActorInGroup(Group group) throws ExecutorOutOfDateException {
        for (Actor actor : executorDAO.getGroupActors(group)) {
            if (actor.isActive()) {
                return true;
            }
        }
        return false;
    }

    protected boolean canParticipateAsSubstitutor(Subject subject, TaskInstance token) throws AuthenticationException {
        Actor actor = SubjectPrincipalsHelper.getActor(subject);
        try {
            Set<Long> canSubIds = substitutionLogic.getSubstituted(actor);
            Set<Actor> canSub = new HashSet<Actor>();
            for (Long id : canSubIds) {
                canSub.add(executorDAO.getActor(id));
            }

            String strAssignedActor = token.getAssignedActorId();
            if (strAssignedActor.charAt(0) != 'G') {
                return hasActorWithCode(Long.parseLong(strAssignedActor), canSub);
            } else {
                for (Actor assignActor : getAssignedActors(token)) {
                    if (hasActorWithCode(assignActor.getCode(), canSub)) {
                        return true;
                    }
                }
            }
        } catch (ExecutorOutOfDateException e) {
        }
        return false;
    }

    protected void checkCanParticipate(Subject subject, TaskInstance token, Actor targetActor) throws AuthorizationException,
            AuthenticationException, TaskDoesNotExistException {
        Actor actor = SubjectPrincipalsHelper.getActor(subject);
        if (targetActor == null) {
            targetActor = actor;
        }
        try {
            if (token.getAssignedActorId() == null || token.getAssignedActorId().equals(InfoHolder.UNASSIGNED_SWIMLANE_VALUE)) {
                throw new InternalApplicationException("Unassigned tasks can't be in processing");
            }

            String strAssignedActor = token.getAssignedActorId();
            if (strAssignedActor.charAt(0) != 'G') {
                if (actor.getCode().longValue() == Long.parseLong(strAssignedActor)) {
                    return;
                }
            } else {
                Long id = new Long(strAssignedActor.substring(1));
                if (hasActorWithCode(actor.getCode(), executorDAO.getGroupActors(executorDAO.getGroup(id)))) {
                    return;
                }
            }
            if (canParticipateAsSubstitutor(subject, token)) {
                return;
            }
        } catch (ExecutorOutOfDateException e) {
            log.error("incorrect executor code assigned to swimlane", e);
            throw new AuthorizationException("Executor " + actor + " has no pemission to participate as " + targetActor + " in task " + token);
        }
        throw new AuthorizationException("Executor " + actor + " has no pemission to participate as " + targetActor + " in task " + token);
    }

    public static final char FUNCTION_SEPARATER = ';';

    public static final char SUBSTITUTION_CRITERIA_SEPARATER = ':';

    protected Map<TaskInstance, Actor> getTokenAssingedActorMap(Collection<TaskInstance> tasks) {
        Map<TaskInstance, Actor> assingedActorTaskMap = new HashMap<TaskInstance, Actor>();
        Map<Long, Set<TaskInstance>> actorCodeTaskSetMap = Maps.newHashMap();
        for (TaskInstance task : tasks) {
            if (task.getAssignedActorId().charAt(0) != 'G') {
                Long actorCode = new Long(task.getAssignedActorId());
                Set<TaskInstance> taskSet = actorCodeTaskSetMap.get(actorCode);
                if (taskSet == null) {
                    taskSet = new HashSet<TaskInstance>();
                }
                taskSet.add(task);
                actorCodeTaskSetMap.put(actorCode, taskSet);
            }
        }
        try {
            for (Actor assingendActor : executorDAO.getActorsByCodes(Lists.newArrayList(actorCodeTaskSetMap.keySet()))) {
                Set<TaskInstance> taskSet = actorCodeTaskSetMap.get(assingendActor.getCode());
                for (TaskInstance task : taskSet) {
                    assingedActorTaskMap.put(task, assingendActor);
                }
            }
        } catch (ExecutorOutOfDateException e) {
            // TODO add token information here
            log.error("incorrect executor code assigned to swimlane", e);
        }
        return assingedActorTaskMap;
    }

    protected void checkReadToVariablesAllowed(Subject subject, TaskInstance taskInstance) throws AuthorizationException, AuthenticationException,
            TaskDoesNotExistException {
        // TODO duplicated LOGIC alg!
        Actor actor = SubjectPrincipalsHelper.getActor(subject);
        ProcessInstanceStub processInstanceStub = createProcessInstanceStub(taskInstance.getProcessInstance());
        try {
            if (isPermissionAllowed(subject, processInstanceStub, ProcessInstancePermission.READ)) {
                return;
            }

            if (taskInstance.getAssignedActorId() == null || taskInstance.getAssignedActorId().equals(InfoHolder.UNASSIGNED_SWIMLANE_VALUE)) {
                throw new InternalApplicationException("Unassigned tasks can't be in processing");
            }

            String strAssignedActor = taskInstance.getAssignedActorId();
            if (strAssignedActor.charAt(0) != 'G') {
                if (Long.parseLong(strAssignedActor) == actor.getCode()) {
                    return;
                }
            } else {
                Long id = new Long(strAssignedActor.substring(1));
                if (hasActorWithCode(actor.getCode(), executorDAO.getGroupActors(executorDAO.getGroup(id)))) {
                    return;
                }
            }
            if (canParticipateAsSubstitutor(subject, taskInstance)) {
                return;
            }
        } catch (ExecutorOutOfDateException e) {
            log.error("incorrect ex)ecutor code assigned to swimlane", e);
            throw new AuthorizationException("Executor " + actor + " has no pemission to read task " + taskInstance + " variables");
        }
        throw new AuthorizationException("Executor " + actor + " has no pemission to read task " + taskInstance + " variables");
    }

    protected Set<Actor> getAssignedActors(TaskInstance taskInstance) throws ExecutorOutOfDateException {
        if (taskInstance.getAssignedActorId() == null || taskInstance.getAssignedActorId().equals(InfoHolder.UNASSIGNED_SWIMLANE_VALUE)) {
            throw new InternalApplicationException("Unassigned tasks can't be in processing");
        }

        String strAssignedActor = taskInstance.getAssignedActorId();
        if (strAssignedActor.charAt(0) != 'G') {
            Set<Actor> retVal = new HashSet<Actor>();
            retVal.add(executorDAO.getActorByCode(new Long(strAssignedActor)));
            return retVal;
        } else {
            Long id = new Long(strAssignedActor.substring(1));
            return executorDAO.getGroupActors(executorDAO.getGroup(id));
        }
    }

    protected List<ProcessInstance> getSubprocesses(ProcessInstance instance) {
        return HibernateSessionFactory
                .getSession()
                .createQuery(
                        "select p from " + ProcessInstance.class.getCanonicalName() + " as p where p.superProcessToken.processInstance.id="
                                + instance.getId()).list();
    }

    protected void prepareProcessRemoval(ProcessInstance processInstance) {
        securedObjectDAO.remove(createProcessInstanceStub(processInstance));
        executorDAO.removeOpenTasks(processInstance.getId());
        for (ProcessInstance subInstance : getSubprocesses(processInstance)) {
            prepareProcessRemoval(subInstance);
        }
    }

    /**
     * Loads graph presentation elements for process definition.
     * 
     * @param subject
     *            Current subject.
     * @param definitionId
     *            Identity of process definition, which presentation elements
     *            must be loaded.
     * @param operation
     *            Operation, which must be applied to loaded graph elements, or
     *            null, if nothing to apply.
     * @return List of graph presentation elements.
     */
    public List<GraphElementPresentation> getProcessDefinitionGraphElements(Subject subject, Long definitionId,
            GraphElementPresentationVisitor operation) throws AuthenticationException, AuthorizationException {
        try {
            ExecutableProcessDefinition definition = processDefCache.getDefinition(definitionId);
            ProcessDefinition definitionStub = createProcessDefinitionStub(definition);
            checkPermissionAllowed(subject, definitionStub, ProcessDefinitionPermission.READ);
            GraphConverter converter = new GraphConverter(definition);
            List<GraphElementPresentation> result = converter.getDefinitionElements(definition.getNodes());
            if (operation != null) {
                for (GraphElementPresentation elementPresentation : result) {
                    elementPresentation.visit(operation);
                }
            }
            return result;
        } catch (Exception e) {
            log.warn("Unable to draw diagram", e);
            throw new InternalApplicationException(e);
        }
    }

}
