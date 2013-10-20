package ru.cg.runaex.components.bean.component.part;

/**
 * @author urmancheev
 */
@ru.cg.runaex.components.validation.annotation.TableReference
public class TableReference extends Reference {
  private static final long serialVersionUID = 4292124749862523671L;

  public TableReference(String schema, String table, int termCount) {
    super(schema, table, termCount);
  }
}
