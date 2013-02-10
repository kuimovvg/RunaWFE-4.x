package ru.runa.wf.webservice;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.ejb.Stateless;
import javax.interceptor.Interceptors;
import javax.jws.WebMethod;
import javax.jws.WebParam;
import javax.jws.WebParam.Mode;
import javax.jws.WebService;
import javax.jws.soap.SOAPBinding;
import javax.xml.parsers.ParserConfigurationException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.ejb.interceptor.SpringBeanAutowiringInterceptor;

import ru.runa.WSLoggerInterceptor;
import ru.runa.wfe.definition.DefinitionAlreadyExistException;
import ru.runa.wfe.definition.DefinitionArchiveFormatException;
import ru.runa.wfe.definition.DefinitionDoesNotExistException;
import ru.runa.wfe.definition.dto.WfDefinition;
import ru.runa.wfe.definition.logic.DefinitionLogic;
import ru.runa.wfe.execution.ParentProcessExistsException;
import ru.runa.wfe.execution.ProcessDoesNotExistException;
import ru.runa.wfe.presentation.BatchPresentation;
import ru.runa.wfe.presentation.BatchPresentationFactory;
import ru.runa.wfe.security.AuthenticationException;
import ru.runa.wfe.security.AuthorizationException;
import ru.runa.wfe.user.User;

import com.google.common.base.Throwables;
import com.google.common.collect.Lists;
import com.google.common.io.Files;

@Stateless
@WebService(name = "Definition", targetNamespace = "http://runa.ru/workflow/webservices", serviceName = "DefinitionWebService")
@SOAPBinding(parameterStyle = SOAPBinding.ParameterStyle.WRAPPED)
@Interceptors({ SpringBeanAutowiringInterceptor.class, WSLoggerInterceptor.class })
public class DefinitionBean {
    @Autowired
    private DefinitionLogic definitionLogic;

    @WebMethod
    public void deployProcessDefinitionLocal(
            @WebParam(mode = Mode.IN, name = "actorPrincipal", targetNamespace = "http://runa.ru/workflow/webservices") User user,
            @WebParam(mode = Mode.IN, name = "file", targetNamespace = "http://runa.ru/workflow/webservices") String file,
            @WebParam(mode = Mode.IN, name = "type", targetNamespace = "http://runa.ru/workflow/webservices") String type)
            throws AuthenticationException, AuthorizationException, DefinitionAlreadyExistException, DefinitionArchiveFormatException,
            ParserConfigurationException {
        try {
            byte[] scriptBytes = Files.toByteArray(new File(file));
            deployProcessDefinition(user, scriptBytes, type);
        } catch (IOException e) {
            throw Throwables.propagate(e);
        }
    }

    @WebMethod
    public void deployProcessDefinition(
            @WebParam(mode = Mode.IN, name = "actorPrincipal", targetNamespace = "http://runa.ru/workflow/webservices") User user,
            @WebParam(mode = Mode.IN, name = "parFiles", targetNamespace = "http://runa.ru/workflow/webservices") byte[] parBytes,
            @WebParam(mode = Mode.IN, name = "type", targetNamespace = "http://runa.ru/workflow/webservices") String type)
            throws AuthenticationException, AuthorizationException, DefinitionAlreadyExistException, DefinitionArchiveFormatException,
            ParserConfigurationException {
        definitionLogic.deployProcessDefinition(user, parBytes, parseTypes(type));
    }

    private List<String> parseTypes(String type) {
        if (type == null || type.length() == 0) {
            return Lists.newArrayList("Script");
        } else {
            int slashIdx = 0;
            List<String> result = new ArrayList<String>();
            while (true) {
                if (type.indexOf('/', slashIdx) != -1) {
                    result.add(type.substring(slashIdx, type.indexOf('/', slashIdx)));
                    slashIdx = type.indexOf('/', slashIdx) + 1;
                } else {
                    result.add(type.substring(slashIdx));
                    break;
                }
            }
            return result;
        }
    }

