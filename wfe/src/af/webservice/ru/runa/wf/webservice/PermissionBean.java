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

import java.util.Collection;
import java.util.List;
import java.util.Set;

import javax.ejb.Stateless;
import javax.interceptor.Interceptors;
import javax.jws.WebMethod;
import javax.jws.WebParam;
import javax.jws.WebParam.Mode;
import javax.jws.WebService;
import javax.jws.soap.SOAPBinding;
import javax.security.auth.Subject;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.ejb.interceptor.SpringBeanAutowiringInterceptor;

import ru.runa.af.ASystem;
import ru.runa.af.ActorPrincipal;
import ru.runa.af.AttributeRequiredException;
import ru.runa.af.AuthenticationException;
import ru.runa.af.AuthorizationException;
import ru.runa.af.BotStation;
import ru.runa.af.Executor;
import ru.runa.af.ExecutorOutOfDateException;
import ru.runa.af.Identifiable;
import ru.runa.af.ObjectDoesNotExistException;
import ru.runa.af.Permission;
import ru.runa.af.PermissionNotFoundException;
import ru.runa.af.UnapplicablePermissionException;
import ru.runa.af.logic.AuthorizationLogic;
import ru.runa.af.logic.ExecutorLogic;
import ru.runa.af.presentation.AFProfileStrategy;
import ru.runa.af.presentation.BatchPresentation;
import ru.runa.af.presentation.BatchPresentationConsts;
import ru.runa.af.service.impl.ejb.LoggerInterceptor;
import ru.runa.wf.ProcessDefinitionDoesNotExistException;
import ru.runa.wf.ProcessInstanceStub;
import ru.runa.wf.logic.JbpmDefinitionLogic;
import ru.runa.wf.logic.JbpmExecutionLogic;

import com.google.common.collect.Sets;

@Stateless
@WebService(name = "Permission", targetNamespace = "http://runa.ru/workflow/webservices", serviceName = "PermissionWebService")
@SOAPBinding(parameterStyle = SOAPBinding.ParameterStyle.WRAPPED)
@Interceptors({SpringBeanAutowiringInterceptor.class, LoggerInterceptor.class})
public class PermissionBean {
    @Autowired
    private AuthorizationLogic authorizationLogic;
    @Autowired
    private JbpmDefinitionLogic definitionLogic;
    @Autowired
    private JbpmExecutionLogic executionLogic;
    @Autowired
    private ExecutorLogic executorLogic;

    @WebMethod
    public void addPermissionsOnActor(
            @WebParam(mode = Mode.IN, name = "actorPrincipal", targetNamespace = "http://runa.ru/workflow/webservices") ActorPrincipal actorPrincipal,
            @WebParam(mode = Mode.IN, name = "actor", targetNamespace = "http://runa.ru/workflow/webservices") String actorStr,
            @WebParam(mode = Mode.IN, name = "executor", targetNamespace = "http://runa.ru/workflow/webservices") String executorStr,
            @WebParam(mode = Mode.IN, name = "permissions", targetNamespace = "http://runa.ru/workflow/webservices") List<String> permissionList)
            throws PermissionNotFoundException, UnapplicablePermissionException, ExecutorOutOfDateException, AuthorizationException,
            AuthenticationException {
        Subject subject = getSubject(actorPrincipal);
        Executor executor = executorLogic.getExecutor(subject, executorStr);
        Identifiable actor = executorLogic.getActor(subject, actorStr);
        addPermissionOnIdentifiable(subject, executor, actor, permissionList);
    }

    @WebMethod
    public void setPermissionsOnActor(
            @WebParam(mode = Mode.IN, name = "actorPrincipal", targetNamespace = "http://runa.ru/workflow/webservices") ActorPrincipal actorPrincipal,
            @WebParam(mode = Mode.IN, name = "actor", targetNamespace = "http://runa.ru/workflow/webservices") String actorStr,
            @WebParam(mode = Mode.IN, name = "executor", targetNamespace = "http://runa.ru/workflow/webservices") String executorStr,
            @WebParam(mode = Mode.IN, name = "permissions", targetNamespace = "http://runa.ru/workflow/webservices") List<String> permissionList)
            throws PermissionNotFoundException, UnapplicablePermissionException, ExecutorOutOfDateException, AuthorizationException,
            AuthenticationException {
        Subject subject = getSubject(actorPrincipal);
        Executor executor = executorLogic.getExecutor(subject, executorStr);
        Identifiable actor = executorLogic.getActor(subject, actorStr);
        setPermissionOnIdentifiable(subject, executor, actor, permissionList);
    }

