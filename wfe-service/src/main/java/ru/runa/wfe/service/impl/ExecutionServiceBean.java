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
package ru.runa.wfe.service.impl;

import java.util.Date;
import java.util.List;
import java.util.Map;

import javax.ejb.Stateless;
import javax.ejb.TransactionManagement;
import javax.ejb.TransactionManagementType;
import javax.interceptor.Interceptors;
import javax.jws.WebMethod;
import javax.jws.WebService;
import javax.jws.soap.SOAPBinding;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.ejb.interceptor.SpringBeanAutowiringInterceptor;

import ru.runa.wfe.InternalApplicationException;
import ru.runa.wfe.audit.ProcessLogFilter;
import ru.runa.wfe.audit.ProcessLogs;
import ru.runa.wfe.audit.SystemLog;
import ru.runa.wfe.audit.logic.AuditLogic;
import ru.runa.wfe.execution.dto.WfProcess;
import ru.runa.wfe.execution.dto.WfSwimlane;
import ru.runa.wfe.execution.logic.ExecutionLogic;
import ru.runa.wfe.graph.view.GraphElementPresentation;
import ru.runa.wfe.presentation.BatchPresentation;
import ru.runa.wfe.presentation.BatchPresentationFactory;
import ru.runa.wfe.service.client.FileVariableProxy;
import ru.runa.wfe.service.decl.ExecutionServiceLocal;
import ru.runa.wfe.service.decl.ExecutionServiceRemote;
import ru.runa.wfe.service.interceptors.EjbExceptionSupport;
import ru.runa.wfe.service.interceptors.EjbTransactionSupport;
import ru.runa.wfe.task.dto.WfTask;
import ru.runa.wfe.task.logic.TaskLogic;
import ru.runa.wfe.user.Executor;
import ru.runa.wfe.user.User;
import ru.runa.wfe.var.FileVariable;
import ru.runa.wfe.var.converter.SerializableToByteArrayConverter;
import ru.runa.wfe.var.dto.WfVariable;
import ru.runa.wfe.var.logic.VariableLogic;

import com.google.common.base.Preconditions;
import com.google.common.base.Throwables;
import com.google.common.collect.Maps;

@Stateless(name = "ExecutionServiceBean")
@TransactionManagement(TransactionManagementType.BEAN)
@Interceptors({ EjbExceptionSupport.class, EjbTransactionSupport.class, SpringBeanAutowiringInterceptor.class })
@WebService(name = "ExecutionAPI", serviceName = "ExecutionWebService")
@SOAPBinding
public class ExecutionServiceBean implements ExecutionServiceLocal, ExecutionServiceRemote {
    @Autowired
    private ExecutionLogic executionLogic;
    @Autowired
    private TaskLogic taskLogic;
    @Autowired
    private VariableLogic variableLogic;
    @Autowired
    private AuditLogic auditLogic;

    @WebMethod(exclude = true)
    @Override
    public Long startProcess(User user, String definitionName, Map<String, Object> variables) {
        Preconditions.checkArgument(user != null);
        Preconditions.checkArgument(definitionName != null && !definitionName.isEmpty());
        return executionLogic.startProcess(user, definitionName, variables);
    }

    @Override
    public int getAllProcessesCount(User user, BatchPresentation batchPresentation) {
        Preconditions.checkArgument(user != null);
        if (batchPresentation == null) {
            batchPresentation = BatchPresentationFactory.PROCESSES.createDefault();
        }
        return executionLogic.getAllProcessesCount(user, batchPresentation);
    }

    @Override
    public List<WfProcess> getProcesses(User user, BatchPresentation batchPresentation) {
        Preconditions.checkArgument(user != null);
        if (batchPresentation == null) {
            batchPresentation = BatchPresentationFactory.PROCESSES.createDefault();
        }
        return executionLogic.getProcesses(user, batchPresentation);
    }

