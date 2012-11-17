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

import javax.security.auth.Subject;

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
import ru.runa.wfe.security.auth.SubjectPrincipalsHelper;
import ru.runa.wfe.task.dto.WfTaskFactory;
import ru.runa.wfe.user.Actor;
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

    public void cancelProcess(Subject subject, Long processId) throws ProcessDoesNotExistException {
        Actor actor = SubjectPrincipalsHelper.getActor(subject);
        Process process = processDAO.getNotNull(processId);
        ProcessDefinition processDefinition = getDefinition(process);
        ExecutionContext executionContext = new ExecutionContext(processDefinition, process);
        checkPermissionAllowed(subject, process, ProcessPermission.CANCEL_PROCESS);
        process.cancel(executionContext, actor);
        log.info("Process " + process + " was cancelled by " + actor);
        // TODO test without this processDAO.saveProcess(process);
    }

    public int getAllProcessesCount(Subject subject, BatchPresentation batchPresentation) throws InternalApplicationException,
            AuthenticationException {
        return getPersistentObjectCount(subject, batchPresentation, ProcessPermission.READ, PROCESS_EXECUTION_CLASSES);
    }

    private static final SecuredObjectType[] PROCESS_EXECUTION_CLASSES = { SecuredObjectType.PROCESS };

    public List<WfProcess> getProcesses(Subject subject, BatchPresentation batchPresentation) throws AuthenticationException {
        // Uncomment for WFDEMO (default ordering in processes is decrease time
        // start)
        /*
         * if(batchPresentation.isDefault()){
         * batchPresentation.setFieldsToSort(new int[]{2}, new
         * boolean[]{false}); }
         */
        List<Process> list = getPersistentObjects(subject, batchPresentation, ProcessPermission.READ, PROCESS_EXECUTION_CLASSES, true);
        return getProcesses(list);
    }

    public List<WfProcess> getProcessesForDefinitionName(Subject subject, String processDefinitionName) throws AuthenticationException {
        ProcessFilter filter = new ProcessFilter();
        filter.setDefinitionName(processDefinitionName);
        List<Process> process = processDAO.getProcesses(filter);
        process = filterIdentifiable(subject, process, ProcessPermission.READ);
        return getProcesses(process);
    }

    public WfProcess getProcess(Subject subject, Long id) throws AuthorizationException, AuthenticationException, ProcessDoesNotExistException {
        Process process = processDAO.getNotNull(id);
        checkPermissionAllowed(subject, process, Permission.READ);
        return new WfProcess(process);
    }

    public WfProcess getParentProcess(Subject subject, Long id) throws AuthenticationException, ProcessDoesNotExistException {
        Process process = processDAO.getNotNull(id);
        NodeProcess nodeProcess = nodeProcessDAO.getNodeProcessByChild(process.getId());
        if (nodeProcess == null) {
            return null;
        }
        return new WfProcess(nodeProcess.getProcess());
    }

    public Long startProcess(Subject subject, String definitionName, Map<String, Object> variablesMap) throws AuthorizationException,
            AuthenticationException, DefinitionDoesNotExistException, ValidationException {
        return startProcessInternal(subject, definitionName, variablesMap);
    }

    private List<WfProcess> getProcesses(List<Process> processes) {
        List<WfProcess> result = Lists.newArrayListWithExpectedSize(processes.size());
        for (Process process : processes) {
            result.add(new WfProcess(process));
        }
        return result;
    }

    private Long startProcessInternal(Subject subject, String definitionName, Map<String, Object> variables) throws AuthorizationException,
            AuthenticationException, DefinitionDoesNotExistException, ValidationException {
        try {
            Actor actor = SubjectPrincipalsHelper.getActor(subject);
            if (variables == null) {
                variables = Maps.newHashMap();
            }
            ProcessDefinition processDefinition = getLatestDefinition(definitionName);
            checkPermissionAllowed(subject, processDefinition, DefinitionPermission.START_PROCESS);
            Map<String, Object> defaultValues = processDefinition.getDefaultVariableValues();
            for (Map.Entry<String, Object> entry : defaultValues.entrySet()) {
                if (!variables.containsKey(entry.getKey())) {
                    variables.put(entry.getKey(), entry.getValue());
                }
            }
            validateVariables(processDefinition, processDefinition.getStartStateNotNull().getNodeId(), new MapDelegableVariableProvider(variables,
                    null));
            String transitionName = (String) variables.remove(WfProcess.SELECTED_TRANSITION_KEY);
            Process process = processFactory.startProcess(processDefinition, variables, actor, transitionName);
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

    public byte[] getProcessDiagram(Subject subject, Long processId, Long taskId, Long childProcessId) throws AuthorizationException,
            AuthenticationException, ProcessDoesNotExistException {
        try {
            Process process = processDAO.getNotNull(processId);
            checkPermissionAllowed(subject, process, ProcessPermission.READ);
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
            throw new InternalApplicationException("Unable to draw diagram", e);
        }
    }

    public byte[] getProcessHistoryDiagram(Subject subject, Long processId, Long taskId) throws AuthorizationException, AuthenticationException,
            ProcessDoesNotExistException {
        try {
            // Process process = processDAO.getInstanceNotNull(processId);
            // checkPermissionAllowed(subject, process, ProcessPermission.READ);
            // ProcessDefinition processDefinition = getDefinition(process);
            // Task task = taskDAO.getTaskNotNull(taskId);
            // Token token = task == null ? null : task.getToken();
            // while (token != null && token.getProcess().getId() !=
            // process.getId()) {
            // token = token.getProcess().getSuperProcessToken();
            // }
            // List<ProcessLog> logs = getProcessLogs(subject, processId);
            // GraphConverter converter = new GraphConverter(processDefinition);
            return null;
        } catch (Exception e) {
            throw new InternalApplicationException("Unable to draw history diagram", e);
        }
    }

    public List<GraphElementPresentation> getProcessUIHistoryData(Subject subject, Long processId, Long taskId) throws AuthorizationException,
            AuthenticationException, ProcessDoesNotExistException {
        try {
            // Process process = processDAO.getInstanceNotNull(processId);
            // checkPermissionAllowed(subject, process, ProcessPermission.READ);
            // ProcessDefinition processDefinition = getDefinition(process);
            // Task task = taskDAO.getTaskNotNull(taskId);
            // Token token = task == null ? null : task.getToken();
            // while (token != null && token.getProcess().getId() !=
            // process.getId()) {
            // token = token.getProcess().getSuperProcessToken();
            // }
            // List<ProcessLog> logs = getProcessLogs(subject, processId);
            // GraphConverter converter = new GraphConverter(processDefinition);
            // List<Token> tokens = tmpDAO.getProcessTokens(process.getId());
            // List<GraphElementPresentation> logElements =
            // converter.getProcessUIHistoryData(subject, process, tokens,
            // logs);
            return null;
        } catch (Exception e) {
            throw new InternalApplicationException("Unable to retrieve history", e);
        }
    }

    /**
     * Loads graph presentation elements for process definition and set identity
     * of started subprocesses.
     * 
     * @param subject
     *            Current subject.
     * @param definitionId
     *            Identity of process definition, which presentation elements
     *            must be loaded.
     * @return List of graph presentation elements.
     */
    public List<GraphElementPresentation> getProcessGraphElements(Subject subject, Long processId) throws AuthenticationException,
            AuthorizationException {
        try {
            Process process = processDAO.getNotNull(processId);
            List<NodeProcess> nodeProcesses = nodeProcessDAO.getNodeProcesses(processId);
            StartedSubprocessesVisitor operation = new StartedSubprocessesVisitor(subject, nodeProcesses);
            return getDefinitionGraphElements(subject, process.getDefinition().getId(), operation);
        } catch (ProcessDoesNotExistException e) {
            log.warn("Unable to draw diagram", e);
            throw new InternalApplicationException(e);
        }
    }

}
