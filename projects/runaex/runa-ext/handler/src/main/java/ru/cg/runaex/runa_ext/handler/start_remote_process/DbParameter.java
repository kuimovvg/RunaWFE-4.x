package ru.cg.runaex.runa_ext.handler.start_remote_process;

import java.io.Serializable;

import ru.cg.runaex.components.bean.component.part.ColumnReference;

/**
 * @author urmancheev
 */
public class DbParameter implements Serializable {
  private static final long serialVersionUID = -7287769261460408009L;

  private String name;
  private ColumnReference reference;

  public DbParameter(String name, ColumnReference reference) {
    this.name = name;
    this.reference = reference;
  }

  public String getName() {
    return name;
  }

  public ColumnReference getReference() {
    return reference;
  }
}
