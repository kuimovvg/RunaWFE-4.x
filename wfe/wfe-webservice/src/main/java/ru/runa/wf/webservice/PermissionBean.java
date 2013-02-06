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
import ru.runa.wfe.user.Executor;
import ru.runa.wfe.user.ExecutorDoesNotExistException;
import ru.runa.wfe.user.User;
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
            @WebParam(mode = Mode.IN, name = "actorPrincipal", targetNamespace = "http://runa.ru/workflow/webservices") User user,
            @WebParam(mode = Mode.IN, name = "actor", targetNamespace = "http://runa.ru/workflow/webservices") String actorStr,
            @WebParam(mode = Mode.IN, name = "executor", targetNamespace = "http://runa.ru/workflow/webservices") String executorStr,
            @WebParam(mode = Mode.IN, name = "permissions", targetNamespace = "http://runa.ru/workflow/webservices") List<String> permissionList)
            throws PermissionNotFoundException, UnapplicablePermissionException, ExecutorDoesNotExistException, AuthorizationException,
            AuthenticationException {
        Executor executor = executorLogic.getExecutor(user, executorStr);
        Identifiable actor = executorLogic.getActor(user, actorStr);
        addPermissionOnIdentifiable(user, executor, actor, permissionList);
    }

    @WebMethod
    public void setPermissionsOnActor(
            @WebParam(mode = Mode.IN, name = "actorPrincipal", targetNamespace = "http://runa.ru/workflow/webservices") User user,
            @WebParam(mode = Mode.IN, name = "actor", targetNamespace = "http://runa.ru/workflow/webservices") String actorStr,
            @WebParam(mode = Mode.IN, name = "executor", targetNamespace = "http://runa.ru/workflow/webservices") String executorStr,
            @WebParam(mode = Mode.IN, name = "permissions", targetNamespace = "http://runa.ru/workflow/webservices") List<String> permissionList)
            throws PermissionNotFoundException, UnapplicablePermissionException, ExecutorDoesNotExistException, AuthorizationException,
            AuthenticationException {
        Executor executor = executorLogic.getExecutor(user, executorStr);
        Identifiable actor = executorLogic.getActor(user, actorStr);
        setPermissionOnIdentifiable(user, executor, actor, permissionList);
    }

    @WebMethod
    public void removePermissionsOnActor(
            @WebParam(mode = Mode.IN, name = "actorPrincipal", targetNamespace = "http://runa.ru/workflow/webservices") User user,
            @WebParam(mode = Mode.IN, name = "actor", targetNamespace = "http://runa.ru/workflow/webservices") String actorStr,
            @WebParam(mode = Mode.IN, name = "executor", targetNamespace = "http://runa.ru/workflow/webservices") String executorStr,
            @WebParam(mode = Mode.IN, name = "permissions", targetNamespace = "http://runa.ru/workflow/webservices") List<String> permissionList)
            throws PermissionNotFoundException, UnapplicablePermissionException, ExecutorDoesNotExistException, AuthorizationException,
            AuthenticationException {
        Executor executor = executorLogic.getExecutor(user, executorStr);
        Identifiable actor = executorLogic.getActor(user, actorStr);
        removePermissionOnIdentifiable(user, executor, actor, permissionList);
    }

    @WebMethod
    public void addPermissionsOnGroup(
            @WebParam(mode = Mode.IN, name = "actorPrincipal", targetNamespace = "http://runa.ru/workflow/webservices") User user,
            @WebParam(mode = Mode.IN, name = "group", targetNamespace = "http://runa.ru/workflow/webservices") String groupStr,
            @WebParam(mode = Mode.IN, name = "executor", targetNamespace = "http://runa.ru/workflow/webservices") String executorStr,
            @WebParam(mode = Mode.IN, name = "permissions", targetNamespace = "http://runa.ru/workflow/webservices") List<String> permissionList)
            throws PermissionNotFoundException, UnapplicablePermissionException, ExecutorDoesNotExistException, AuthorizationException,
            AuthenticationException {
        Executor executor = executorLogic.getExecutor(user, executorStr);
        Identifiable group = executorLogic.getGroup(user, groupStr);
        addPermissionOnIdentifiable(user, executor, group, permissionList);
    }

    @WebMethod
    public void setPermissionsOnGroup(
            @WebParam(mode = Mode.IN, name = "actorPrincipal", targetNamespace = "http://runa.ru/workflow/webservices") User user,
            @WebParam(mode = Mode.IN, name = "group", targetNamespace = "http://runa.ru/workflow/webservices") String groupStr,
            @WebParam(mode = Mode.IN, name = "executor", targetNamespace = "http://runa.ru/workflow/webservices") String executorStr,
            @WebParam(mode = Mode.IN, name = "permissions", targetNamespace = "http://runa.ru/workflow/webservices") List<String> permissionList)
            throws PermissionNotFoundException, UnapplicablePermissionException, ExecutorDoesNotExistException, AuthorizationException,
            AuthenticationException {
        Executor executor = executorLogic.getExecutor(user, executorStr);
        Identifiable group = executorLogic.getGroup(user, groupStr);
        setPermissionOnIdentifiable(user, executor, group, permissionList);
    }

    @WebMethod
    public void removePermissionsOnGroup(
            @WebParam(mode = Mode.IN, name = "actorPrincipal", targetNamespace = "http://runa.ru/workflow/webservices") User user,
            @WebParam(mode = Mode.IN, name = "group", targetNamespace = "http://runa.ru/workflow/webservices") String groupStr,
            @WebParam(mode = Mode.IN, name = "executor", targetNamespace = "http://runa.ru/workflow/webservices") String executorStr,
            @WebParam(mode = Mode.IN, name = "permissions", targetNamespace = "http://runa.ru/workflow/webservices") List<String> permissionList)
            throws PermissionNotFoundException, UnapplicablePermissionException, ExecutorDoesNotExistException, AuthorizationException,
            AuthenticationException {
        Executor executor = executorLogic.getExecutor(user, executorStr);
        Identifiable group = executorLogic.getGroup(user, groupStr);
        removePermissionOnIdentifiable(user, executor, group, permissionList);
    }

    @WebMethod
    public void addPermissionsOnSystem(
            @WebParam(mode = Mode.IN, name = "actorPrincipal", targetNamespace = "http://runa.ru/workflow/webservices") User user,
            @WebParam(mode = Mode.IN, name = "executor", targetNamespace = "http://runa.ru/workflow/webservices") String executorStr,
            @WebParam(mode = Mode.IN, name = "permissions", targetNamespace = "http://runa.ru/workflow/webservices") List<String> permissionList)
            throws PermissionNotFoundException, UnapplicablePermissionException, ExecutorDoesNotExistException, AuthorizationException,
            AuthenticationException {
        Executor executor = executorLogic.getExecutor(user, executorStr);
        addPermissionOnIdentifiable(user, executor, ASystem.INSTANCE, permissionList);
    }

    @WebMethod
    public void setPermissionsOnSystem(
            @WebParam(mode = Mode.IN, name = "actorPrincipal", targetNamespace = "http://runa.ru/workflow/webservices") User user,
            @WebParam(mode = Mode.IN, name = "executor", targetNamespace = "http://runa.ru/workflow/webservices") String executorStr,
            @WebParam(mode = Mode.IN, name = "permissions", targetNamespace = "http://runa.ru/workflow/webservices") List<String> permissionList)
            throws PermissionNotFoundException, UnapplicablePermissionException, ExecutorDoesNotExistException, AuthorizationException,
            AuthenticationException {
        Executor executor = executorLogic.getExecutor(user, executorStr);
        setPermissionOnIdentifiable(user, executor, ASystem.INSTANCE, permissionList);
    }

    @WebMethod
    public void removePermissionsOnSystem(
            @WebParam(mode = Mode.IN, name = "actorPrincipal", targetNamespace = "http://runa.ru/workflow/webservices") User user,
            @WebParam(mode = Mode.IN, name = "executor", targetNamespace = "http://runa.ru/workflow/webservices") String executorStr,
            @WebParam(mode = Mode.IN, name = "permissions", targetNamespace = "http://runa.ru/workflow/webservices") List<String> permissionList)
            throws PermissionNotFoundException, UnapplicablePermissionException, ExecutorDoesNotExistException, AuthorizationException,
            AuthenticationException {
        Executor executor = executorLogic.getExecutor(user, executorStr);
        removePermissionOnIdentifiable(user, executor, ASystem.INSTANCE, permissionList);
    }

    @WebMethod
    public void addPermissionsOnDefinition(
            @WebParam(mode = Mode.IN, name = "actorPrincipal", targetNamespace = "http://runa.ru/workflow/webservices") User user,
            @WebParam(mode = Mode.IN, name = "definition", targetNamespace = "http://runa.ru/workflow/webservices") String definitionStr,
            @WebParam(mode = Mode.IN, name = "executor", targetNamespace = "http://runa.ru/workflow/webservices") String executorStr,
            @WebParam(mode = Mode.IN, name = "permissions", targetNamespace = "http://runa.ru/workflow/webservices") List<String> permissionList)
            throws PermissionNotFoundException, UnapplicablePermissionException, ExecutorDoesNotExistException, AuthorizationException,
            AuthenticationException, DefinitionDoesNotExistException {
        Executor executor = executorLogic.getExecutor(user, executorStr);
        Identifiable definition = definitionLogic.getLatestProcessDefinition(user, definitionStr);
        addPermissionOnIdentifiable(user, executor, definition, permissionList);
    }

    @WebMethod
    public void setPermissionsOnDefinition(
            @WebParam(mode = Mode.IN, name = "actorPrincipal", targetNamespace = "http://runa.ru/workflow/webservices") User user,
            @WebParam(mode = Mode.IN, name = "definition", targetNamespace = "http://runa.ru/workflow/webservices") String definitionStr,
            @WebParam(mode = Mode.IN, name = "executor", targetNamespace = "http://runa.ru/workflow/webservices") String executorStr,
            @WebParam(mode = Mode.IN, name = "permissions", targetNamespace = "http://runa.ru/workflow/webservices") List<String> permissionList)
            throws PermissionNotFoundException, UnapplicablePermissionException, ExecutorDoesNotExistException, AuthorizationException,
            AuthenticationException, DefinitionDoesNotExistException {
        Executor executor = executorLogic.getExecutor(user, executorStr);
        Identifiable definition = definitionLogic.getLatestProcessDefinition(user, definitionStr);
        setPermissionOnIdentifiable(user, executor, definition, permissionList);
    }

    @WebMethod
    public void removePermissionsOnDefinition(
            @WebParam(mode = Mode.IN, name = "actorPrincipal", targetNamespace = "http://runa.ru/workflow/webservices") User user,
            @WebParam(mode = Mode.IN, name = "definition", targetNamespace = "http://runa.ru/workflow/webservices") String definitionStr,
            @WebParam(mode = Mode.IN, name = "executor", targetNamespace = "http://runa.ru/workflow/webservices") String executorStr,
            @WebParam(mode = Mode.IN, name = "permissions", targetNamespace = "http://runa.ru/workflow/webservices") List<String> permissionList)
            throws PermissionNotFoundException, UnapplicablePermissionException, ExecutorDoesNotExistException, AuthorizationException,
            AuthenticationException, DefinitionDoesNotExistException {
        Executor executor = executorLogic.getExecutor(user, executorStr);
        Identifiable definition = definitionLogic.getLatestProcessDefinition(user, definitionStr);
        removePermissionOnIdentifiable(user, executor, definition, permissionList);
    }

    @WebMethod
    public void addPermissionsOnProcesses(
            @WebParam(mode = Mode.IN, name = "actorPrincipal", targetNamespace = "http://runa.ru/workflow/webservices") User user,
            @WebParam(mode = Mode.IN, name = "processName", targetNamespace = "http://runa.ru/workflow/webservices") String processName,
            @WebParam(mode = Mode.IN, name = "executor", targetNamespace = "http://runa.ru/workflow/webservices") String executorStr,
            @WebParam(mode = Mode.IN, name = "permissions", targetNamespace = "http://runa.ru/workflow/webservices") List<String> permissionList)
            throws PermissionNotFoundException, UnapplicablePermissionException, ExecutorDoesNotExistException, AuthorizationException,
            AuthenticationException {
        Executor executor = executorLogic.getExecutor(user, executorStr);
        List<WfProcess> processes = executionLogic.getProcessesForDefinitionName(user, processName);
        for (WfProcess process : processes) {
            addPermissionOnIdentifiable(user, executor, process, permissionList);
        }

    }

    @WebMethod
    public void setPermissionsOnProcesses(
            @WebParam(mode = Mode.IN, name = "actorPrincipal", targetNamespace = "http://runa.ru/workflow/webservices") User user,
            @WebParam(mode = Mode.IN, name = "processName", targetNamespace = "http://runa.ru/workflow/webservices") String processName,
            @WebParam(mode = Mode.IN, name = "executor", targetNamespace = "http://runa.ru/workflow/webservices") String executorStr,
            @WebParam(mode = Mode.IN, name = "permissions", targetNamespace = "http://runa.ru/workflow/webservices") List<String> permissionList)
            throws PermissionNotFoundException, UnapplicablePermissionException, ExecutorDoesNotExistException, AuthorizationException,
            AuthenticationException {
        Executor executor = executorLogic.getExecutor(user, executorStr);
        List<WfProcess> processes = executionLogic.getProcessesForDefinitionName(user, processName);
        for (WfProcess process : processes) {
            setPermissionOnIdentifiable(user, executor, process, permissionList);
        }
    }

    @WebMethod
    public void removePermissionsOnProcesses(
            @WebParam(mode = Mode.IN, name = "actorPrincipal", targetNamespace = "http://runa.ru/workflow/webservices") User user,
            @WebParam(mode = Mode.IN, name = "processName", targetNamespace = "http://runa.ru/workflow/webservices") String processName,
            @WebParam(mode = Mode.IN, name = "executor", targetNamespace = "http://runa.ru/workflow/webservices") String executorStr,
            @WebParam(mode = Mode.IN, name = "permissions", targetNamespace = "http://runa.ru/workflow/webservices") List<String> permissionList)
            throws PermissionNotFoundException, UnapplicablePermissionException, ExecutorDoesNotExistException, AuthorizationException,
            AuthenticationException {
        Executor executor = executorLogic.getExecutor(user, executorStr);
        List<WfProcess> processes = executionLogic.getProcessesForDefinitionName(user, processName);
        for (WfProcess process : processes) {
            removePermissionOnIdentifiable(user, executor, process, permissionList);
        }
    }

    @WebMethod
    public void removeAllPermissionsFromProcessDefinition(
            @WebParam(mode = Mode.IN, name = "actorPrincipal", targetNamespace = "http://runa.ru/workflow/webservices") User user,
            @WebParam(mode = Mode.IN, name = "definitions", targetNamespace = "http://runa.ru/workflow/webservices") List<String> definitionList)
            throws UnapplicablePermissionException, ExecutorDoesNotExistException, AuthorizationException, AuthenticationException,
            DefinitionDoesNotExistException, AttributeRequiredException {
        if (definitionList.size() == 0) {
            throw new AttributeRequiredException("removeAllPermissionsFromProcessDefinition", "definitions");
        }
        for (String definitionName : definitionList) {
            Identifiable definition = definitionLogic.getLatestProcessDefinition(user, definitionName);
            removeAllPermissionOnIdentifiable(user, definition);
        }
    }

    @WebMethod
    public void removeAllPermissionsFromProcesses(
            @WebParam(mode = Mode.IN, name = "actorPrincipal", targetNamespace = "http://runa.ru/workflow/webservices") User user,
            @WebParam(mode = Mode.IN, name = "processNames", targetNamespace = "http://runa.ru/workflow/webservices") List<String> processNames)
            throws UnapplicablePermissionException, ExecutorDoesNotExistException, AuthorizationException, AuthenticationException,
            DefinitionDoesNotExistException, AttributeRequiredException {
        if (processNames.size() == 0) {
            throw new AttributeRequiredException("removeAllPermissionsFromProcesses", "processes");
        }
        for (String processName : processNames) {
            List<WfProcess> processes = executionLogic.getProcessesForDefinitionName(user, processName);
            if (processes.size() == 0) {
                throw new DefinitionDoesNotExistException(processName);
            }
            for (WfProcess process : processes) {
                removeAllPermissionOnIdentifiable(user, process);
            }
        }
    }

    @WebMethod
    public void removeAllPermissionsFromExecutor(
            @WebParam(mode = Mode.IN, name = "actorPrincipal", targetNamespace = "http://runa.ru/workflow/webservices") User user,
            @WebParam(mode = Mode.IN, name = "executor", targetNamespace = "http://runa.ru/workflow/webservices") String name)
            throws UnapplicablePermissionException, ExecutorDoesNotExistException, AuthorizationException, AuthenticationException {
        Executor executor = executorLogic.getExecutor(user, name);
        removeAllPermissionOnIdentifiable(user, executor);
    }

    @WebMethod
    public void removeAllPermissionsFromSystem(
            @WebParam(mode = Mode.IN, name = "actorPrincipal", targetNamespace = "http://runa.ru/workflow/webservices") User user)
            throws UnapplicablePermissionException, ExecutorDoesNotExistException, AuthorizationException, AuthenticationException {
        removeAllPermissionOnIdentifiable(user, ASystem.INSTANCE);
    }

    @WebMethod
    public void addPermissionsOnBotStations(
            @WebParam(mode = Mode.IN, name = "actorPrincipal", targetNamespace = "http://runa.ru/workflow/webservices") User user,
            @WebParam(mode = Mode.IN, name = "executor", targetNamespace = "http://runa.ru/workflow/webservices") String executorStr,
            @WebParam(mode = Mode.IN, name = "permissions", targetNamespace = "http://runa.ru/workflow/webservices") List<String> permissionList)
            throws PermissionNotFoundException, UnapplicablePermissionException, ExecutorDoesNotExistException, AuthorizationException,
            AuthenticationException {
        Executor executor = executorLogic.getExecutor(user, executorStr);
        addPermissionOnIdentifiable(user, executor, BotStation.INSTANCE, permissionList);
    }

    @WebMethod
    public void setPermissionsOnBotStations(
            @WebParam(mode = Mode.IN, name = "actorPrincipal", targetNamespace = "http://runa.ru/workflow/webservices") User user,
            @WebParam(mode = Mode.IN, name = "executor", targetNamespace = "http://runa.ru/workflow/webservices") String executorStr,
            @WebParam(mode = Mode.IN, name = "permissions", targetNamespace = "http://runa.ru/workflow/webservices") List<String> permissionList)
            throws PermissionNotFoundException, UnapplicablePermissionException, ExecutorDoesNotExistException, AuthorizationException,
            AuthenticationException {
        Executor executor = executorLogic.getExecutor(user, executorStr);
        setPermissionOnIdentifiable(user, executor, BotStation.INSTANCE, permissionList);
    }

    @WebMethod
    public void removePermissionsOnBotStations(
            @WebParam(mode = Mode.IN, name = "actorPrincipal", targetNamespace = "http://runa.ru/workflow/webservices") User user,
            @WebParam(mode = Mode.IN, name = "executor", targetNamespace = "http://runa.ru/workflow/webservices") String executorStr,
            @WebParam(mode = Mode.IN, name = "permissions", targetNamespace = "http://runa.ru/workflow/webservices") List<String> permissionList)
            throws PermissionNotFoundException, UnapplicablePermissionException, ExecutorDoesNotExistException, AuthorizationException,
            AuthenticationException {
        Executor executor = executorLogic.getExecutor(user, executorStr);
        removePermissionOnIdentifiable(user, executor, BotStation.INSTANCE, permissionList);
    }

    private void addPermissionOnIdentifiable(User user, Executor executor, Identifiable identifiable, List<String> permissionList)
            throws PermissionNotFoundException, UnapplicablePermissionException, ExecutorDoesNotExistException, AuthorizationException,
            AuthenticationException {
        Collection<Permission> permissions = getPermissions(permissionList, identifiable);
        // Collection<Permission> ownPermissions =
        // authorizationLogic.getOwnPermissions(user, executor, identifiable);
        // permissions = Permission.mergePermissions(permissions,
        // ownPermissions);
        // authorizationLogic.setPermissions(user, executor, permissions,
        // identifiable);
    }

    private void setPermissionOnIdentifiable(User user, Executor executor, Identifiable identifiable, List<String> permissionList)
            throws PermissionNotFoundException, UnapplicablePermissionException, ExecutorDoesNotExistException, AuthorizationException,
            AuthenticationException {
        Collection<Permission> permissions = getPermissions(permissionList, identifiable);
        authorizationLogic.setPermissions(user, executor, permissions, identifiable);
    }

    private void removePermissionOnIdentifiable(User user, Executor executor, Identifiable identifiable, List<String> permissionList)
            throws PermissionNotFoundException, UnapplicablePermissionException, ExecutorDoesNotExistException, AuthorizationException,
            AuthenticationException {
        Collection<Permission> permissions = getPermissions(permissionList, identifiable);
        // Collection<Permission> ownPermissions =
        // authorizationLogic.getOwnPermissions(user, executor, identifiable);
        // permissions = Permission.subtractPermissions(ownPermissions,
        // permissions);
        // authorizationLogic.setPermissions(user, executor, permissions,
        // identifiable);
    }

    private void removeAllPermissionOnIdentifiable(User user, Identifiable identifiable) throws ExecutorDoesNotExistException,
            AuthorizationException, AuthenticationException {
        BatchPresentation batchPresentation = BatchPresentationFactory.EXECUTORS.createNonPaged();
        List<? extends Executor> executors = authorizationLogic.getExecutorsWithPermission(user, identifiable, batchPresentation, true);
        for (Executor executor : executors) {
            if (!authorizationLogic.isPrivelegedExecutor(user, executor, identifiable)) {
                authorizationLogic.setPermissions(user, executor, Permission.getNoPermissions(), identifiable);
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

}
