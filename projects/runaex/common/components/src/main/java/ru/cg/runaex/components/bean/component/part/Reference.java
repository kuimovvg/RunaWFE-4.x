package ru.cg.runaex.components.bean.component.part;

import java.io.Serializable;
import javax.validation.constraints.NotNull;

import ru.cg.runaex.components.validation.NotNullChecks;
import ru.cg.runaex.components.validation.annotation.DatabaseStructureElement;
import ru.cg.runaex.components.validation.annotation.NotNullSchema;

/**
 * @author urmancheev
 */
public abstract class Reference implements Serializable {
  private static final long serialVersionUID = 8287379907967626289L;

  @NotNullSchema
  @DatabaseStructureElement
  protected String schema;
  @NotNull(groups = NotNullChecks.class)
  @DatabaseStructureElement
  protected String table;

  protected int termCount;

  public Reference(String schema, String table, int termCount) {
    this.schema = schema;
    this.table = table;
    this.termCount = termCount;
  }

  public String getTable() {
    return table;
  }

  public String getSchema() {
    return schema;
  }

  public int getTermCount() {
    return termCount;
  }

  @Override
  public String toString() {
    StringBuilder str = new StringBuilder();
    str.append(schema).append(".").append(table);
    return str.toString();
  }
}