    @Override
    public WfProcess getProcess(User user, Long id) {
        Preconditions.checkArgument(user != null);
        return executionLogic.getProcess(user, id);
    }

    @Override
    public WfProcess getParentProcess(User user, Long id) {
        Preconditions.checkArgument(user != null);
        return executionLogic.getParentProcess(user, id);
    }

    @Override
    public List<WfProcess> getSubprocesses(User user, Long id) {
        Preconditions.checkArgument(user != null);
        return executionLogic.getSubprocesses(user, id);
    }

    @Override
    public List<WfTask> getTasks(User user, BatchPresentation batchPresentation) {
        Preconditions.checkArgument(user != null);
        if (batchPresentation == null) {
            batchPresentation = BatchPresentationFactory.TASKS.createDefault();
        }
        return taskLogic.getTasks(user, batchPresentation);
    }

    @Override
    public WfTask getTask(User user, Long taskId) {
        Preconditions.checkArgument(user != null);
        return taskLogic.getTask(user, taskId);
    }

    @Override
    public List<WfVariable> getVariables(User user, Long processId) {
        Preconditions.checkArgument(user != null);
        List<WfVariable> list = variableLogic.getVariables(user, processId);
        for (WfVariable variable : list) {
            convertValueToProxy(user, processId, variable);
        }
        return list;
    }

    @WebMethod(exclude = true)
    @Override
    public Map<Long, WfVariable> getVariablesFromProcesses(User user, List<Long> processIds, String variableName) {
        Preconditions.checkArgument(user != null);
        Map<Long, WfVariable> map = variableLogic.getVariableValueFromProcesses(user, processIds, variableName);
        for (Map.Entry<Long, WfVariable> entry : map.entrySet()) {
            convertValueToProxy(user, entry.getKey(), entry.getValue());
        }
        return map;
    }

    @Override
    public WfVariable getVariable(User user, Long processId, String variableName) {
        Preconditions.checkArgument(user != null);
        return variableLogic.getVariable(user, processId, variableName);
    }

    @WebMethod(exclude = true)
    @Override
    public void updateVariables(User user, Long processId, Map<String, Object> variables) {
        Preconditions.checkArgument(user != null);
        variableLogic.updateVariables(user, processId, variables);
    }

    @WebMethod(exclude = true)
    @Override
    public void completeTask(User user, Long taskId, Map<String, Object> variables, Long swimlaneActorId) {
        Preconditions.checkArgument(user != null);
        taskLogic.completeTask(user, taskId, variables, swimlaneActorId);
    }

    @Override
    public void cancelProcess(User user, Long processId) {
        Preconditions.checkArgument(user != null);
        executionLogic.cancelProcess(user, processId);
    }

    @Override
    public List<WfSwimlane> getSwimlanes(User user, Long processId) {
        Preconditions.checkArgument(user != null);
        return taskLogic.getSwimlanes(user, processId);
    }

    @Override
    public List<WfTask> getProcessTasks(User user, Long processId) {
        Preconditions.checkArgument(user != null);
        return taskLogic.getTasks(user, processId);
    }

    @Override
    public byte[] getProcessDiagram(User user, Long processId, Long taskId, Long childProcessId) {
        Preconditions.checkArgument(user != null);
        return executionLogic.getProcessDiagram(user, processId, taskId, childProcessId);
    }

    @Override
    public byte[] getProcessHistoryDiagram(User user, Long processId, Long taskId) {
        Preconditions.checkArgument(user != null);
        return executionLogic.getProcessHistoryDiagram(user, processId, taskId);
    }

    @Override
    public List<GraphElementPresentation> getProcessUIHistoryData(User user, Long processId, Long taskId) {
        Preconditions.checkArgument(user != null);
        return executionLogic.getProcessUIHistoryData(user, processId, taskId);
    }

    @Override
    public List<GraphElementPresentation> getProcessGraphElements(User user, Long processId) {
        Preconditions.checkArgument(user != null);
        return executionLogic.getProcessGraphElements(user, processId);
    }

