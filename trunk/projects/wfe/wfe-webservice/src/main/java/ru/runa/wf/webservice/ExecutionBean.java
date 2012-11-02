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

import ru.runa.WSLoggerInterceptor;
import ru.runa.wf.webservice.types.wfeTask;
import ru.runa.wfe.definition.DefinitionDoesNotExistException;
import ru.runa.wfe.execution.ProcessDoesNotExistException;
import ru.runa.wfe.execution.dto.WfProcess;
import ru.runa.wfe.execution.logic.ExecutionLogic;
import ru.runa.wfe.presentation.BatchPresentation;
import ru.runa.wfe.presentation.BatchPresentationConsts;
import ru.runa.wfe.presentation.BatchPresentationFactory;
import ru.runa.wfe.security.AuthenticationException;
import ru.runa.wfe.security.AuthorizationException;
import ru.runa.wfe.task.TaskAlreadyAcceptedException;
import ru.runa.wfe.task.TaskDoesNotExistException;
import ru.runa.wfe.task.dto.WfTask;
import ru.runa.wfe.task.logic.TaskLogic;
import ru.runa.wfe.user.Actor;
import ru.runa.wfe.user.ActorPrincipal;
import ru.runa.wfe.user.Executor;
import ru.runa.wfe.user.ExecutorDoesNotExistException;
import ru.runa.wfe.validation.impl.ValidationException;
import ru.runa.wfe.var.dto.WfVariable;
import ru.runa.wfe.var.logic.VariableLogic;

@Stateless
@WebService(name = "Execution", targetNamespace = "http://runa.ru/workflow/webservices", serviceName = "ExecutionWebService")
@SOAPBinding(parameterStyle = SOAPBinding.ParameterStyle.WRAPPED)
@Interceptors({ SpringBeanAutowiringInterceptor.class, WSLoggerInterceptor.class })
public class ExecutionBean {
    @Autowired
    private ExecutionLogic executionLogic;
    @Autowired
    private TaskLogic taskLogic;
    @Autowired
    private VariableLogic variableLogic;

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
    public Long startProcess(
            @WebParam(mode = Mode.IN, name = "actorPrincipal", targetNamespace = "http://runa.ru/workflow/webservices") ActorPrincipal actor,
            @WebParam(mode = Mode.IN, name = "processDefinitionName", targetNamespace = "http://runa.ru/workflow/webservices") String processDefinitionName,
            @WebParam(mode = Mode.IN, name = "variables", targetNamespace = "http://runa.ru/workflow/webservices") VariableDescr[] variables)
            throws AuthenticationException, AuthorizationException, ru.runa.wf.webservice.types.ValidationException, DefinitionDoesNotExistException {
        try {
            return executionLogic.startProcess(getSubject(actor), processDefinitionName, getVariableMap(variables));
        } catch (ValidationException e) {
            throw new ru.runa.wf.webservice.types.ValidationException();
        }
    }

    @WebMethod
    public void completeTask(
            @WebParam(mode = Mode.IN, name = "actorPrincipal", targetNamespace = "http://runa.ru/workflow/webservices") ActorPrincipal actor,
            @WebParam(mode = Mode.IN, name = "taskId", targetNamespace = "http://runa.ru/workflow/webservices") Long taskId,
            @WebParam(mode = Mode.IN, name = "variables", targetNamespace = "http://runa.ru/workflow/webservices") VariableDescr[] variables)
            throws ru.runa.wf.webservice.types.ValidationException, AuthenticationException, AuthorizationException, TaskDoesNotExistException,
            ExecutorDoesNotExistException {
        try {
            taskLogic.completeTask(getSubject(actor), taskId, getVariableMap(variables));
        } catch (ValidationException e) {
            throw new ru.runa.wf.webservice.types.ValidationException();
        }
    }

    @WebMethod
    public List<wfeTask> getTasks(
            @WebParam(mode = Mode.IN, name = "actorPrincipal", targetNamespace = "http://runa.ru/workflow/webservices") ActorPrincipal actor)
            throws AuthenticationException {
        List<WfTask> tasks = taskLogic.getTasks(getSubject(actor), BatchPresentationFactory.TASKS.createDefault());
        List<wfeTask> result = new ArrayList<wfeTask>();
        if (tasks != null) {
            for (WfTask task : tasks) {
                result.add(new wfeTask(task));
            }
        }
        return result;
    }

    // @WebMethod
    // public VariableDescr[] getVariables(
    // @WebParam(mode = Mode.IN, name = "actorPrincipal", targetNamespace = "http://runa.ru/workflow/webservices") ActorPrincipal actor,
    // @WebParam(mode = Mode.IN, name = "taskId", targetNamespace = "http://runa.ru/workflow/webservices") Long taskId)
    // throws AuthorizationException, AuthenticationException, TaskDoesNotExistException {
    // return convertVariableMap(executionLogic.getVariableValues(getSubject(actor), taskId));
    // }