    @WebMethod
    public void redeployProcessDefinitionLocal(
            @WebParam(mode = Mode.IN, name = "actorPrincipal", targetNamespace = "http://runa.ru/workflow/webservices") User user,
            @WebParam(mode = Mode.IN, name = "definitionId", targetNamespace = "http://runa.ru/workflow/webservices") Long definitionId,
            @WebParam(mode = Mode.IN, name = "file", targetNamespace = "http://runa.ru/workflow/webservices") String file,
            @WebParam(mode = Mode.IN, name = "type", targetNamespace = "http://runa.ru/workflow/webservices") String type)
            throws AuthenticationException, AuthorizationException, DefinitionAlreadyExistException, DefinitionArchiveFormatException {
        try {
            byte[] scriptBytes = Files.toByteArray(new File(file));
            redeployProcessDefinition(user, definitionId, scriptBytes, type);
        } catch (IOException e) {
            throw Throwables.propagate(e);
        }
    }

    @WebMethod
    public void redeployProcessDefinition(
            @WebParam(mode = Mode.IN, name = "actorPrincipal", targetNamespace = "http://runa.ru/workflow/webservices") User user,
            @WebParam(mode = Mode.IN, name = "definitionId", targetNamespace = "http://runa.ru/workflow/webservices") Long definitionId,
            @WebParam(mode = Mode.IN, name = "parBytes", targetNamespace = "http://runa.ru/workflow/webservices") byte[] parBytes,
            @WebParam(mode = Mode.IN, name = "type", targetNamespace = "http://runa.ru/workflow/webservices") String type)
            throws AuthenticationException, AuthorizationException, DefinitionAlreadyExistException, DefinitionArchiveFormatException {
        definitionLogic.redeployProcessDefinition(user, definitionId, parBytes, parseTypes(type));
    }

    @WebMethod
    public void undeployProcessDefinition(
            @WebParam(mode = Mode.IN, name = "actorPrincipal", targetNamespace = "http://runa.ru/workflow/webservices") User user,
            @WebParam(mode = Mode.IN, name = "name", targetNamespace = "http://runa.ru/workflow/webservices") String name)
            throws AuthenticationException, AuthorizationException, DefinitionDoesNotExistException, ParentProcessExistsException {
        definitionLogic.undeployProcessDefinition(user, name);
    }

    @WebMethod
    public WfDefinition getLatestProcessDefinition(
            @WebParam(mode = Mode.IN, name = "actorPrincipal", targetNamespace = "http://runa.ru/workflow/webservices") User user,
            @WebParam(mode = Mode.IN, name = "name", targetNamespace = "http://runa.ru/workflow/webservices") String name)
            throws AuthenticationException, AuthorizationException, DefinitionDoesNotExistException {
        return definitionLogic.getLatestProcessDefinition(user, name);
    }

    @WebMethod
    public WfDefinition getProcessDefinition(
            @WebParam(mode = Mode.IN, name = "actorPrincipal", targetNamespace = "http://runa.ru/workflow/webservices") User user,
            @WebParam(mode = Mode.IN, name = "definitionId", targetNamespace = "http://runa.ru/workflow/webservices") Long definitionId)
            throws AuthenticationException, AuthorizationException, DefinitionDoesNotExistException {
        return definitionLogic.getProcessDefinition(user, definitionId);
    }

    @WebMethod
    public WfDefinition getProcessDefinitionByProcessId(
            @WebParam(mode = Mode.IN, name = "actorPrincipal", targetNamespace = "http://runa.ru/workflow/webservices") User user,
            @WebParam(mode = Mode.IN, name = "processId", targetNamespace = "http://runa.ru/workflow/webservices") Long processId)
            throws AuthenticationException, AuthorizationException, DefinitionDoesNotExistException, ProcessDoesNotExistException {
        return definitionLogic.getProcessDefinitionByProcessId(user, processId);
    }

    @WebMethod
    public List<WfDefinition> getLatestProcessDefinitions(
            @WebParam(mode = Mode.IN, name = "actorPrincipal", targetNamespace = "http://runa.ru/workflow/webservices") User user)
            throws AuthenticationException, AuthorizationException, DefinitionDoesNotExistException {
        BatchPresentation batchPresentation = BatchPresentationFactory.DEFINITIONS.createNonPaged();
        return definitionLogic.getLatestProcessDefinitions(user, batchPresentation);
    }

}