    @WebMethod
    public void removePermissionsOnActor(
            @WebParam(mode = Mode.IN, name = "actorPrincipal", targetNamespace = "http://runa.ru/workflow/webservices") ActorPrincipal actorPrincipal,
            @WebParam(mode = Mode.IN, name = "actor", targetNamespace = "http://runa.ru/workflow/webservices") String actorStr,
            @WebParam(mode = Mode.IN, name = "executor", targetNamespace = "http://runa.ru/workflow/webservices") String executorStr,
            @WebParam(mode = Mode.IN, name = "permissions", targetNamespace = "http://runa.ru/workflow/webservices") List<String> permissionList)
            throws PermissionNotFoundException, UnapplicablePermissionException, ExecutorOutOfDateException, AuthorizationException,
            AuthenticationException {
        Subject subject = getSubject(actorPrincipal);
        Executor executor = executorLogic.getExecutor(subject, executorStr);
        Identifiable actor = executorLogic.getActor(subject, actorStr);
        removePermissionOnIdentifiable(subject, executor, actor, permissionList);
    }

    @WebMethod
    public void addPermissionsOnGroup(
            @WebParam(mode = Mode.IN, name = "actorPrincipal", targetNamespace = "http://runa.ru/workflow/webservices") ActorPrincipal actorPrincipal,
            @WebParam(mode = Mode.IN, name = "group", targetNamespace = "http://runa.ru/workflow/webservices") String groupStr,
            @WebParam(mode = Mode.IN, name = "executor", targetNamespace = "http://runa.ru/workflow/webservices") String executorStr,
            @WebParam(mode = Mode.IN, name = "permissions", targetNamespace = "http://runa.ru/workflow/webservices") List<String> permissionList)
            throws PermissionNotFoundException, UnapplicablePermissionException, ExecutorOutOfDateException, AuthorizationException,
            AuthenticationException {
        Subject subject = getSubject(actorPrincipal);
        Executor executor = executorLogic.getExecutor(subject, executorStr);
        Identifiable group = executorLogic.getGroup(subject, groupStr);
        addPermissionOnIdentifiable(subject, executor, group, permissionList);
    }

    @WebMethod
    public void setPermissionsOnGroup(
            @WebParam(mode = Mode.IN, name = "actorPrincipal", targetNamespace = "http://runa.ru/workflow/webservices") ActorPrincipal actorPrincipal,
            @WebParam(mode = Mode.IN, name = "group", targetNamespace = "http://runa.ru/workflow/webservices") String groupStr,
            @WebParam(mode = Mode.IN, name = "executor", targetNamespace = "http://runa.ru/workflow/webservices") String executorStr,
            @WebParam(mode = Mode.IN, name = "permissions", targetNamespace = "http://runa.ru/workflow/webservices") List<String> permissionList)
            throws PermissionNotFoundException, UnapplicablePermissionException, ExecutorOutOfDateException, AuthorizationException,
            AuthenticationException {
        Subject subject = getSubject(actorPrincipal);
        Executor executor = executorLogic.getExecutor(subject, executorStr);
        Identifiable group = executorLogic.getGroup(subject, groupStr);
        setPermissionOnIdentifiable(subject, executor, group, permissionList);
    }

