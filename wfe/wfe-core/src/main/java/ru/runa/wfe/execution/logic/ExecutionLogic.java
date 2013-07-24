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
package ru.runa.wfe.execution.logic;

import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;

import ru.runa.wfe.audit.ProcessLog;
import ru.runa.wfe.audit.ProcessLogs;
import ru.runa.wfe.commons.TypeConversionUtil;
import ru.runa.wfe.commons.logic.WFCommonLogic;
import ru.runa.wfe.definition.DefinitionPermission;
import ru.runa.wfe.execution.ExecutionContext;
import ru.runa.wfe.execution.NodeProcess;
import ru.runa.wfe.execution.Process;
import ru.runa.wfe.execution.ProcessDoesNotExistException;
import ru.runa.wfe.execution.ProcessFactory;
import ru.runa.wfe.execution.ProcessFilter;
import ru.runa.wfe.execution.ProcessPermission;
import ru.runa.wfe.execution.Token;
import ru.runa.wfe.execution.dto.WfProcess;
import ru.runa.wfe.graph.image.GraphHistoryBuilder;
import ru.runa.wfe.graph.image.GraphImageBuilder;
import ru.runa.wfe.graph.image.StartedSubprocessesVisitor;
import ru.runa.wfe.graph.view.GraphElementPresentation;
import ru.runa.wfe.lang.ProcessDefinition;
import ru.runa.wfe.lang.SwimlaneDefinition;
import ru.runa.wfe.presentation.BatchPresentation;
import ru.runa.wfe.presentation.BatchPresentationFactory;
import ru.runa.wfe.security.Permission;
import ru.runa.wfe.security.SecuredObjectType;
import ru.runa.wfe.task.Task;
import ru.runa.wfe.task.dto.WfTaskFactory;
import ru.runa.wfe.user.Executor;
import ru.runa.wfe.user.User;

import com.google.common.base.Throwables;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

/**
 * Process execution logic.
 * 
 * @author Dofs
 * @since 2.0
 */
public class ExecutionLogic extends WFCommonLogic {
    private static final Log log = LogFactory.getLog(ExecutionLogic.class);
    @Autowired
    private WfTaskFactory taskObjectFactory;
    @Autowired
    private ProcessFactory processFactory;

    public void cancelProcess(User user, Long processId) throws ProcessDoesNotExistException {
        ProcessFilter filter = new ProcessFilter();
        filter.setId(processId);
        cancelProcesses(user, filter);
    }

    public int getAllProcessesCount(User user, BatchPresentation batchPresentation) {
        return getPersistentObjectCount(user, batchPresentation, ProcessPermission.READ, PROCESS_EXECUTION_CLASSES);
    }

    private static final SecuredObjectType[] PROCESS_EXECUTION_CLASSES = { SecuredObjectType.PROCESS };

    public List<WfProcess> getProcesses(User user, BatchPresentation batchPresentation) {
        // Uncomment for WFDEMO (default ordering in processes is decrease time
        // start)
        /*
         * if(batchPresentation.isDefault()){
         * batchPresentation.setFieldsToSort(new int[]{2}, new
         * boolean[]{false}); }
         */
        List<Process> list = getPersistentObjects(user, batchPresentation, ProcessPermission.READ, PROCESS_EXECUTION_CLASSES, true);
        return getProcesses(list);
    }

    public List<WfProcess> getProcessesForDefinitionName(User user, String processDefinitionName) {
        ProcessFilter filter = new ProcessFilter();
        filter.setDefinitionName(processDefinitionName);
        List<Process> process = processDAO.getProcesses(filter);
        process = filterIdentifiable(user, process, ProcessPermission.READ);
        return getProcesses(process);
    }

    public void deleteProcesses(User user, final ProcessFilter filter) {
        List<Process> processes = processDAO.getProcesses(filter);
        // TODO add ProcessPermission.DELETE_PROCESS
        processes = filterIdentifiable(user, processes, ProcessPermission.CANCEL_PROCESS);
        for (Process process : processes) {
            deleteProcess(process);
        }
    }

    public void cancelProcesses(User user, final ProcessFilter filter) {
        List<Process> processes = processDAO.getProcesses(filter);
        processes = filterIdentifiable(user, processes, ProcessPermission.CANCEL_PROCESS);
        for (Process process : processes) {
            ProcessDefinition processDefinition = getDefinition(process);
            ExecutionContext executionContext = new ExecutionContext(processDefinition, process);
            process.end(executionContext, user.getActor());
            log.info(process + " was cancelled by " + user);
        }
    }

    public WfProcess getProcess(User user, Long id) throws ProcessDoesNotExistException {
        Process process = processDAO.getNotNull(id);
        checkPermissionAllowed(user, process, Permission.READ);
        return new WfProcess(process);
    }

    public WfProcess getParentProcess(User user, Long id) throws ProcessDoesNotExistException {
        Process process = processDAO.getNotNull(id);
        NodeProcess nodeProcess = nodeProcessDAO.getNodeProcessByChild(process.getId());
        if (nodeProcess == null) {
            return null;
        }
        return new WfProcess(nodeProcess.getProcess());
    }

    public List<WfProcess> getSubprocessesRecursive(User user, Long id) throws ProcessDoesNotExistException {
        Process process = processDAO.getNotNull(id);
        List<Process> subprocesses = nodeProcessDAO.getSubprocessesRecursive(process);
        subprocesses = filterIdentifiable(user, subprocesses, ProcessPermission.READ);
        return getProcesses(subprocesses);
    }

