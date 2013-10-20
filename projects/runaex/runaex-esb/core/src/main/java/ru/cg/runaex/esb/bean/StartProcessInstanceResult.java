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
@XmlType(name = "startProcessInstanceResult", namespace = "http://runaex", propOrder = {
    "result"
})
public class StartProcessInstanceResult {

  @XmlElement(name = "variable", namespace = "http://runaex")
  private List<Variable> result;

  public StartProcessInstanceResult() {
  }

  public StartProcessInstanceResult(List<Variable> result) {
    this.result = result;
  }

  public List<Variable> getResult() {
    return result;
  }

  public void setResult(List<Variable> result) {
    this.result = result;
  }
}
