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
package ru.runa.service.delegate;

import java.util.Date;
import java.util.List;
import java.util.Map;

import javax.security.auth.Subject;

import ru.runa.service.wf.ExecutionService;
import ru.runa.wfe.audit.ProcessLogFilter;
import ru.runa.wfe.audit.ProcessLogs;
import ru.runa.wfe.audit.SystemLog;
import ru.runa.wfe.definition.DefinitionDoesNotExistException;
import ru.runa.wfe.execution.ProcessDoesNotExistException;
import ru.runa.wfe.execution.SuperProcessExistsException;
import ru.runa.wfe.execution.dto.WfProcess;
import ru.runa.wfe.execution.dto.WfSwimlane;
import ru.runa.wfe.graph.view.GraphElementPresentation;
import ru.runa.wfe.presentation.BatchPresentation;
import ru.runa.wfe.security.AuthenticationException;
import ru.runa.wfe.security.AuthorizationException;
import ru.runa.wfe.task.TaskAlreadyAcceptedException;
import ru.runa.wfe.task.TaskDoesNotExistException;
import ru.runa.wfe.task.dto.WfTask;
import ru.runa.wfe.user.Actor;
import ru.runa.wfe.user.Executor;
import ru.runa.wfe.user.ExecutorDoesNotExistException;
import ru.runa.wfe.validation.impl.ValidationException;
import ru.runa.wfe.var.dto.WfVariable;

/**
 * Created on 28.09.2004
 */
public class ExecutionServiceDelegate extends EJB3Delegate implements ExecutionService {

    public ExecutionServiceDelegate() {
        super(ExecutionService.class);
    }

    private ExecutionService getExecutionService() {
        return getService();
    }

    @Override
    public Long startProcess(Subject subject, String definitionName, Map<String, Object> variablesMap) throws AuthorizationException,
            AuthenticationException, DefinitionDoesNotExistException, ValidationException {
        return getExecutionService().startProcess(subject, definitionName, variablesMap);
    }

    @Override
    public void cancelProcess(Subject subject, Long processId) throws AuthorizationException, AuthenticationException, ProcessDoesNotExistException {
        getExecutionService().cancelProcess(subject, processId);
    }

    @Override
    public int getAllProcessesCount(Subject subject, BatchPresentation batchPresentation) throws AuthorizationException, AuthenticationException {
        return getExecutionService().getAllProcessesCount(subject, batchPresentation);
    }

    @Override
    public List<WfProcess> getProcesses(Subject subject, BatchPresentation batchPresentation) throws AuthorizationException, AuthenticationException {
        return getExecutionService().getProcesses(subject, batchPresentation);
    }

    @Override
    public WfProcess getProcess(Subject subject, Long id) throws AuthorizationException, AuthenticationException, ProcessDoesNotExistException {
        return getExecutionService().getProcess(subject, id);
    }

    @Override
    public WfProcess getParentProcess(Subject subject, Long id) throws AuthenticationException, ProcessDoesNotExistException {
        return getExecutionService().getParentProcess(subject, id);
    }

    @Override
    public List<WfTask> getTasks(Subject subject, BatchPresentation batchPresentation) throws AuthorizationException, AuthenticationException {
        return getExecutionService().getTasks(subject, batchPresentation);
    }

    @Override
    public WfTask getTask(Subject subject, Long taskId) throws AuthorizationException, AuthenticationException {
        return getExecutionService().getTask(subject, taskId);
    }

    @Override
    public List<WfVariable> getVariables(Subject subject, Long processId) throws AuthorizationException, AuthenticationException,
            ProcessDoesNotExistException {
        return getExecutionService().getVariables(subject, processId);
    }

    @Override
    public WfVariable getVariable(Subject subject, Long processId, String variableName) throws ProcessDoesNotExistException {
        return getExecutionService().getVariable(subject, processId, variableName);
    }

