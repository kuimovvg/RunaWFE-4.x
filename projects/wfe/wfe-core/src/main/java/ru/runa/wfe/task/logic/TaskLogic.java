package ru.runa.wfe.task.logic;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;

import ru.runa.wfe.InternalApplicationException;
import ru.runa.wfe.commons.SystemProperties;
import ru.runa.wfe.commons.logic.WFCommonLogic;
import ru.runa.wfe.execution.ExecutionContext;
import ru.runa.wfe.execution.Process;
import ru.runa.wfe.execution.ProcessDoesNotExistException;
import ru.runa.wfe.execution.ProcessPermission;
import ru.runa.wfe.execution.Swimlane;
import ru.runa.wfe.execution.Token;
import ru.runa.wfe.execution.dto.WfProcess;
import ru.runa.wfe.execution.dto.WfSwimlane;
import ru.runa.wfe.execution.logic.ProcessExecutionErrors;
import ru.runa.wfe.extension.assign.AssignmentHelper;
import ru.runa.wfe.lang.InteractionNode;
import ru.runa.wfe.lang.MultiTaskNode;
import ru.runa.wfe.lang.ProcessDefinition;
import ru.runa.wfe.lang.SwimlaneDefinition;
import ru.runa.wfe.lang.Synchronizable;
import ru.runa.wfe.lang.Transition;
import ru.runa.wfe.presentation.BatchPresentation;
import ru.runa.wfe.task.Task;
import ru.runa.wfe.task.TaskAlreadyAcceptedException;
import ru.runa.wfe.task.TaskCompletionBy;
import ru.runa.wfe.task.TaskCompletionInfo;
import ru.runa.wfe.task.TaskDoesNotExistException;
import ru.runa.wfe.task.dto.WfTask;
import ru.runa.wfe.task.dto.WfTaskFactory;
import ru.runa.wfe.user.Actor;
import ru.runa.wfe.user.Executor;
import ru.runa.wfe.user.ExecutorPermission;
import ru.runa.wfe.user.User;
import ru.runa.wfe.validation.ValidationException;
import ru.runa.wfe.var.IVariableProvider;
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
    @Autowired
    private WfTaskFactory taskObjectFactory;
    @Autowired
    private ITaskListBuilder taskListBuilder;

    public void completeTask(User user, Long taskId, Map<String, Object> variables, Long swimlaneActorId) throws TaskDoesNotExistException {
        Task task = taskDAO.getNotNull(taskId);
        try {
            if (variables == null) {
                variables = Maps.newHashMap();
            }
            ProcessDefinition processDefinition = getDefinition(task);
            ExecutionContext executionContext = new ExecutionContext(processDefinition, task);
            TaskCompletionBy completionBy = checkCanParticipate(user.getActor(), task);
            InteractionNode node = (InteractionNode) executionContext.getProcessDefinition().getNodeNotNull(task.getNodeId());
            if (swimlaneActorId != null) {
                Actor swimlaneActor = executorDAO.getActor(swimlaneActorId);
                checkCanParticipate(swimlaneActor, task);
                boolean reassignSwimlane = node.getFirstTaskNotNull().isReassignSwimlaneToTaskPerformer();
                AssignmentHelper.reassignTask(executionContext, task, swimlaneActor, reassignSwimlane);
            }
            // don't persist selected transition name
            String transitionName = (String) variables.remove(WfProcess.SELECTED_TRANSITION_KEY);
            Map<String, Object> transitionMap = Maps.newHashMap();
            transitionMap.put(WfProcess.SELECTED_TRANSITION_KEY, transitionName);
            if (SystemProperties.isV3CompatibilityMode()) {
                transitionMap.put("transition", transitionName);
            }
            // transient variables
            Map<String, Object> transientVariables = (Map<String, Object>) variables.remove(WfProcess.TRANSIENT_VARIABLES);
            if (transientVariables != null) {
                for (Map.Entry<String, Object> entry : transientVariables.entrySet()) {
                    executionContext.setTransientVariable(entry.getKey(), entry.getValue());
                }
            }
            IVariableProvider validationVariableProvider = new MapDelegableVariableProvider(transitionMap, executionContext.getVariableProvider());
            validateVariables(user, processDefinition, task.getNodeId(), variables, validationVariableProvider);
            executionContext.setVariableValues(variables);
            Transition transition;
            if (transitionName != null) {
                transition = node.getLeavingTransitionNotNull(transitionName);
            } else {
                transition = node.getDefaultLeavingTransitionNotNull();
            }
            executionContext.setTransientVariable(WfProcess.SELECTED_TRANSITION_KEY, transition.getName());
            task.end(executionContext, TaskCompletionInfo.createForUser(completionBy, user.getActor()));
            if (!(node instanceof Synchronizable) || !((Synchronizable) node).isAsync()) {
                signalToken(executionContext, task, transition);
            }
            log.info("Task '" + task.getName() + "' was done by " + user + " in process " + task.getProcess());
            ProcessExecutionErrors.removeProcessError(task.getProcess().getId(), task.getNodeId());
        } catch (ValidationException ex) {
            throw Throwables.propagate(ex);
        } catch (Throwable th) {
            ProcessExecutionErrors.addProcessError(task, th);
            throw Throwables.propagate(th);
        }
    }

    private void signalToken(ExecutionContext executionContext, Task task, Transition transition) {
        Token token = executionContext.getToken();
        if (!Objects.equal(task.getNodeId(), token.getNodeId())) {
            throw new InternalApplicationException("completion of " + task + " failed. Different node id in task and token: " + token.getNodeId());
        }
        InteractionNode node = (InteractionNode) executionContext.getNode();
        if (node instanceof MultiTaskNode && !((MultiTaskNode) node).isCompletionTriggersSignal(task)) {
            log.debug("!MultiTaskNode.isCompletionTriggersSignal in " + task);
            return;
        }
        log.debug("completion of " + task + " by " + transition);
        token.signal(executionContext, transition);
    }

    public void markTaskOpened(User user, Long taskId) {
        Task task = taskDAO.getNotNull(taskId);
        task.getOpenedByExecutorIds().add(user.getActor().getId());
    }

    public WfTask getTask(User user, Long taskId) {
        Task task = taskDAO.getNotNull(taskId);
        return taskObjectFactory.create(task, user.getActor(), false, null);
    }

    public Long getProcessId(User user, Long taskId) {
        return taskDAO.getNotNull(taskId).getProcess().getId();
    }

    public List<WfTask> getTasks(User user, BatchPresentation batchPresentation) {
        return taskListBuilder.getTasks(user.getActor(), batchPresentation);
    }

    public List<WfTask> getTasks(User user, Long processId) throws ProcessDoesNotExistException {
        List<WfTask> result = Lists.newArrayList();
        Process process = processDAO.getNotNull(processId);
        checkPermissionAllowed(user, process, ProcessPermission.READ);
        for (Task task : process.getTasks()) {
            result.add(taskObjectFactory.create(task, user.getActor(), false, null));
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
        AssignmentHelper.assign(new ExecutionContext(processDefinition, process), swimlane, Lists.newArrayList(executor));
    }

    public void assignTask(User user, Long taskId, Executor previousOwner, Executor newExecutor) throws TaskAlreadyAcceptedException {
        // check assigned executor for the task
        Task task = taskDAO.getNotNull(taskId);
        if (!Objects.equal(previousOwner, task.getExecutor())) {
            throw new TaskAlreadyAcceptedException(task.getName());
        }
        if (SystemProperties.isTaskAssignmentStrictRulesEnabled()) {
            checkCanParticipate(user.getActor(), task);
        }
        ProcessDefinition processDefinition = getDefinition(task);
        AssignmentHelper.reassignTask(new ExecutionContext(processDefinition, task), task, newExecutor, false);
    }

}
