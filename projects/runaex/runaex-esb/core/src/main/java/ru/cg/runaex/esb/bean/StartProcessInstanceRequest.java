package ru.cg.runaex.esb.bean;

import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;

/**
 * @author Петров А.
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "startProcessInstanceRequest", namespace = "http://runaex", propOrder = {
    "endpointName",
    "processName",
    "variables"
})
public class StartProcessInstanceRequest {

  @XmlElement(name = "endpointName", namespace = "http://runaex")
  private String endpointName;

  @XmlElement(name = "processName", required = true, nillable = false, namespace = "http://runaex")
  private String processName;

  @XmlElement(name = "variable", namespace = "http://runaex")
  private List<Variable> variables;

  public StartProcessInstanceRequest() {
  }

  public StartProcessInstanceRequest(String endpointName, String processName, List<Variable> variables) {
    this.endpointName = endpointName;
    this.processName = processName;
    this.variables = variables;
  }

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

  public List<Variable> getVariables() {
    return variables;
  }

  public void setVariables(List<Variable> variables) {
    this.variables = variables;
  }

  public String toString() {
    return variables != null ? processName + variables.toString() : processName;
  }

}