    @Override
    public Map<Long, Object> getVariableValuesFromProcesses(Subject subject, List<Long> processIds, String variableName)
            throws AuthenticationException {
        return getExecutionService().getVariableValuesFromProcesses(subject, processIds, variableName);
    }

    @Override
    public void updateVariables(Subject subject, Long processId, Map<String, Object> variables) throws ProcessDoesNotExistException {
        getExecutionService().updateVariables(subject, processId, variables);
    }

    @Override
    public void completeTask(Subject subject, Long taskId, Map<String, Object> variables) throws AuthorizationException, AuthenticationException,
            TaskDoesNotExistException, ExecutorDoesNotExistException, ValidationException {
        getExecutionService().completeTask(subject, taskId, variables);
    }

    @Override
    public List<WfSwimlane> getSwimlanes(Subject subject, Long processId) throws AuthorizationException, AuthenticationException,
            ProcessDoesNotExistException {
        return getExecutionService().getSwimlanes(subject, processId);
    }

    @Override
    public List<WfTask> getActiveTasks(Subject subject, Long processId) throws AuthorizationException, AuthenticationException,
            ProcessDoesNotExistException, ExecutorDoesNotExistException {
        return getExecutionService().getActiveTasks(subject, processId);
    }

    @Override
    public byte[] getProcessDiagram(Subject subject, Long processId, Long taskId, Long childProcessId) throws AuthorizationException,
            AuthenticationException, ProcessDoesNotExistException {
        return getExecutionService().getProcessDiagram(subject, processId, taskId, childProcessId);
    }

    @Override
    public byte[] getProcessHistoryDiagram(Subject subject, Long processId, Long taskId) throws AuthorizationException, AuthenticationException,
            ProcessDoesNotExistException {
        return getExecutionService().getProcessHistoryDiagram(subject, processId, taskId);
    }

    @Override
    public List<GraphElementPresentation> getProcessUIHistoryData(Subject subject, Long processId, Long taskId) throws AuthorizationException,
            AuthenticationException, ProcessDoesNotExistException {
        return getExecutionService().getProcessUIHistoryData(subject, processId, taskId);
    }

    @Override
    public List<GraphElementPresentation> getProcessGraphElements(Subject subject, Long processId) throws AuthenticationException,
            AuthorizationException {
        return getExecutionService().getProcessGraphElements(subject, processId);
    }

    @Override
    public void assignSwimlane(Subject subject, Long processId, String swimlaneName, Executor executor) throws AuthenticationException {
        getExecutionService().assignSwimlane(subject, processId, swimlaneName, executor);
    }

    @Override
    public void assignTask(Subject subject, Long taskId, Executor previousOwner, Actor actor) throws AuthenticationException,
            TaskAlreadyAcceptedException, ExecutorDoesNotExistException {
        getExecutionService().assignTask(subject, taskId, previousOwner, actor);
    }

    @Override
    public ProcessLogs getProcessLogs(Subject subject, ProcessLogFilter filter) {
        return getExecutionService().getProcessLogs(subject, filter);
    }

    @Override
    public void markTaskOpened(Subject subject, Long taskId) throws AuthenticationException, TaskDoesNotExistException {
        getExecutionService().markTaskOpened(subject, taskId);
    }

    @Override
    public void removeProcesses(Subject subject, Date startDate, Date finishDate, String name, int version, Long id, Long idTill,
            boolean onlyFinished, boolean dateInterval) throws AuthenticationException, ProcessDoesNotExistException, SuperProcessExistsException {
        getExecutionService().removeProcesses(subject, startDate, finishDate, name, version, id, idTill, onlyFinished, dateInterval);
    }

    @Override
    public List<SystemLog> getSystemLogs(Subject subject, BatchPresentation batchPresentation) throws AuthorizationException, AuthenticationException {
        return getExecutionService().getSystemLogs(subject, batchPresentation);
    }

    @Override
    public int getSystemLogsCount(Subject subject, BatchPresentation batchPresentation) throws AuthorizationException, AuthenticationException {
        return getExecutionService().getSystemLogsCount(subject, batchPresentation);
    }
}
