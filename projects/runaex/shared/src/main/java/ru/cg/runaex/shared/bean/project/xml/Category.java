package ru.cg.runaex.shared.bean.project.xml;

import java.io.Serializable;
import java.util.LinkedList;
import java.util.List;

import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.annotations.XStreamAsAttribute;

/**
 * @author Петров А.
 */
@XStreamAlias("category")
public class Category implements Serializable {

  private static final long serialVersionUID = 1L;

  @XStreamAsAttribute
  @XStreamAlias("name")
  private String categoryName;

  private List<Category> categories = new LinkedList<Category>();

  private List<Process> processes = new LinkedList<Process>();

  public String getCategoryName() {
    return categoryName;
  }

  public void setCategoryName(String categoryName) {
    this.categoryName = categoryName;
  }

  public List<Category> getCategories() {
    return categories;
  }

  public List<Process> getProcesses() {
    return processes;
  }
}
