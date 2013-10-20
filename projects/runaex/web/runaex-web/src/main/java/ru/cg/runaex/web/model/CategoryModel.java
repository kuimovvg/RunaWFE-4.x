package ru.cg.runaex.web.model;

import java.io.Serializable;
import java.util.LinkedList;
import java.util.List;

/**
 * @author Петров А.
 */
public class CategoryModel implements Serializable {

  private static final long serialVersionUID = 1L;

  private Long id;
  private String name;
  private List<CategoryModel> categories = new LinkedList<CategoryModel>();
  private List<ProcessModel> processes = new LinkedList<ProcessModel>();
  private int taskCount;

  public Long getId() {
    return id;
  }

  public void setId(Long id) {
    this.id = id;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public void setCategories (List<CategoryModel> categories) {
    this.categories = categories;
  }

  public List<CategoryModel> getCategories() {
    return categories;
  }

  public List<ProcessModel> getProcesses() {
    return processes;
  }

  public int getTaskCount() {
    return taskCount;
  }

  public void setTaskCount(int taskCount) {
    this.taskCount = taskCount;
  }
}
