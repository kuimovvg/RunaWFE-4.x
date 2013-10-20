package ru.cg.runaex.web.model;

import java.io.Serializable;
import java.util.LinkedList;
import java.util.List;

/**
 * @author Петров А.
 */
public class ProcessModel implements Serializable {

  private static final long serialVersionUID = 1L;

  private String name;
  private List<Task> tasks = new LinkedList<Task>();
  private int taskCount;

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public List<Task> getTasks() {
    return tasks;
  }

  public int getTaskCount() {
    return taskCount;
  }

  public void setTaskCount(int taskCount) {
    this.taskCount = taskCount;
  }
}
