package ru.cg.runaex.components.bean.component;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;

import ru.cg.runaex.components.bean.component.part.ColumnReference;
import ru.cg.runaex.components.validation.annotation.DatabaseStructureElement;
import ru.cg.runaex.components.validation.annotation.NotNullSchema;

/**
 * @author urmancheev
 */
public class Label extends Component {
  private static final long serialVersionUID = -1771798183599451481L;

  private static final int FIELD = 0;
  private static final int COLUMN_REFERENCE = 1;
  private static final int VISIBILITY_RULE = 2;

  private ColumnReference field;
  private ColumnReference columnReference;

  @Override
  public int getParametersNumber() {
    return 3;
  }

  @Override
  protected void initLazyFields() {
    super.initLazyFields();

    field = parseColumnReferenceInitTerm(getParameter(FIELD));
    columnReference = parseColumnReference(getParameter(COLUMN_REFERENCE));
  }

  @NotNullSchema
  @DatabaseStructureElement
  public String getSchema() {
    ensureFullyInitialized();
    return field.getSchema();
  }

  @NotNull
  @DatabaseStructureElement
  public String getTable() {
    ensureFullyInitialized();
    return field.getTable();
  }

  @NotNull
  @DatabaseStructureElement
  public String getField() {
    ensureFullyInitialized();
    return field.getColumn();
  }

  @Valid
  public ColumnReference getColumnReference() {
    ensureFullyInitialized();
    return columnReference;
  }

  @Override
  protected int getVisibilityRuleParameterIndex() {
    return VISIBILITY_RULE;
  }
}
