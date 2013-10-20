package ru.cg.runaex.components.bean.component.filter;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import javax.validation.constraints.NotNull;

import ru.cg.runaex.components.bean.component.ComponentWithSingleField;
import ru.cg.runaex.components.bean.component.EditableFieldImpl;
import ru.cg.runaex.components.bean.component.part.ColumnReference;
import ru.cg.runaex.components.bean.component.part.ComparisonType;
import ru.cg.runaex.components.bean.component.part.DateType;
import ru.cg.runaex.components.parser.ComponentParser;
import ru.cg.runaex.components.validation.ComponentWithCustomValidation;
import ru.cg.runaex.components.validation.annotation.DatabaseStructureElement;
import ru.cg.runaex.components.validation.annotation.NotNullSchema;

/**
 * @author urmancheev
 */
public class FilterDateTimePicker extends EditableFieldImpl implements ComponentWithSingleField, ComponentWithCustomValidation {
  private static final long serialVersionUID = 1353049298959899025L;

  private static final int FIELD = 0;
  private static final int TABLE_ID = 1;
  private static final int COMPARISON = 2;
  private static final int TYPE = 3;
  private static final int DEFAULT_VALUE = 4;
  private static final int PLACE_HOLDER = 5;
  private static final int VISIBILITY_RULE = 6;
  private static final int EDITABILITY_RULE = 7;

  private ColumnReference field;
  private ComparisonType comparisonType;
  private DateType type;

  @Override
  public int getParametersNumber() {
    return 8;
  }

  @Override
  protected void initLazyFields() {
    super.initLazyFields();

    field = parseColumnReferenceInitTerm(getParameter(FIELD));
    type = ComponentParser.convertDateType(getTypeStr());
    comparisonType = ComponentParser.convertComparisonType(getParameter(COMPARISON));
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

  @NotNull
  public String getComparisonStr() {
    return getParameter(COMPARISON);
  }

  public ComparisonType getComparison() {
    ensureFullyInitialized();
    return comparisonType;
  }

  public String getTypeStr() {
    return getParameter(TYPE);
  }

  public String getDefaultValue() {
    return getParameter(DEFAULT_VALUE);
  }

  public String getPlaceHolder() {
    return getParameter(PLACE_HOLDER);
  }

  public DateType getType() {
    ensureFullyInitialized();
    return type;
  }

  @Override
  protected int getVisibilityRuleParameterIndex() {
    return VISIBILITY_RULE;
  }

  @Override
  protected int getEditabilityRuleParameterIndex() {
    return EDITABILITY_RULE;
  }

  @Override
  public List<String> customValidate() {
    if (getTypeStr() == null)
      return Collections.emptyList();

    List<String> constraintCodes = new ArrayList<String>(1);

    if (!"date".equals(getTypeStr()) && !"date_time".equals(getTypeStr()))
      constraintCodes.add("DateTimePicker.invalidType");

    return constraintCodes;
  }
}
