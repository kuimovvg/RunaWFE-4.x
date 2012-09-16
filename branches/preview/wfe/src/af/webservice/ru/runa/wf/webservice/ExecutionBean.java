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
package ru.runa.wf.webservice;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.ejb.Stateless;
import javax.interceptor.Interceptors;
import javax.jws.WebMethod;
import javax.jws.WebParam;
import javax.jws.WebParam.Mode;
import javax.jws.WebService;
import javax.jws.soap.SOAPBinding;
import javax.security.auth.Subject;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;

import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.ejb.interceptor.SpringBeanAutowiringInterceptor;

import ru.runa.af.ActorPrincipal;
import ru.runa.af.AuthenticationException;
import ru.runa.af.AuthorizationException;
import ru.runa.af.ExecutorOutOfDateException;
import ru.runa.af.presentation.BatchPresentation;
import ru.runa.af.presentation.BatchPresentationConsts;
import ru.runa.af.service.impl.ejb.LoggerInterceptor;
import ru.runa.bpm.taskmgmt.exe.TaskInstance;
import ru.runa.wf.ProcessDefinitionDoesNotExistException;
import ru.runa.wf.ProcessInstanceDoesNotExistException;
import ru.runa.wf.ProcessInstanceStub;
import ru.runa.wf.TaskAlreadyAcceptedException;
import ru.runa.wf.TaskDoesNotExistException;
import ru.runa.wf.TaskStub;
import ru.runa.wf.TaskStubFactory;
import ru.runa.wf.form.VariablesValidationException;
import ru.runa.wf.logic.JbpmExecutionLogic;
import ru.runa.wf.presentation.WFProfileStrategy;
import ru.runa.wf.webservice.types.wfeTask;

@Stateless
@WebService(name = "Execution", targetNamespace = "http://runa.ru/workflow/webservices", serviceName = "ExecutionWebService")
@SOAPBinding(parameterStyle = SOAPBinding.ParameterStyle.WRAPPED)
@Interceptors({SpringBeanAutowiringInterceptor.class, LoggerInterceptor.class})
public class ExecutionBean {
    @Autowired
    private JbpmExecutionLogic executionLogic;
    @Autowired
    private TaskStubFactory taskStubFactory;

    @XmlAccessorType(XmlAccessType.FIELD)
    @XmlType(name = "VariableDescr", namespace = "http://runa.ru/workflow/webservices")
    public static class VariableDescr {

        @XmlElement(namespace = "http://runa.ru/workflow/webservices")
        String name;

        @XmlElement(namespace = "http://runa.ru/workflow/webservices")
        byte[] byteValue;
        @XmlElement(namespace = "http://runa.ru/workflow/webservices")
        Boolean booleanValue;
        @XmlElement(namespace = "http://runa.ru/workflow/webservices")
        Long lValue;
        @XmlElement(namespace = "http://runa.ru/workflow/webservices")
        Double dValue;
        @XmlElement(namespace = "http://runa.ru/workflow/webservices")
        String strValue;
        @XmlElement(namespace = "http://runa.ru/workflow/webservices")
        Date dateValue;

        public VariableDescr() {
        };

        public VariableDescr(String name, Object value) {
            this.name = name;
            if (value instanceof Long) {
                lValue = (Long) value;
            } else if (value instanceof Integer) {
                lValue = ((Integer) value).longValue();
            } else if (value instanceof Short) {
                lValue = ((Short) value).longValue();
            } else if (value instanceof byte[]) {
                byteValue = (byte[]) value;
            } else if (value instanceof Boolean) {
                booleanValue = (Boolean) value;
            } else if (value instanceof Double) {
                dValue = (Double) value;
            } else if (value instanceof Float) {
                dValue = ((Float) value).doubleValue();
            } else if (value instanceof Date) {
                dateValue = (Date) value;
            } else if (value instanceof String) {
                strValue = (String) value;
            }
        }

