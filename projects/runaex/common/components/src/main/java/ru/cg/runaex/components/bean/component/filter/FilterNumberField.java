package ru.cg.runaex.components.bean.component.filter;

import javax.validation.constraints.NotNull;

import ru.cg.runaex.components.bean.component.ComponentWithSingleField;
import ru.cg.runaex.components.bean.component.EditableFieldImpl;
import ru.cg.runaex.components.bean.component.part.ColumnReference;
import ru.cg.runaex.components.bean.component.part.ComparisonType;
import ru.cg.runaex.components.parser.ComponentParser;
import ru.cg.runaex.components.validation.annotation.AssertNumber;
import ru.cg.runaex.components.validation.annotation.DatabaseStructureElement;
import ru.cg.runaex.components.validation.annotation.NotNullSchema;
import ru.cg.runaex.components.validation.annotation.SpeechInput;

/**
 * @author Kochetkov
 */
public class FilterNumberField extends EditableFieldImpl implements ComponentWithSingleField {

  private static final long serialVersionUID = -1647161118531322764L;

  private ComparisonType comparisonType;
  private boolean speechInputEnabled;

  private static final int FIELD = 0;
  private static final int TABLE_ID = 1;
  private static final int COMPARISON = 2;
  private static final int DEFAULT_VALUE = 3;
  private static final int PLACE_HOLDER = 4;
  private static final int SPEECH_INPUT = 5;
  private static final int VISIBILITY_RULE = 6;
  private static final int EDITABILITY_RULE = 7;

  private ColumnReference field;

  @Override
  public int getParametersNumber() {
    return 8;
  }

  @Override
  protected void initLazyFields() {
    super.initLazyFields();
    field = parseColumnReferenceInitTerm(getParameter(FIELD));
    comparisonType = ComponentParser.convertComparisonType(getComparisonStr());
    speechInputEnabled = convertEnableSpeechInput(getSpeechInput());
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

  public String getComparisonStr() {
    return getParameter(COMPARISON);
  }

  @SpeechInput
  public String getSpeechInput() {
    return getParameter(SPEECH_INPUT);
  }

  @AssertNumber(allowFractional = false, allowNegative = true)
  public String getDefaultValue() {
    return getParameter(DEFAULT_VALUE);
  }

  public String getPlaceHolder() {
    return getParameter(PLACE_HOLDER);
  }

  @NotNull
  public ComparisonType getComparison() {
    ensureFullyInitialized();
    return comparisonType;
  }

  public boolean isSpeechInputEnabled() {
    ensureFullyInitialized();
    return speechInputEnabled;
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
