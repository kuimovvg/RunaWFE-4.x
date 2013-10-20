package ru.cg.runaex.shared.bean.project.xml;

import java.io.Serializable;
import java.lang.*;
import java.util.LinkedList;
import java.util.List;

import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.annotations.XStreamAsAttribute;

/**
 * @author Петров А.
 */
@XStreamAlias("project")
public class Project implements Serializable {

  private static final long serialVersionUID = 1L;

  @XStreamAsAttribute
  @XStreamAlias("name")
  private String projectName;

  private List<Category> categories = new LinkedList<Category>();

  private List<Process> processes = new LinkedList<Process>();

  public String getProjectName() {
    return projectName;
  }

  public void setProjectName(String projectName) {
    this.projectName = projectName;
  }

  public List<Category> getCategories() {
    return categories;
  }

  public List<Process> getProcesses() {
    return processes;
  }
}
