package ru.cg.runaex.database.bean.model;

import java.io.Serializable;

/**
 * @author Петров А.
 */
public class Category implements Serializable {

  private static final long serialVersionUID = 1L;

  private Long id;
  private Long parentId;
  private String name;

  public Long getId() {
    return id;
  }

  public void setId(Long id) {
    this.id = id;
  }

  public Long getParentId() {
    return parentId;
  }

  public void setParentId(Long parentId) {
    this.parentId = parentId;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }
}
