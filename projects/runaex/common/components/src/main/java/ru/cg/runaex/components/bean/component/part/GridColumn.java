package ru.cg.runaex.components.bean.component.part;

import java.io.Serializable;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;

import ru.cg.runaex.components.validation.ColumnReferenceChecks;
import ru.cg.runaex.components.validation.NotNullChecks;
import ru.cg.runaex.components.validation.annotation.DatabaseStructureElement;
import ru.cg.runaex.components.validation.annotation.GridColumnValidation;

/**
 * @author urmancheev
 */

@GridColumnValidation(groups = ColumnReferenceChecks.class)
public class GridColumn implements Serializable {
  private static final long serialVersionUID = 566330187830185031L;

  @NotNull(groups = NotNullChecks.class)
  protected String displayName;
  @NotNull(groups = NotNullChecks.class)
  @DatabaseStructureElement
  protected String databaseColumn;
  @Valid
  protected ColumnReference columnReference;

  protected String width;

  protected int termCount;

  public GridColumn(String displayName, String databaseColumn, ColumnReference columnReference, int termCount, String width) {
    this.displayName = displayName;
    this.databaseColumn = databaseColumn;
    this.columnReference = columnReference;
    this.termCount = termCount;
    this.width = width;
  }

  public String getDisplayName() {
    return displayName;
  }

  public void setDisplayName(String displayName) {
    this.displayName = displayName;
  }

  public String getDatabaseColumn() {
    return databaseColumn;
  }

  public void setDatabaseColumn(String databaseColumn) {
    this.databaseColumn = databaseColumn;
  }

  public ColumnReference getColumnReference() {
    return columnReference;
  }

  public void setColumnReference(ColumnReference columnReference) {
    this.columnReference = columnReference;
  }

  public int getTermCount() {
    return termCount;
  }

  public String getWidth() {
    return width;
  }

  public void setWidth(String width) {
    this.width = width;
  }

  @Override
  public String toString() {
    final StringBuilder sb = new StringBuilder();

    if (displayName != null) {
      sb.append(displayName);
    }
    sb.append(", ");

    if (databaseColumn != null) {
      sb.append(databaseColumn);
    }
    sb.append(", ");

    if (columnReference != null) {
      sb.append(columnReference.toString());
    }
    sb.append(", ");

    if (width != null) {
      sb.append(width);
    }

    return sb.toString();
  }
}