    @WebMethod
    public void removePermissionsOnGroup(
            @WebParam(mode = Mode.IN, name = "actorPrincipal", targetNamespace = "http://runa.ru/workflow/webservices") ActorPrincipal actorPrincipal,
            @WebParam(mode = Mode.IN, name = "group", targetNamespace = "http://runa.ru/workflow/webservices") String groupStr,
            @WebParam(mode = Mode.IN, name = "executor", targetNamespace = "http://runa.ru/workflow/webservices") String executorStr,
            @WebParam(mode = Mode.IN, name = "permissions", targetNamespace = "http://runa.ru/workflow/webservices") List<String> permissionList)
            throws PermissionNotFoundException, UnapplicablePermissionException, ExecutorOutOfDateException, AuthorizationException,
            AuthenticationException {
        Subject subject = getSubject(actorPrincipal);
        Executor executor = executorLogic.getExecutor(subject, executorStr);
        Identifiable group = executorLogic.getGroup(subject, groupStr);
        removePermissionOnIdentifiable(subject, executor, group, permissionList);
    }

    @WebMethod
    public void addPermissionsOnSystem(
            @WebParam(mode = Mode.IN, name = "actorPrincipal", targetNamespace = "http://runa.ru/workflow/webservices") ActorPrincipal actorPrincipal,
            @WebParam(mode = Mode.IN, name = "executor", targetNamespace = "http://runa.ru/workflow/webservices") String executorStr,
            @WebParam(mode = Mode.IN, name = "permissions", targetNamespace = "http://runa.ru/workflow/webservices") List<String> permissionList)
            throws PermissionNotFoundException, UnapplicablePermissionException, ExecutorOutOfDateException, AuthorizationException,
            AuthenticationException {
        Subject subject = getSubject(actorPrincipal);
        Executor executor = executorLogic.getExecutor(subject, executorStr);
        addPermissionOnIdentifiable(subject, executor, ASystem.SYSTEM, permissionList);
    }

    @WebMethod
    public void setPermissionsOnSystem(
            @WebParam(mode = Mode.IN, name = "actorPrincipal", targetNamespace = "http://runa.ru/workflow/webservices") ActorPrincipal actorPrincipal,
            @WebParam(mode = Mode.IN, name = "executor", targetNamespace = "http://runa.ru/workflow/webservices") String executorStr,
            @WebParam(mode = Mode.IN, name = "permissions", targetNamespace = "http://runa.ru/workflow/webservices") List<String> permissionList)
            throws PermissionNotFoundException, UnapplicablePermissionException, ExecutorOutOfDateException, AuthorizationException,
            AuthenticationException {
        Subject subject = getSubject(actorPrincipal);
        Executor executor = executorLogic.getExecutor(subject, executorStr);
        setPermissionOnIdentifiable(subject, executor, ASystem.SYSTEM, permissionList);
    }

    @WebMethod
    public void removePermissionsOnSystem(
            @WebParam(mode = Mode.IN, name = "actorPrincipal", targetNamespace = "http://runa.ru/workflow/webservices") ActorPrincipal actorPrincipal,
            @WebParam(mode = Mode.IN, name = "executor", targetNamespace = "http://runa.ru/workflow/webservices") String executorStr,
            @WebParam(mode = Mode.IN, name = "permissions", targetNamespace = "http://runa.ru/workflow/webservices") List<String> permissionList)
            throws PermissionNotFoundException, UnapplicablePermissionException, ExecutorOutOfDateException, AuthorizationException,
            AuthenticationException {
        Subject subject = getSubject(actorPrincipal);
        Executor executor = executorLogic.getExecutor(subject, executorStr);
        removePermissionOnIdentifiable(subject, executor, ASystem.SYSTEM, permissionList);
    }

    @WebMethod
    public void addPermissionsOnDefinition(
            @WebParam(mode = Mode.IN, name = "actorPrincipal", targetNamespace = "http://runa.ru/workflow/webservices") ActorPrincipal actorPrincipal,
            @WebParam(mode = Mode.IN, name = "definition", targetNamespace = "http://runa.ru/workflow/webservices") String definitionStr,
            @WebParam(mode = Mode.IN, name = "executor", targetNamespace = "http://runa.ru/workflow/webservices") String executorStr,
            @WebParam(mode = Mode.IN, name = "permissions", targetNamespace = "http://runa.ru/workflow/webservices") List<String> permissionList)
            throws PermissionNotFoundException, UnapplicablePermissionException, ExecutorOutOfDateException, AuthorizationException,
            AuthenticationException, ProcessDefinitionDoesNotExistException {
        Subject subject = getSubject(actorPrincipal);
        Executor executor = executorLogic.getExecutor(subject, executorStr);
        Identifiable definition = definitionLogic.getLatestProcessDefinitionStub(subject, definitionStr);
        addPermissionOnIdentifiable(subject, executor, definition, permissionList);
    }

