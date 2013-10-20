package ru.cg.runaex.components.bean.component.part;

import java.io.Serializable;

import ru.cg.runaex.components.validation.annotation.DatabaseStructureElement;

/**
 * @author Kochetkov
 */
public class Column implements Serializable {

  private static final long serialVersionUID = -6587303634169064068L;

  @DatabaseStructureElement
  private String name;

  public Column(String name) {
    this.name = name;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }
}
