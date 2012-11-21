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
package ru.runa.service.wf;

import java.util.Date;
import java.util.List;
import java.util.Map;

import javax.security.auth.Subject;

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
import ru.runa.wfe.task.TaskAlreadyAcceptedException;
import ru.runa.wfe.task.TaskDoesNotExistException;
import ru.runa.wfe.task.dto.WfTask;
import ru.runa.wfe.user.Actor;
import ru.runa.wfe.user.Executor;
import ru.runa.wfe.validation.impl.ValidationException;
import ru.runa.wfe.var.dto.WfVariable;

/**
 * Process execution service.
 * 
 * @author Dofs
 * @since 2.0
 */
public interface ExecutionService {

    public Long startProcess(Subject subject, String definitionName, Map<String, Object> variables) throws DefinitionDoesNotExistException,
            ValidationException;

    public int getAllProcessesCount(Subject subject, BatchPresentation batchPresentation);

    public List<WfProcess> getProcesses(Subject subject, BatchPresentation batchPresentation);

    public WfProcess getProcess(Subject subject, Long processId) throws ProcessDoesNotExistException;

    public WfProcess getParentProcess(Subject subject, Long processId) throws ProcessDoesNotExistException;

    public void cancelProcess(Subject subject, Long processId) throws ProcessDoesNotExistException;

    public List<WfTask> getTasks(Subject subject, BatchPresentation batchPresentation);

    public WfTask getTask(Subject subject, Long taskId) throws TaskDoesNotExistException;

    public List<WfTask> getActiveTasks(Subject subject, Long processId) throws ProcessDoesNotExistException;

    public void assignTask(Subject subject, Long taskId, Executor previousOwner, Actor actor) throws TaskAlreadyAcceptedException;

    public void completeTask(Subject subject, Long taskId, Map<String, Object> variables) throws TaskDoesNotExistException, ValidationException;

    public List<WfSwimlane> getSwimlanes(Subject subject, Long processId) throws ProcessDoesNotExistException;

    public void assignSwimlane(Subject subject, Long processId, String swimlaneName, Executor executor) throws ProcessDoesNotExistException;

    public List<WfVariable> getVariables(Subject subject, Long processId) throws ProcessDoesNotExistException;

    public WfVariable getVariable(Subject subject, Long processId, String variableName) throws ProcessDoesNotExistException;

    public Map<Long, Object> getVariableValuesFromProcesses(Subject subject, List<Long> processIds, String variableName);

    public void updateVariables(Subject subject, Long processId, Map<String, Object> variables) throws ProcessDoesNotExistException;

    public byte[] getProcessDiagram(Subject subject, Long processId, Long taskId, Long childProcessId) throws ProcessDoesNotExistException;

    public List<GraphElementPresentation> getProcessGraphElements(Subject subject, Long processId) throws ProcessDoesNotExistException;

    public List<GraphElementPresentation> getProcessUIHistoryData(Subject subject, Long processId, Long taskId) throws ProcessDoesNotExistException;

    public byte[] getProcessHistoryDiagram(Subject subject, Long processId, Long taskId) throws ProcessDoesNotExistException;

    public void markTaskOpened(Subject subject, Long taskId) throws TaskDoesNotExistException;

    public ProcessLogs getProcessLogs(Subject subject, ProcessLogFilter filter);

    public void removeProcesses(Subject subject, Date startDate, Date finishDate, String name, int version, Long id, Long idTill,
            boolean onlyFinished, boolean dateInterval) throws ProcessDoesNotExistException, SuperProcessExistsException;

    public List<SystemLog> getSystemLogs(Subject subject, BatchPresentation batchPresentation);

    public int getSystemLogsCount(Subject subject, BatchPresentation batchPresentation);
}
