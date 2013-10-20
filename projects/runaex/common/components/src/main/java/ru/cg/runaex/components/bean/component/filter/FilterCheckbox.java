package ru.cg.runaex.components.bean.component.filter;

import javax.validation.constraints.NotNull;

import ru.cg.runaex.components.bean.component.ComponentWithSingleField;
import ru.cg.runaex.components.bean.component.EditableFieldImpl;
import ru.cg.runaex.components.bean.component.part.ColumnReference;
import ru.cg.runaex.components.validation.annotation.DatabaseStructureElement;
import ru.cg.runaex.components.validation.annotation.NotNullSchema;

/**
 * @author urmancheev
 */
public class FilterCheckbox extends EditableFieldImpl implements ComponentWithSingleField {
  private static final long serialVersionUID = 5255212132523946699L;

  private static final int FIELD = 0;
  private static final int TABLE_ID = 1;
  private static final int DEFAULT_VALUE = 2;
  private static final int VISIBILITY_RULE = 3;
  private static final int EDITABILITY_RULE = 4;

  private ColumnReference field;

  @Override
  public int getParametersNumber() {
    return 5;
  }

  @Override
  protected void initLazyFields() {
    super.initLazyFields();

    field = parseColumnReferenceInitTerm(getParameter(FIELD));
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

  @NotNull
  public String getTableId() {
    return getParameter(TABLE_ID);
  }

  public String getDefaultValue() {
    return getParameter(DEFAULT_VALUE);
  }

  @Override
  protected int getVisibilityRuleParameterIndex() {
    return VISIBILITY_RULE;
  }

  @Override
  protected int getEditabilityRuleParameterIndex() {
    return EDITABILITY_RULE;
  }
}
