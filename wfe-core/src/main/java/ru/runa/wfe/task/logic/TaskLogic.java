package ru.runa.wfe.task.logic;

import java.util.List;
import java.util.Map;

import javax.security.auth.Subject;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;

import ru.runa.wfe.commons.logic.WFCommonLogic;
import ru.runa.wfe.execution.ExecutionContext;
import ru.runa.wfe.execution.Process;
import ru.runa.wfe.execution.ProcessDoesNotExistException;
import ru.runa.wfe.execution.ProcessPermission;
import ru.runa.wfe.execution.Swimlane;
import ru.runa.wfe.execution.dto.WfProcess;
import ru.runa.wfe.execution.dto.WfSwimlane;
import ru.runa.wfe.execution.logic.ExecutionLogic;
import ru.runa.wfe.handler.assign.AssignmentHandler;
import ru.runa.wfe.handler.assign.AssignmentHelper;
import ru.runa.wfe.lang.Delegation;
import ru.runa.wfe.lang.ProcessDefinition;
import ru.runa.wfe.lang.SwimlaneDefinition;
import ru.runa.wfe.lang.Transition;
import ru.runa.wfe.presentation.BatchPresentation;
import ru.runa.wfe.security.AuthenticationException;
import ru.runa.wfe.security.auth.SubjectPrincipalsHelper;
import ru.runa.wfe.task.Task;
import ru.runa.wfe.task.TaskAlreadyAcceptedException;
import ru.runa.wfe.task.TaskAlreadyCompletedException;
import ru.runa.wfe.task.TaskDoesNotExistException;
import ru.runa.wfe.task.TasklistBuilder;
import ru.runa.wfe.task.dto.WfTask;
import ru.runa.wfe.task.dto.WfTaskFactory;
import ru.runa.wfe.user.Actor;
import ru.runa.wfe.user.ActorPermission;
import ru.runa.wfe.user.Executor;
import ru.runa.wfe.user.ExecutorPermission;
import ru.runa.wfe.validation.impl.ValidationException;
import ru.runa.wfe.var.MapDelegableVariableProvider;

import com.google.common.base.Objects;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

/**
 * Task logic.
 * 
 * @author Dofs
 * @since 4.0
 */
public class TaskLogic extends WFCommonLogic {
    private static final Log log = LogFactory.getLog(ExecutionLogic.class);
    @Autowired
    private WfTaskFactory taskObjectFactory;
    @Autowired
    private TasklistBuilder tasklistBuilder;
    @Autowired
    private AssignmentHelper assignmentHelper;

    public void completeTask(Subject subject, Long taskId, Map<String, Object> variables) throws TaskDoesNotExistException, ValidationException {
        Actor actor = SubjectPrincipalsHelper.getActor(subject);
        Task task = taskDAO.getNotNull(taskId);
        if (!task.isActive()) {
            throw new TaskAlreadyCompletedException(task.getName());
        }
        ProcessDefinition processDefinition = getDefinition(task);
        ExecutionContext executionContext = new ExecutionContext(processDefinition, task);
        if (variables == null) {
            variables = Maps.newHashMap();
        }
        String transitionName = (String) variables.remove(WfProcess.SELECTED_TRANSITION_KEY);
        checkCanParticipate(subject, task, actor);
        actor = checkPermissionsOnExecutor(subject, actor, ActorPermission.READ);

        assignmentHelper.reassignTask(executionContext, task, actor, true);

        validateVariables(processDefinition, task.getNodeId(), new MapDelegableVariableProvider(variables, executionContext.getVariableProvider()));
        executionContext.setVariables(variables);
        Transition transition = null;
        if (transitionName != null) {
            transition = processDefinition.getNodeNotNull(task.getNodeId()).getLeavingTransitionNotNull(transitionName);
        }
        task.end(executionContext, transition, true);
        log.info("Task '" + task.getName() + "' was done by " + actor + " in process " + task.getProcess());
    }

    public void markTaskOpened(Subject subject, Long taskId) {
        Task task = taskDAO.getNotNull(taskId);
        task.setFirstOpen(false);
    }

