package ru.cg.runaex.components.util;

import java.util.List;

import ru.cg.runaex.components.bean.component.part.EditableTreeGridColumn;
import ru.cg.runaex.components.bean.component.part.GridColumn;

/**
 * @author urmancheev
 */
public final class ComponentSerializer {

  public static String serializeGridColumns(List<GridColumn> columns) {
    StringBuilder builder = new StringBuilder();

    boolean first = true;
    for (GridColumn column : columns) {
      if (!first)
        builder.append("; ");
      else
        first = false;

      if (column.getDisplayName() != null) {
        builder.append(column.getDisplayName());
      }
      builder.append(", ");

      if (column.getDatabaseColumn() != null) {
        builder.append(column.getDatabaseColumn());
      }
      builder.append(", ");

      if (column.getColumnReference() != null) {
        builder.append(column.getColumnReference().toString());
      }
      builder.append(", ");

      if (column.getWidth() != null) {
        builder.append(column.getWidth());
      }
    }

    return builder.toString();
  }

  public static String serializeEditableTreeGridColumns(List<EditableTreeGridColumn> columns) {
    StringBuilder builder = new StringBuilder();

    boolean first = true;
    for (EditableTreeGridColumn column : columns) {
      if (!first)
        builder.append("; ");
      else
        first = false;

      if (column.getDisplayName() != null) {
        builder.append(column.getDisplayName());
      }
      builder.append(", ");

      if (column.getDatabaseColumn() != null) {
        builder.append(column.getDatabaseColumn());
      }
      builder.append(", ");

      if (column.getColumnReference() != null) {
        builder.append(column.getColumnReference().toString());
      }
      builder.append(", ");

      if (column.getWidth() != null) {
        builder.append(column.getWidth());
      }
      builder.append(", ");

      if (column.getColumnFormat() != null) {
        builder.append(column.getColumnFormat().toString());
      }
    }

    return builder.toString();
  }

}
