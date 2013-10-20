package ru.cg.runaex.components.bean.component.part;

import java.io.Serializable;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;

/**
 * @author Абдулин Ильдар
 */
public class SphinxIndexingColumn implements Serializable {
  private static final long serialVersionUID = -6138405938191082240L;

  @Valid
  @NotNull
  private ColumnReference reference;

  public SphinxIndexingColumn(ColumnReference reference) {
    this.reference = reference;
  }

  public ColumnReference getReference() {
    return reference;
  }

  @Override
  public String toString() {
    return reference != null ? reference.toString() : "";
  }
}
