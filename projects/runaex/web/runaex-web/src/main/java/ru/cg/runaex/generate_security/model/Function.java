package ru.cg.runaex.generate_security.model;

import java.util.List;

import com.cg.security_manager.core.bean.RequestType;

/**
 * @author urmancheev
 */
public class Function {
  private Long id;
  private Long moduleId;
  private String name;
  private String url;
  private Boolean ignoreUrlEnding;
  private RequestType requestType;
  private List<Parameter> parameters;

  public Function() {
  }

  public Function(Long id, Long moduleId, String name, String url) {
    this.id = id;
    this.moduleId = moduleId;
    this.name = name;
    this.url = url;
  }

  public Long getId() {
    return id;
  }

  public void setId(Long id) {
    this.id = id;
  }

  public Long getModuleId() {
    return moduleId;
  }

  public void setModuleId(Long moduleId) {
    this.moduleId = moduleId;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public String getUrl() {
    return url;
  }

  public void setUrl(String url) {
    this.url = url;
  }

  public Boolean getIgnoreUrlEnding() {
    return ignoreUrlEnding;
  }

  public void setIgnoreUrlEnding(Boolean ignoreUrlEnding) {
    this.ignoreUrlEnding = ignoreUrlEnding;
  }

  public RequestType getRequestType() {
    return requestType;
  }

  public void setRequestType(RequestType requestType) {
    this.requestType = requestType;
  }

  public List<Parameter> getParameters() {
    return parameters;
  }

  public void setParameters(List<Parameter> parameters) {
    this.parameters = parameters;
  }

  public static class Parameter {
    private String name;
    private String value;

    public Parameter() {
    }

    public Parameter(String name, String value) {
      this.name = name;
      this.value = value;
    }

    public String getName() {
      return name;
    }

    public void setName(String name) {
      this.name = name;
    }

    public String getValue() {
      return value;
    }

    public void setValue(String value) {
      this.value = value;
    }
  }
}
