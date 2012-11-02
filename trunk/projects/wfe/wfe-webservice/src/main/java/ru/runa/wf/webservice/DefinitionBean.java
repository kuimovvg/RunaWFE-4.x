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
import javax.security.auth.Subject;
import javax.xml.parsers.ParserConfigurationException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.ejb.interceptor.SpringBeanAutowiringInterceptor;

import ru.runa.WSLoggerInterceptor;
import ru.runa.wfe.ApplicationException;
import ru.runa.wfe.InternalApplicationException;
import ru.runa.wfe.definition.DefinitionAlreadyExistException;
import ru.runa.wfe.definition.DefinitionArchiveFormatException;
import ru.runa.wfe.definition.DefinitionDoesNotExistException;
import ru.runa.wfe.definition.dto.WfDefinition;
import ru.runa.wfe.definition.logic.DefinitionLogic;
import ru.runa.wfe.execution.ProcessDoesNotExistException;
import ru.runa.wfe.execution.SuperProcessExistsException;
import ru.runa.wfe.presentation.BatchPresentation;
import ru.runa.wfe.presentation.BatchPresentationConsts;
import ru.runa.wfe.presentation.BatchPresentationFactory;
import ru.runa.wfe.security.AuthenticationException;
import ru.runa.wfe.security.AuthorizationException;
import ru.runa.wfe.user.ActorPrincipal;

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
            @WebParam(mode = Mode.IN, name = "actorPrincipal", targetNamespace = "http://runa.ru/workflow/webservices") ActorPrincipal actor,
            @WebParam(mode = Mode.IN, name = "file", targetNamespace = "http://runa.ru/workflow/webservices") String file,
            @WebParam(mode = Mode.IN, name = "type", targetNamespace = "http://runa.ru/workflow/webservices") String type)
            throws AuthenticationException, AuthorizationException, DefinitionAlreadyExistException, DefinitionArchiveFormatException,
            ParserConfigurationException {
        try {
            byte[] scriptBytes = Files.toByteArray(new File(file));
            deployProcessDefinition(actor, scriptBytes, type);
        } catch (IOException e) {
            throw new ApplicationException(e);
        }
    }

    @WebMethod
    public void deployProcessDefinition(
            @WebParam(mode = Mode.IN, name = "actorPrincipal", targetNamespace = "http://runa.ru/workflow/webservices") ActorPrincipal actor,
            @WebParam(mode = Mode.IN, name = "parFiles", targetNamespace = "http://runa.ru/workflow/webservices") byte[] parBytes,
            @WebParam(mode = Mode.IN, name = "type", targetNamespace = "http://runa.ru/workflow/webservices") String type)
            throws AuthenticationException, AuthorizationException, DefinitionAlreadyExistException, DefinitionArchiveFormatException,
            ParserConfigurationException {
        Subject subject = getSubject(actor);
        definitionLogic.deployProcessDefinition(subject, parBytes, parseTypes(type));
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
            @WebParam(mode = Mode.IN, name = "actorPrincipal", targetNamespace = "http://runa.ru/workflow/webservices") ActorPrincipal actor,
            @WebParam(mode = Mode.IN, name = "definitionId", targetNamespace = "http://runa.ru/workflow/webservices") Long definitionId,
            @WebParam(mode = Mode.IN, name = "file", targetNamespace = "http://runa.ru/workflow/webservices") String file,
            @WebParam(mode = Mode.IN, name = "type", targetNamespace = "http://runa.ru/workflow/webservices") String type)
            throws AuthenticationException, AuthorizationException, DefinitionAlreadyExistException, DefinitionArchiveFormatException,
            ParserConfigurationException {
        try {
            byte[] scriptBytes = Files.toByteArray(new File(file));
            redeployProcessDefinition(actor, definitionId, scriptBytes, type);
        } catch (Exception e) {
            throw new InternalApplicationException(e);
        }
    }

    @WebMethod
    public void redeployProcessDefinition(
            @WebParam(mode = Mode.IN, name = "actorPrincipal", targetNamespace = "http://runa.ru/workflow/webservices") ActorPrincipal actor,
            @WebParam(mode = Mode.IN, name = "definitionId", targetNamespace = "http://runa.ru/workflow/webservices") Long definitionId,
            @WebParam(mode = Mode.IN, name = "parBytes", targetNamespace = "http://runa.ru/workflow/webservices") byte[] parBytes,
            @WebParam(mode = Mode.IN, name = "type", targetNamespace = "http://runa.ru/workflow/webservices") String type)
            throws AuthenticationException, AuthorizationException, DefinitionAlreadyExistException, DefinitionArchiveFormatException,
            ParserConfigurationException {
        try {
            Subject subject = getSubject(actor);
            definitionLogic.redeployProcessDefinition(subject, definitionId, parBytes, parseTypes(type));
        } catch (Exception e) {
            throw new InternalApplicationException(e);
        }
    }

    @WebMethod
    public void undeployProcessDefinition(
            @WebParam(mode = Mode.IN, name = "actorPrincipal", targetNamespace = "http://runa.ru/workflow/webservices") ActorPrincipal actor,
            @WebParam(mode = Mode.IN, name = "name", targetNamespace = "http://runa.ru/workflow/webservices") String name)
            throws AuthenticationException, AuthorizationException, DefinitionDoesNotExistException, SuperProcessExistsException {
        Subject subject = getSubject(actor);
        definitionLogic.undeployProcessDefinition(subject, name);
    }

    @WebMethod
    public WfDefinition getLatestProcessDefinitionStub(
            @WebParam(mode = Mode.IN, name = "actorPrincipal", targetNamespace = "http://runa.ru/workflow/webservices") ActorPrincipal actor,
            @WebParam(mode = Mode.IN, name = "name", targetNamespace = "http://runa.ru/workflow/webservices") String name)
            throws AuthenticationException, AuthorizationException, DefinitionDoesNotExistException {
        Subject subject = getSubject(actor);
        return definitionLogic.getLatestProcessDefinition(subject, name);
    }

    @WebMethod
    public WfDefinition getProcessDefinitionStub(
            @WebParam(mode = Mode.IN, name = "actorPrincipal", targetNamespace = "http://runa.ru/workflow/webservices") ActorPrincipal actor,
            @WebParam(mode = Mode.IN, name = "definitionId", targetNamespace = "http://runa.ru/workflow/webservices") Long definitionId)
            throws AuthenticationException, AuthorizationException, DefinitionDoesNotExistException {
        Subject subject = getSubject(actor);
        return definitionLogic.getProcessDefinition(subject, definitionId);
    }

    @WebMethod
    public WfDefinition getProcessDefinitionStubByProcessId(
            @WebParam(mode = Mode.IN, name = "actorPrincipal", targetNamespace = "http://runa.ru/workflow/webservices") ActorPrincipal actor,
            @WebParam(mode = Mode.IN, name = "processId", targetNamespace = "http://runa.ru/workflow/webservices") Long processId)
            throws AuthenticationException, AuthorizationException, DefinitionDoesNotExistException, ProcessDoesNotExistException {
        Subject subject = getSubject(actor);
        return definitionLogic.getProcessDefinitionByProcessId(subject, processId);
    }

    private Subject getSubject(ActorPrincipal actor) {
        Subject result = new Subject();
        result.getPrincipals().add(actor);
        return result;
    }

    @WebMethod
    public List<WfDefinition> getLatestProcessDefinitionStubs(
            @WebParam(mode = Mode.IN, name = "actorPrincipal", targetNamespace = "http://runa.ru/workflow/webservices") ActorPrincipal actor)
            throws AuthenticationException, AuthorizationException, DefinitionDoesNotExistException {
        Subject subject = getSubject(actor);
        BatchPresentation batchPresentation = BatchPresentationFactory.DEFINITIONS.createDefault();
        batchPresentation.setRangeSize(BatchPresentationConsts.MAX_UNPAGED_REQUEST_SIZE);
        return definitionLogic.getLatestProcessDefinitions(subject, batchPresentation);
    }

}
