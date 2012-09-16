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
package ru.runa.delegate.impl;

import java.util.Date;
import java.util.List;
import java.util.Map;

import javax.security.auth.Subject;

import ru.runa.af.AuthenticationException;
import ru.runa.af.AuthorizationException;
import ru.runa.af.Executor;
import ru.runa.af.ExecutorOutOfDateException;
import ru.runa.af.SecuredObjectOutOfDateException;
import ru.runa.af.log.SystemLog;
import ru.runa.af.presentation.BatchPresentation;
import ru.runa.bpm.job.Job;
import ru.runa.bpm.taskmgmt.exe.TaskInstance;
import ru.runa.wf.LogPresentationBuilder;
import ru.runa.wf.ProcessDefinition;
import ru.runa.wf.ProcessDefinitionDoesNotExistException;
import ru.runa.wf.ProcessInstanceDoesNotExistException;
import ru.runa.wf.ProcessInstanceStub;
import ru.runa.wf.SuperProcessInstanceExistsException;
import ru.runa.wf.SwimlaneStub;
import ru.runa.wf.TaskAlreadyAcceptedException;
import ru.runa.wf.TaskDoesNotExistException;
import ru.runa.wf.TaskStub;
import ru.runa.wf.form.VariablesValidationException;
import ru.runa.wf.graph.GraphElementPresentation;
import ru.runa.wf.service.ExecutionService;

/**
 * Created on 28.09.2004
 */
public class ExecutionServiceDelegateImpl extends EJB3Delegate implements ExecutionService {
    @Override
    protected String getBeanName() {
        return "ExecutionServiceBean";
    }

    private ExecutionService getExecutionService() {
        return getService();
    }

    @Override
    public Date getLastChangeDate() {
        return getExecutionService().getLastChangeDate();
    }

    @Override
    public List<TaskInstance> getProcessInstanceTokens(Subject subject, Long processInstanceId) throws AuthenticationException {
        return getExecutionService().getProcessInstanceTokens(subject, processInstanceId);
    }

    @Override
    public Long startProcessInstance(Subject subject, String definitionName) throws AuthorizationException, AuthenticationException,
            ProcessDefinitionDoesNotExistException, VariablesValidationException {
        return getExecutionService().startProcessInstance(subject, definitionName);
    }

    @Override
    public Long startProcessInstance(Subject subject, String definitionName, Map<String, Object> variablesMap) throws AuthorizationException,
            AuthenticationException, ProcessDefinitionDoesNotExistException, VariablesValidationException {
        return getExecutionService().startProcessInstance(subject, definitionName, variablesMap);
    }

    @Override
    public void cancelProcessInstance(Subject subject, Long processInstanceId) throws AuthorizationException, AuthenticationException,
            ProcessInstanceDoesNotExistException {
        getExecutionService().cancelProcessInstance(subject, processInstanceId);
    }

    @Override
    public int getAllProcessInstanceStubsCount(Subject subject, BatchPresentation batchPresentation) throws AuthorizationException,
            AuthenticationException {
        return getExecutionService().getAllProcessInstanceStubsCount(subject, batchPresentation);
    }

    @Override
    public List<ProcessInstanceStub> getProcessInstanceStubs(Subject subject, BatchPresentation batchPresentation) throws AuthorizationException,
            AuthenticationException {
        return getExecutionService().getProcessInstanceStubs(subject, batchPresentation);
    }

    @Override
    public ProcessInstanceStub getProcessInstanceStub(Subject subject, Long id) throws AuthorizationException, AuthenticationException,
            ProcessInstanceDoesNotExistException {
        return getExecutionService().getProcessInstanceStub(subject, id);
    }

    @Override
    public ProcessInstanceStub getSuperProcessInstanceStub(Subject subject, Long id) throws AuthenticationException,
            ProcessInstanceDoesNotExistException {
        return getExecutionService().getSuperProcessInstanceStub(subject, id);
    }

    @Override
    public List<TaskStub> getTasks(Subject subject, BatchPresentation batchPresentation) throws AuthorizationException, AuthenticationException {
        return getExecutionService().getTasks(subject, batchPresentation);
    }

    @Override
    public TaskStub getTask(Subject subject, Long taskId) throws AuthorizationException, AuthenticationException {
        return getExecutionService().getTask(subject, taskId);
    }

    @Override
    public Map<String, Object> getInstanceVariables(Subject subject, Long instanceId) throws AuthorizationException, AuthenticationException,
            ProcessInstanceDoesNotExistException {
        return getExecutionService().getInstanceVariables(subject, instanceId);
    }

    @Override
    public Map<String, Object> getVariables(Subject subject, Long taskId) throws AuthorizationException, AuthenticationException,
            TaskDoesNotExistException {
        return getExecutionService().getVariables(subject, taskId);
    }

    @Override
    public Object getVariable(Subject subject, Long taskId, String variableName) throws AuthorizationException, AuthenticationException,
            TaskDoesNotExistException {
        return getExecutionService().getVariable(subject, taskId, variableName);
    }

    @Override
    public List<Object> getVariable(Subject subject, List<Long> processIds, String variableName) throws AuthenticationException {
        return getExecutionService().getVariable(subject, processIds, variableName);
    }

    @Override
    public List<String> getVariableNames(Subject subject, Long taskId) throws AuthorizationException, AuthenticationException,
            TaskDoesNotExistException {
        return getExecutionService().getVariableNames(subject, taskId);
    }

    @Override
    public void updateVariables(Subject subject, Long taskInstanceId, Map<String, Object> variables) throws AuthorizationException,
            AuthenticationException, TaskDoesNotExistException {
        getExecutionService().updateVariables(subject, taskInstanceId, variables);
    }