    @WebMethod
    public void setPermissionsOnDefinition(
            @WebParam(mode = Mode.IN, name = "actorPrincipal", targetNamespace = "http://runa.ru/workflow/webservices") ActorPrincipal actorPrincipal,
            @WebParam(mode = Mode.IN, name = "definition", targetNamespace = "http://runa.ru/workflow/webservices") String definitionStr,
            @WebParam(mode = Mode.IN, name = "executor", targetNamespace = "http://runa.ru/workflow/webservices") String executorStr,
            @WebParam(mode = Mode.IN, name = "permissions", targetNamespace = "http://runa.ru/workflow/webservices") List<String> permissionList)
            throws PermissionNotFoundException, UnapplicablePermissionException, ExecutorOutOfDateException, AuthorizationException,
            AuthenticationException, ProcessDefinitionDoesNotExistException {
        Subject subject = getSubject(actorPrincipal);
        Executor executor = executorLogic.getExecutor(subject, executorStr);
        Identifiable definition = definitionLogic.getLatestProcessDefinitionStub(subject, definitionStr);
        setPermissionOnIdentifiable(subject, executor, definition, permissionList);
    }

    @WebMethod
    public void removePermissionsOnDefinition(
            @WebParam(mode = Mode.IN, name = "actorPrincipal", targetNamespace = "http://runa.ru/workflow/webservices") ActorPrincipal actorPrincipal,
            @WebParam(mode = Mode.IN, name = "definition", targetNamespace = "http://runa.ru/workflow/webservices") String definitionStr,
            @WebParam(mode = Mode.IN, name = "executor", targetNamespace = "http://runa.ru/workflow/webservices") String executorStr,
            @WebParam(mode = Mode.IN, name = "permissions", targetNamespace = "http://runa.ru/workflow/webservices") List<String> permissionList)
            throws PermissionNotFoundException, UnapplicablePermissionException, ExecutorOutOfDateException, AuthorizationException,
            AuthenticationException, ProcessDefinitionDoesNotExistException {
        Subject subject = getSubject(actorPrincipal);
        Executor executor = executorLogic.getExecutor(subject, executorStr);
        Identifiable definition = definitionLogic.getLatestProcessDefinitionStub(subject, definitionStr);
        removePermissionOnIdentifiable(subject, executor, definition, permissionList);
    }

    @WebMethod
    public void addPermissionsOnProcessInstances(
            @WebParam(mode = Mode.IN, name = "actorPrincipal", targetNamespace = "http://runa.ru/workflow/webservices") ActorPrincipal actorPrincipal,
            @WebParam(mode = Mode.IN, name = "processInstance", targetNamespace = "http://runa.ru/workflow/webservices") String processName,
            @WebParam(mode = Mode.IN, name = "executor", targetNamespace = "http://runa.ru/workflow/webservices") String executorStr,
            @WebParam(mode = Mode.IN, name = "permissions", targetNamespace = "http://runa.ru/workflow/webservices") List<String> permissionList)
            throws PermissionNotFoundException, UnapplicablePermissionException, ExecutorOutOfDateException, AuthorizationException,
            AuthenticationException, ObjectDoesNotExistException {
        Subject subject = getSubject(actorPrincipal);
        Executor executor = executorLogic.getExecutor(subject, executorStr);
        List<ProcessInstanceStub> processInstanceStub = executionLogic.getProcessInstanceStubsForDefinitionName(subject, processName);
        if (processInstanceStub.size() == 0) {
            throw new ObjectDoesNotExistException(processName);
        }
        for (ProcessInstanceStub processInstance : processInstanceStub) {
            addPermissionOnIdentifiable(subject, executor, processInstance, permissionList);
        }

    }

