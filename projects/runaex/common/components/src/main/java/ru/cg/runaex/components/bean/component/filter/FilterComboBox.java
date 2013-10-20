package ru.cg.runaex.components.bean.component.filter;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;

import ru.cg.runaex.components.bean.component.ComponentWithSingleField;
import ru.cg.runaex.components.bean.component.EditableFieldImpl;
import ru.cg.runaex.components.bean.component.part.ColumnReference;
import ru.cg.runaex.components.validation.annotation.DatabaseStructureElement;
import ru.cg.runaex.components.validation.annotation.NotNullSchema;
import ru.cg.runaex.components.validation.annotation.SortOrder;
import ru.cg.runaex.components.validation.annotation.SpeechInput;

/**
 * @author urmancheev
 */
public class FilterComboBox extends EditableFieldImpl implements ComponentWithSingleField {
  private static final long serialVersionUID = -3260418489983907673L;

  private static final int FIELD = 0;
  private static final int COLUMN_REFERENCE = 1;
  private static final int TABLE_ID = 2;
  private static final int DEFAULT_VALUE = 3;
  private static final int SORT_ORDER = 4;
  private static final int SPEECH_INPUT = 5;
  private static final int VISIBILITY_RULE = 6;
  private static final int EDITABILITY_RULE = 7;

  private ColumnReference field;
  private ColumnReference columnReference;
  private boolean speechInputEnabled;

  @Override
  public int getParametersNumber() {
    return 8;
  }

  @Override
  protected void initLazyFields() {
    super.initLazyFields();

    field = parseColumnReferenceInitTerm(getParameter(FIELD));
    speechInputEnabled = convertEnableSpeechInput(getSpeechInput());
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

  @NotNull
  public String getTableId() {
    return getParameter(TABLE_ID);
  }

  public String getDefaultValue() {
    return getParameter(DEFAULT_VALUE);
  }

  @SortOrder
  public String getSortOrder() {
    return getParameter(SORT_ORDER);
  }

  @SpeechInput
  public String getSpeechInput() {
    return getParameter(SPEECH_INPUT);
  }

  public boolean isSpeechInputEnabled() {
    ensureFullyInitialized();
    return speechInputEnabled;
  }

  @Valid
  @NotNull
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
