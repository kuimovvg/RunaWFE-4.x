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

import ru.runa.AttributeRequiredException;
import ru.runa.WSLoggerInterceptor;
import ru.runa.wfe.bot.BotStation;
import ru.runa.wfe.definition.DefinitionDoesNotExistException;
import ru.runa.wfe.definition.logic.DefinitionLogic;
import ru.runa.wfe.execution.dto.WfProcess;
import ru.runa.wfe.execution.logic.ExecutionLogic;
import ru.runa.wfe.presentation.BatchPresentation;
import ru.runa.wfe.presentation.BatchPresentationFactory;
import ru.runa.wfe.security.ASystem;
import ru.runa.wfe.security.AuthenticationException;
import ru.runa.wfe.security.AuthorizationException;
import ru.runa.wfe.security.Identifiable;
import ru.runa.wfe.security.Permission;
import ru.runa.wfe.security.PermissionNotFoundException;
import ru.runa.wfe.security.UnapplicablePermissionException;
import ru.runa.wfe.security.logic.AuthorizationLogic;
import ru.runa.wfe.user.ActorPrincipal;
import ru.runa.wfe.user.Executor;
import ru.runa.wfe.user.ExecutorDoesNotExistException;
import ru.runa.wfe.user.logic.ExecutorLogic;

import com.google.common.collect.Sets;

@Stateless
@WebService(name = "Permission", targetNamespace = "http://runa.ru/workflow/webservices", serviceName = "PermissionWebService")
@SOAPBinding(parameterStyle = SOAPBinding.ParameterStyle.WRAPPED)
@Interceptors({ SpringBeanAutowiringInterceptor.class, WSLoggerInterceptor.class })
public class PermissionBean {
    @Autowired
    private AuthorizationLogic authorizationLogic;
    @Autowired
    private DefinitionLogic definitionLogic;
    @Autowired
    private ExecutionLogic executionLogic;
    @Autowired
    private ExecutorLogic executorLogic;

    @WebMethod
    public void addPermissionsOnActor(
            @WebParam(mode = Mode.IN, name = "actorPrincipal", targetNamespace = "http://runa.ru/workflow/webservices") ActorPrincipal actorPrincipal,
            @WebParam(mode = Mode.IN, name = "actor", targetNamespace = "http://runa.ru/workflow/webservices") String actorStr,
            @WebParam(mode = Mode.IN, name = "executor", targetNamespace = "http://runa.ru/workflow/webservices") String executorStr,
            @WebParam(mode = Mode.IN, name = "permissions", targetNamespace = "http://runa.ru/workflow/webservices") List<String> permissionList)
            throws PermissionNotFoundException, UnapplicablePermissionException, ExecutorDoesNotExistException, AuthorizationException,
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
            throws PermissionNotFoundException, UnapplicablePermissionException, ExecutorDoesNotExistException, AuthorizationException,
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
            throws PermissionNotFoundException, UnapplicablePermissionException, ExecutorDoesNotExistException, AuthorizationException,
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
            throws PermissionNotFoundException, UnapplicablePermissionException, ExecutorDoesNotExistException, AuthorizationException,
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
            throws PermissionNotFoundException, UnapplicablePermissionException, ExecutorDoesNotExistException, AuthorizationException,
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
            throws PermissionNotFoundException, UnapplicablePermissionException, ExecutorDoesNotExistException, AuthorizationException,
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
            throws PermissionNotFoundException, UnapplicablePermissionException, ExecutorDoesNotExistException, AuthorizationException,
            AuthenticationException {
        Subject subject = getSubject(actorPrincipal);
        Executor executor = executorLogic.getExecutor(subject, executorStr);
        addPermissionOnIdentifiable(subject, executor, ASystem.INSTANCE, permissionList);
    }