        public Object get() {
            if (byteValue != null) {
                return byteValue;
            } else if (strValue != null) {
                return strValue;
            } else if (booleanValue != null) {
                return booleanValue;
            } else if (dateValue != null) {
                return dateValue;
            } else if (lValue != null) {
                return lValue;
            } else if (dValue != null) {
                return dValue;
            }
            return null;
        }
    };

    @WebMethod
    public List<wfeTask> getProcessInstanceTasks(
            @WebParam(mode = Mode.IN, name = "actorPrincipal", targetNamespace = "http://runa.ru/workflow/webservices") ActorPrincipal actor,
            @WebParam(mode = Mode.IN, name = "processInstanceId", targetNamespace = "http://runa.ru/workflow/webservices") Long processInstanceId)
            throws AuthenticationException {
        List<TaskInstance> tasks = executionLogic.getProcessInstanceTasks(getSubject(actor), processInstanceId);
        List<wfeTask> result = new ArrayList<wfeTask>();
        if (tasks != null) {
            for (TaskInstance task : tasks) {
                if (!task.hasEnded()) {
                    result.add(new wfeTask(taskStubFactory.create(task, null, null)));
                }
            }
        }
        return result;
    }

    @WebMethod
    public Long startProcessInstance(
            @WebParam(mode = Mode.IN, name = "actorPrincipal", targetNamespace = "http://runa.ru/workflow/webservices") ActorPrincipal actor,
            @WebParam(mode = Mode.IN, name = "processDefinitionName", targetNamespace = "http://runa.ru/workflow/webservices") String processDefinitionName,
            @WebParam(mode = Mode.IN, name = "variables", targetNamespace = "http://runa.ru/workflow/webservices") VariableDescr[] variables)
            throws AuthenticationException, AuthorizationException, ru.runa.wf.webservice.types.ValidationException,
            ProcessDefinitionDoesNotExistException {
        try {
            return executionLogic.startProcessInstance(getSubject(actor), processDefinitionName, getVariableMap(variables));
        } catch (VariablesValidationException e) {
            throw new ru.runa.wf.webservice.types.ValidationException();
        }
    }

    @WebMethod
    public void completeTask(
            @WebParam(mode = Mode.IN, name = "actorPrincipal", targetNamespace = "http://runa.ru/workflow/webservices") ActorPrincipal actor,
            @WebParam(mode = Mode.IN, name = "taskId", targetNamespace = "http://runa.ru/workflow/webservices") Long taskId,
            @WebParam(mode = Mode.IN, name = "taskName", targetNamespace = "http://runa.ru/workflow/webservices") String taskName,
            @WebParam(mode = Mode.IN, name = "actorId", targetNamespace = "http://runa.ru/workflow/webservices") Long actorId,
            @WebParam(mode = Mode.IN, name = "variables", targetNamespace = "http://runa.ru/workflow/webservices") VariableDescr[] variables)
            throws ru.runa.wf.webservice.types.ValidationException, AuthenticationException, AuthorizationException, TaskDoesNotExistException,
            ExecutorOutOfDateException {
        try {
            executionLogic.completeTask(getSubject(actor), taskId, taskName, actorId, getVariableMap(variables), null);
        } catch (VariablesValidationException e) {
            throw new ru.runa.wf.webservice.types.ValidationException();
        }
    }

    @WebMethod
    public List<wfeTask> getTasks(
            @WebParam(mode = Mode.IN, name = "actorPrincipal", targetNamespace = "http://runa.ru/workflow/webservices") ActorPrincipal actor)
            throws AuthenticationException {
        List<TaskStub> tasks = executionLogic.getTasks(getSubject(actor), WFProfileStrategy.TASK_DEFAULT_BATCH_PRESENTATION_FACTORY
                .getDefaultBatchPresentation());
        List<wfeTask> result = new ArrayList<wfeTask>();
        if (tasks != null) {
            for (TaskStub task : tasks) {
                result.add(new wfeTask(task));
            }
        }
        return result;
    }