    @WebMethod
    public VariableDescr[] getProcessVariables(
            @WebParam(mode = Mode.IN, name = "actorPrincipal", targetNamespace = "http://runa.ru/workflow/webservices") ActorPrincipal actor,
            @WebParam(mode = Mode.IN, name = "processId", targetNamespace = "http://runa.ru/workflow/webservices") Long processId)
            throws AuthorizationException, AuthenticationException, TaskDoesNotExistException, ProcessDoesNotExistException {
        return convertVariableList(variableLogic.getVariables(getSubject(actor), processId));
    }

    @WebMethod
    public VariableDescr getVariable(
            @WebParam(mode = Mode.IN, name = "actorPrincipal", targetNamespace = "http://runa.ru/workflow/webservices") ActorPrincipal actor,
            @WebParam(mode = Mode.IN, name = "taskId", targetNamespace = "http://runa.ru/workflow/webservices") Long taskId,
            @WebParam(mode = Mode.IN, name = "variableName", targetNamespace = "http://runa.ru/workflow/webservices") String variableName)
            throws AuthorizationException, AuthenticationException, TaskDoesNotExistException {
        return convertVariable(variableName, variableLogic.getVariable(getSubject(actor), taskId, variableName));
    }

    @WebMethod
    public WfProcess getProcess(
            @WebParam(mode = Mode.IN, name = "actorPrincipal", targetNamespace = "http://runa.ru/workflow/webservices") ActorPrincipal actor,
            @WebParam(mode = Mode.IN, name = "processId", targetNamespace = "http://runa.ru/workflow/webservices") Long processId)
            throws AuthorizationException, AuthenticationException, ProcessDoesNotExistException {
        return executionLogic.getProcess(getSubject(actor), processId);
    }

    @WebMethod
    public void cancelProcess(
            @WebParam(mode = Mode.IN, name = "actorPrincipal", targetNamespace = "http://runa.ru/workflow/webservices") ActorPrincipal actor,
            @WebParam(mode = Mode.IN, name = "processId", targetNamespace = "http://runa.ru/workflow/webservices") Long processId)
            throws AuthorizationException, AuthenticationException, ProcessDoesNotExistException {
        executionLogic.cancelProcess(getSubject(actor), processId);
    }

    @WebMethod
    public byte[] getProcessDiagram(
            @WebParam(mode = Mode.IN, name = "actorPrincipal", targetNamespace = "http://runa.ru/workflow/webservices") ActorPrincipal actor,
            @WebParam(mode = Mode.IN, name = "processId", targetNamespace = "http://runa.ru/workflow/webservices") Long processId,
            @WebParam(mode = Mode.IN, name = "taskId", targetNamespace = "http://runa.ru/workflow/webservices") Long taskId)
            throws AuthorizationException, AuthenticationException, ProcessDoesNotExistException {
        return executionLogic.getProcessDiagram(getSubject(actor), processId, taskId, null);
    }

    @WebMethod
    public void assignTask(
            @WebParam(mode = Mode.IN, name = "actorPrincipal", targetNamespace = "http://runa.ru/workflow/webservices") ActorPrincipal actor,
            @WebParam(mode = Mode.IN, name = "taskId", targetNamespace = "http://runa.ru/workflow/webservices") Long taskId,
            @WebParam(mode = Mode.IN, name = "previousExecutorId", targetNamespace = "http://runa.ru/workflow/webservices") Executor previousExecutor,
            @WebParam(mode = Mode.IN, name = "assignActor", targetNamespace = "http://runa.ru/workflow/webservices") Actor assignActor)
            throws AuthenticationException, TaskAlreadyAcceptedException, ExecutorDoesNotExistException {
        taskLogic.assignTask(getSubject(actor), taskId, previousExecutor, assignActor);
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
                    LogFactory.getLog(ExecutionBean.class).warn(
                            "Unable to read object from bytes " + value.name + ", ignore this message if this is just regular byte array", e);
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

    private VariableDescr[] convertVariableList(List<WfVariable> list) {
        VariableDescr[] result = new VariableDescr[list.size()];
        int idx = 0;
        for (WfVariable variable : list) {
            result[idx++] = convertVariable(variable.getDefinition().getName(), variable.getValue());
        }
        return result;
    }

    private VariableDescr convertVariable(String name, Object value) {
        VariableDescr v = new VariableDescr(name, value);
        if (value != null && v.get() == null) {
            // Object will not be transfered (WSDL does not define type mapping
            // for arbitrary type)
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
    public List<WfProcess> getProcesses(
            @WebParam(mode = Mode.IN, name = "actorPrincipal", targetNamespace = "http://runa.ru/workflow/webservices") ActorPrincipal actor)
            throws AuthorizationException, AuthenticationException, ProcessDoesNotExistException {
        BatchPresentation batchPresentation = BatchPresentationFactory.PROCESSES.createDefault();
        batchPresentation.setRangeSize(BatchPresentationConsts.MAX_UNPAGED_REQUEST_SIZE);
        return executionLogic.getProcesses(getSubject(actor), batchPresentation);
    }

}