    @WebMethod
    public void setPermissionsOnSystem(
            @WebParam(mode = Mode.IN, name = "actorPrincipal", targetNamespace = "http://runa.ru/workflow/webservices") ActorPrincipal actorPrincipal,
            @WebParam(mode = Mode.IN, name = "executor", targetNamespace = "http://runa.ru/workflow/webservices") String executorStr,
            @WebParam(mode = Mode.IN, name = "permissions", targetNamespace = "http://runa.ru/workflow/webservices") List<String> permissionList)
            throws PermissionNotFoundException, UnapplicablePermissionException, ExecutorDoesNotExistException, AuthorizationException,
            AuthenticationException {
        Subject subject = getSubject(actorPrincipal);
        Executor executor = executorLogic.getExecutor(subject, executorStr);
        setPermissionOnIdentifiable(subject, executor, ASystem.INSTANCE, permissionList);
    }

    @WebMethod
    public void removePermissionsOnSystem(
            @WebParam(mode = Mode.IN, name = "actorPrincipal", targetNamespace = "http://runa.ru/workflow/webservices") ActorPrincipal actorPrincipal,
            @WebParam(mode = Mode.IN, name = "executor", targetNamespace = "http://runa.ru/workflow/webservices") String executorStr,
            @WebParam(mode = Mode.IN, name = "permissions", targetNamespace = "http://runa.ru/workflow/webservices") List<String> permissionList)
            throws PermissionNotFoundException, UnapplicablePermissionException, ExecutorDoesNotExistException, AuthorizationException,
            AuthenticationException {
        Subject subject = getSubject(actorPrincipal);
        Executor executor = executorLogic.getExecutor(subject, executorStr);
        removePermissionOnIdentifiable(subject, executor, ASystem.INSTANCE, permissionList);
    }

    @WebMethod
    public void addPermissionsOnDefinition(
            @WebParam(mode = Mode.IN, name = "actorPrincipal", targetNamespace = "http://runa.ru/workflow/webservices") ActorPrincipal actorPrincipal,
            @WebParam(mode = Mode.IN, name = "definition", targetNamespace = "http://runa.ru/workflow/webservices") String definitionStr,
            @WebParam(mode = Mode.IN, name = "executor", targetNamespace = "http://runa.ru/workflow/webservices") String executorStr,
            @WebParam(mode = Mode.IN, name = "permissions", targetNamespace = "http://runa.ru/workflow/webservices") List<String> permissionList)
            throws PermissionNotFoundException, UnapplicablePermissionException, ExecutorDoesNotExistException, AuthorizationException,
            AuthenticationException, DefinitionDoesNotExistException {
        Subject subject = getSubject(actorPrincipal);
        Executor executor = executorLogic.getExecutor(subject, executorStr);
        Identifiable definition = definitionLogic.getLatestProcessDefinition(subject, definitionStr);
        addPermissionOnIdentifiable(subject, executor, definition, permissionList);
    }

    @WebMethod
    public void setPermissionsOnDefinition(
            @WebParam(mode = Mode.IN, name = "actorPrincipal", targetNamespace = "http://runa.ru/workflow/webservices") ActorPrincipal actorPrincipal,
            @WebParam(mode = Mode.IN, name = "definition", targetNamespace = "http://runa.ru/workflow/webservices") String definitionStr,
            @WebParam(mode = Mode.IN, name = "executor", targetNamespace = "http://runa.ru/workflow/webservices") String executorStr,
            @WebParam(mode = Mode.IN, name = "permissions", targetNamespace = "http://runa.ru/workflow/webservices") List<String> permissionList)
            throws PermissionNotFoundException, UnapplicablePermissionException, ExecutorDoesNotExistException, AuthorizationException,
            AuthenticationException, DefinitionDoesNotExistException {
        Subject subject = getSubject(actorPrincipal);
        Executor executor = executorLogic.getExecutor(subject, executorStr);
        Identifiable definition = definitionLogic.getLatestProcessDefinition(subject, definitionStr);
        setPermissionOnIdentifiable(subject, executor, definition, permissionList);
    }

