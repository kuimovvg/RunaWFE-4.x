package ru.cg.runaex.esb.bean;

import ru.runa.workflow.webservices.*;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;

/**
 * @author Петров А.
 */
public class RunaexEndpoint {

  private ActorPrincipal actorPrincipal;
  private Execution port;

  public RunaexEndpoint(String endpointName) throws MalformedURLException, AuthenticationException_Exception {
    authenticate(endpointName);

    ExecutionWebService executionWS = new ExecutionWebService(new URL("http://localhost:8180/runaex-esb/http/RunaexServices/".concat(endpointName).concat("-Execution?wsdl")));
    port = executionWS.getExecutionPort();
  }

  private void authenticate(String endpointName) throws AuthenticationException_Exception, MalformedURLException {
    AuthenticationWebService authenticationWebService = new AuthenticationWebService(new URL("http://localhost:8180/runaex-esb/http/RunaexServices/".concat(endpointName).concat("-Authentication?wsdl")));
    actorPrincipal = authenticationWebService.getAuthenticationPort().authenticateDB("Administrator", "wf");
  }

  public Long startProcessInstance(String processName, List<VariableDescr> variables) throws AuthenticationException_Exception, AuthorizationException_Exception, ValidationException_Exception, ProcessDefinitionDoesNotExistException_Exception {
    return port.startProcessInstance(actorPrincipal, processName, variables);
  }

  public ProcessInstanceStub getProcessInstanceStub(Long processInstanceId) throws AuthenticationException_Exception, ProcessInstanceDoesNotExistException_Exception, AuthorizationException_Exception {
    return port.getProcessInstanceStub(actorPrincipal, processInstanceId);
  }

  public List<VariableDescr> getProcessInstanceVariables(Long processInstanceId) throws AuthenticationException_Exception, AuthorizationException_Exception, ProcessInstanceDoesNotExistException_Exception, TaskDoesNotExistException_Exception {
    return port.getProcessInstanceVariables(actorPrincipal, processInstanceId);
  }
}
