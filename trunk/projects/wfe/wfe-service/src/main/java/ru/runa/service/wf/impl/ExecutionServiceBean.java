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
package ru.runa.service.wf.impl;

import java.util.Date;
import java.util.List;
import java.util.Map;

import javax.ejb.Stateless;
import javax.ejb.TransactionManagement;
import javax.ejb.TransactionManagementType;
import javax.interceptor.Interceptors;
import javax.security.auth.Subject;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.ejb.interceptor.SpringBeanAutowiringInterceptor;

import ru.runa.service.interceptors.EjbExceptionSupport;
import ru.runa.service.interceptors.EjbTransactionSupport;
import ru.runa.service.wf.ExecutionServiceLocal;
import ru.runa.service.wf.ExecutionServiceRemote;
import ru.runa.wfe.audit.ProcessLogFilter;
import ru.runa.wfe.audit.ProcessLogs;
import ru.runa.wfe.audit.SystemLog;
import ru.runa.wfe.audit.logic.AuditLogic;
import ru.runa.wfe.definition.DefinitionDoesNotExistException;
import ru.runa.wfe.execution.ProcessDoesNotExistException;
import ru.runa.wfe.execution.SuperProcessExistsException;
import ru.runa.wfe.execution.dto.WfProcess;
import ru.runa.wfe.execution.dto.WfSwimlane;
import ru.runa.wfe.execution.logic.ExecutionLogic;
import ru.runa.wfe.graph.view.GraphElementPresentation;
import ru.runa.wfe.presentation.BatchPresentation;
import ru.runa.wfe.security.AuthenticationException;
import ru.runa.wfe.security.AuthorizationException;
import ru.runa.wfe.task.TaskAlreadyAcceptedException;
import ru.runa.wfe.task.TaskDoesNotExistException;
import ru.runa.wfe.task.dto.WfTask;
import ru.runa.wfe.task.logic.TaskLogic;
import ru.runa.wfe.user.Actor;
import ru.runa.wfe.user.Executor;
import ru.runa.wfe.user.ExecutorDoesNotExistException;
import ru.runa.wfe.validation.impl.ValidationException;
import ru.runa.wfe.var.dto.WfVariable;
import ru.runa.wfe.var.logic.VariableLogic;

import com.google.common.base.Preconditions;

@Stateless(name = "ExecutionServiceBean")
@TransactionManagement(TransactionManagementType.BEAN)
@Interceptors({ EjbExceptionSupport.class, EjbTransactionSupport.class, SpringBeanAutowiringInterceptor.class })
public class ExecutionServiceBean implements ExecutionServiceLocal, ExecutionServiceRemote {
    @Autowired
    private ExecutionLogic executionLogic;
    @Autowired
    private TaskLogic taskLogic;
    @Autowired
    private VariableLogic variableLogic;
    @Autowired
    private AuditLogic auditLogic;

    @Override
    public Long startProcess(Subject subject, String definitionName, Map<String, Object> variablesMap) throws AuthorizationException,
            AuthenticationException, DefinitionDoesNotExistException, ValidationException {
        Preconditions.checkNotNull(subject);
        return executionLogic.startProcess(subject, definitionName, variablesMap);
    }

    @Override
    public int getAllProcessesCount(Subject subject, BatchPresentation batchPresentation) throws AuthorizationException, AuthenticationException {
        Preconditions.checkNotNull(subject);
        Preconditions.checkNotNull(batchPresentation);
        return executionLogic.getAllProcessesCount(subject, batchPresentation);
    }

    @Override
    public List<WfProcess> getProcesses(Subject subject, BatchPresentation batchPresentation) throws AuthorizationException, AuthenticationException {
        Preconditions.checkNotNull(subject);
        Preconditions.checkNotNull(batchPresentation);
        return executionLogic.getProcesses(subject, batchPresentation);
    }

    @Override
    public WfProcess getProcess(Subject subject, Long id) throws AuthorizationException, AuthenticationException, ProcessDoesNotExistException {
        Preconditions.checkNotNull(subject);
        return executionLogic.getProcess(subject, id);
    }

    @Override
    public WfProcess getParentProcess(Subject subject, Long id) throws ProcessDoesNotExistException, AuthenticationException {
        Preconditions.checkNotNull(subject);
        return executionLogic.getParentProcess(subject, id);
    }

    @Override
    public List<WfTask> getTasks(Subject subject, BatchPresentation batchPresentation) throws AuthorizationException, AuthenticationException {
        Preconditions.checkNotNull(subject);
        Preconditions.checkNotNull(batchPresentation);
        return taskLogic.getTasks(subject, batchPresentation);
    }

    @Override
    public WfTask getTask(Subject subject, Long taskId) throws AuthorizationException, AuthenticationException {
        Preconditions.checkNotNull(subject);
        return taskLogic.getTask(subject, taskId);
    }

    @Override
    public List<WfVariable> getVariables(Subject subject, Long processId) throws AuthorizationException, ProcessDoesNotExistException {
        Preconditions.checkNotNull(subject);
        return variableLogic.getVariables(subject, processId);
    }

