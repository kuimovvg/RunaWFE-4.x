package ru.cg.runaex.components.bean.component.filter;

import javax.validation.constraints.NotNull;

import ru.cg.runaex.components.bean.component.ComponentWithSingleField;
import ru.cg.runaex.components.bean.component.EditableFieldImpl;
import ru.cg.runaex.components.bean.component.part.ComparisonType;
import ru.cg.runaex.components.bean.component.part.TextType;
import ru.cg.runaex.components.parser.ComponentParser;
import ru.cg.runaex.components.validation.annotation.*;

/**
 * @author urmancheev
 */
public class FilterField extends EditableFieldImpl implements ComponentWithSingleField {
  private static final long serialVersionUID = -10544985362032845L;

  private static final int FIELD = 0;
  private static final int TYPE = 1;
  private static final int REGEX = 2;
  private static final int LENGTH = 3;
  private static final int TABLE_ID = 4;
  private static final int COMPARISON = 5;
  private static final int DEFAULT_VALUE = 6; //todo add validation by value type like in TextField
  private static final int PLACE_HOLDER = 7;
  private static final int SPEECH_INPUT = 8;
  private static final int VISIBILITY_RULE = 9;
  private static final int EDITABILITY_RULE = 10;

  private Integer length;

  private ru.cg.runaex.components.bean.component.part.ColumnReference field;
  private TextType valueType;
  private ComparisonType comparisonType;
  private boolean speechInputEnabled;

  @Override
  public int getParametersNumber() {
    return 11;
  }

  @Override
  protected void initLazyFields() {
    super.initLazyFields();

    field = parseColumnReferenceInitTerm(getParameter(FIELD));
    valueType = ComponentParser.convertTextType(getTypeStr());
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
  public String getTypeStr() {
    return getParameter(TYPE);
  }

  @Regex
  public String getRegex() {
    return getParameter(REGEX);
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

  public String getDefaultValue() {
    return getParameter(DEFAULT_VALUE);
  }

  public String getPlaceHolder() {
    return getParameter(PLACE_HOLDER);
  }

  public TextType getValueType() {
    ensureFullyInitialized();
    return valueType;
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

  @AssertNumber(allowFractional = false)
  public String getLengthStr() {
    return getParameter(LENGTH);
  }

  public int getLength() {
    if (length == null)
      length = convertLength(getLengthStr());
    return length;
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