    public WfTask getTask(Subject subject, Long taskId) throws AuthenticationException {
        Task task = taskDAO.getNotNull(taskId);
        return taskObjectFactory.create(task, SubjectPrincipalsHelper.getActor(subject), null);
    }

    public void assignUnassignedTasks() {
        List<Task> unassignedTasks = taskDAO.findUnassignedActiveTasks();
        for (Task task : unassignedTasks) {
            if (task.getProcess().hasEnded()) {
                log.warn("Ended process for " + task);
                continue;
            }
            try {
                ProcessDefinition processDefinition = getDefinition(task);
                if (task.getSwimlane() != null) {
                    Delegation delegation = task.getSwimlane().getDefinition(processDefinition).getDelegation();
                    AssignmentHandler handler = delegation.getInstance();
                    handler.assign(new ExecutionContext(processDefinition, task), task);
                }
            } catch (Exception e) {
                log.warn(task.getSwimlane().getName(), e);
            }
        }
    }

    public List<WfTask> getTasks(Subject subject, BatchPresentation batchPresentation) {
        Actor actor = SubjectPrincipalsHelper.getActor(subject);
        return tasklistBuilder.getTasks(actor, batchPresentation);
    }

    public List<WfTask> getActiveTasks(Subject subject, Long processId) throws ProcessDoesNotExistException {
        Actor actor = SubjectPrincipalsHelper.getActor(subject);
        List<WfTask> result = Lists.newArrayList();
        Process process = processDAO.getNotNull(processId);
        checkPermissionAllowed(subject, process, ProcessPermission.READ);
        for (Task task : process.getActiveTasks(null)) {
            result.add(taskObjectFactory.create(task, actor, null));
        }
        return result;
    }

    public List<WfSwimlane> getSwimlanes(Subject subject, Long processId) throws ProcessDoesNotExistException {
        Process process = processDAO.getNotNull(processId);
        ProcessDefinition processDefinition = getDefinition(process);
        checkPermissionAllowed(subject, process, ProcessPermission.READ);
        Actor performer = SubjectPrincipalsHelper.getActor(subject);
        Map<String, SwimlaneDefinition> swimlaneMap = processDefinition.getSwimlanes();
        List<WfSwimlane> wfSwimlanes = Lists.newArrayListWithExpectedSize(swimlaneMap.size());
        for (SwimlaneDefinition swimlaneDefinition : swimlaneMap.values()) {
            Swimlane swimlane = process.getSwimlane(swimlaneDefinition.getName());
            Executor assignedExecutor = null;
            if (swimlane != null && swimlane.getExecutor() != null) {
                if (permissionDAO.isAllowed(performer, ExecutorPermission.READ, swimlane.getExecutor())) {
                    assignedExecutor = swimlane.getExecutor();
                } else {
                    assignedExecutor = Actor.UNAUTHORIZED_ACTOR;
                }
            }
            wfSwimlanes.add(new WfSwimlane(swimlaneDefinition, assignedExecutor));
        }
        return wfSwimlanes;
    }

    public void assignSwimlane(Subject subject, Long processId, String swimlaneName, Executor executor) {
        Process process = processDAO.getNotNull(processId);
        ProcessDefinition processDefinition = getDefinition(process);
        SwimlaneDefinition swimlaneDefinition = processDefinition.getSwimlaneNotNull(swimlaneName);
        Swimlane swimlane = process.getSwimlaneNotNull(swimlaneDefinition);
        assignmentHelper.assignSwimlane(new ExecutionContext(processDefinition, process), swimlane, Lists.newArrayList(executor));
    }

    public void assignTask(Subject subject, Long taskId, Executor previousOwner, Actor actor) throws TaskAlreadyAcceptedException {
        // check assigned executor for the task
        Task task = taskDAO.getNotNull(taskId);
        if (!task.isActive() || !Objects.equal(previousOwner, task.getExecutor())) {
            throw new TaskAlreadyAcceptedException(task.getName());
        }
        ProcessDefinition processDefinition = getDefinition(task);
        assignmentHelper.reassignTask(new ExecutionContext(processDefinition, task), task, actor, false);
    }

}
