package ru.runa.af.webservice;

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

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.ejb.interceptor.SpringBeanAutowiringInterceptor;

import ru.runa.af.Actor;
import ru.runa.af.ActorPrincipal;
import ru.runa.af.AuthenticationException;
import ru.runa.af.AuthorizationException;
import ru.runa.af.Executor;
import ru.runa.af.ExecutorAlreadyExistsException;
import ru.runa.af.ExecutorAlreadyInGroupException;
import ru.runa.af.ExecutorNotInGroupException;
import ru.runa.af.ExecutorOutOfDateException;
import ru.runa.af.Group;
import ru.runa.af.WeakPasswordException;
import ru.runa.af.logic.ExecutorLogic;
import ru.runa.af.service.impl.ejb.LoggerInterceptor;

import com.google.common.collect.Lists;

@Stateless
@WebService(name = "Executor", targetNamespace = "http://runa.ru/workflow/webservices", serviceName = "ExecutorWebService")
@SOAPBinding(parameterStyle = SOAPBinding.ParameterStyle.WRAPPED)
@Interceptors({SpringBeanAutowiringInterceptor.class, LoggerInterceptor.class})
public class ExecutorBean {
    @Autowired
    private ExecutorLogic executorLogic;

    @WebMethod
    public void createActor(
            @WebParam(mode = Mode.IN, name = "actorPrincipal", targetNamespace = "http://runa.ru/workflow/webservices") ActorPrincipal actor,
            @WebParam(mode = Mode.IN, name = "name", targetNamespace = "http://runa.ru/workflow/webservices") String name,
            @WebParam(mode = Mode.IN, name = "fullName", targetNamespace = "http://runa.ru/workflow/webservices") String fullName,
            @WebParam(mode = Mode.IN, name = "description", targetNamespace = "http://runa.ru/workflow/webservices") String description,
            @WebParam(mode = Mode.IN, name = "passwd", targetNamespace = "http://runa.ru/workflow/webservices") String passwd)
            throws ExecutorAlreadyExistsException, AuthorizationException, AuthenticationException, ExecutorOutOfDateException, WeakPasswordException {
        Subject subject = getSubject(actor);
        Actor newActor = new Actor(name, description, fullName);
        executorLogic.create(subject, newActor);
        newActor = executorLogic.getActor(subject, name);
        executorLogic.setPassword(subject, newActor, passwd);
    }

    @WebMethod
    public void createGroup(
            @WebParam(mode = Mode.IN, name = "actorPrincipal", targetNamespace = "http://runa.ru/workflow/webservices") ActorPrincipal actor,
            @WebParam(mode = Mode.IN, name = "name", targetNamespace = "http://runa.ru/workflow/webservices") String name,
            @WebParam(mode = Mode.IN, name = "description", targetNamespace = "http://runa.ru/workflow/webservices") String description)
            throws ExecutorAlreadyExistsException, AuthorizationException, AuthenticationException {
        Subject subject = getSubject(actor);
        Group newGroup = new Group(name, description);
        executorLogic.create(subject, newGroup);
    }

    @WebMethod
    public void deleteExecutor(
            @WebParam(mode = Mode.IN, name = "actorPrincipal", targetNamespace = "http://runa.ru/workflow/webservices") ActorPrincipal actor,
            @WebParam(mode = Mode.IN, name = "name", targetNamespace = "http://runa.ru/workflow/webservices") String name)
            throws ExecutorOutOfDateException, AuthorizationException, AuthenticationException {
        Subject subject = getSubject(actor);
        executorLogic.remove(subject, Lists.newArrayList(executorLogic.getExecutor(subject, name).getId()));
    }

    @WebMethod
    public void deleteExecutors(
            @WebParam(mode = Mode.IN, name = "actorPrincipal", targetNamespace = "http://runa.ru/workflow/webservices") ActorPrincipal actor,
            @WebParam(mode = Mode.IN, name = "names", targetNamespace = "http://runa.ru/workflow/webservices") List<String> names)
            throws ExecutorOutOfDateException, AuthorizationException, AuthenticationException {
        Subject subject = getSubject(actor);
        ArrayList<Long> idsList = new ArrayList<Long>();
        for (String name : names) {
            idsList.add(executorLogic.getExecutor(subject, name).getId());
        }
        executorLogic.remove(subject, idsList);
    }

    @WebMethod
    public void addExecutorsToGroup(
            @WebParam(mode = Mode.IN, name = "actorPrincipal", targetNamespace = "http://runa.ru/workflow/webservices") ActorPrincipal actor,
            @WebParam(mode = Mode.IN, name = "executors", targetNamespace = "http://runa.ru/workflow/webservices") List<String> executors,
            @WebParam(mode = Mode.IN, name = "groupName", targetNamespace = "http://runa.ru/workflow/webservices") String groupName)
            throws ExecutorAlreadyInGroupException, ExecutorOutOfDateException, AuthorizationException, AuthenticationException {
        List<Executor> result = Lists.newArrayList();
        Subject subject = getSubject(actor);
        for (String executorName : executors) {
            Executor executor = executorLogic.getExecutor(subject, executorName);
            result.add(executor);
        }

        executorLogic.addExecutorsToGroup(subject, result, executorLogic.getGroup(subject, groupName));
    }

    @WebMethod
    public void removeExecutorsFromGroup(
            @WebParam(mode = Mode.IN, name = "actorPrincipal", targetNamespace = "http://runa.ru/workflow/webservices") ActorPrincipal actor,
            @WebParam(mode = Mode.IN, name = "executors", targetNamespace = "http://runa.ru/workflow/webservices") List<String> executors,
            @WebParam(mode = Mode.IN, name = "groupName", targetNamespace = "http://runa.ru/workflow/webservices") String groupName)
            throws ExecutorNotInGroupException, ExecutorOutOfDateException, AuthorizationException, AuthenticationException {
        List<Executor> result = Lists.newArrayList();
        Subject subject = getSubject(actor);
        for (String executorName : executors) {
            Executor executor = executorLogic.getExecutor(subject, executorName);
            result.add(executor);
        }

        executorLogic.removeExecutorsFromGroup(subject, result, executorLogic.getGroup(subject, groupName));
    }

    private Subject getSubject(ActorPrincipal actor) {
        Subject result = new Subject();
        result.getPrincipals().add(actor);
        return result;
    }
}
