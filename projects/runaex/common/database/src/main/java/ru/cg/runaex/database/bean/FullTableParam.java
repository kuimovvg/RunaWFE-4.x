package ru.cg.runaex.database.bean;

/**
 * @author Абдулин Ильдар
 */
public class FullTableParam {
  private String schema;
  private String table;
  private String columns[];
  private String linkColumns;
  private String parentCol;
  private String tableId;

  public FullTableParam() {

  }

  public FullTableParam(String schema, String table, String[] columns, String linkColumns, String parentCol, String tableId) {
    this.schema = schema;
    this.table = table;
    this.columns = columns;
    this.linkColumns = linkColumns;
    this.parentCol = parentCol;
    this.tableId = tableId;
  }

  public String getSchema() {
    return schema;
  }

  public void setSchema(String schema) {
    this.schema = schema;
  }

  public String getTable() {
    return table;
  }

  public void setTable(String table) {
    this.table = table;
  }

  public String[] getColumns() {
    return columns;
  }

  public void setColumns(String[] columns) {
    this.columns = columns;
  }

  public String getLinkColumns() {
    return linkColumns;
  }

  public void setLinkColumns(String linkColumns) {
    this.linkColumns = linkColumns;
  }

  public String getParentCol() {
    return parentCol;
  }

  public void setParentCol(String parentCol) {
    this.parentCol = parentCol;
  }

  public String getTableId() {
    return tableId;
  }

  public void setTableId(String tableId) {
    this.tableId = tableId;
  }
}