    private List<WfProcess> getProcesses(List<Process> processes) {
        List<WfProcess> result = Lists.newArrayListWithExpectedSize(processes.size());
        for (Process process : processes) {
            result.add(new WfProcess(process));
        }
        return result;
    }

    public Long startProcess(User user, String definitionName, Map<String, Object> variables) {
        if (variables == null) {
            variables = Maps.newHashMap();
        }
        ProcessDefinition processDefinition = getLatestDefinition(definitionName);
        checkPermissionAllowed(user, processDefinition.getDeployment(), DefinitionPermission.START_PROCESS);
        Map<String, Object> defaultValues = processDefinition.getDefaultVariableValues();
        for (Map.Entry<String, Object> entry : defaultValues.entrySet()) {
            if (!variables.containsKey(entry.getKey())) {
                variables.put(entry.getKey(), entry.getValue());
            }
        }
        validateVariables(user, processDefinition, processDefinition.getStartStateNotNull().getNodeId(), variables, null);
        String transitionName = (String) variables.remove(WfProcess.SELECTED_TRANSITION_KEY);
        Process process = processFactory.startProcess(processDefinition, variables, user.getActor(), transitionName);
        SwimlaneDefinition startTaskSwimlaneDefinition = processDefinition.getStartStateNotNull().getFirstTaskNotNull().getSwimlane();
        Object predefinedProcessStarterObject = variables.get(startTaskSwimlaneDefinition.getName());
        if (predefinedProcessStarterObject != null) {
            Executor predefinedProcessStarter = TypeConversionUtil.convertTo(Executor.class, predefinedProcessStarterObject);
            ExecutionContext executionContext = new ExecutionContext(processDefinition, process);
            process.getSwimlaneNotNull(startTaskSwimlaneDefinition).assignExecutor(executionContext, predefinedProcessStarter, true);
        }
        log.info("Process " + process + " was successfully started");
        return process.getId();
    }

    public byte[] getProcessDiagram(User user, Long processId, Long taskId, Long childProcessId) throws ProcessDoesNotExistException {
        try {
            Process process = processDAO.getNotNull(processId);
            checkPermissionAllowed(user, process, ProcessPermission.READ);
            ProcessDefinition processDefinition = getDefinition(process);
            Token highlightedToken = null;
            if (taskId != null) {
                Task task = taskDAO.get(taskId);
                if (task != null) {
                    log.debug("Task id='" + taskId + "' is null due to completion and graph auto-refresh?");
                    highlightedToken = task.getToken();
                }
            }
            if (childProcessId != null) {
                highlightedToken = nodeProcessDAO.getNodeProcessByChild(childProcessId).getParentToken();
            }
            GraphImageBuilder builder = new GraphImageBuilder(taskObjectFactory, processDefinition);
            builder.setHighlightedToken(highlightedToken);
            ProcessLogs logs = new ProcessLogs(processId);
            logs.addLogs(processLogDAO.getAll(processId), false);
            return builder.createDiagram(process, logs);
        } catch (Exception e) {
            throw Throwables.propagate(e);
        }
    }

    public byte[] getProcessHistoryDiagram(User user, Long processId, Long taskId) throws ProcessDoesNotExistException {
        try {
            Process process = processDAO.getNotNull(processId);
            checkPermissionAllowed(user, process, ProcessPermission.READ);
            ProcessDefinition processDefinition = getDefinition(process);
            List<ProcessLog> logs = processLogDAO.getAll(processId);
            List<Executor> executors = executorDAO.getAllExecutors(BatchPresentationFactory.EXECUTORS.createNonPaged());
            GraphHistoryBuilder converter = new GraphHistoryBuilder(executors, taskObjectFactory, processDefinition, logs);
            return converter.createDiagram(process, processLogDAO.getPassedTransitions(processDefinition, process));
        } catch (Exception e) {
            throw Throwables.propagate(e);
        }
    }

    public List<GraphElementPresentation> getProcessUIHistoryData(User user, Long processId, Long taskId) throws ProcessDoesNotExistException {
        try {
            Process process = processDAO.getNotNull(processId);
            checkPermissionAllowed(user, process, ProcessPermission.READ);
            ProcessDefinition processDefinition = getDefinition(process);
            List<ProcessLog> logs = processLogDAO.getAll(processId);
            List<Executor> executors = executorDAO.getAllExecutors(BatchPresentationFactory.EXECUTORS.createNonPaged());
            GraphHistoryBuilder converter = new GraphHistoryBuilder(executors, taskObjectFactory, processDefinition, logs);
            converter.createDiagram(process, processLogDAO.getPassedTransitions(processDefinition, process));
            return converter.getLogElements();
        } catch (Exception e) {
            throw Throwables.propagate(e);
        }
    }

    /**
     * Loads graph presentation elements for process definition and set identity
     * of started subprocesses.
     * 
     * @param user
     *            Current user.
     * @param definitionId
     *            Identity of process definition, which presentation elements
     *            must be loaded.
     * @return List of graph presentation elements.
     */
    public List<GraphElementPresentation> getProcessGraphElements(User user, Long processId) {
        Process process = processDAO.getNotNull(processId);
        ProcessDefinition definition = getDefinition(process.getDeployment().getId());
        List<NodeProcess> nodeProcesses = nodeProcessDAO.getNodeProcesses(processId);
        StartedSubprocessesVisitor operation = new StartedSubprocessesVisitor(user, nodeProcesses);
        return getDefinitionGraphElements(user, definition, operation);
    }

}