    @WebMethod
    public void setPermissionsOnProcessInstances(
            @WebParam(mode = Mode.IN, name = "actorPrincipal", targetNamespace = "http://runa.ru/workflow/webservices") ActorPrincipal actorPrincipal,
            @WebParam(mode = Mode.IN, name = "processInstance", targetNamespace = "http://runa.ru/workflow/webservices") String processName,
            @WebParam(mode = Mode.IN, name = "executor", targetNamespace = "http://runa.ru/workflow/webservices") String executorStr,
            @WebParam(mode = Mode.IN, name = "permissions", targetNamespace = "http://runa.ru/workflow/webservices") List<String> permissionList)
            throws PermissionNotFoundException, UnapplicablePermissionException, ExecutorOutOfDateException, AuthorizationException,
            AuthenticationException, ObjectDoesNotExistException {
        Subject subject = getSubject(actorPrincipal);
        Executor executor = executorLogic.getExecutor(subject, executorStr);
        List<ProcessInstanceStub> processInstanceStub = executionLogic.getProcessInstanceStubsForDefinitionName(subject, processName);
        if (processInstanceStub.size() == 0) {
            throw new ObjectDoesNotExistException(processName);
        }
        for (ProcessInstanceStub processInstance : processInstanceStub) {
            setPermissionOnIdentifiable(subject, executor, processInstance, permissionList);
        }
    }

    @WebMethod
    public void removePermissionsOnProcessInstances(
            @WebParam(mode = Mode.IN, name = "actorPrincipal", targetNamespace = "http://runa.ru/workflow/webservices") ActorPrincipal actorPrincipal,
            @WebParam(mode = Mode.IN, name = "processInstance", targetNamespace = "http://runa.ru/workflow/webservices") String processName,
            @WebParam(mode = Mode.IN, name = "executor", targetNamespace = "http://runa.ru/workflow/webservices") String executorStr,
            @WebParam(mode = Mode.IN, name = "permissions", targetNamespace = "http://runa.ru/workflow/webservices") List<String> permissionList)
            throws PermissionNotFoundException, UnapplicablePermissionException, ExecutorOutOfDateException, AuthorizationException,
            AuthenticationException, ObjectDoesNotExistException {
        Subject subject = getSubject(actorPrincipal);
        Executor executor = executorLogic.getExecutor(subject, executorStr);
        List<ProcessInstanceStub> processInstanceStub = executionLogic.getProcessInstanceStubsForDefinitionName(subject, processName);
        if (processInstanceStub.size() == 0) {
            throw new ObjectDoesNotExistException(processName);
        }
        for (ProcessInstanceStub processInstance : processInstanceStub) {
            removePermissionOnIdentifiable(subject, executor, processInstance, permissionList);
        }
    }

    @WebMethod
    public void removeAllPermissionsFromProcessDefinition(
            @WebParam(mode = Mode.IN, name = "actorPrincipal", targetNamespace = "http://runa.ru/workflow/webservices") ActorPrincipal actorPrincipal,
            @WebParam(mode = Mode.IN, name = "definitions", targetNamespace = "http://runa.ru/workflow/webservices") List<String> definitionList)
            throws UnapplicablePermissionException, ExecutorOutOfDateException, AuthorizationException, AuthenticationException,
            ProcessDefinitionDoesNotExistException, AttributeRequiredException {
        Subject subject = getSubject(actorPrincipal);
        if (definitionList.size() == 0) {
            throw new AttributeRequiredException("removeAllPermissionsFromProcessDefinition", "definitions");
        }
        for (String definitionName : definitionList) {
            Identifiable definition = definitionLogic.getLatestProcessDefinitionStub(subject, definitionName);
            removeAllPermissionOnIdentifiable(subject, definition);
        }
    }

