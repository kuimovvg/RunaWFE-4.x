package ru.cg.runaex.components.bean.session;

/**
 * @author Петров А.
 */
public class LinkTableInfo {

  private String schema;
  private String table;
  private String baseObjectSchema;
  private String baseObjectTable;

  public LinkTableInfo() {
  }

  public LinkTableInfo(String schema, String table, String baseObjectSchema, String baseObjectTable) {
    this.schema = schema;
    this.table = table;
    this.baseObjectSchema = baseObjectSchema;
    this.baseObjectTable = baseObjectTable;
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

  public String getBaseObjectSchema() {
    return baseObjectSchema;
  }

  public void setBaseObjectSchema(String baseObjectSchema) {
    this.baseObjectSchema = baseObjectSchema;
  }

  public String getBaseObjectTable() {
    return baseObjectTable;
  }

  public void setBaseObjectTable(String baseObjectTable) {
    this.baseObjectTable = baseObjectTable;
  }
}
