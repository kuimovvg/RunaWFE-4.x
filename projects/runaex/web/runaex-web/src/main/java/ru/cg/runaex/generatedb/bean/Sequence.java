package ru.cg.runaex.generatedb.bean;

import java.io.Serializable;
import java.text.MessageFormat;

import ru.cg.runaex.generatedb.GenerateDBImpl;

/**
 * @author korablev
 */
public class Sequence extends GenerateDBImpl implements Serializable {
  private static final long serialVersionUID = -6220861863049000720L;

  private Schema schema;
  private String tableName;
  private String fieldName;
  private String sequenceName;
  private boolean isGenerate = true;

  public Schema getSchema() {
    return schema;
  }

  public void setSchema(Schema schema) {
    this.schema = schema;
  }

  public String getTableName() {
    return tableName;
  }

  public void setTableName(String tableName) {
    this.tableName = tableName;
  }

  public String getFieldName() {
    return fieldName;
  }

  public void setFieldName(String fieldName) {
    this.fieldName = fieldName;
  }

  public String getSequenceName() {
    return sequenceName;
  }

  public void setSequenceName(String sequenceName) {
    this.sequenceName = sequenceName;
  }

  public boolean isGenerate() {
    return isGenerate;
  }

  public void setGenerate(boolean generate) {
    isGenerate = generate;
  }

  @Override
  public boolean equals(Object obj) {
    if (obj instanceof Sequence) {
      if (this == obj)
        return true;
      if ((obj.getClass() != this.getClass()))
        return false;

      Sequence sequence = (Sequence) obj;
      return getSchema() != null && getSchema().equals(sequence.getSchema()) &&
          getTableName() != null && getTableName().equals(sequence.getTableName()) &&
          getFieldName() != null && getFieldName().equals(sequence.getFieldName());

    }
    return false;
  }

  @Override
  public String getSQL() {
    if (!this.isGenerate()) {
      return "";
    }
    String template = super.getSQL();
    return MessageFormat.format(template, getSchema().getName(), getSequenceName());
  }
}