    @Override
    public void removeVariable(Subject subject, Long taskInstanceId, String variableName) throws AuthorizationException, AuthenticationException,
            TaskDoesNotExistException {
        getExecutionService().removeVariable(subject, taskInstanceId, variableName);
    }

    @Override
    public List<Job> getActiveJobs(Subject subject, Long taskInstanceId) throws AuthenticationException, AuthorizationException {
        return getExecutionService().getActiveJobs(subject, taskInstanceId);
    }

    @Override
    public void completeTask(Subject subject, Long taskId, String taskName, Long actorId, Map<String, Object> variables, String transitionName)
            throws TaskDoesNotExistException, AuthorizationException, AuthenticationException, ExecutorOutOfDateException,
            VariablesValidationException {
        getExecutionService().completeTask(subject, taskId, taskName, actorId, variables, transitionName);
    }

    @Override
    public void completeTask(Subject subject, Long taskId, String taskName, Long actorId, Map<String, Object> variables)
            throws AuthorizationException, AuthenticationException, TaskDoesNotExistException, ExecutorOutOfDateException,
            VariablesValidationException {
        getExecutionService().completeTask(subject, taskId, taskName, actorId, variables);
    }

    @Override
    public ProcessDefinition getProcessDefinition(Subject subject, Long taskId) throws AuthorizationException, AuthenticationException,
            TaskDoesNotExistException {
        return getExecutionService().getProcessDefinition(subject, taskId);
    }

    @Override
    public List<SwimlaneStub> getSwimlanes(Subject subject, Long processId) throws AuthorizationException, AuthenticationException,
            ProcessInstanceDoesNotExistException {
        return getExecutionService().getSwimlanes(subject, processId);
    }

    @Override
    public Map<String, List<Executor>> getSwimlaneExecutorMap(Subject subject, Long processId, Long swimlaneId) throws AuthorizationException,
            AuthenticationException, ProcessInstanceDoesNotExistException, ExecutorOutOfDateException {
        return getExecutionService().getSwimlaneExecutorMap(subject, processId, swimlaneId);
    }

    @Override
    public byte[] getProcessInstanceDiagram(Subject subject, Long instanceId, Long taskId, Long childProcessId) throws AuthorizationException,
            AuthenticationException, ProcessInstanceDoesNotExistException {
        return getExecutionService().getProcessInstanceDiagram(subject, instanceId, taskId, childProcessId);
    }

    @Override
    public byte[] getProcessInstanceHistoryDiagram(Subject subject, Long instanceId, Long taskId) throws AuthorizationException,
            AuthenticationException, ProcessInstanceDoesNotExistException {
        return getExecutionService().getProcessInstanceHistoryDiagram(subject, instanceId, taskId);
    }

    @Override
    public List<GraphElementPresentation> getProcessInstanceUIHistoryData(Subject subject, Long instanceId, Long taskId)
            throws AuthorizationException, AuthenticationException, ProcessInstanceDoesNotExistException {
        return getExecutionService().getProcessInstanceUIHistoryData(subject, instanceId, taskId);
    }

    @Override
    public List<GraphElementPresentation> getProcessInstanceGraphElements(Subject subject, Long instanceId) throws AuthenticationException,
            AuthorizationException {
        return getExecutionService().getProcessInstanceGraphElements(subject, instanceId);
    }

    @Override
    public void assignTask(Subject subject, Long taskId, String taskName, Long actorId) throws AuthenticationException, TaskAlreadyAcceptedException,
            ExecutorOutOfDateException {
        getExecutionService().assignTask(subject, taskId, taskName, actorId);
    }

    @Override
    public Object getInvocationLogs(Subject subject, Long processInstanceId, LogPresentationBuilder builder) throws Exception {
        return getExecutionService().getInvocationLogs(subject, processInstanceId, builder);
    }

    @Override
    public void createOpenTask(Subject subject, BatchPresentation batchPresentation, Long taskId) throws AuthenticationException,
            TaskDoesNotExistException {
        getExecutionService().createOpenTask(subject, batchPresentation, taskId);
    }

    @Override
    public void removeProcessInstances(Subject subject, Date startDate, Date finishDate, String name, int version, Long id, Long idTill,
            boolean onlyFinished, boolean dateInterval) throws AuthenticationException, ProcessInstanceDoesNotExistException,
            SuperProcessInstanceExistsException, SecuredObjectOutOfDateException {
        getExecutionService().removeProcessInstances(subject, startDate, finishDate, name, version, id, idTill, onlyFinished, dateInterval);
    }

    @Override
    public void archiveProcessInstances(Subject subject, Date startDate, Date finishDate, String name, int version, Long id, Long idTill,
            boolean onlyFinished, boolean dateInterval) throws AuthenticationException, ProcessInstanceDoesNotExistException,
            SuperProcessInstanceExistsException, SecuredObjectOutOfDateException {
        getExecutionService().archiveProcessInstances(subject, startDate, finishDate, name, version, id, idTill, onlyFinished, dateInterval);
    }

    @Override
    public void restoreProcessInstancesFromArchive(Subject subject, Date startDate, Date finishDate, String name, int version, Long id, Long idTill,
            boolean onlyFinished, boolean dateInterval) throws AuthenticationException, ProcessInstanceDoesNotExistException,
            SuperProcessInstanceExistsException, SecuredObjectOutOfDateException {
        getExecutionService().restoreProcessInstancesFromArchive(subject, startDate, finishDate, name, version, id, idTill, onlyFinished,
                dateInterval);
    }

    @Override
    public boolean isProcessInstanceInArchive(Long processId) {
        return getExecutionService().isProcessInstanceInArchive(processId);
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