    @WebMethod
    public void removePermissionsOnDefinition(
            @WebParam(mode = Mode.IN, name = "actorPrincipal", targetNamespace = "http://runa.ru/workflow/webservices") ActorPrincipal actorPrincipal,
            @WebParam(mode = Mode.IN, name = "definition", targetNamespace = "http://runa.ru/workflow/webservices") String definitionStr,
            @WebParam(mode = Mode.IN, name = "executor", targetNamespace = "http://runa.ru/workflow/webservices") String executorStr,
            @WebParam(mode = Mode.IN, name = "permissions", targetNamespace = "http://runa.ru/workflow/webservices") List<String> permissionList)
            throws PermissionNotFoundException, UnapplicablePermissionException, ExecutorDoesNotExistException, AuthorizationException,
            AuthenticationException, DefinitionDoesNotExistException {
        Subject subject = getSubject(actorPrincipal);
        Executor executor = executorLogic.getExecutor(subject, executorStr);
        Identifiable definition = definitionLogic.getLatestProcessDefinition(subject, definitionStr);
        removePermissionOnIdentifiable(subject, executor, definition, permissionList);
    }

    @WebMethod
    public void addPermissionsOnProcesses(
            @WebParam(mode = Mode.IN, name = "actorPrincipal", targetNamespace = "http://runa.ru/workflow/webservices") ActorPrincipal actorPrincipal,
            @WebParam(mode = Mode.IN, name = "processName", targetNamespace = "http://runa.ru/workflow/webservices") String processName,
            @WebParam(mode = Mode.IN, name = "executor", targetNamespace = "http://runa.ru/workflow/webservices") String executorStr,
            @WebParam(mode = Mode.IN, name = "permissions", targetNamespace = "http://runa.ru/workflow/webservices") List<String> permissionList)
            throws PermissionNotFoundException, UnapplicablePermissionException, ExecutorDoesNotExistException, AuthorizationException,
            AuthenticationException {
        Subject subject = getSubject(actorPrincipal);
        Executor executor = executorLogic.getExecutor(subject, executorStr);
        List<WfProcess> processes = executionLogic.getProcessesForDefinitionName(subject, processName);
        for (WfProcess process : processes) {
            addPermissionOnIdentifiable(subject, executor, process, permissionList);
        }

    }

    @WebMethod
    public void setPermissionsOnProcesses(
            @WebParam(mode = Mode.IN, name = "actorPrincipal", targetNamespace = "http://runa.ru/workflow/webservices") ActorPrincipal actorPrincipal,
            @WebParam(mode = Mode.IN, name = "processName", targetNamespace = "http://runa.ru/workflow/webservices") String processName,
            @WebParam(mode = Mode.IN, name = "executor", targetNamespace = "http://runa.ru/workflow/webservices") String executorStr,
            @WebParam(mode = Mode.IN, name = "permissions", targetNamespace = "http://runa.ru/workflow/webservices") List<String> permissionList)
            throws PermissionNotFoundException, UnapplicablePermissionException, ExecutorDoesNotExistException, AuthorizationException,
            AuthenticationException {
        Subject subject = getSubject(actorPrincipal);
        Executor executor = executorLogic.getExecutor(subject, executorStr);
        List<WfProcess> processes = executionLogic.getProcessesForDefinitionName(subject, processName);
        for (WfProcess process : processes) {
            setPermissionOnIdentifiable(subject, executor, process, permissionList);
        }
    }

    @WebMethod
    public void removePermissionsOnProcesses(
            @WebParam(mode = Mode.IN, name = "actorPrincipal", targetNamespace = "http://runa.ru/workflow/webservices") ActorPrincipal actorPrincipal,
            @WebParam(mode = Mode.IN, name = "processName", targetNamespace = "http://runa.ru/workflow/webservices") String processName,
            @WebParam(mode = Mode.IN, name = "executor", targetNamespace = "http://runa.ru/workflow/webservices") String executorStr,
            @WebParam(mode = Mode.IN, name = "permissions", targetNamespace = "http://runa.ru/workflow/webservices") List<String> permissionList)
            throws PermissionNotFoundException, UnapplicablePermissionException, ExecutorDoesNotExistException, AuthorizationException,
            AuthenticationException {
        Subject subject = getSubject(actorPrincipal);
        Executor executor = executorLogic.getExecutor(subject, executorStr);
        List<WfProcess> processes = executionLogic.getProcessesForDefinitionName(subject, processName);
        for (WfProcess process : processes) {
            removePermissionOnIdentifiable(subject, executor, process, permissionList);
        }
    }