    @WebMethod
    public void removeAllPermissionsFromProcessInstances(
            @WebParam(mode = Mode.IN, name = "actorPrincipal", targetNamespace = "http://runa.ru/workflow/webservices") ActorPrincipal actorPrincipal,
            @WebParam(mode = Mode.IN, name = "processes", targetNamespace = "http://runa.ru/workflow/webservices") List<String> processes)
            throws UnapplicablePermissionException, ExecutorOutOfDateException, AuthorizationException, AuthenticationException,
            ProcessDefinitionDoesNotExistException, AttributeRequiredException {
        Subject subject = getSubject(actorPrincipal);

        if (processes.size() == 0) {
            throw new AttributeRequiredException("removeAllPermissionsFromProcessInstances", "processes");
        }
        for (String process : processes) {
            List<ProcessInstanceStub> processInstanceStub = executionLogic.getProcessInstanceStubsForDefinitionName(subject, process);
            if (processInstanceStub.size() == 0) {
                throw new ProcessDefinitionDoesNotExistException(process);
            }
            for (ProcessInstanceStub processInstance : processInstanceStub) {
                removeAllPermissionOnIdentifiable(subject, processInstance);
            }
        }
    }

    @WebMethod
    public void removeAllPermissionsFromExecutor(
            @WebParam(mode = Mode.IN, name = "actorPrincipal", targetNamespace = "http://runa.ru/workflow/webservices") ActorPrincipal actorPrincipal,
            @WebParam(mode = Mode.IN, name = "executor", targetNamespace = "http://runa.ru/workflow/webservices") String name)
            throws UnapplicablePermissionException, ExecutorOutOfDateException, AuthorizationException, AuthenticationException {
        Subject subject = getSubject(actorPrincipal);
        Executor executor = executorLogic.getExecutor(subject, name);
        removeAllPermissionOnIdentifiable(subject, executor);
    }

    @WebMethod
    public void removeAllPermissionsFromSystem(
            @WebParam(mode = Mode.IN, name = "actorPrincipal", targetNamespace = "http://runa.ru/workflow/webservices") ActorPrincipal actorPrincipal)
            throws UnapplicablePermissionException, ExecutorOutOfDateException, AuthorizationException, AuthenticationException {
        Subject subject = getSubject(actorPrincipal);
        removeAllPermissionOnIdentifiable(subject, ASystem.SYSTEM);
    }

    @WebMethod
    public void addPermissionsOnBotStations(
            @WebParam(mode = Mode.IN, name = "actorPrincipal", targetNamespace = "http://runa.ru/workflow/webservices") ActorPrincipal actorPrincipal,
            @WebParam(mode = Mode.IN, name = "executor", targetNamespace = "http://runa.ru/workflow/webservices") String executorStr,
            @WebParam(mode = Mode.IN, name = "permissions", targetNamespace = "http://runa.ru/workflow/webservices") List<String> permissionList)
            throws PermissionNotFoundException, UnapplicablePermissionException, ExecutorOutOfDateException, AuthorizationException,
            AuthenticationException {
        Subject subject = getSubject(actorPrincipal);
        Executor executor = executorLogic.getExecutor(subject, executorStr);
        addPermissionOnIdentifiable(subject, executor, BotStation.SECURED_INSTANCE, permissionList);
    }

    @WebMethod
    public void setPermissionsOnBotStations(
            @WebParam(mode = Mode.IN, name = "actorPrincipal", targetNamespace = "http://runa.ru/workflow/webservices") ActorPrincipal actorPrincipal,
            @WebParam(mode = Mode.IN, name = "executor", targetNamespace = "http://runa.ru/workflow/webservices") String executorStr,
            @WebParam(mode = Mode.IN, name = "permissions", targetNamespace = "http://runa.ru/workflow/webservices") List<String> permissionList)
            throws PermissionNotFoundException, UnapplicablePermissionException, ExecutorOutOfDateException, AuthorizationException,
            AuthenticationException {
        Subject subject = getSubject(actorPrincipal);
        Executor executor = executorLogic.getExecutor(subject, executorStr);
        setPermissionOnIdentifiable(subject, executor, BotStation.SECURED_INSTANCE, permissionList);
    }

