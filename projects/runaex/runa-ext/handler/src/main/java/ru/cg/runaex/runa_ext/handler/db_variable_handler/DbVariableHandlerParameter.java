package ru.cg.runaex.runa_ext.handler.db_variable_handler;

import java.io.Serializable;

/**
 * @author Петров А.
 */
public class DbVariableHandlerParameter implements Serializable {

  private static final long serialVersionUID = 1L;

  private String variableName;
  private String columnName;

  public String getVariableName() {
    return variableName;
  }

  public void setVariableName(String variableName) {
    this.variableName = variableName;
  }

  public String getColumnName() {
    return columnName;
  }

  public void setColumnName(String columnName) {
    this.columnName = columnName;
  }
}
