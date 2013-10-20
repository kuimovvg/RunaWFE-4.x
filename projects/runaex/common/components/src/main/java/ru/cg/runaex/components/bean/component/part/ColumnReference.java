package ru.cg.runaex.components.bean.component.part;

import javax.validation.constraints.NotNull;

import ru.cg.runaex.components.validation.ColumnReferenceChecks;
import ru.cg.runaex.components.validation.NotNullChecks;
import ru.cg.runaex.components.validation.annotation.DatabaseStructureElement;

/**
 * @author urmancheev
 */
@ru.cg.runaex.components.validation.annotation.ColumnReference(groups = ColumnReferenceChecks.class)
public class ColumnReference extends Reference implements Cloneable {
  private static final long serialVersionUID = -6402642189099694769L;

  @NotNull(groups = NotNullChecks.class)
  @DatabaseStructureElement
  private String column;

  public ColumnReference(String schema, String table, String column, int termCount) {
    super(schema, table, termCount);
    this.column = column;
  }

  public String getColumn() {
    return column;
  }

  public void setColumn(String column){
    this.column = column;
  }

  public final ColumnReference clone() {
    ColumnReference newReference = new ColumnReference(schema, table, column, termCount);
    return newReference;
  }

  @Override
  public String toString() {
    return super.toString().concat(".").concat(column);
  }
}