    @WebMethod
    public void removePermissionsOnBotStations(
            @WebParam(mode = Mode.IN, name = "actorPrincipal", targetNamespace = "http://runa.ru/workflow/webservices") ActorPrincipal actorPrincipal,
            @WebParam(mode = Mode.IN, name = "executor", targetNamespace = "http://runa.ru/workflow/webservices") String executorStr,
            @WebParam(mode = Mode.IN, name = "permissions", targetNamespace = "http://runa.ru/workflow/webservices") List<String> permissionList)
            throws PermissionNotFoundException, UnapplicablePermissionException, ExecutorOutOfDateException, AuthorizationException,
            AuthenticationException {
        Subject subject = getSubject(actorPrincipal);
        Executor executor = executorLogic.getExecutor(subject, executorStr);
        removePermissionOnIdentifiable(subject, executor, BotStation.SECURED_INSTANCE, permissionList);
    }

    private void addPermissionOnIdentifiable(Subject subject, Executor executor, Identifiable identifiable, List<String> permissionList)
            throws PermissionNotFoundException, UnapplicablePermissionException, ExecutorOutOfDateException, AuthorizationException,
            AuthenticationException {
        Collection<Permission> permissions = getPermissions(permissionList, identifiable);
        Collection<Permission> ownPermissions = authorizationLogic.getOwnPermissions(subject, executor, identifiable);
        permissions = Permission.mergePermissions(permissions, ownPermissions);
        authorizationLogic.setPermissions(subject, executor, permissions, identifiable);
    }

    private void setPermissionOnIdentifiable(Subject subject, Executor executor, Identifiable identifiable, List<String> permissionList)
            throws PermissionNotFoundException, UnapplicablePermissionException, ExecutorOutOfDateException, AuthorizationException,
            AuthenticationException {
        Collection<Permission> permissions = getPermissions(permissionList, identifiable);
        authorizationLogic.setPermissions(subject, executor, permissions, identifiable);
    }

    private void removePermissionOnIdentifiable(Subject subject, Executor executor, Identifiable identifiable, List<String> permissionList)
            throws PermissionNotFoundException, UnapplicablePermissionException, ExecutorOutOfDateException, AuthorizationException,
            AuthenticationException {
        Collection<Permission> permissions = getPermissions(permissionList, identifiable);
        Collection<Permission> ownPermissions = authorizationLogic.getOwnPermissions(subject, executor, identifiable);
        permissions = Permission.subtractPermissions(ownPermissions, permissions);
        authorizationLogic.setPermissions(subject, executor, permissions, identifiable);
    }

    private void removeAllPermissionOnIdentifiable(Subject subject, Identifiable identifiable) throws UnapplicablePermissionException,
            ExecutorOutOfDateException, AuthorizationException, AuthenticationException {
        BatchPresentation batchPresentation = AFProfileStrategy.EXECUTOR_DEAFAULT_BATCH_PRESENTATOIN_FACTORY.getDefaultBatchPresentation();
        batchPresentation.setRangeSize(BatchPresentationConsts.MAX_UNPAGED_REQUEST_SIZE);
        List<Executor> executors = authorizationLogic.getExecutorsWithPermission(subject, identifiable, batchPresentation, true);
        for (Executor executor : executors) {
            if (!authorizationLogic.isPrivelegedExecutor(subject, executor, identifiable)) {
                authorizationLogic.setPermissions(subject, executor, Permission.getNoPermissions(), identifiable);
            }
        }
    }

    private Collection<Permission> getPermissions(List<String> permissionList, Identifiable identifiable) throws PermissionNotFoundException {
        Permission noPermission = authorizationLogic.getNoPermission(identifiable);
        Set<Permission> permissions = Sets.newHashSet();
        for (String permissionName : permissionList) {
            Permission permission = noPermission.getPermission(permissionName);
            permissions.add(permission);
        }
        return permissions;
    }

    private Subject getSubject(ActorPrincipal actor) {
        Subject result = new Subject();
        result.getPrincipals().add(actor);
        return result;
    }
}
