package ru.cg.runaex.runa_ext.handler.start_remote_process;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

import ru.cg.runaex.components.bean.component.part.ColumnReference;

/**
 * @author urmancheev
 */
public class StartRemoteProcessConfiguration implements Serializable {
  private static final long serialVersionUID = 5382633650926402351L;

  private String endpointName;
  private String processName;
  private List<StartRemoteProcessParameter> requestParameters;
  private List<StartRemoteProcessParameter> responseParameters;

  private List<DbParameter> requestDbParameters;
  private Map<String, String> requestVariableParameters;
  private Map<String, ColumnReference> responseDbParameters;
  private Map<String, String> responseVariableParameters;

  public String getEndpointName() {
    return endpointName;
  }

  public void setEndpointName(String endpointName) {
    this.endpointName = endpointName;
  }

  public String getProcessName() {
    return processName;
  }

  public void setProcessName(String processName) {
    this.processName = processName;
  }

  public List<StartRemoteProcessParameter> getRequestParameters() {
    return requestParameters;
  }

  public void setRequestParameters(List<StartRemoteProcessParameter> requestParameters) {
    this.requestParameters = requestParameters;
  }

  public List<StartRemoteProcessParameter> getResponseParameters() {
    return responseParameters;
  }

  public void setResponseParameters(List<StartRemoteProcessParameter> responseParameters) {
    this.responseParameters = responseParameters;
  }

  public List<DbParameter> getRequestDbParameters() {
    return requestDbParameters;
  }

  public void setRequestDbParameters(List<DbParameter> requestDbParameters) {
    this.requestDbParameters = requestDbParameters;
  }

  public Map<String, String> getRequestVariableParameters() {
    return requestVariableParameters;
  }

  public void setRequestVariableParameters(Map<String, String> requestVariableParameters) {
    this.requestVariableParameters = requestVariableParameters;
  }

  public Map<String, ColumnReference> getResponseDbParameters() {
    return responseDbParameters;
  }

  public void setResponseDbParameters(Map<String, ColumnReference> responseDbParameters) {
    this.responseDbParameters = responseDbParameters;
  }

  public Map<String, String> getResponseVariableParameters() {
    return responseVariableParameters;
  }

  public void setResponseVariableParameters(Map<String, String> responseVariableParameters) {
    this.responseVariableParameters = responseVariableParameters;
  }
}
