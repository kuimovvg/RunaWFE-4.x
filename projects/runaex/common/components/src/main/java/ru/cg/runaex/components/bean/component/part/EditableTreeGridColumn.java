package ru.cg.runaex.components.bean.component.part;

import java.io.Serializable;

import ru.cg.runaex.components.validation.ColumnReferenceChecks;
import ru.cg.runaex.components.validation.annotation.EditableTreeGridColumnValidation;
import ru.cg.runaex.components.validation.annotation.GridColumnValidation;

/**
 * @author Kochetkov
 */

@EditableTreeGridColumnValidation(groups = ColumnReferenceChecks.class)
public class EditableTreeGridColumn extends GridColumn implements Serializable {
  private static final long serialVersionUID = -9120269542366226314L;

  private Integer columnFormat;

  public EditableTreeGridColumn(String displayName, String databaseColumn, ColumnReference columnReference, int termCount, Integer columnFormat, String width) {
    super(displayName, databaseColumn, columnReference, termCount, width);
    this.columnFormat = columnFormat;
  }

  public Integer getColumnFormat() {
    return columnFormat;
  }

  public void setColumnFormat(Integer columnFormat) {
    this.columnFormat = columnFormat;
  }

  @Override
  public String toString() {
    final StringBuilder sb = new StringBuilder();
    sb.append(super.toString());

    sb.append(", ");
    if (columnFormat != null) {
      sb.append(columnFormat.toString());
    }
    return sb.toString();
  }
}
