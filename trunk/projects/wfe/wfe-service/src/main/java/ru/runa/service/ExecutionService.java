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
package ru.runa.service;

import java.util.Date;
import java.util.HashMap;
import java.util.List;

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
import ru.runa.wfe.user.Actor;
import ru.runa.wfe.user.Executor;
import ru.runa.wfe.user.User;
import ru.runa.wfe.validation.impl.ValidationException;
import ru.runa.wfe.var.dto.WfVariable;

/**
 * Process execution service.
 * 
 * @author Dofs
 * @since 2.0
 */
public interface ExecutionService {

    public Long startProcess(User user, String definitionName, HashMap<String, Object> variables) throws DefinitionDoesNotExistException,
            ValidationException;

    public int getAllProcessesCount(User user, BatchPresentation batchPresentation);

    public List<WfProcess> getProcesses(User user, BatchPresentation batchPresentation);

    public WfProcess getProcess(User user, Long processId) throws ProcessDoesNotExistException;

    public WfProcess getParentProcess(User user, Long processId) throws ProcessDoesNotExistException;

    public void cancelProcess(User user, Long processId) throws ProcessDoesNotExistException;

    public List<WfTask> getTasks(User user, BatchPresentation batchPresentation);

    public WfTask getTask(User user, Long taskId) throws TaskDoesNotExistException;

    public List<WfTask> getActiveTasks(User user, Long processId) throws ProcessDoesNotExistException;

    public void assignTask(User user, Long taskId, Executor previousOwner, Actor actor) throws TaskAlreadyAcceptedException;

    public void completeTask(User user, Long taskId, HashMap<String, Object> variables) throws TaskDoesNotExistException, ValidationException;

    public List<WfSwimlane> getSwimlanes(User user, Long processId) throws ProcessDoesNotExistException;

    public void assignSwimlane(User user, Long processId, String swimlaneName, Executor executor) throws ProcessDoesNotExistException;

    public List<WfVariable> getVariables(User user, Long processId) throws ProcessDoesNotExistException;

    public WfVariable getVariable(User user, Long processId, String variableName) throws ProcessDoesNotExistException;

    public HashMap<Long, Object> getVariableValuesFromProcesses(User user, List<Long> processIds, String variableName);

    public void updateVariables(User user, Long processId, HashMap<String, Object> variables) throws ProcessDoesNotExistException;

    public byte[] getProcessDiagram(User user, Long processId, Long taskId, Long childProcessId) throws ProcessDoesNotExistException;

    public List<GraphElementPresentation> getProcessGraphElements(User user, Long processId) throws ProcessDoesNotExistException;

    public List<GraphElementPresentation> getProcessUIHistoryData(User user, Long processId, Long taskId) throws ProcessDoesNotExistException;

    public byte[] getProcessHistoryDiagram(User user, Long processId, Long taskId) throws ProcessDoesNotExistException;

    public void markTaskOpened(User user, Long taskId) throws TaskDoesNotExistException;

    public ProcessLogs getProcessLogs(User user, ProcessLogFilter filter);

    public void removeProcesses(User user, Date startDate, Date finishDate, String name, int version, Long id, Long idTill, boolean onlyFinished,
            boolean dateInterval) throws ProcessDoesNotExistException, ParentProcessExistsException;

    public List<SystemLog> getSystemLogs(User user, BatchPresentation batchPresentation);

    public int getSystemLogsCount(User user, BatchPresentation batchPresentation);
}
