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
package ru.runa.wf.service;

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

/**
 * Created on 28.09.2004
 */
public interface ExecutionService {
    public byte[] getProcessInstanceHistoryDiagram(Subject subject, Long instanceId, Long taskId) throws AuthorizationException,
            AuthenticationException, ProcessInstanceDoesNotExistException;

    public Date getLastChangeDate();

    public List<TaskInstance> getProcessInstanceTokens(Subject subject, Long processInstanceId) throws AuthenticationException;

    public Long startProcessInstance(Subject subject, String definitionName) throws ProcessDefinitionDoesNotExistException, AuthorizationException,
            AuthenticationException, VariablesValidationException;

    public Long startProcessInstance(Subject subject, String definitionName, Map<String, Object> variablesMap)
            throws ProcessDefinitionDoesNotExistException, AuthorizationException, AuthenticationException, VariablesValidationException;

    public int getAllProcessInstanceStubsCount(Subject subject, BatchPresentation batchPresentation) throws AuthorizationException,
            AuthenticationException;

    public List<ProcessInstanceStub> getProcessInstanceStubs(Subject subject, BatchPresentation batchPresentation) throws AuthorizationException,
            AuthenticationException;

    public ProcessInstanceStub getProcessInstanceStub(Subject subject, Long id) throws ProcessInstanceDoesNotExistException, AuthorizationException,
            AuthenticationException;

    public ProcessInstanceStub getSuperProcessInstanceStub(Subject subject, Long id) throws ProcessInstanceDoesNotExistException,
            AuthenticationException;

    public List<TaskStub> getTasks(Subject subject, BatchPresentation batchPresentation) throws AuthorizationException, AuthenticationException;

    public TaskStub getTask(Subject subject, Long taskId) throws AuthorizationException, AuthenticationException;

    public List<Job> getActiveJobs(Subject subject, Long taskInstanceId) throws AuthenticationException, AuthorizationException;

    public void completeTask(Subject subject, Long taskId, String taskName, Long actorId, Map<String, Object> variables, String transitionName)
            throws TaskDoesNotExistException, AuthorizationException, AuthenticationException, ExecutorOutOfDateException,
            VariablesValidationException;

    public void completeTask(Subject subject, Long taskId, String taskName, Long actorId, Map<String, Object> variables)
            throws TaskDoesNotExistException, AuthorizationException, AuthenticationException, ExecutorOutOfDateException,
            VariablesValidationException;

    public void cancelProcessInstance(Subject subject, Long processInstanceId) throws ProcessInstanceDoesNotExistException, AuthorizationException,
            AuthenticationException;

    public Map<String, Object> getInstanceVariables(Subject subject, Long instanceId) throws AuthorizationException, AuthenticationException,
            ProcessInstanceDoesNotExistException;

    public Map<String, Object> getVariables(Subject subject, Long taskId) throws TaskDoesNotExistException, AuthorizationException,
            AuthenticationException;

    public Object getVariable(Subject subject, Long taskId, String variableName) throws TaskDoesNotExistException, AuthorizationException,
            AuthenticationException;

    public List<Object> getVariable(Subject subject, List<Long> processIds, String variableName) throws AuthenticationException;

    public List<String> getVariableNames(Subject subject, Long taskId) throws TaskDoesNotExistException, AuthorizationException,
            AuthenticationException;

    public void updateVariables(Subject subject, Long taskInstanceId, Map<String, Object> variables) throws AuthorizationException,
            AuthenticationException, TaskDoesNotExistException;

    public void removeVariable(Subject subject, Long taskInstanceId, String variableName) throws AuthorizationException, AuthenticationException,
            TaskDoesNotExistException;

    public ProcessDefinition getProcessDefinition(Subject subject, Long taskId) throws TaskDoesNotExistException, AuthorizationException,
            AuthenticationException;

    public List<SwimlaneStub> getSwimlanes(Subject subject, Long instanceId) throws ProcessInstanceDoesNotExistException, AuthorizationException,
            AuthenticationException;

    public Map<String, List<Executor>> getSwimlaneExecutorMap(Subject subject, Long instanceId, Long swimlaneId)
            throws ProcessInstanceDoesNotExistException, AuthorizationException, AuthenticationException, ExecutorOutOfDateException;

    public byte[] getProcessInstanceDiagram(Subject subject, Long instanceId, Long taskId, Long childProcessId) throws AuthorizationException,
            AuthenticationException, ProcessInstanceDoesNotExistException;

    public List<GraphElementPresentation> getProcessInstanceGraphElements(Subject subject, Long instanceId) throws AuthenticationException,
            AuthorizationException;

    public void assignTask(Subject subject, Long taskId, String taskName, Long actorId) throws AuthenticationException, TaskAlreadyAcceptedException,
            ExecutorOutOfDateException;

    public Object getInvocationLogs(Subject subject, Long processInstanceId, LogPresentationBuilder builder) throws Exception;

    public void createOpenTask(Subject subject, BatchPresentation batchPresentation, Long taskId) throws AuthenticationException,
            TaskDoesNotExistException;

    public void archiveProcessInstances(Subject subject, Date startDate, Date finishDate, String name, int version, Long id, Long idTill,
            boolean onlyFinished, boolean dateInterval) throws AuthenticationException, ProcessInstanceDoesNotExistException,
            SuperProcessInstanceExistsException, SecuredObjectOutOfDateException;

    public void removeProcessInstances(Subject subject, Date startDate, Date finishDate, String name, int version, Long id, Long idTill,
            boolean onlyFinished, boolean dateInterval) throws AuthenticationException, ProcessInstanceDoesNotExistException,
            SuperProcessInstanceExistsException, SecuredObjectOutOfDateException;

    public void restoreProcessInstancesFromArchive(Subject subject, Date startDate, Date finishDate, String name, int version, Long id, Long idTill,
            boolean onlyFinished, boolean dateInterval) throws AuthenticationException, ProcessInstanceDoesNotExistException,
            SuperProcessInstanceExistsException, SecuredObjectOutOfDateException;

    public boolean isProcessInstanceInArchive(Long processId);

    public List<GraphElementPresentation> getProcessInstanceUIHistoryData(Subject subject, Long instanceId, Long taskId)
            throws AuthorizationException, AuthenticationException, ProcessInstanceDoesNotExistException;

    public List<SystemLog> getSystemLogs(Subject subject, BatchPresentation batchPresentation) throws AuthorizationException, AuthenticationException;

    public int getSystemLogsCount(Subject subject, BatchPresentation batchPresentation) throws AuthorizationException, AuthenticationException;
}
