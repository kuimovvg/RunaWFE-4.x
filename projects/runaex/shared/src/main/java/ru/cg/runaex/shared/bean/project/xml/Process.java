package ru.cg.runaex.shared.bean.project.xml;

import java.io.Serializable;

import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.annotations.XStreamAsAttribute;

/**
 * @author Петров А.
 */
@XStreamAlias("process")
public class Process implements Serializable {

  private static final long serialVersionUID = 1L;

  @XStreamAsAttribute
  @XStreamAlias("name")
  private String processName;

  public String getProcessName() {
    return processName;
  }

  public void setProcessName(String processName) {
    this.processName = processName;
  }
}
