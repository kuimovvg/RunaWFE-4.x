package ru.cg.runaex.generatedb.bean;

import java.text.MessageFormat;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;

import ru.cg.runaex.components.GenerateFieldType;
import ru.cg.runaex.exceptions.DuplicateColumnWithDifferentType;
import ru.cg.runaex.generatedb.GenerateDBImpl;
import ru.cg.runaex.generatedb.util.FieldHashSet;

/**
 * @author Sabirov
 */
public class Table extends GenerateDBImpl {
  public static final String POSTFIX_TABLE_ID = "_id";
  public static final int DEFAULTS_FIELD_LENGTH = 500;

  private Schema schema;
  private String name;
  private FieldHashSet<Field> fields;

  public Table() {
  }

  public Schema getSchema() {
    return schema;
  }

  public void setSchema(Schema schema) {
    this.schema = schema;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public FieldHashSet<Field> getFields() {
    if (this.fields == null) {
      this.fields = new FieldHashSet<Field>();
    }
    return fields;
  }

  public void setFields(FieldHashSet<Field> fields) {
    this.fields = fields;
  }

  public void setField(Field field) {
    if (field == null || field.isEmpty()) {
      return;
    }
    /**
     * if field equals with tablePk (person_id and table_name + "_id") then this field don't add to fields table
     */
    if (field.getName().equals(this.getName() + POSTFIX_TABLE_ID)) {
      return;
    }

    if (this.fields == null) {
      this.fields = new FieldHashSet<Field>();
    }
    boolean isExist = false;
    for (Field f : this.fields) {
      if (f.hashCode() == field.hashCode()) {
        isExist = true;
        if (f.getType() != field.getType()) {
          //Остальные типы более приоритетные чем VARCHAR
          if (f.getType() == GenerateFieldType.VARCHAR) {
            f.copy(field);
          }
          else if (field.getType() != GenerateFieldType.VARCHAR) {
            StringBuilder stringBuilder = new StringBuilder("duplicate field ");
            if (getSchema() != null)
              stringBuilder.append("schemaname=").append(getSchema().getName());
            stringBuilder.append(" tablename=").append(getName())
                .append("fields (").append(f.getName()).append(" with type ").append(f.getType().toString())
                .append(",").append(field.getName()).append("with type ").append(field.getType().toString())
                .append(")");
            throw new DuplicateColumnWithDifferentType(stringBuilder.toString());
          }
        }
      }
    }
    if (!isExist) {
      this.fields.add(field);
    }
  }

  public void addFields(FieldHashSet<Field> fields) {
    if (this.fields == null) {
      this.fields = new FieldHashSet<Field>();
    }
    for (Field field : fields) {
      this.setField(field);
    }
  }

  /**
   * Check empty field by name
   *
   * @return true if field name is empty
   */
  public boolean isEmpty() {
    return this.schema == null || this.schema.isEmpty() ||
        this.getName() == null || this.getName().isEmpty() ||
        this.fields == null || this.fields.isEmpty();
  }

  /**
   * Check object on equals by next values: schema, table name and fields
   *
   * @param obj - object
   * @return true if current object is equals
   */
  @Override
  public boolean equals(Object obj) {
    if (this == obj)
      return true;
    if ((obj == null) || (obj.getClass() != this.getClass()))
      return false;

    Table table = (Table) obj;
    return getSchema().equals(table.getSchema()) &&
        getName().equals(table.getName()) && getFields().equals(table.getFields());
  }

  /**
   * Check hash code by next values: schema, table name and fields
   *
   * @return hash code
   */
  @Override
  public int hashCode() {
    int hash = 7;
    hash = 31 * hash + (getSchema() != null ? getSchema().hashCode() : "schema".hashCode());
    hash = 31 * hash + (getName() != null && !getName().isEmpty() ? getName().hashCode() : "name".hashCode());
    hash = 31 * hash + (getFields() != null ? getFields().hashCode() : "fields".hashCode());
    return hash;
  }

  @Override
  public String toString() {
    String schema = null;
    if (getSchema() != null) {
      schema = getSchema().toString();
    }
    String fields = null;
    if (getFields() != null) {
      fields = getFields().toString();
    }
    return "\n " + schema + "\n Table - " + getName() + "\n " + fields;
  }

  @Override
  public String getSQL() {
    StringBuilder stringBuilder = new StringBuilder();

    /**
     * generate sql code from tables and fields
     */
    String template = super.getSQL();
    String sql = MessageFormat.format(template, getSchema().getName(), getName(), getFields().getSQL());
    stringBuilder.append(sql);

    /**
     * generate sql code comments to table field from fields
     */
    template = super.getSQLTemplate("comment_to_column");
    for (Field field : getFields()) {
      if (field.getComment() == null || field.getComment().isEmpty())
        continue;
      sql = MessageFormat.format(template, getSchema().getName(),
          getName(), field.getName(), field.getComment());
      stringBuilder.append(sql);
    }

    return stringBuilder.toString();
  }

  @Override
  public String getSQLReferences() {
    /**
     * generate sql code from references
     */
    StringBuilder sbRefTable = new StringBuilder();
    for (Field field : getFields().getSortFieldList()) {
      if (field.getReferences() != null) {
        String fkTemplate = field.getReferences().getSQL();
        String sql = MessageFormat.format(fkTemplate, field.getReferences().getSQL());
        sbRefTable.append(sql);
      }
    }

    return sbRefTable.toString();
  }

  public boolean hasReferences() {
    for (Field field : fields) {
      if (field.getReferences() != null) {
        return true;
      }
    }
    return false;
  }

  public List<Field> getSortedFields() {
    List<Field> list = new LinkedList<Field>(fields);
    Collections.sort(list, new Comparator<Field>() {
      @Override
      public int compare(Field o1, Field o2) {
        return o1.getName().compareTo(o2.getName());
      }
    });
    return list;
  }
}
