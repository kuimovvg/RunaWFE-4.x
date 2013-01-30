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

import ru.runa.service.wf.ExecutionService;
import ru.runa.wfe.audit.ProcessLogFilter;
import ru.runa.wfe.audit.ProcessLogs;
import ru.runa.wfe.audit.SystemLog;
import ru.runa.wfe.definition.DefinitionDoesNotExistException;
import ru.runa.wfe.execution.ParentProcessExistsException;
import ru.runa.wfe.execution.ProcessDoesNotExistException;
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
import ru.runa.wfe.user.User;
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
    public Long startProcess(User user, String definitionName, Map<String, Object> variablesMap) throws AuthorizationException,
            AuthenticationException, DefinitionDoesNotExistException, ValidationException {
        return getExecutionService().startProcess(user, definitionName, variablesMap);
    }

    @Override
    public void cancelProcess(User user, Long processId) throws AuthorizationException, AuthenticationException, ProcessDoesNotExistException {
        getExecutionService().cancelProcess(user, processId);
    }

    @Override
    public int getAllProcessesCount(User user, BatchPresentation batchPresentation) throws AuthorizationException, AuthenticationException {
        return getExecutionService().getAllProcessesCount(user, batchPresentation);
    }

    @Override
    public List<WfProcess> getProcesses(User user, BatchPresentation batchPresentation) throws AuthorizationException, AuthenticationException {
        return getExecutionService().getProcesses(user, batchPresentation);
    }

    @Override
    public WfProcess getProcess(User user, Long id) throws AuthorizationException, AuthenticationException, ProcessDoesNotExistException {
        return getExecutionService().getProcess(user, id);
    }

    @Override
    public WfProcess getParentProcess(User user, Long id) throws AuthenticationException, ProcessDoesNotExistException {
        return getExecutionService().getParentProcess(user, id);
    }

    @Override
    public List<WfTask> getTasks(User user, BatchPresentation batchPresentation) throws AuthorizationException, AuthenticationException {
        return getExecutionService().getTasks(user, batchPresentation);
    }

    @Override
    public WfTask getTask(User user, Long taskId) throws AuthorizationException, AuthenticationException {
        return getExecutionService().getTask(user, taskId);
    }

    @Override
    public List<WfVariable> getVariables(User user, Long processId) throws AuthorizationException, AuthenticationException,
            ProcessDoesNotExistException {
        return getExecutionService().getVariables(user, processId);
    }

    @Override
    public WfVariable getVariable(User user, Long processId, String variableName) throws ProcessDoesNotExistException {
        return getExecutionService().getVariable(user, processId, variableName);
    }

    @Override
    public Map<Long, Object> getVariableValuesFromProcesses(User user, List<Long> processIds, String variableName) throws AuthenticationException {
        return getExecutionService().getVariableValuesFromProcesses(user, processIds, variableName);
    }

    @Override
    public void updateVariables(User user, Long processId, Map<String, Object> variables) throws ProcessDoesNotExistException {
        getExecutionService().updateVariables(user, processId, variables);
    }

    @Override
    public void completeTask(User user, Long taskId, Map<String, Object> variables) throws AuthorizationException, AuthenticationException,
            TaskDoesNotExistException, ExecutorDoesNotExistException, ValidationException {
        getExecutionService().completeTask(user, taskId, variables);
    }

    @Override
    public List<WfSwimlane> getSwimlanes(User user, Long processId) throws AuthorizationException, AuthenticationException,
            ProcessDoesNotExistException {
        return getExecutionService().getSwimlanes(user, processId);
    }

    @Override
    public List<WfTask> getActiveTasks(User user, Long processId) throws AuthorizationException, AuthenticationException,
            ProcessDoesNotExistException, ExecutorDoesNotExistException {
        return getExecutionService().getActiveTasks(user, processId);
    }

    @Override
    public byte[] getProcessDiagram(User user, Long processId, Long taskId, Long childProcessId) throws AuthorizationException,
            AuthenticationException, ProcessDoesNotExistException {
        return getExecutionService().getProcessDiagram(user, processId, taskId, childProcessId);
    }

    @Override
    public byte[] getProcessHistoryDiagram(User user, Long processId, Long taskId) throws AuthorizationException, AuthenticationException,
            ProcessDoesNotExistException {
        return getExecutionService().getProcessHistoryDiagram(user, processId, taskId);
    }

    @Override
    public List<GraphElementPresentation> getProcessUIHistoryData(User user, Long processId, Long taskId) throws AuthorizationException,
            AuthenticationException, ProcessDoesNotExistException {
        return getExecutionService().getProcessUIHistoryData(user, processId, taskId);
    }

    @Override
    public List<GraphElementPresentation> getProcessGraphElements(User user, Long processId) throws AuthenticationException, AuthorizationException {
        return getExecutionService().getProcessGraphElements(user, processId);
    }

    @Override
    public void assignSwimlane(User user, Long processId, String swimlaneName, Executor executor) throws AuthenticationException {
        getExecutionService().assignSwimlane(user, processId, swimlaneName, executor);
    }

    @Override
    public void assignTask(User user, Long taskId, Executor previousOwner, Actor actor) throws AuthenticationException, TaskAlreadyAcceptedException,
            ExecutorDoesNotExistException {
        getExecutionService().assignTask(user, taskId, previousOwner, actor);
    }

    @Override
    public ProcessLogs getProcessLogs(User user, ProcessLogFilter filter) {
        return getExecutionService().getProcessLogs(user, filter);
    }

    @Override
    public void markTaskOpened(User user, Long taskId) throws AuthenticationException, TaskDoesNotExistException {
        getExecutionService().markTaskOpened(user, taskId);
    }

    @Override
    public void removeProcesses(User user, Date startDate, Date finishDate, String name, int version, Long id, Long idTill, boolean onlyFinished,
            boolean dateInterval) throws AuthenticationException, ProcessDoesNotExistException, ParentProcessExistsException {
        getExecutionService().removeProcesses(user, startDate, finishDate, name, version, id, idTill, onlyFinished, dateInterval);
    }

    @Override
    public List<SystemLog> getSystemLogs(User user, BatchPresentation batchPresentation) throws AuthorizationException, AuthenticationException {
        return getExecutionService().getSystemLogs(user, batchPresentation);
    }

    @Override
    public int getSystemLogsCount(User user, BatchPresentation batchPresentation) throws AuthorizationException, AuthenticationException {
        return getExecutionService().getSystemLogsCount(user, batchPresentation);
    }
}
