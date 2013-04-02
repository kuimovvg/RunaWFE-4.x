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
package ru.runa.wfe.service;

import java.util.Date;
import java.util.List;
import java.util.Map;

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
import ru.runa.wfe.task.TaskAlreadyAcceptedException;
import ru.runa.wfe.task.TaskDoesNotExistException;
import ru.runa.wfe.task.dto.WfTask;
import ru.runa.wfe.user.Executor;
import ru.runa.wfe.user.User;
import ru.runa.wfe.validation.ValidationException;
import ru.runa.wfe.var.dto.WfVariable;

/**
 * Process execution service.
 * 
 * @author Dofs
 * @since 2.0
 */
public interface ExecutionService {

    public Long startProcess(User user, String definitionName, Map<String, Object> variables) throws DefinitionDoesNotExistException,
            ValidationException;

    public Long startProcessWS(User user, String definitionName, List<WfVariable> variables) throws DefinitionDoesNotExistException;

    public int getAllProcessesCount(User user, BatchPresentation batchPresentation);

    public List<WfProcess> getProcesses(User user, BatchPresentation batchPresentation);

    public WfProcess getProcess(User user, Long processId) throws ProcessDoesNotExistException;

    public WfProcess getParentProcess(User user, Long processId) throws ProcessDoesNotExistException;

    public void cancelProcess(User user, Long processId) throws ProcessDoesNotExistException;

    public List<WfTask> getTasks(User user, BatchPresentation batchPresentation);

    public WfTask getTask(User user, Long taskId) throws TaskDoesNotExistException;

    public List<WfTask> getProcessTasks(User user, Long processId) throws ProcessDoesNotExistException;

    /**
     * Reassigns task to another executor.
     * 
     * @param previousOwner
     *            old executor (check for multi-threaded change)
     * @param newOwner
     *            new executor
     * @throws TaskAlreadyAcceptedException
     *             if previous owner differs from provided
     */
    public void assignTask(User user, Long taskId, Executor previousOwner, Executor newOwner) throws TaskAlreadyAcceptedException;

    /**
     * Completes task.
     * 
     * @param user
     * @param taskId
     * @param variables
     * @param swimlaneActorId
     *            actor id who will be assigned to task swimlane, can be
     *            <code>null</code>
     * @throws TaskDoesNotExistException
     * @throws ValidationException
     */
    public void completeTask(User user, Long taskId, Map<String, Object> variables, Long swimlaneActorId) throws TaskDoesNotExistException,
            ValidationException;

    public void completeTaskWS(User user, Long taskId, List<WfVariable> variables, Long swimlaneActorId) throws TaskDoesNotExistException;

    public List<WfSwimlane> getSwimlanes(User user, Long processId) throws ProcessDoesNotExistException;

    public void assignSwimlane(User user, Long processId, String swimlaneName, Executor executor) throws ProcessDoesNotExistException;

    public List<WfVariable> getVariables(User user, Long processId) throws ProcessDoesNotExistException;

    public WfVariable getVariable(User user, Long processId, String variableName) throws ProcessDoesNotExistException;

    public Map<Long, WfVariable> getVariablesFromProcesses(User user, List<Long> processIds, String variableName);

    public void updateVariables(User user, Long processId, Map<String, Object> variables) throws ProcessDoesNotExistException;

    public void updateVariablesWS(User user, Long processId, List<WfVariable> variables) throws ProcessDoesNotExistException;

    public byte[] getProcessDiagram(User user, Long processId, Long taskId, Long childProcessId) throws ProcessDoesNotExistException;

    public List<GraphElementPresentation> getProcessGraphElements(User user, Long processId) throws ProcessDoesNotExistException;

    public List<GraphElementPresentation> getProcessUIHistoryData(User user, Long processId, Long taskId) throws ProcessDoesNotExistException;

    public byte[] getProcessHistoryDiagram(User user, Long processId, Long taskId) throws ProcessDoesNotExistException;

    public void markTaskOpened(User user, Long taskId) throws TaskDoesNotExistException;

    public ProcessLogs getProcessLogs(User user, ProcessLogFilter filter);

    public Object getProcessLogValue(User user, Long logId);

    public void removeProcesses(User user, Date startDate, Date finishDate, String name, int version, Long id, Long idTill, boolean onlyFinished,
            boolean dateInterval) throws ProcessDoesNotExistException, ParentProcessExistsException;

    public List<SystemLog> getSystemLogs(User user, BatchPresentation batchPresentation);

    public int getSystemLogsCount(User user, BatchPresentation batchPresentation);
}
