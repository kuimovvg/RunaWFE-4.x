package ru.cg.runaex.runa_ext.handler.webservice_call_handler;

import java.util.List;

/**
 * @author urmancheev
 */
public class WebserviceCallConfiguration {
  private String esbRelativeUrl;
  private String service;
  private String operation;

  private String request;
  private String response;

  private List<DbParameter> requestDbParameters;
  private List<VariableParameter> requestVariableParameters;
  private List<DbParameter> responseDbParameters;
  private List<VariableParameter> responseVariableParameters;

  public String getEsbRelativeUrl() {
    return esbRelativeUrl;
  }

  public void setEsbRelativeUrl(String esbRelativeUrl) {
    this.esbRelativeUrl = esbRelativeUrl;
  }

  public String getService() {
    return service;
  }

  public void setService(String service) {
    this.service = service;
  }

  public String getOperation() {
    return operation;
  }

  public void setOperation(String operation) {
    this.operation = operation;
  }

  public String getRequest() {
    return request;
  }

  public void setRequest(String request) {
    this.request = request;
  }

  public String getResponse() {
    return response;
  }

  public void setResponse(String response) {
    this.response = response;
  }

  public List<DbParameter> getRequestDbParameters() {
    return requestDbParameters;
  }

  public void setRequestDbParameters(List<DbParameter> requestDbParameters) {
    this.requestDbParameters = requestDbParameters;
  }

  public List<VariableParameter> getRequestVariableParameters() {
    return requestVariableParameters;
  }

  public void setRequestVariableParameters(List<VariableParameter> requestVariableParameters) {
    this.requestVariableParameters = requestVariableParameters;
  }

  public List<DbParameter> getResponseDbParameters() {
    return responseDbParameters;
  }

  public void setResponseDbParameters(List<DbParameter> responseDbParameters) {
    this.responseDbParameters = responseDbParameters;
  }

  public List<VariableParameter> getResponseVariableParameters() {
    return responseVariableParameters;
  }

  public void setResponseVariableParameters(List<VariableParameter> responseVariableParameters) {
    this.responseVariableParameters = responseVariableParameters;
  }
}
