package ru.cg.runaex.esb.service;

import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import javax.jws.WebMethod;
import javax.jws.WebParam;
import javax.jws.WebResult;
import javax.jws.WebService;
import javax.xml.datatype.XMLGregorianCalendar;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.runa.workflow.webservices.*;

import ru.cg.runaex.esb.bean.*;
import ru.cg.runaex.esb.util.VariableHelper;

/**
 * @author Петров А.
 */
@WebService(name = "StartProcessInstanceService", targetNamespace = "http://runaex")
public class StartProcessInstanceService {

  private final static String ESB_RUNAEX_ENDPOINT = "ESB";

  private Logger logger = LoggerFactory.getLogger(getClass());

  private RunaexEndpoint esbRunaEndpoint;
  private Map<String, StartProcessInstanceEndpoint> startProcessInstanceEndpoints;

  public StartProcessInstanceService() throws MalformedURLException, AuthenticationException_Exception {
    esbRunaEndpoint = new RunaexEndpoint(ESB_RUNAEX_ENDPOINT);
    startProcessInstanceEndpoints = new ConcurrentHashMap<String, StartProcessInstanceEndpoint>();
  }

  @WebMethod(operationName = "startProcessInstance", action = "http://runaex/startProcessInstance")
  @WebResult(name = "startProcessInstanceResult", targetNamespace = "http://runaex")
  public StartProcessInstanceResult startProcessInstance(@WebParam(name = "startProcessInstanceRequest", targetNamespace = "http://runaex") StartProcessInstanceRequest startProcessInstanceRequest) {
    String endpointName = startProcessInstanceRequest.getEndpointName();
    if (endpointName == null || endpointName.isEmpty()) {
      return startLocalProcess(startProcessInstanceRequest);
    }
    else {
      try {
        StartProcessInstanceEndpoint startProcessInstanceEndpoint = startProcessInstanceEndpoints.get(startProcessInstanceRequest.getEndpointName());
        if (startProcessInstanceEndpoint == null) {
          startProcessInstanceEndpoint = new StartProcessInstanceEndpoint(startProcessInstanceRequest.getEndpointName());
          startProcessInstanceEndpoints.put(startProcessInstanceRequest.getEndpointName(), startProcessInstanceEndpoint);
        }
        return startProcessInstanceEndpoint.startProcessInstance(startProcessInstanceRequest);
      }
      catch (MalformedURLException ex) {
        logger.error(ex.toString(), ex);
      }
    }

    return null;
  }

  private List<VariableDescr> convertToVariableDescrs(List<Variable> variables) {
    List<VariableDescr> convertedVariables = new ArrayList<VariableDescr>(variables.size());

    for (Variable variable : variables) {
      VariableDescr variableDescr = new VariableDescr();
      variableDescr.setName(variable.getName());

      Object oValue = VariableHelper.getValue(variable);
      if (oValue instanceof String) {
        variableDescr.setStrValue((String) oValue);
      }
      else if (oValue instanceof Boolean) {
        variableDescr.setBooleanValue((Boolean) oValue);
      }
      else if (oValue instanceof Long) {
        variableDescr.setLValue((Long) oValue);
      }
      else if (oValue instanceof Double) {
        variableDescr.setDValue((Double) oValue);
      }
      else if (oValue instanceof XMLGregorianCalendar) {
        variableDescr.setDateValue((XMLGregorianCalendar) oValue);
      }
      else if (oValue instanceof byte[]) {
        variableDescr.setByteValue((byte[]) oValue);
      }

      convertedVariables.add(variableDescr);
    }

    return convertedVariables;
  }

  private ArrayList<Variable> convertFromVariableDescrs(List<VariableDescr> variables) {
    ArrayList<Variable> convertedVariables = new ArrayList<Variable>(variables.size());

    Variable variable;
    for (VariableDescr variableDescr : variables) {
      variable = new Variable();
      variable.setName(variableDescr.getName());
      if (variableDescr.getStrValue() != null) {
        variable.setStringValue(variableDescr.getStrValue());
      }
      else if (variableDescr.getByteValue() != null) {
        variable.setByteaValue(variableDescr.getByteValue());
      }
      else if (variableDescr.getDateValue() != null) {
        variable.setDateValue(variableDescr.getDateValue());
      }
      else if (variableDescr.getDValue() != null) {
        variable.setDoubleValue(variableDescr.getDValue());
      }
      else if (variableDescr.getLValue() != null) {
        variable.setLongValue(variableDescr.getLValue());
      }
      else if (variableDescr.isBooleanValue() != null) {
        variable.setBooleanValue(variableDescr.isBooleanValue());
      }

      convertedVariables.add(variable);
    }

    return convertedVariables;
  }

  private StartProcessInstanceResult startLocalProcess(StartProcessInstanceRequest request) {
    try {
      List<VariableDescr> variables = convertToVariableDescrs(request.getVariables());
      Long startedProcessInstanceId = esbRunaEndpoint.startProcessInstance(request.getProcessName(), variables);

      ProcessInstanceStub processInstanceStub;
      while (true) {
        processInstanceStub = esbRunaEndpoint.getProcessInstanceStub(startedProcessInstanceId);
        if (processInstanceStub.getEndDate() != null) {
          break;
        }
        Thread.sleep(5000);
      }

      List<VariableDescr> processedVariables = esbRunaEndpoint.getProcessInstanceVariables(startedProcessInstanceId);
      return new StartProcessInstanceResult(convertFromVariableDescrs(processedVariables));
    }
    catch (ProcessDefinitionDoesNotExistException_Exception ex) {
      logger.error(ex.toString(), ex);
    }
    catch (AuthorizationException_Exception ex) {
      logger.error(ex.toString(), ex);
    }
    catch (AuthenticationException_Exception ex) {
      logger.error(ex.toString(), ex);
    }
    catch (TaskDoesNotExistException_Exception ex) {
      logger.error(ex.toString(), ex);
    }
    catch (ProcessInstanceDoesNotExistException_Exception ex) {
      logger.error(ex.toString(), ex);
    }
    catch (ValidationException_Exception ex) {
      logger.error(ex.toString(), ex);
    }
    catch (InterruptedException ex) {
      logger.error(ex.toString(), ex);
    }
    return null;
  }
}
