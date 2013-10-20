package ru.cg.runaex.wsdl_analyzer.bean;

import java.util.HashMap;
import java.util.Map;

/**
 * Service Info an in memory representation of a service defined in WSDL
 *
 * @author urmancheev
 */
public class ServiceInfo {
  private String name = "";
  private Map<String, OperationInfo> operationsByName = new HashMap<String, OperationInfo>();

  public ServiceInfo() {
  }

  public void setName(String value) {
    name = value;
  }

  public String getName() {
    return name;
  }

  public Map<String, OperationInfo> getOperationsByName() {
    return operationsByName;
  }

  public void setOperationsByName(Map<String, OperationInfo> operationsByName) {
    this.operationsByName = operationsByName;
  }

  public String toString() {
    return getName();
  }
}