    @WebMethod
    public VariableDescr[] getVariables(
            @WebParam(mode = Mode.IN, name = "actorPrincipal", targetNamespace = "http://runa.ru/workflow/webservices") ActorPrincipal actor,
            @WebParam(mode = Mode.IN, name = "taskId", targetNamespace = "http://runa.ru/workflow/webservices") Long taskId)
            throws AuthorizationException, AuthenticationException, TaskDoesNotExistException {
        return convertVariableMap(executionLogic.getVariables(getSubject(actor), taskId));
    }

    @WebMethod
    public VariableDescr[] getProcessInstanceVariables(
            @WebParam(mode = Mode.IN, name = "actorPrincipal", targetNamespace = "http://runa.ru/workflow/webservices") ActorPrincipal actor,
            @WebParam(mode = Mode.IN, name = "instanceId", targetNamespace = "http://runa.ru/workflow/webservices") Long instanceId)
            throws AuthorizationException, AuthenticationException, TaskDoesNotExistException, ProcessInstanceDoesNotExistException {
        return convertVariableMap(executionLogic.getInstanceVariables(getSubject(actor), instanceId));
    }

    @WebMethod
    public VariableDescr getVariable(
            @WebParam(mode = Mode.IN, name = "actorPrincipal", targetNamespace = "http://runa.ru/workflow/webservices") ActorPrincipal actor,
            @WebParam(mode = Mode.IN, name = "taskId", targetNamespace = "http://runa.ru/workflow/webservices") Long taskId,
            @WebParam(mode = Mode.IN, name = "variableName", targetNamespace = "http://runa.ru/workflow/webservices") String variableName)
            throws AuthorizationException, AuthenticationException, TaskDoesNotExistException {
        return convertVariable(variableName, executionLogic.getVariable(getSubject(actor), taskId, variableName));
    }

    @WebMethod
    public ProcessInstanceStub getProcessInstanceStub(
            @WebParam(mode = Mode.IN, name = "actorPrincipal", targetNamespace = "http://runa.ru/workflow/webservices") ActorPrincipal actor,
            @WebParam(mode = Mode.IN, name = "processInstanceId", targetNamespace = "http://runa.ru/workflow/webservices") Long id)
            throws AuthorizationException, AuthenticationException, ProcessInstanceDoesNotExistException {
        return executionLogic.getProcessInstanceStub(getSubject(actor), id);
    }

    @WebMethod
    public List<String> getVariableNames(
            @WebParam(mode = Mode.IN, name = "actorPrincipal", targetNamespace = "http://runa.ru/workflow/webservices") ActorPrincipal actor,
            @WebParam(mode = Mode.IN, name = "taskId", targetNamespace = "http://runa.ru/workflow/webservices") Long taskId)
            throws AuthorizationException, AuthenticationException, TaskDoesNotExistException {
        return executionLogic.getVariableNames(getSubject(actor), taskId);
    }

    @WebMethod
    public void cancelProcessInstance(
            @WebParam(mode = Mode.IN, name = "actorPrincipal", targetNamespace = "http://runa.ru/workflow/webservices") ActorPrincipal actor,
            @WebParam(mode = Mode.IN, name = "processInstanceId", targetNamespace = "http://runa.ru/workflow/webservices") Long processInstanceId)
            throws AuthorizationException, AuthenticationException, ProcessInstanceDoesNotExistException {
        executionLogic.cancelProcessInstance(getSubject(actor), processInstanceId);
    }

