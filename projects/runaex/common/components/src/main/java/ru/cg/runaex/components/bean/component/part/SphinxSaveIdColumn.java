package ru.cg.runaex.components.bean.component.part;

import java.io.Serializable;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;

import ru.cg.runaex.components.validation.annotation.DatabaseStructureElement;

/**
 * @author Абдулин Ильдар
 */
public class SphinxSaveIdColumn implements Serializable {
  private static final long serialVersionUID = 5513073808096950350L;

  @NotNull
  private TableReference reference;

  @DatabaseStructureElement
  @NotNull
  private String columnName;

  public SphinxSaveIdColumn(TableReference reference, String columnName) {
    this.reference = reference;
    this.columnName = columnName;
  }

  @Valid
  public TableReference getReference() {
    return reference;
  }

  public String getColumnName() {
    return columnName;
  }
}