    @WebMethod
    public void removeAllPermissionsFromProcessDefinition(
            @WebParam(mode = Mode.IN, name = "actorPrincipal", targetNamespace = "http://runa.ru/workflow/webservices") ActorPrincipal actorPrincipal,
            @WebParam(mode = Mode.IN, name = "definitions", targetNamespace = "http://runa.ru/workflow/webservices") List<String> definitionList)
            throws UnapplicablePermissionException, ExecutorDoesNotExistException, AuthorizationException, AuthenticationException,
            DefinitionDoesNotExistException, AttributeRequiredException {
        Subject subject = getSubject(actorPrincipal);
        if (definitionList.size() == 0) {
            throw new AttributeRequiredException("removeAllPermissionsFromProcessDefinition", "definitions");
        }
        for (String definitionName : definitionList) {
            Identifiable definition = definitionLogic.getLatestProcessDefinition(subject, definitionName);
            removeAllPermissionOnIdentifiable(subject, definition);
        }
    }

    @WebMethod
    public void removeAllPermissionsFromProcesses(
            @WebParam(mode = Mode.IN, name = "actorPrincipal", targetNamespace = "http://runa.ru/workflow/webservices") ActorPrincipal actorPrincipal,
            @WebParam(mode = Mode.IN, name = "processNames", targetNamespace = "http://runa.ru/workflow/webservices") List<String> processNames)
            throws UnapplicablePermissionException, ExecutorDoesNotExistException, AuthorizationException, AuthenticationException,
            DefinitionDoesNotExistException, AttributeRequiredException {
        Subject subject = getSubject(actorPrincipal);

        if (processNames.size() == 0) {
            throw new AttributeRequiredException("removeAllPermissionsFromProcesses", "processes");
        }
        for (String processName : processNames) {
            List<WfProcess> processes = executionLogic.getProcessesForDefinitionName(subject, processName);
            if (processes.size() == 0) {
                throw new DefinitionDoesNotExistException(processName);
            }
            for (WfProcess process : processes) {
                removeAllPermissionOnIdentifiable(subject, process);
            }
        }
    }

    @WebMethod
    public void removeAllPermissionsFromExecutor(
            @WebParam(mode = Mode.IN, name = "actorPrincipal", targetNamespace = "http://runa.ru/workflow/webservices") ActorPrincipal actorPrincipal,
            @WebParam(mode = Mode.IN, name = "executor", targetNamespace = "http://runa.ru/workflow/webservices") String name)
            throws UnapplicablePermissionException, ExecutorDoesNotExistException, AuthorizationException, AuthenticationException {
        Subject subject = getSubject(actorPrincipal);
        Executor executor = executorLogic.getExecutor(subject, name);
        removeAllPermissionOnIdentifiable(subject, executor);
    }

    @WebMethod
    public void removeAllPermissionsFromSystem(
            @WebParam(mode = Mode.IN, name = "actorPrincipal", targetNamespace = "http://runa.ru/workflow/webservices") ActorPrincipal actorPrincipal)
            throws UnapplicablePermissionException, ExecutorDoesNotExistException, AuthorizationException, AuthenticationException {
        Subject subject = getSubject(actorPrincipal);
        removeAllPermissionOnIdentifiable(subject, ASystem.INSTANCE);
    }

    @WebMethod
    public void addPermissionsOnBotStations(
            @WebParam(mode = Mode.IN, name = "actorPrincipal", targetNamespace = "http://runa.ru/workflow/webservices") ActorPrincipal actorPrincipal,
            @WebParam(mode = Mode.IN, name = "executor", targetNamespace = "http://runa.ru/workflow/webservices") String executorStr,
            @WebParam(mode = Mode.IN, name = "permissions", targetNamespace = "http://runa.ru/workflow/webservices") List<String> permissionList)
            throws PermissionNotFoundException, UnapplicablePermissionException, ExecutorDoesNotExistException, AuthorizationException,
            AuthenticationException {
        Subject subject = getSubject(actorPrincipal);
        Executor executor = executorLogic.getExecutor(subject, executorStr);
        addPermissionOnIdentifiable(subject, executor, BotStation.INSTANCE, permissionList);
    }

