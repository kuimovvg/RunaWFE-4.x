package ru.cg.runaex.components.bean.component.part;

import java.io.Serializable;
import java.util.List;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;

import ru.cg.runaex.components.validation.annotation.DatabaseStructureElement;
import ru.cg.runaex.components.validation.annotation.NotNullSchema;

/**
 * @author Kochetkov
 */
public class StoredProcedure implements Serializable {
  private static final long serialVersionUID = -5685891771770762266L;

  @NotNullSchema
  @DatabaseStructureElement
  protected String schema;

  @NotNull
  @DatabaseStructureElement
  protected String procedureName;

  @Valid
  protected List<Column> parameters;

  public StoredProcedure(String schema, String procedureName, List<Column> parameters) {
    this.schema = schema;
    this.procedureName = procedureName;
    this.parameters = parameters;
  }

  public String getSchema() {
    return schema;
  }

  public void setSchema(String schema) {
    this.schema = schema;
  }

  public String getProcedureName() {
    return procedureName;
  }

  public void setProcedureName(String procedureName) {
    this.procedureName = procedureName;
  }

  public List<Column> getParameters() {
    return parameters;
  }

  public void setParameters(List<Column> parameters) {
    this.parameters = parameters;
  }
}