    @Override
    public void assignSwimlane(User user, Long processId, String swimlaneName, Executor executor) {
        Preconditions.checkArgument(user != null);
        taskLogic.assignSwimlane(user, processId, swimlaneName, executor);
    }

    @Override
    public void assignTask(User user, Long taskId, Executor previousOwner, Executor newExecutor) {
        Preconditions.checkArgument(user != null);
        taskLogic.assignTask(user, taskId, previousOwner, newExecutor);
    }

    @Override
    public ProcessLogs getProcessLogs(User user, ProcessLogFilter filter) {
        Preconditions.checkArgument(user != null);
        return auditLogic.getProcessLogs(user, filter);
    }

    @Override
    public Object getProcessLogValue(User user, Long logId) {
        return auditLogic.getProcessLogValue(user, logId);
    }

    @Override
    public void markTaskOpened(User user, Long taskId) {
        Preconditions.checkArgument(user != null);
        taskLogic.markTaskOpened(user, taskId);
    }

    @Override
    public void removeProcesses(User user, Date startDate, Date finishDate, String name, int version, Long id, Long idTill, boolean onlyFinished,
            boolean dateInterval) {
        Preconditions.checkArgument(user != null);
        // TODO removeProcesses
    }

    @Override
    public List<SystemLog> getSystemLogs(User user, BatchPresentation batchPresentation) {
        Preconditions.checkArgument(user != null);
        if (batchPresentation == null) {
            batchPresentation = BatchPresentationFactory.SYSTEM_LOGS.createDefault();
        }
        return auditLogic.getSystemLogs(user, batchPresentation);
    }

    @Override
    public int getSystemLogsCount(User user, BatchPresentation batchPresentation) {
        Preconditions.checkArgument(user != null);
        if (batchPresentation == null) {
            batchPresentation = BatchPresentationFactory.SYSTEM_LOGS.createDefault();
        }
        return auditLogic.getSystemLogsCount(user, batchPresentation);
    }

    private boolean convertValueToProxy(User user, Long processId, WfVariable variable) {
        if (variable.getValue() instanceof FileVariable) {
            FileVariable fileVariable = (FileVariable) variable.getValue();
            FileVariableProxy proxy = new FileVariableProxy(user, processId, variable.getDefinition().getName(), fileVariable);
            variable.setValue(proxy);
            return true;
        }
        return false;
    }

    // workaround for web services
    private Map<String, Object> toVariablesMap(List<WfVariable> variables) {
        Map<String, Object> map = Maps.newHashMap();
        if (variables != null) {
            for (WfVariable variable : variables) {
                Preconditions.checkNotNull(variable.getDefinition(), "variable.definition");
                Object value = variable.getValue();
                if (variable.getDefinition().getFormatClassName() != null) {
                    try {
                        if (value instanceof String) {
                            value = variable.getFormatNotNull().parse(new String[] { (String) value });
                        } else if (value instanceof byte[]) {
                            value = new SerializableToByteArrayConverter().revert(value);
                        } else {
                            throw new InternalApplicationException("Expected value of type String or byte[] when formatClassName is set");
                        }
                    } catch (Exception e) {
                        throw Throwables.propagate(e);
                    }
                }
                map.put(variable.getDefinition().getName(), value);
            }
        }
        return map;
    }

    @Override
    public Long startProcessWS(User user, String definitionName, List<WfVariable> variables) {
        return startProcess(user, definitionName, toVariablesMap(variables));
    }

    @Override
    public void completeTaskWS(User user, Long taskId, List<WfVariable> variables, Long swimlaneActorId) {
        completeTask(user, taskId, toVariablesMap(variables), swimlaneActorId);
    }

    @Override
    public void updateVariablesWS(User user, Long processId, List<WfVariable> variables) {
        updateVariables(user, processId, toVariablesMap(variables));
    }
}
