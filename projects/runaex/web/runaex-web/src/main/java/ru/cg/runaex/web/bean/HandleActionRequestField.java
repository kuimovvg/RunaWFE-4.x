package ru.cg.runaex.web.bean;

/**
 * @author urmancheev
 */
public class HandleActionRequestField {
  private String schema;
  private String table;
  private String column;
  private Object value;
  private String tableId;
  private String currentTimeDefaultValue;
  private Boolean currentUserAsDefaultValue;
  private String autoGeneratePattern;
  private String autoGenerateSequence;
  private Boolean defaultFileFromDb;
  private String schemaReference;
  private String tableReference;
  private String fieldReference;

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

  public String getColumn() {
    return column;
  }

  public void setColumn(String column) {
    this.column = column;
  }

  public Object getValue() {
    return value;
  }

  public void setValue(Object value) {
    this.value = value;
  }

  public String getTableId() {
    return tableId;
  }

  public void setTableId(String tableId) {
    this.tableId = tableId;
  }

  public String getCurrentTimeDefaultValue() {
    return currentTimeDefaultValue;
  }

  public void setCurrentTimeDefaultValue(String currentTimeDefaultValue) {
    this.currentTimeDefaultValue = currentTimeDefaultValue;
  }

  public Boolean getCurrentUserAsDefaultValue() {
    return currentUserAsDefaultValue;
  }

  public void setCurrentUserAsDefaultValue(Boolean currentUserAsDefaultValue) {
    this.currentUserAsDefaultValue = currentUserAsDefaultValue;
  }

  public String getAutoGeneratePattern() {
    return autoGeneratePattern;
  }

  public void setAutoGeneratePattern(String autoGeneratePattern) {
    this.autoGeneratePattern = autoGeneratePattern;
  }

  public String getAutoGenerateSequence() {
    return autoGenerateSequence;
  }

  public void setAutoGenerateSequence(String autoGenerateSequence) {
    this.autoGenerateSequence = autoGenerateSequence;
  }

  public Boolean getDefaultFileFromDb() {
    return defaultFileFromDb;
  }

  public void setDefaultFileFromDb(Boolean defaultFileFromDb) {
    this.defaultFileFromDb = defaultFileFromDb;
  }

  public String getSchemaReference() {
    return schemaReference;
  }

  public void setSchemaReference(String schemaReference) {
    this.schemaReference = schemaReference;
  }

  public String getTableReference() {
    return tableReference;
  }

  public void setTableReference(String tableReference) {
    this.tableReference = tableReference;
  }

  public String getFieldReference() {
    return fieldReference;
  }

  public void setFieldReference(String fieldReference) {
    this.fieldReference = fieldReference;
  }
}
