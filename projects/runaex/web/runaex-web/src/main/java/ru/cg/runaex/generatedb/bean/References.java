package ru.cg.runaex.generatedb.bean;

import ru.cg.runaex.generatedb.GenerateDBImpl;

import java.text.MessageFormat;


/**
 * Examples
 * CONSTRAINT fk_curr_tableN FOREIGN KEY (field)
 * COLUMN_REFERENCE ref_schema.ref_table (ref_field) MATCH SIMPLE
 * ON UPDATE RESTRICT ON DELETE RESTRICT
 *
 * @author Sabirov
 */
public class References extends GenerateDBImpl {
  private Table parentTable;
  private Table refTable;
  private Field field;
  private boolean cascadeDeletion;

  public References(Table parentTable, Table refTable, Field field) {
    this.parentTable = parentTable;
    this.refTable = refTable;
    this.field = field;
  }

  public Table getParentTable() {
    return parentTable;
  }

  public Table getRefTable() {
    return refTable;
  }

  public Field getField() {
    return field;
  }

  public boolean isCascadeDeletion() {
    return cascadeDeletion;
  }

  public void setCascadeDeletion(boolean cascadeDeletion) {
    this.cascadeDeletion = cascadeDeletion;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (!(o instanceof References)) return false;

    References that = (References) o;

    if (field != null ? !field.equals(that.field) : that.field != null) return false;
    if (parentTable != null ? !parentTable.equals(that.parentTable) : that.parentTable != null) return false;
    if (refTable != null ? !refTable.equals(that.refTable) : that.refTable != null) return false;

    return true;
  }

  @Override
  public int hashCode() {
    int result = parentTable != null ? parentTable.hashCode() : 0;
    result = 31 * result + (refTable != null ? refTable.hashCode() : 0);
    result = 31 * result + (field != null ? field.hashCode() : 0);
    return result;
  }

  @Override
  public String toString() {
    return "references: " + (refTable != null ? refTable.toString() : "");
  }

  @Override
  public String getSQL() {

    if (field == null) {
      return "";
    }

    String schemaName = parentTable.getSchema().getName();
    String tableName = parentTable.getName();
    String refSchemaName = refTable.getSchema().getName();
    String refTableName = refTable.getName();
    String onDelete = cascadeDeletion ? "CASCADE" : "NO ACTION";

    String template = super.getSQL();
    return MessageFormat.format(template, schemaName, tableName,
        refSchemaName, field.getName(), refTableName, onDelete);
  }
}
