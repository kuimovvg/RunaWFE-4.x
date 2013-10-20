package ru.cg.runaex.web.model;

import java.util.List;

/**
 * @author Bagautdinov
 */
public class TaskGroup {
  private Long id;
  private String name;
  private int count;
  private List<Task> taskList;

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

  public int getCount() {
    return count;
  }

  public void setCount(int count) {
    this.count = count;
  }

  public List<Task> getTaskList() {
    return taskList;
  }

  public void setTaskList(List<Task> taskList) {
    this.taskList = taskList;
  }
}
