package ru.cg.runaex.shared.bean.project.xml;

import java.io.Serializable;

import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.annotations.XStreamAsAttribute;

/**
 * @author Kochetkov
 */
@XStreamAlias("groovyFunction")
public class GroovyFunction implements Serializable {

  private static final long serialVersionUID = 1L;

  @XStreamAsAttribute
  @XStreamAlias("code")
  private String code;

  @XStreamAsAttribute
  @XStreamAlias("description")
  private String description;

  public GroovyFunction(String code, String description) {
    this.code = code;
    this.description = description;
  }

  public String getCode() {
    return code;
  }

  public void setCode(String code) {
    this.code = code;
  }

  public String getDescription() {
    return description;
  }

  public void setDescription(String description) {
    this.description = description;
  }
}
