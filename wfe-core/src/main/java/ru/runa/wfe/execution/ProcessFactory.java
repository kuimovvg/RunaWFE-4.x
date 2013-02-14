package ru.runa.wfe.execution;

import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;

import ru.runa.wfe.audit.ProcessStartLog;
import ru.runa.wfe.audit.SubprocessStartLog;
import ru.runa.wfe.definition.DefinitionPermission;
import ru.runa.wfe.execution.dao.NodeProcessDAO;
import ru.runa.wfe.execution.dao.ProcessDAO;
import ru.runa.wfe.lang.Event;
import ru.runa.wfe.lang.Node;
import ru.runa.wfe.lang.ProcessDefinition;
import ru.runa.wfe.lang.StartState;
import ru.runa.wfe.lang.SwimlaneDefinition;
import ru.runa.wfe.lang.Transition;
import ru.runa.wfe.security.Permission;
import ru.runa.wfe.security.dao.PermissionDAO;
import ru.runa.wfe.task.Task;
import ru.runa.wfe.user.Actor;
import ru.runa.wfe.user.Executor;
import ru.runa.wfe.user.SystemExecutors;
import ru.runa.wfe.user.dao.ExecutorDAO;

import com.google.common.base.Objects;
import com.google.common.base.Preconditions;

public class ProcessFactory {
    @Autowired
    private ProcessDAO processDAO;
    @Autowired
    private PermissionDAO permissionDAO;
    @Autowired
    private ExecutorDAO executorDAO;
    @Autowired
    private NodeProcessDAO nodeProcessDAO;

    private static final Map<Permission, Permission> DEFINITION_TO_PROCESS_PERMISSION_MAP;
    static {
        DEFINITION_TO_PROCESS_PERMISSION_MAP = new HashMap<Permission, Permission>();
        DEFINITION_TO_PROCESS_PERMISSION_MAP.put(DefinitionPermission.READ_STARTED_PROCESS, ProcessPermission.READ);
        DEFINITION_TO_PROCESS_PERMISSION_MAP.put(DefinitionPermission.CANCEL_STARTED_PROCESS, ProcessPermission.CANCEL_PROCESS);
    }

    private Set<Permission> getProcessPermissions(Executor executor, ProcessDefinition processDefinition) {
        List<Permission> definitionPermissions = permissionDAO.getIssuedPermissions(executor, processDefinition.getDeployment());
        Set<Permission> result = new HashSet<Permission>();
        for (Permission permission : definitionPermissions) {
            if (DEFINITION_TO_PROCESS_PERMISSION_MAP.containsKey(permission)) {
                result.add(DEFINITION_TO_PROCESS_PERMISSION_MAP.get(permission));
            }
        }
        return result;
    }

    /**
     * Creates and starts a new process for the given process definition, puts
     * the root-token (=main path of execution) in the start state and executes
     * the initial node.
     * 
     * @param variables
     *            will be inserted into the context variables after the context
     *            submodule has been created and before the process-start event
     *            is fired, which is also before the execution of the initial
     *            node.
     */
    public Process startProcess(ProcessDefinition processDefinition, Map<String, Object> variables, Actor actor, String transitionName) {
        Preconditions.checkNotNull(processDefinition, "can't start a process when processDefinition is null");
        Preconditions.checkNotNull(actor, "can't start a process when actor is null");
        Process process = startProcessInternal(processDefinition, variables, actor, transitionName);
        grantProcessPermissions(processDefinition, process, actor);
        return process;
    }

    private void grantProcessPermissions(ProcessDefinition processDefinition, Process process, Actor actor) {
        boolean permissionsAreSetToProcessStarter = false;
        for (Executor executor : permissionDAO.getExecutorsWithPermission(processDefinition.getDeployment())) {
            Set<Permission> permissions = getProcessPermissions(executor, processDefinition);
            if (Objects.equal(actor, executor)) {
                Executor processStarter = executorDAO.getExecutor(SystemExecutors.PROCESS_STARTER_NAME);
                Set<Permission> processStarterPermissions = getProcessPermissions(processStarter, processDefinition);
                permissions = Permission.mergePermissions(permissions, processStarterPermissions);
                permissionsAreSetToProcessStarter = true;
            }
            permissionDAO.setPermissions(executor, permissions, process);
        }
        if (!permissionsAreSetToProcessStarter) {
            Executor processStarter = executorDAO.getExecutor(SystemExecutors.PROCESS_STARTER_NAME);
            Set<Permission> processStarterPermissions = getProcessPermissions(processStarter, processDefinition);
            permissionDAO.setPermissions(actor, processStarterPermissions, process);
        }
    }

