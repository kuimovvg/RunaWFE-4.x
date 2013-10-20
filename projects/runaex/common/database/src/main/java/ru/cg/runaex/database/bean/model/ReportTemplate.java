package ru.cg.runaex.database.bean.model;

import java.io.Serializable;

/**
 * @author Kochetkov
 */
public class ReportTemplate implements Serializable {
  private static final long serialVersionUID = 3837539796944976522L;

  private Long id;
  private String templateName;

  public ReportTemplate(Long id, String templateName) {
    this.id = id;
    this.templateName = templateName;
  }

  public Long getId() {
    return id;
  }

  public void setId(Long id) {
    this.id = id;
  }

  public String getTemplateName() {
    return templateName;
  }

  public void setTemplateName(String templateName) {
    this.templateName = templateName;
  }
}
