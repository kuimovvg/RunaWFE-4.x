package ru.runa.wfe.task.logic;

import java.util.Date;
import java.util.List;
import java.util.Map;

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
import ru.runa.wfe.execution.logic.ProcessExecutionErrors;
import ru.runa.wfe.execution.logic.ProcessExecutionException;
import ru.runa.wfe.handler.assign.AssignmentHandler;
import ru.runa.wfe.handler.assign.AssignmentHelper;
import ru.runa.wfe.lang.Delegation;
import ru.runa.wfe.lang.ProcessDefinition;
import ru.runa.wfe.lang.SwimlaneDefinition;
import ru.runa.wfe.lang.Transition;
import ru.runa.wfe.presentation.BatchPresentation;
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
import ru.runa.wfe.user.User;
import ru.runa.wfe.validation.impl.ValidationException;
import ru.runa.wfe.var.MapDelegableVariableProvider;

import com.google.common.base.Objects;
import com.google.common.base.Throwables;
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

    public void completeTask(User user, Long taskId, Map<String, Object> variables) throws TaskDoesNotExistException, ValidationException {
        Task task = taskDAO.getNotNull(taskId);
        if (!task.isActive()) {
            throw new TaskAlreadyCompletedException(task.toString());
        }
        try {
            ProcessDefinition processDefinition = getDefinition(task);
            ExecutionContext executionContext = new ExecutionContext(processDefinition, task);
            if (variables == null) {
                variables = Maps.newHashMap();
            }
            String transitionName = (String) variables.remove(WfProcess.SELECTED_TRANSITION_KEY);
            checkCanParticipate(user, task, user.getActor());
            checkPermissionsOnExecutor(user, user.getActor(), ActorPermission.READ);
            assignmentHelper.reassignTask(executionContext, task, user.getActor(), true);
            validateVariables(processDefinition, task.getNodeId(),
                    new MapDelegableVariableProvider(variables, executionContext.getVariableProvider()));
            executionContext.setVariables(variables);
            Transition transition = null;
            if (transitionName != null) {
                transition = processDefinition.getNodeNotNull(task.getNodeId()).getLeavingTransitionNotNull(transitionName);
            }
            task.end(executionContext, transition, true);
            log.info("Task '" + task.getName() + "' was done by " + user + " in process " + task.getProcess());
            ProcessExecutionErrors.removeProcessError(task.getProcess().getId(), task.getName());
        } catch (Throwable th) {
            ProcessExecutionErrors.addProcessError(task.getProcess().getId(), task.getName(), th);
            throw Throwables.propagate(th);
        }
    }

    public void markTaskOpened(User user, Long taskId) {
        Task task = taskDAO.getNotNull(taskId);
        task.setFirstOpen(false);
    }

    public WfTask getTask(User user, Long taskId) {
        Task task = taskDAO.getNotNull(taskId);
        return taskObjectFactory.create(task, user.getActor(), null);
    }

    public void assignUnassignedTasks() {
        List<Task> unassignedTasks = taskDAO.findUnassignedActiveTasks();
        for (Task task : unassignedTasks) {
            if (task.getProcess().hasEnded()) {
                log.warn("Ending task for finished process " + task);
                task.setEndDate(new Date());
                continue;
            }
            try {
                ProcessDefinition processDefinition = getDefinition(task);
                if (task.getSwimlane() != null) {
                    Delegation delegation = task.getSwimlane().getDefinition(processDefinition).getDelegation();
                    AssignmentHandler handler = delegation.getInstance();
                    handler.assign(new ExecutionContext(processDefinition, task), task);
                }
                ProcessExecutionErrors.removeProcessError(task.getProcess().getId(), task.getName());
            } catch (Throwable th) {
                log.warn("Unable to assign task '" + task + "' with swimlane '" + task.getSwimlane() + "'", th);
                ProcessExecutionException e = new ProcessExecutionException(ProcessExecutionException.TASK_ASSIGNMENT_FAILED, task.getName());
                ProcessExecutionErrors.addProcessError(task.getProcess().getId(), task.getName(), e);
            }
        }
    }

    public List<WfTask> getTasks(User user, BatchPresentation batchPresentation) {
        return tasklistBuilder.getTasks(user.getActor(), batchPresentation);
    }

    public List<WfTask> getActiveTasks(User user, Long processId) throws ProcessDoesNotExistException {
        List<WfTask> result = Lists.newArrayList();
        Process process = processDAO.getNotNull(processId);
        checkPermissionAllowed(user, process, ProcessPermission.READ);
        for (Task task : process.getActiveTasks(null)) {
            result.add(taskObjectFactory.create(task, user.getActor(), null));
        }
        return result;
    }

    public List<WfSwimlane> getSwimlanes(User user, Long processId) throws ProcessDoesNotExistException {
        Process process = processDAO.getNotNull(processId);
        ProcessDefinition processDefinition = getDefinition(process);
        checkPermissionAllowed(user, process, ProcessPermission.READ);
        Map<String, SwimlaneDefinition> swimlaneMap = processDefinition.getSwimlanes();
        List<WfSwimlane> swimlanes = Lists.newArrayListWithExpectedSize(swimlaneMap.size());
        for (SwimlaneDefinition swimlaneDefinition : swimlaneMap.values()) {
            Swimlane swimlane = process.getSwimlane(swimlaneDefinition.getName());
            Executor assignedExecutor = null;
            if (swimlane != null && swimlane.getExecutor() != null) {
                if (permissionDAO.isAllowed(user, ExecutorPermission.READ, swimlane.getExecutor())) {
                    assignedExecutor = swimlane.getExecutor();
                } else {
                    assignedExecutor = Actor.UNAUTHORIZED_ACTOR;
                }
            }
            swimlanes.add(new WfSwimlane(swimlaneDefinition, assignedExecutor));
        }
        return swimlanes;
    }

    public void assignSwimlane(User user, Long processId, String swimlaneName, Executor executor) {
        Process process = processDAO.getNotNull(processId);
        ProcessDefinition processDefinition = getDefinition(process);
        SwimlaneDefinition swimlaneDefinition = processDefinition.getSwimlaneNotNull(swimlaneName);
        Swimlane swimlane = process.getSwimlaneNotNull(swimlaneDefinition);
        assignmentHelper.assignSwimlane(new ExecutionContext(processDefinition, process), swimlane, Lists.newArrayList(executor));
    }

    public void assignTask(User user, Long taskId, Executor previousOwner, Actor actor) throws TaskAlreadyAcceptedException {
        // check assigned executor for the task
        Task task = taskDAO.getNotNull(taskId);
        if (!task.isActive() || !Objects.equal(previousOwner, task.getExecutor())) {
            throw new TaskAlreadyAcceptedException(task.getName());
        }
        ProcessDefinition processDefinition = getDefinition(task);
        assignmentHelper.reassignTask(new ExecutionContext(processDefinition, task), task, actor, false);
    }

}
