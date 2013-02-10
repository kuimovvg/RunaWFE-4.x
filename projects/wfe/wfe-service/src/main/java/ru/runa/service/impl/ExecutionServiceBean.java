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
package ru.runa.service.impl;

import java.util.Date;
import java.util.List;
import java.util.Map;

import javax.ejb.Stateless;
import javax.ejb.TransactionManagement;
import javax.ejb.TransactionManagementType;
import javax.interceptor.Interceptors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.ejb.interceptor.SpringBeanAutowiringInterceptor;

import ru.runa.service.decl.ExecutionServiceLocal;
import ru.runa.service.decl.ExecutionServiceRemote;
import ru.runa.service.interceptors.EjbExceptionSupport;
import ru.runa.service.interceptors.EjbTransactionSupport;
import ru.runa.wfe.audit.ProcessLogFilter;
import ru.runa.wfe.audit.ProcessLogs;
import ru.runa.wfe.audit.SystemLog;
import ru.runa.wfe.audit.logic.AuditLogic;
import ru.runa.wfe.execution.dto.WfProcess;
import ru.runa.wfe.execution.dto.WfSwimlane;
import ru.runa.wfe.execution.logic.ExecutionLogic;
import ru.runa.wfe.graph.view.GraphElementPresentation;
import ru.runa.wfe.presentation.BatchPresentation;
import ru.runa.wfe.task.dto.WfTask;
import ru.runa.wfe.task.logic.TaskLogic;
import ru.runa.wfe.user.Actor;
import ru.runa.wfe.user.Executor;
import ru.runa.wfe.user.User;
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
    public Long startProcess(User user, String definitionName, Map<String, Object> variablesMap) {
        Preconditions.checkNotNull(user);
        return executionLogic.startProcess(user, definitionName, variablesMap);
    }

    @Override
    public int getAllProcessesCount(User user, BatchPresentation batchPresentation) {
        Preconditions.checkNotNull(user);
        Preconditions.checkNotNull(batchPresentation);
        return executionLogic.getAllProcessesCount(user, batchPresentation);
    }

    @Override
    public List<WfProcess> getProcesses(User user, BatchPresentation batchPresentation) {
        Preconditions.checkNotNull(user);
        Preconditions.checkNotNull(batchPresentation);
        return executionLogic.getProcesses(user, batchPresentation);
    }

    @Override
    public WfProcess getProcess(User user, Long id) {
        Preconditions.checkNotNull(user);
        return executionLogic.getProcess(user, id);
    }

    @Override
    public WfProcess getParentProcess(User user, Long id) {
        Preconditions.checkNotNull(user);
        return executionLogic.getParentProcess(user, id);
    }

    @Override
    public List<WfTask> getTasks(User user, BatchPresentation batchPresentation) {
        Preconditions.checkNotNull(user);
        Preconditions.checkNotNull(batchPresentation);
        return taskLogic.getTasks(user, batchPresentation);
    }

    @Override
    public WfTask getTask(User user, Long taskId) {
        Preconditions.checkNotNull(user);
        return taskLogic.getTask(user, taskId);
    }

    @Override
    public List<WfVariable> getVariables(User user, Long processId) {
        Preconditions.checkNotNull(user);
        return variableLogic.getVariables(user, processId);
    }

    @Override
    public Map<Long, Object> getVariableValuesFromProcesses(User user, List<Long> processIds, String variableName) {
        Preconditions.checkNotNull(user);
        return variableLogic.getVariableValueFromProcesses(user, processIds, variableName);
    }

    @Override
    public WfVariable getVariable(User user, Long processId, String variableName) {
        Preconditions.checkNotNull(user);
        return variableLogic.getVariable(user, processId, variableName);
    }

    @Override
    public void updateVariables(User user, Long processId, Map<String, Object> variables) {
        Preconditions.checkNotNull(user);
        variableLogic.updateVariables(user, processId, variables);
    }

    @Override
    public void completeTask(User user, Long taskId, Map<String, Object> variables) {
        Preconditions.checkNotNull(user);
        taskLogic.completeTask(user, taskId, variables);
    }

    @Override
    public void cancelProcess(User user, Long processId) {
        Preconditions.checkNotNull(user);
        executionLogic.cancelProcess(user, processId);
    }

    @Override
    public List<WfSwimlane> getSwimlanes(User user, Long processId) {
        Preconditions.checkNotNull(user);
        return taskLogic.getSwimlanes(user, processId);
    }

    @Override
    public List<WfTask> getActiveTasks(User user, Long processId) {
        Preconditions.checkNotNull(user);
        return taskLogic.getActiveTasks(user, processId);
    }

    @Override
    public byte[] getProcessDiagram(User user, Long processId, Long taskId, Long childProcessId) {
        Preconditions.checkNotNull(user);
        return executionLogic.getProcessDiagram(user, processId, taskId, childProcessId);
    }

    @Override
    public byte[] getProcessHistoryDiagram(User user, Long processId, Long taskId) {
        Preconditions.checkNotNull(user);
        return executionLogic.getProcessHistoryDiagram(user, processId, taskId);
    }

    @Override
    public List<GraphElementPresentation> getProcessUIHistoryData(User user, Long processId, Long taskId) {
        Preconditions.checkNotNull(user);
        return executionLogic.getProcessUIHistoryData(user, processId, taskId);
    }

    @Override
    public List<GraphElementPresentation> getProcessGraphElements(User user, Long processId) {
        Preconditions.checkNotNull(user);
        return executionLogic.getProcessGraphElements(user, processId);
    }

    @Override
    public void assignSwimlane(User user, Long processId, String swimlaneName, Executor executor) {
        Preconditions.checkNotNull(user);
        taskLogic.assignSwimlane(user, processId, swimlaneName, executor);
    }

    @Override
    public void assignTask(User user, Long taskId, Executor previousOwner, Actor actor) {
        Preconditions.checkNotNull(user);
        taskLogic.assignTask(user, taskId, previousOwner, actor);
    }

    @Override
    public ProcessLogs getProcessLogs(User user, ProcessLogFilter filter) {
        Preconditions.checkNotNull(user);
        return auditLogic.getProcessLogs(user, filter);
    }

    @Override
    public void markTaskOpened(User user, Long taskId) {
        Preconditions.checkNotNull(user);
        taskLogic.markTaskOpened(user, taskId);
    }

    @Override
    public void removeProcesses(User user, Date startDate, Date finishDate, String name, int version, Long id, Long idTill, boolean onlyFinished,
            boolean dateInterval) {
        Preconditions.checkNotNull(user);
        // archivingLogic.removeProcesses(user, startDate, finishDate, name,
        // version, id, idTill, onlyFinished, dateInterval);
    }

    @Override
    public List<SystemLog> getSystemLogs(User user, BatchPresentation batchPresentation) {
        Preconditions.checkNotNull(user);
        Preconditions.checkNotNull(batchPresentation);
        return auditLogic.getSystemLogs(user, batchPresentation);
    }

    @Override
    public int getSystemLogsCount(User user, BatchPresentation batchPresentation) {
        Preconditions.checkNotNull(user);
        Preconditions.checkNotNull(batchPresentation);
        return auditLogic.getSystemLogsCount(user, batchPresentation);
    }
}