    @WebMethod
    public void setPermissionsOnBotStations(
            @WebParam(mode = Mode.IN, name = "actorPrincipal", targetNamespace = "http://runa.ru/workflow/webservices") ActorPrincipal actorPrincipal,
            @WebParam(mode = Mode.IN, name = "executor", targetNamespace = "http://runa.ru/workflow/webservices") String executorStr,
            @WebParam(mode = Mode.IN, name = "permissions", targetNamespace = "http://runa.ru/workflow/webservices") List<String> permissionList)
            throws PermissionNotFoundException, UnapplicablePermissionException, ExecutorDoesNotExistException, AuthorizationException,
            AuthenticationException {
        Subject subject = getSubject(actorPrincipal);
        Executor executor = executorLogic.getExecutor(subject, executorStr);
        setPermissionOnIdentifiable(subject, executor, BotStation.INSTANCE, permissionList);
    }

    @WebMethod
    public void removePermissionsOnBotStations(
            @WebParam(mode = Mode.IN, name = "actorPrincipal", targetNamespace = "http://runa.ru/workflow/webservices") ActorPrincipal actorPrincipal,
            @WebParam(mode = Mode.IN, name = "executor", targetNamespace = "http://runa.ru/workflow/webservices") String executorStr,
            @WebParam(mode = Mode.IN, name = "permissions", targetNamespace = "http://runa.ru/workflow/webservices") List<String> permissionList)
            throws PermissionNotFoundException, UnapplicablePermissionException, ExecutorDoesNotExistException, AuthorizationException,
            AuthenticationException {
        Subject subject = getSubject(actorPrincipal);
        Executor executor = executorLogic.getExecutor(subject, executorStr);
        removePermissionOnIdentifiable(subject, executor, BotStation.INSTANCE, permissionList);
    }

    private void addPermissionOnIdentifiable(Subject subject, Executor executor, Identifiable identifiable, List<String> permissionList)
            throws PermissionNotFoundException, UnapplicablePermissionException, ExecutorDoesNotExistException, AuthorizationException,
            AuthenticationException {
        Collection<Permission> permissions = getPermissions(permissionList, identifiable);
        Collection<Permission> ownPermissions = authorizationLogic.getOwnPermissions(subject, executor, identifiable);
        permissions = Permission.mergePermissions(permissions, ownPermissions);
        authorizationLogic.setPermissions(subject, executor, permissions, identifiable);
    }

    private void setPermissionOnIdentifiable(Subject subject, Executor executor, Identifiable identifiable, List<String> permissionList)
            throws PermissionNotFoundException, UnapplicablePermissionException, ExecutorDoesNotExistException, AuthorizationException,
            AuthenticationException {
        Collection<Permission> permissions = getPermissions(permissionList, identifiable);
        authorizationLogic.setPermissions(subject, executor, permissions, identifiable);
    }

    private void removePermissionOnIdentifiable(Subject subject, Executor executor, Identifiable identifiable, List<String> permissionList)
            throws PermissionNotFoundException, UnapplicablePermissionException, ExecutorDoesNotExistException, AuthorizationException,
            AuthenticationException {
        Collection<Permission> permissions = getPermissions(permissionList, identifiable);
        Collection<Permission> ownPermissions = authorizationLogic.getOwnPermissions(subject, executor, identifiable);
        permissions = Permission.subtractPermissions(ownPermissions, permissions);
        authorizationLogic.setPermissions(subject, executor, permissions, identifiable);
    }

    private void removeAllPermissionOnIdentifiable(Subject subject, Identifiable identifiable) throws ExecutorDoesNotExistException,
            AuthorizationException, AuthenticationException {
        BatchPresentation batchPresentation = BatchPresentationFactory.EXECUTORS.createNonPaged();
        List<Executor> executors = authorizationLogic.getExecutorsWithPermission(subject, identifiable, batchPresentation, true);
        for (Executor executor : executors) {
            if (!authorizationLogic.isPrivelegedExecutor(subject, executor, identifiable)) {
                authorizationLogic.setPermissions(subject, executor, Permission.getNoPermissions(), identifiable);
            }
        }
    }

    private Collection<Permission> getPermissions(List<String> permissionList, Identifiable identifiable) throws PermissionNotFoundException {
        Permission noPermission = identifiable.getSecuredObjectType().getNoPermission();
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
