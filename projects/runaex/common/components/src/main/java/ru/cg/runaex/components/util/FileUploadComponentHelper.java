package ru.cg.runaex.components.util;

import ru.cg.runaex.components.bean.component.part.ColumnReference;

/**
 * @author urmancheev
 */
public class FileUploadComponentHelper {
  public static final String DATA_COLUMN_POSTFIX = "_filedata";

  public static String getNameColumn(String genericColumn) {
    return genericColumn;
  }

  public static String getDataColumn(String genericColumn) {
    return genericColumn.concat(DATA_COLUMN_POSTFIX);
  }

  public static ColumnReference getFileColumnReference(ColumnReference filenameColumnReference) {
    ColumnReference fileColumnReference = filenameColumnReference.clone();
    fileColumnReference.setColumn(getDataColumn(filenameColumnReference.getColumn()));
    return fileColumnReference;
  }
}
