package ru.cg.runaex.components.bean.component.field;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;

import ru.cg.runaex.components.GenerateFieldType;
import ru.cg.runaex.components.bean.component.ComponentWithSingleField;
import ru.cg.runaex.components.bean.component.EditableFieldImpl;
import ru.cg.runaex.components.bean.component.part.ColumnReference;
import ru.cg.runaex.components.bean.component.part.DefaultValue;
import ru.cg.runaex.components.bean.component.part.RequireRuleComponentPart;
import ru.cg.runaex.components.validation.annotation.DatabaseStructureElement;
import ru.cg.runaex.components.validation.annotation.NotNullSchema;
import ru.cg.runaex.components.validation.annotation.SortOrder;
import ru.cg.runaex.components.validation.annotation.SpeechInput;

/**
 * @author urmancheev
 */
public class ComboBox extends EditableFieldImpl implements ComponentWithSingleField {
  private static final long serialVersionUID = 8594091383051092870L;

  private static final int FIELD = 0;
  private static final int COLUMN_REFERENCE = 1;
  private static final int REQUIRED = 2;
  private static final int SORT_ORDER = 3;
  private static final int SPEECH_INPUT = 4;
  private static final int DEFAULT_VALUE = 5;
  private static final int VISIBILITY_RULE = 6;
  private static final int EDITABILITY_RULE = 7;

  private RequireRuleComponentPart requireRule;
  private boolean speechInputEnabled;
  private DefaultValue defaultValue;

  private ColumnReference field;
  private ColumnReference columnReference;

  @Override
  public int getParametersNumber() {
    return 8;
  }

  @Override
  protected void initLazyFields() {
    super.initLazyFields();

    speechInputEnabled = convertEnableSpeechInput(getSpeechInput());
    defaultValue = parseDefaultValue(getDefaultValueStr());

    field = parseColumnReferenceInitTerm(getParameter(FIELD));
    columnReference = parseColumnReference(getParameter(COLUMN_REFERENCE));
  }

  @Override
  public GenerateFieldType getGenerateFieldType() {
    return GenerateFieldType.BIGINT;
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

  @SortOrder
  public String getSortOrder() {
    return getParameter(SORT_ORDER);
  }

  @SpeechInput
  public String getSpeechInput() {
    return getParameter(SPEECH_INPUT);
  }

  public String getDefaultValueStr() {
    return getParameter(DEFAULT_VALUE);
  }

  @NotNull
  public RequireRuleComponentPart getRequireRule() {
    if (requireRule == null)
      requireRule = parseRequireRule(getParameter(REQUIRED));
    return requireRule;
  }

  public boolean isSpeechInputEnabled() {
    ensureFullyInitialized();
    return speechInputEnabled;
  }

  public DefaultValue getDefaultValue() {
    ensureFullyInitialized();
    return defaultValue;
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

  @Override
  protected int getEditabilityRuleParameterIndex() {
    return EDITABILITY_RULE;
  }
}