    @WebMethod
    public byte[] getProcessInstanceDiagram(
            @WebParam(mode = Mode.IN, name = "actorPrincipal", targetNamespace = "http://runa.ru/workflow/webservices") ActorPrincipal actor,
            @WebParam(mode = Mode.IN, name = "instanceId", targetNamespace = "http://runa.ru/workflow/webservices") Long instanceId,
            @WebParam(mode = Mode.IN, name = "taskId", targetNamespace = "http://runa.ru/workflow/webservices") Long taskId)
            throws AuthorizationException, AuthenticationException, ProcessInstanceDoesNotExistException {
        return executionLogic.getProcessInstanceDiagram(getSubject(actor), instanceId, taskId, null);
    }

    @WebMethod
    public void assignTask(
            @WebParam(mode = Mode.IN, name = "actorPrincipal", targetNamespace = "http://runa.ru/workflow/webservices") ActorPrincipal actor,
            @WebParam(mode = Mode.IN, name = "taskId", targetNamespace = "http://runa.ru/workflow/webservices") Long taskId,
            @WebParam(mode = Mode.IN, name = "taskName", targetNamespace = "http://runa.ru/workflow/webservices") String taskName,
            @WebParam(mode = Mode.IN, name = "assigningActorId", targetNamespace = "http://runa.ru/workflow/webservices") Long actorId)
            throws AuthenticationException, TaskAlreadyAcceptedException, ExecutorOutOfDateException {
        executionLogic.assignTask(getSubject(actor), taskId, taskName, actorId);
    }

    private Subject getSubject(ActorPrincipal actor) {
        Subject result = new Subject();
        result.getPrincipals().add(actor);
        return result;
    }

    private Map<String, Object> getVariableMap(VariableDescr[] values) {
        Map<String, Object> result = new HashMap<String, Object>();
        if (values == null || values.length == 0) {
            return result;
        }
        for (VariableDescr value : values) {
            Object object = value.get();
            if (object instanceof byte[]) {
                try {
                    ByteArrayInputStream is = new ByteArrayInputStream((byte[]) object);
                    ObjectInputStream ois = new ObjectInputStream(is);
                    object = ois.readObject();
                    ois.close();
                } catch (Exception e) {
                    LogFactory.getLog(ExecutionBean.class).warn("Unable to read object from bytes " + value.name + 
                            ", ignore this message if this is just regular byte array", e);
                }
            }
            result.put(value.name, object);
        }
        return result;
    }
    
    private VariableDescr[] convertVariableMap(Map<String, Object> map) {
        VariableDescr[] result = new VariableDescr[map.size()];
        int idx = 0;
        for (Map.Entry<String, Object> entry : map.entrySet()) {
            result[idx++] = convertVariable(entry.getKey(), entry.getValue());
        }
        return result;
    }
    
    private VariableDescr convertVariable(String name, Object value) {
        VariableDescr v = new VariableDescr(name, value);
        if (value != null && v.get() == null) {
            // Object will not be transfered (WSDL does not define type mapping for arbitrary type)
            try {
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                ObjectOutputStream oos = new ObjectOutputStream(baos);
                oos.writeObject(value);
                oos.close();
                v.byteValue = baos.toByteArray();
            } catch (IOException e) {
                LogFactory.getLog(ExecutionBean.class).warn("Unable to convert object variable " + name + ", setting it to null", e);
            }
        }
        return v;
    }

    @WebMethod
    public List<ProcessInstanceStub> getProcessInstanceStubs(
            @WebParam(mode = Mode.IN, name = "actorPrincipal", targetNamespace = "http://runa.ru/workflow/webservices") ActorPrincipal actor)
            throws AuthorizationException, AuthenticationException, ProcessInstanceDoesNotExistException {
        BatchPresentation batchPresentation = WFProfileStrategy.PROCESS_INSTANCE_DEFAULT_BATCH_PRESENTATION_FACTORY.getDefaultBatchPresentation();
        batchPresentation.setRangeSize(BatchPresentationConsts.MAX_UNPAGED_REQUEST_SIZE);
        return executionLogic.getProcessInstanceStubs(getSubject(actor), batchPresentation);
    }

}
