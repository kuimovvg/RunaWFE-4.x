package ru.cg.runaex.components.bean.component.part;

import java.io.Serializable;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;

/**
 * @author Абдулин Ильдар
 */
public class SphinxViewColumn implements Serializable {
  private static final long serialVersionUID = -8357715021025512283L;

  @NotNull
  private String displayName;

  @Valid
  @NotNull
  private ColumnReference reference;

  public SphinxViewColumn(String displayName, ColumnReference reference) {
    this.displayName = displayName;
    this.reference = reference;
  }

  public String getDisplayName() {
    return displayName;
  }

  public ColumnReference getReference() {
    return reference;
  }

  @Override
  public String toString() {
    return displayName != null && reference != null ? displayName + "," + reference.toString() : "";
  }
}
