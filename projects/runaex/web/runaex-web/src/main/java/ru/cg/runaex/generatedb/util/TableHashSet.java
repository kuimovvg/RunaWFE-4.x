package ru.cg.runaex.generatedb.util;

import java.util.HashSet;

import ru.cg.runaex.generatedb.GenerateDB;
import ru.cg.runaex.generatedb.bean.Schema;
import ru.cg.runaex.generatedb.bean.Table;

/**
 * Date: 14.08.12
 * Time: 8:53
 *
 * @author Sabirov
 */
public class TableHashSet<T extends Table> extends HashSet<T> implements GenerateDB {

  private static final long serialVersionUID = 1119798650042709577L;

  private SchemaHashSet<Schema> schemas;

  public TableHashSet() {
    this.schemas = new SchemaHashSet();
  }

  @Override
  public boolean add(T newT) {
    boolean isAdd = false;
    boolean isAddToTable = false;

    for (T t : this) {
      /**
       * check new table on exist in hash set by schema and table name
       */
      if (((t.getSchema() == null && newT.getSchema() == null)
          || t.getSchema().equals(newT.getSchema())) &&
          t.getName().equals(newT.getName())) {
        isAddToTable = true;
        t.addFields(newT.getFields());
      }
    }

    if (!isAddToTable)
      isAdd = super.add(newT);

    boolean isNeedAddSchema = false;
    Schema newS = newT.getSchema();
    for (Schema s : this.schemas) {
      if (!s.equals(newS))
        isNeedAddSchema = true;
    }
    if(this.schemas.isEmpty() || isNeedAddSchema) {
      this.schemas.add(newS);
    }

    return isAdd;
  }

  public SchemaHashSet<Schema> getSchemas() {
    return schemas;
  }

  public TableHashSet clone() {
    return (TableHashSet) super.clone();
  }

  @Override
  public String getSQL() {
    StringBuilder sb = new StringBuilder();
    for (Schema schema : this.schemas) {
      sb.append(schema.getSQL());
    }
    for (T t : this) {
      sb.append(t.getSQL());
    }
    for (T t : this) {
      sb.append(t.getSQLReferences());
    }
    return sb.toString();
  }
}
