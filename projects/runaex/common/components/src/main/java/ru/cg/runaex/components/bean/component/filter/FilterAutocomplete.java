package ru.cg.runaex.components.bean.component.filter;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;

import ru.cg.runaex.components.bean.component.ComponentWithSingleField;
import ru.cg.runaex.components.bean.component.EditableFieldImpl;
import ru.cg.runaex.components.bean.component.part.ColumnReference;
import ru.cg.runaex.components.bean.component.part.TableReference;
import ru.cg.runaex.components.validation.annotation.DatabaseStructureElement;
import ru.cg.runaex.components.validation.annotation.NotNullSchema;
import ru.cg.runaex.components.validation.annotation.SortOrder;
import ru.cg.runaex.components.validation.annotation.SpeechInput;

/**
 * @author urmancheev
 */
public class FilterAutocomplete extends EditableFieldImpl implements ComponentWithSingleField {
  private static final long serialVersionUID = 7953688577352399855L;

  private static final int FIELD = 0;
  private static final int COLUMN_REFERENCE = 1;
  private static final int TABLE_ID = 2;
  private static final int DEFAULT_VALUE = 3;
  private static final int RELATED_FIELD = 4;
  private static final int RELATED_TABLE = 5;
  private static final int RELATED_TABLE_COLUMN = 6;
  private static final int RELATED_LINK_TABLE = 7;
  private static final int SORT_ORDER = 8;
  private static final int PLACE_HOLDER = 9;
  private static final int SPEECH_INPUT = 10;
  private static final int VISIBILITY_RULE = 11;
  private static final int EDITABILITY_RULE = 12;

  private boolean speechInputEnabled;

  private ColumnReference field;
  private String relatedTableColumn;

  private ColumnReference relatedFieldReference;
  private ColumnReference columnReference;
  private TableReference relatedTableReference;
  private TableReference relatedLinkTableReference;

  @Override
  public int getParametersNumber() {
    return 13;
  }

  @Override
  protected void initLazyFields() {
    super.initLazyFields();

    speechInputEnabled = convertEnableSpeechInput(getSpeechInput());

    field = parseColumnReferenceInitTerm(getParameter(FIELD));
    ColumnReference relatedTableColumnReference = parseColumnReference(getParameter(RELATED_TABLE_COLUMN));
    relatedTableColumn = relatedTableColumnReference != null ? relatedTableColumnReference.getColumn() : null;

    relatedFieldReference = parseColumnReference(getParameter(RELATED_FIELD));
    columnReference = parseColumnReference(getParameter(COLUMN_REFERENCE));
    relatedTableReference = parseTableReference(getParameter(RELATED_TABLE));
    relatedLinkTableReference = parseTableReference(getParameter(RELATED_LINK_TABLE));
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

  @DatabaseStructureElement
  public String getRelatedTableColumn() {
    ensureFullyInitialized();
    return relatedTableColumn;
  }

  @SortOrder
  public String getSortOrder() {
    return getParameter(SORT_ORDER);
  }

  public String getPlaceHolder() {
    return getParameter(PLACE_HOLDER);
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
  public ColumnReference getRelatedFieldReference() {
    ensureFullyInitialized();
    return relatedFieldReference;
  }

  @Valid
  @NotNull
  public ColumnReference getColumnReference() {
    ensureFullyInitialized();
    return columnReference;
  }

  @Valid
  public TableReference getRelatedTableReference() {
    ensureFullyInitialized();
    return relatedTableReference;
  }

  @Valid
  public TableReference getRelatedLinkTableReference() {
    ensureFullyInitialized();
    return relatedLinkTableReference;
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