    @Override
    public Map<Long, Object> getVariableValuesFromProcesses(Subject subject, List<Long> processIds, String variableName) {
        Preconditions.checkNotNull(subject);
        return variableLogic.getVariableValueFromProcesses(subject, processIds, variableName);
    }

    @Override
    public WfVariable getVariable(Subject subject, Long processId, String variableName) throws AuthorizationException {
        Preconditions.checkNotNull(subject);
        return variableLogic.getVariable(subject, processId, variableName);
    }

    @Override
    public void updateVariables(Subject subject, Long processId, Map<String, Object> variables) throws ProcessDoesNotExistException {
        Preconditions.checkNotNull(subject);
        variableLogic.updateVariables(subject, processId, variables);
    }

    @Override
    public void completeTask(Subject subject, Long taskId, Map<String, Object> variables) throws AuthorizationException, AuthenticationException,
            TaskDoesNotExistException, ExecutorDoesNotExistException, ValidationException {
        Preconditions.checkNotNull(subject);
        taskLogic.completeTask(subject, taskId, variables);
    }

    @Override
    public void cancelProcess(Subject subject, Long processId) throws AuthorizationException, AuthenticationException, ProcessDoesNotExistException {
        Preconditions.checkNotNull(subject);
        executionLogic.cancelProcess(subject, processId);
    }

    @Override
    public List<WfSwimlane> getSwimlanes(Subject subject, Long processId) throws AuthorizationException, AuthenticationException,
            ProcessDoesNotExistException {
        Preconditions.checkNotNull(subject);
        return taskLogic.getSwimlanes(subject, processId);
    }

    @Override
    public List<WfTask> getActiveTasks(Subject subject, Long processId) throws AuthorizationException, AuthenticationException,
            ProcessDoesNotExistException, ExecutorDoesNotExistException {
        Preconditions.checkNotNull(subject);
        return taskLogic.getActiveTasks(subject, processId);
    }

    @Override
    public byte[] getProcessDiagram(Subject subject, Long processId, Long taskId, Long childProcessId) throws AuthorizationException,
            AuthenticationException, ProcessDoesNotExistException {
        Preconditions.checkNotNull(subject);
        return executionLogic.getProcessDiagram(subject, processId, taskId, childProcessId);
    }

    @Override
    public byte[] getProcessHistoryDiagram(Subject subject, Long processId, Long taskId) throws AuthorizationException, AuthenticationException,
            ProcessDoesNotExistException {
        Preconditions.checkNotNull(subject);
        return executionLogic.getProcessHistoryDiagram(subject, processId, taskId);
    }

    @Override
    public List<GraphElementPresentation> getProcessUIHistoryData(Subject subject, Long processId, Long taskId) throws AuthorizationException,
            AuthenticationException, ProcessDoesNotExistException {
        Preconditions.checkNotNull(subject);
        return executionLogic.getProcessUIHistoryData(subject, processId, taskId);
    }

    @Override
    public List<GraphElementPresentation> getProcessGraphElements(Subject subject, Long processId) throws AuthenticationException,
            AuthorizationException {
        Preconditions.checkNotNull(subject);
        return executionLogic.getProcessGraphElements(subject, processId);
    }

    @Override
    public void assignSwimlane(Subject subject, Long processId, String swimlaneName, Executor executor) throws AuthenticationException {
        Preconditions.checkNotNull(subject);
        taskLogic.assignSwimlane(subject, processId, swimlaneName, executor);
    }

    @Override
    public void assignTask(Subject subject, Long taskId, Executor previousOwner, Actor actor) throws AuthenticationException,
            TaskAlreadyAcceptedException, ExecutorDoesNotExistException {
        Preconditions.checkNotNull(subject);
        taskLogic.assignTask(subject, taskId, previousOwner, actor);
    }

    @Override
    public ProcessLogs getProcessLogs(Subject subject, ProcessLogFilter filter) {
        Preconditions.checkNotNull(subject);
        return auditLogic.getProcessLogs(subject, filter);
    }

    @Override
    public void markTaskOpened(Subject subject, Long taskId) throws AuthenticationException, TaskDoesNotExistException {
        Preconditions.checkNotNull(subject);
        taskLogic.markTaskOpened(subject, taskId);
    }

    @Override
    public void removeProcesses(Subject subject, Date startDate, Date finishDate, String name, int version, Long id, Long idTill,
            boolean onlyFinished, boolean dateInterval) throws AuthenticationException, ProcessDoesNotExistException, SuperProcessExistsException {
        Preconditions.checkNotNull(subject);
        // archivingLogic.removeProcesses(subject, startDate, finishDate, name, version, id, idTill, onlyFinished, dateInterval);
    }

    @Override
    public List<SystemLog> getSystemLogs(Subject subject, BatchPresentation batchPresentation) throws AuthorizationException, AuthenticationException {
        Preconditions.checkNotNull(subject);
        Preconditions.checkNotNull(batchPresentation);
        return auditLogic.getSystemLogs(subject, batchPresentation);
    }

    @Override
    public int getSystemLogsCount(Subject subject, BatchPresentation batchPresentation) throws AuthorizationException, AuthenticationException {
        Preconditions.checkNotNull(subject);
        Preconditions.checkNotNull(batchPresentation);
        return auditLogic.getSystemLogsCount(subject, batchPresentation);
    }
}
