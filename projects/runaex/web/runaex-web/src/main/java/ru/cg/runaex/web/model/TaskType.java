package ru.cg.runaex.web.model;

import java.util.List;

/**
 * @author Bagautdinov
 */
public class TaskType {

  private Long id;
  private String name;
  private int count;
  private List<TaskGroup> taskGroupList;

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

  public List<TaskGroup> getTaskGroupList() {
    return taskGroupList;
  }

  public void setTaskGroupList(List<TaskGroup> taskGroupList) {
    this.taskGroupList = taskGroupList;
  }
}
