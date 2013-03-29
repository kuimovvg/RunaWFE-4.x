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
package ru.runa.wfe.service.delegate;

import java.util.Date;
import java.util.List;
import java.util.Map;

import ru.runa.wfe.audit.ProcessLogFilter;
import ru.runa.wfe.audit.ProcessLogs;
import ru.runa.wfe.audit.SystemLog;
import ru.runa.wfe.execution.ProcessDoesNotExistException;
import ru.runa.wfe.execution.dto.WfProcess;
import ru.runa.wfe.execution.dto.WfSwimlane;
import ru.runa.wfe.graph.view.GraphElementPresentation;
import ru.runa.wfe.presentation.BatchPresentation;
import ru.runa.wfe.service.ExecutionService;
import ru.runa.wfe.task.dto.WfTask;
import ru.runa.wfe.user.Executor;
import ru.runa.wfe.user.User;
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
    public Long startProcess(User user, String definitionName, Map<String, Object> variablesMap) {
        return getExecutionService().startProcess(user, definitionName, variablesMap);
    }

    @Override
    public Long startProcessWS(User user, String definitionName, List<WfVariable> variables) {
        throw new UnsupportedOperationException("This method designed for WebServices API only. Use startProcess(User, String, Map<String, Object>)");
    }

    @Override
    public void cancelProcess(User user, Long processId) {
        getExecutionService().cancelProcess(user, processId);
    }

    @Override
    public int getAllProcessesCount(User user, BatchPresentation batchPresentation) {
        return getExecutionService().getAllProcessesCount(user, batchPresentation);
    }

    @Override
    public List<WfProcess> getProcesses(User user, BatchPresentation batchPresentation) {
        return getExecutionService().getProcesses(user, batchPresentation);
    }

    @Override
    public WfProcess getProcess(User user, Long id) {
        return getExecutionService().getProcess(user, id);
    }

    @Override
    public WfProcess getParentProcess(User user, Long id) {
        return getExecutionService().getParentProcess(user, id);
    }

    @Override
    public List<WfTask> getTasks(User user, BatchPresentation batchPresentation) {
        return getExecutionService().getTasks(user, batchPresentation);
    }

    @Override
    public WfTask getTask(User user, Long taskId) {
        return getExecutionService().getTask(user, taskId);
    }

    @Override
    public List<WfVariable> getVariables(User user, Long processId) {
        return getExecutionService().getVariables(user, processId);
    }

    @Override
    public WfVariable getVariable(User user, Long processId, String variableName) {
        return getExecutionService().getVariable(user, processId, variableName);
    }

    @Override
    public Map<Long, WfVariable> getVariablesFromProcesses(User user, List<Long> processIds, String variableName) {
        return getExecutionService().getVariablesFromProcesses(user, processIds, variableName);
    }

    @Override
    public void updateVariables(User user, Long processId, Map<String, Object> variables) {
        getExecutionService().updateVariables(user, processId, variables);
    }

    @Override
    public void updateVariablesWS(User user, Long processId, List<WfVariable> variables) throws ProcessDoesNotExistException {
        throw new UnsupportedOperationException("This method designed for WebServices API only. Use updateVariables(User, Long, List<WfVariable>)");
    }

    @Override
    public void completeTask(User user, Long taskId, Map<String, Object> variables) {
        getExecutionService().completeTask(user, taskId, variables);
    }

    @Override
    public void completeTaskWS(User user, Long taskId, List<WfVariable> variables) {
        throw new UnsupportedOperationException("This method designed for WebServices API only. Use completeTask(User, Long, Map<String, Object>)");
    }

    @Override
    public List<WfSwimlane> getSwimlanes(User user, Long processId) {
        return getExecutionService().getSwimlanes(user, processId);
    }

    @Override
    public List<WfTask> getProcessTasks(User user, Long processId) {
        return getExecutionService().getProcessTasks(user, processId);
    }

    @Override
    public byte[] getProcessDiagram(User user, Long processId, Long taskId, Long childProcessId) {
        return getExecutionService().getProcessDiagram(user, processId, taskId, childProcessId);
    }

    @Override
    public byte[] getProcessHistoryDiagram(User user, Long processId, Long taskId) {
        return getExecutionService().getProcessHistoryDiagram(user, processId, taskId);
    }

    @Override
    public List<GraphElementPresentation> getProcessUIHistoryData(User user, Long processId, Long taskId) {
        return getExecutionService().getProcessUIHistoryData(user, processId, taskId);
    }

    @Override
    public List<GraphElementPresentation> getProcessGraphElements(User user, Long processId) {
        return getExecutionService().getProcessGraphElements(user, processId);
    }

    @Override
    public void assignSwimlane(User user, Long processId, String swimlaneName, Executor executor) {
        getExecutionService().assignSwimlane(user, processId, swimlaneName, executor);
    }

    @Override
    public void assignTask(User user, Long taskId, Executor previousOwner, Executor newExecutor) {
        getExecutionService().assignTask(user, taskId, previousOwner, newExecutor);
    }

    @Override
    public ProcessLogs getProcessLogs(User user, ProcessLogFilter filter) {
        return getExecutionService().getProcessLogs(user, filter);
    }

    @Override
    public void markTaskOpened(User user, Long taskId) {
        getExecutionService().markTaskOpened(user, taskId);
    }

    @Override
    public void removeProcesses(User user, Date startDate, Date finishDate, String name, int version, Long id, Long idTill, boolean onlyFinished,
            boolean dateInterval) {
        getExecutionService().removeProcesses(user, startDate, finishDate, name, version, id, idTill, onlyFinished, dateInterval);
    }

    @Override
    public List<SystemLog> getSystemLogs(User user, BatchPresentation batchPresentation) {
        return getExecutionService().getSystemLogs(user, batchPresentation);
    }

    @Override
    public int getSystemLogsCount(User user, BatchPresentation batchPresentation) {
        return getExecutionService().getSystemLogsCount(user, batchPresentation);
    }

    @Override
    public Object getProcessLogValue(User user, Long logId) {
        return getExecutionService().getProcessLogValue(user, logId);
    }
}