    public Process startSubprocess(ExecutionContext parentExecutionContext, ProcessDefinition processDefinition, Map<String, Object> variables) {
        Process parentProcess = parentExecutionContext.getProcess();
        Node subProcessNode = parentExecutionContext.getNode();
        Process subProcess = startProcessInternal(processDefinition, variables, null, null);
        subProcess.setHierarchySubProcess(ProcessHierarchyUtils.createHierarchy(parentProcess.getHierarchySubProcess(), subProcess.getId()));
        nodeProcessDAO.create(new NodeProcess(parentExecutionContext.getToken(), subProcess, subProcessNode));
        parentExecutionContext.addLog(new SubprocessStartLog(subProcessNode, subProcess));
        grantSubprocessPermissions(processDefinition, subProcess, parentProcess);
        ExecutionContext executionContext = new ExecutionContext(processDefinition, subProcess);
        subProcessNode.fireEvent(executionContext, Event.EVENTTYPE_SUBPROCESS_CREATED);
        return subProcess;
    }

    private void grantSubprocessPermissions(ProcessDefinition processDefinition, Process subProcess, Process parentProcess) {
        Set<Executor> executors = new HashSet<Executor>();
        executors.addAll(permissionDAO.getExecutorsWithPermission(processDefinition.getDeployment()));
        executors.addAll(permissionDAO.getExecutorsWithPermission(parentProcess));
        for (Executor executor : executors) {
            List<Permission> permissionsByParentProcess = permissionDAO.getIssuedPermissions(executor, parentProcess);
            Set<Permission> permissionsByDefinition = getProcessPermissions(executor, processDefinition);
            Set<Permission> permissions = Permission.mergePermissions(permissionsByParentProcess, permissionsByDefinition);
            permissionDAO.setPermissions(executor, permissions, subProcess);
        }
    }

    private Process startProcessInternal(ProcessDefinition processDefinition, Map<String, Object> variables, Actor actor, String transitionName) {
        Preconditions.checkNotNull(processDefinition, "can't create a process when processDefinition is null");
        Process process = new Process();
        process.setStartDate(new Date());
        process.setDeployment(processDefinition.getDeployment());
        process.setSwimlanes(new HashSet<Swimlane>());
        process.setTasks(new HashSet<Task>());
        Token rootToken = new Token(processDefinition, process);
        process.setRootToken(rootToken);
        processDAO.create(process);
        ExecutionContext executionContext = new ExecutionContext(processDefinition, rootToken);

        if (actor != null) {
            executionContext.addLog(new ProcessStartLog(actor));
        }

        executionContext.setVariables(variables);

        if (actor != null) {
            SwimlaneDefinition swimlaneDefinition = processDefinition.getStartStateNotNull().getFirstTaskNotNull().getSwimlane();
            Swimlane swimlane = process.getSwimlaneNotNull(swimlaneDefinition);
            swimlane.assignExecutor(executionContext, actor, false);
        }
        // fire the process start event
        processDefinition.fireEvent(executionContext, Event.EVENTTYPE_PROCESS_START);
        // execute the start node
        StartState startState = processDefinition.getStartStateNotNull();
        // startState.enter(executionContext);
        Transition transition = null;
        if (transitionName != null) {
            transition = processDefinition.getStartStateNotNull().getLeavingTransitionNotNull(transitionName);
        }
        process.setHierarchySubProcess(ProcessHierarchyUtils.createHierarchy(null, process.getId()));
        startState.leave(executionContext, transition);
        return process;
    }
}
