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

import ru.runa.wfe.InternalApplicationException;
import ru.runa.wfe.commons.logic.WFCommonLogic;
import ru.runa.wfe.definition.DefinitionDoesNotExistException;
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
import ru.runa.wfe.graph.image.GraphImageBuilder;
import ru.runa.wfe.graph.image.StartedSubprocessesVisitor;
import ru.runa.wfe.graph.view.GraphElementPresentation;
import ru.runa.wfe.lang.ProcessDefinition;
import ru.runa.wfe.presentation.BatchPresentation;
import ru.runa.wfe.security.AuthenticationException;
import ru.runa.wfe.security.AuthorizationException;
import ru.runa.wfe.security.Permission;
import ru.runa.wfe.security.SecuredObjectType;
import ru.runa.wfe.task.dto.WfTaskFactory;
import ru.runa.wfe.user.User;
import ru.runa.wfe.validation.impl.ValidationException;
import ru.runa.wfe.var.MapDelegableVariableProvider;

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
        Process process = processDAO.getNotNull(processId);
        ProcessDefinition processDefinition = getDefinition(process);
        ExecutionContext executionContext = new ExecutionContext(processDefinition, process);
        checkPermissionAllowed(user, process, ProcessPermission.CANCEL_PROCESS);
        process.cancel(executionContext, user);
        ProcessExecutionErrors.removeProcessErrors(processId);
        log.info("Process " + process + " was cancelled by " + user);
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

    public Long startProcess(User user, String definitionName, Map<String, Object> variablesMap) {
        return startProcessInternal(user, definitionName, variablesMap);
    }

    private List<WfProcess> getProcesses(List<Process> processes) {
        List<WfProcess> result = Lists.newArrayListWithExpectedSize(processes.size());
        for (Process process : processes) {
            result.add(new WfProcess(process));
        }
        return result;
    }

    private Long startProcessInternal(User user, String definitionName, Map<String, Object> variables) {
        try {
            if (variables == null) {
                variables = Maps.newHashMap();
            }
            ProcessDefinition processDefinition = getLatestDefinition(definitionName);
            checkPermissionAllowed(user, processDefinition, DefinitionPermission.START_PROCESS);
            Map<String, Object> defaultValues = processDefinition.getDefaultVariableValues();
            for (Map.Entry<String, Object> entry : defaultValues.entrySet()) {
                if (!variables.containsKey(entry.getKey())) {
                    variables.put(entry.getKey(), entry.getValue());
                }
            }
            validateVariables(processDefinition, processDefinition.getStartStateNotNull().getNodeId(), new MapDelegableVariableProvider(variables,
                    null));
            String transitionName = (String) variables.remove(WfProcess.SELECTED_TRANSITION_KEY);
            Process process = processFactory.startProcess(processDefinition, variables, user, transitionName);
            log.info("Process " + process + " was successfully started");
            return process.getId();
        } catch (Exception e) {
            Throwables.propagateIfInstanceOf(e, InternalApplicationException.class);
            Throwables.propagateIfInstanceOf(e, AuthorizationException.class);
            Throwables.propagateIfInstanceOf(e, AuthenticationException.class);
            Throwables.propagateIfInstanceOf(e, DefinitionDoesNotExistException.class);
            Throwables.propagateIfInstanceOf(e, ValidationException.class);
            throw Throwables.propagate(e);
        }
    }

    public byte[] getProcessDiagram(User user, Long processId, Long taskId, Long childProcessId) throws ProcessDoesNotExistException {
        try {
            Process process = processDAO.getNotNull(processId);
            checkPermissionAllowed(user, process, ProcessPermission.READ);
            ProcessDefinition processDefinition = getDefinition(process);
            Token highlightedToken = null;
            if (taskId != null) {
                highlightedToken = taskDAO.getNotNull(taskId).getToken();
            }
            if (childProcessId != null) {
                highlightedToken = nodeProcessDAO.getNodeProcessByChild(childProcessId).getParentToken();
            }
            GraphImageBuilder builder = new GraphImageBuilder(taskObjectFactory, processDefinition);
            builder.setHighlightedToken(highlightedToken);
            return builder.createDiagram(process, processLogDAO.getPassedTransitions(processDefinition, process));
        } catch (Exception e) {
            throw Throwables.propagate(e);
        }
    }

    public byte[] getProcessHistoryDiagram(User user, Long processId, Long taskId) throws ProcessDoesNotExistException {
        try {
            // Process process = processDAO.getInstanceNotNull(processId);
            // checkPermissionAllowed(user, process, ProcessPermission.READ);
            // ProcessDefinition processDefinition = getDefinition(process);
            // Task task = taskDAO.getTaskNotNull(taskId);
            // Token token = task == null ? null : task.getToken();
            // while (token != null && token.getProcess().getId() !=
            // process.getId()) {
            // token = token.getProcess().getSuperProcessToken();
            // }
            // List<ProcessLog> logs = getProcessLogs(user, processId);
            // GraphConverter converter = new GraphConverter(processDefinition);
            return null;
        } catch (Exception e) {
            throw Throwables.propagate(e);
        }
    }

    public List<GraphElementPresentation> getProcessUIHistoryData(User user, Long processId, Long taskId) throws ProcessDoesNotExistException {
        try {
            // Process process = processDAO.getInstanceNotNull(processId);
            // checkPermissionAllowed(user, process, ProcessPermission.READ);
            // ProcessDefinition processDefinition = getDefinition(process);
            // Task task = taskDAO.getTaskNotNull(taskId);
            // Token token = task == null ? null : task.getToken();
            // while (token != null && token.getProcess().getId() !=
            // process.getId()) {
            // token = token.getProcess().getSuperProcessToken();
            // }
            // List<ProcessLog> logs = getProcessLogs(user, processId);
            // GraphConverter converter = new GraphConverter(processDefinition);
            // List<Token> tokens = tmpDAO.getProcessTokens(process.getId());
            // List<GraphElementPresentation> logElements =
            // converter.getProcessUIHistoryData(user, process, tokens,
            // logs);
            return null;
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
        List<NodeProcess> nodeProcesses = nodeProcessDAO.getNodeProcesses(processId);
        StartedSubprocessesVisitor operation = new StartedSubprocessesVisitor(user, nodeProcesses);
        return getDefinitionGraphElements(user, process.getDefinition().getId(), operation);
    }

}
