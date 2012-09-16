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

import ru.runa.InternalApplicationException;
import ru.runa.af.ActorPrincipal;
import ru.runa.af.AuthenticationException;
import ru.runa.af.AuthorizationException;
import ru.runa.af.presentation.BatchPresentation;
import ru.runa.af.presentation.BatchPresentationConsts;
import ru.runa.af.service.impl.ejb.LoggerInterceptor;
import ru.runa.wf.ProcessDefinitionAlreadyExistsException;
import ru.runa.wf.ProcessDefinitionArchiveException;
import ru.runa.wf.ProcessDefinitionDoesNotExistException;
import ru.runa.wf.ProcessDefinition;
import ru.runa.wf.ProcessInstanceDoesNotExistException;
import ru.runa.wf.SuperProcessInstanceExistsException;
import ru.runa.wf.logic.JbpmDefinitionLogic;
import ru.runa.wf.presentation.WFProfileStrategy;

import com.google.common.collect.Lists;
import com.google.common.io.Files;

@Stateless
@WebService(name = "Definition", targetNamespace = "http://runa.ru/workflow/webservices", serviceName = "DefinitionWebService")
@SOAPBinding(parameterStyle = SOAPBinding.ParameterStyle.WRAPPED)
@Interceptors({SpringBeanAutowiringInterceptor.class, LoggerInterceptor.class})
public class DefinitionBean {
    @Autowired
    private JbpmDefinitionLogic definitionLogic;

    @WebMethod
    public void deployProcessDefinitionLocal(
            @WebParam(mode = Mode.IN, name = "actorPrincipal", targetNamespace = "http://runa.ru/workflow/webservices") ActorPrincipal actor,
            @WebParam(mode = Mode.IN, name = "file", targetNamespace = "http://runa.ru/workflow/webservices") String file,
            @WebParam(mode = Mode.IN, name = "type", targetNamespace = "http://runa.ru/workflow/webservices") String type)
            throws AuthenticationException, AuthorizationException, ProcessDefinitionAlreadyExistsException,
            ProcessDefinitionArchiveException, ParserConfigurationException {
        try {
            byte[] scriptBytes = Files.toByteArray(new File(file));
            deployProcessDefinition(actor, scriptBytes, type);
        } catch (IOException e) {
            throw new InternalApplicationException(e);
        }
    }

    @WebMethod
    public void deployProcessDefinition(
            @WebParam(mode = Mode.IN, name = "actorPrincipal", targetNamespace = "http://runa.ru/workflow/webservices") ActorPrincipal actor,
            @WebParam(mode = Mode.IN, name = "parFiles", targetNamespace = "http://runa.ru/workflow/webservices") byte[] parBytes,
            @WebParam(mode = Mode.IN, name = "type", targetNamespace = "http://runa.ru/workflow/webservices") String type)
            throws AuthenticationException, AuthorizationException, ProcessDefinitionAlreadyExistsException,
            ProcessDefinitionArchiveException, ParserConfigurationException {
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
            throws AuthenticationException, AuthorizationException, ProcessDefinitionAlreadyExistsException,
            ProcessDefinitionArchiveException, ParserConfigurationException {
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
            throws AuthenticationException, AuthorizationException, ProcessDefinitionAlreadyExistsException,
            ProcessDefinitionArchiveException, ParserConfigurationException {
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
            throws AuthenticationException, AuthorizationException, ProcessDefinitionDoesNotExistException,
            SuperProcessInstanceExistsException {
        Subject subject = getSubject(actor);
        definitionLogic.undeployProcessDefinition(subject, name);
    }

    @WebMethod
    public ProcessDefinition getLatestProcessDefinitionStub(
            @WebParam(mode = Mode.IN, name = "actorPrincipal", targetNamespace = "http://runa.ru/workflow/webservices") ActorPrincipal actor,
            @WebParam(mode = Mode.IN, name = "name", targetNamespace = "http://runa.ru/workflow/webservices") String name)
            throws AuthenticationException, AuthorizationException, ProcessDefinitionDoesNotExistException {
        Subject subject = getSubject(actor);
        return definitionLogic.getLatestProcessDefinitionStub(subject, name);
    }

    @WebMethod
    public ProcessDefinition getProcessDefinitionStub(
            @WebParam(mode = Mode.IN, name = "actorPrincipal", targetNamespace = "http://runa.ru/workflow/webservices") ActorPrincipal actor,
            @WebParam(mode = Mode.IN, name = "definitionId", targetNamespace = "http://runa.ru/workflow/webservices") Long definitionId)
            throws AuthenticationException, AuthorizationException, ProcessDefinitionDoesNotExistException {
        Subject subject = getSubject(actor);
        return definitionLogic.getProcessDefinitionStub(subject, definitionId);
    }

    @WebMethod
    public ProcessDefinition getProcessDefinitionStubByProcessId(
            @WebParam(mode = Mode.IN, name = "actorPrincipal", targetNamespace = "http://runa.ru/workflow/webservices") ActorPrincipal actor,
            @WebParam(mode = Mode.IN, name = "processInstanceId", targetNamespace = "http://runa.ru/workflow/webservices") Long processInstanceId)
            throws AuthenticationException, AuthorizationException, ProcessDefinitionDoesNotExistException,
            ProcessInstanceDoesNotExistException {
        Subject subject = getSubject(actor);
        return definitionLogic.getProcessDefinitionStubByProcessId(subject, processInstanceId);
    }

    private Subject getSubject(ActorPrincipal actor) {
        Subject result = new Subject();
        result.getPrincipals().add(actor);
        return result;
    }

    @WebMethod
    public List<ProcessDefinition> getLatestProcessDefinitionStubs(
            @WebParam(mode = Mode.IN, name = "actorPrincipal", targetNamespace = "http://runa.ru/workflow/webservices") ActorPrincipal actor)
            throws AuthenticationException, AuthorizationException, ProcessDefinitionDoesNotExistException {
        Subject subject = getSubject(actor);
        BatchPresentation batchPresentation = WFProfileStrategy.PROCESS_DEFINITION_DEFAULT_BATCH_PRESENTATION_FACTORY.getDefaultBatchPresentation();
        batchPresentation.setRangeSize(BatchPresentationConsts.MAX_UNPAGED_REQUEST_SIZE);
        return definitionLogic.getLatestProcessDefinitionStubs(subject, batchPresentation);
    }

}
