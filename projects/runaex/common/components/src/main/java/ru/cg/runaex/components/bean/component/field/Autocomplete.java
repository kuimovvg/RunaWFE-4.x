package ru.cg.runaex.components.bean.component.field;

import java.util.ArrayList;
import java.util.List;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;

import ru.cg.runaex.components.GenerateFieldType;
import ru.cg.runaex.components.bean.component.ComponentWithSingleField;
import ru.cg.runaex.components.bean.component.EditableFieldImpl;
import ru.cg.runaex.components.bean.component.part.ColumnReference;
import ru.cg.runaex.components.bean.component.part.DefaultValue;
import ru.cg.runaex.components.bean.component.part.RequireRuleComponentPart;
import ru.cg.runaex.components.bean.component.part.TableReference;
import ru.cg.runaex.components.validation.ComponentWithCustomValidation;
import ru.cg.runaex.components.validation.annotation.DatabaseStructureElement;
import ru.cg.runaex.components.validation.annotation.NotNullSchema;
import ru.cg.runaex.components.validation.annotation.SortOrder;
import ru.cg.runaex.components.validation.annotation.SpeechInput;

/**
 * @author urmancheev
 */
public class Autocomplete extends EditableFieldImpl implements ComponentWithSingleField, ComponentWithCustomValidation {
  private static final long serialVersionUID = -1264717694023504225L;

  private static final int FIELD = 0;
  private static final int COLUMN_REFERENCE = 1;
  private static final int REQUIRED = 2;
  private static final int PLACE_HOLDER = 3;
  private static final int RELATED_FIELD = 4;
  private static final int RELATED_TABLE = 5;
  private static final int RELATED_TABLE_COLUMN = 6;
  private static final int RELATED_LINK_TABLE = 7;
  private static final int SORT_ORDER = 8;
  private static final int SPEECH_INPUT = 9;
  private static final int DEFAULT_VALUE = 10;
  private static final int VISIBILITY_RULE = 11;
  private static final int EDITABILITY_RULE = 12;

  private RequireRuleComponentPart requireRule;

  private boolean speechInputEnabled;

  private ColumnReference field;
  private String relatedTableColumn;

  private ColumnReference relatedField;
  private ColumnReference columnReference;
  private TableReference relatedTableReference;
  private TableReference relatedLinkTableReference;
  private DefaultValue defaultValue;

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

    relatedField = parseColumnReference(getParameter(RELATED_FIELD));
    columnReference = parseColumnReference(getParameter(COLUMN_REFERENCE));
    relatedTableReference = parseTableReference(getParameter(RELATED_TABLE));
    relatedLinkTableReference = parseTableReference(getParameter(RELATED_LINK_TABLE));
    defaultValue = parseDefaultValue(getDefaultValueStr());
  }

  @Override
  public GenerateFieldType getGenerateFieldType() {
    return GenerateFieldType.VARCHAR;
  }

  public boolean isDefaultValueNotSpecified() {
    return getDefaultValue() == null || getDefaultValue().getValue() == null;
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

  public String getHiddenField() {
    return getField();
  }

  public String getPlaceHolder() {
    return getParameter(PLACE_HOLDER);
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

  @SpeechInput
  public String getSpeechInput() {
    return getParameter(SPEECH_INPUT);
  }

  public String getDefaultValueStr() {
    return getParameter(DEFAULT_VALUE);
  }

  public DefaultValue getDefaultValue() {
    ensureFullyInitialized();
    return defaultValue;
  }

  @Valid
  public TableReference getRelatedLinkTable() {
    ensureFullyInitialized();
    return relatedLinkTableReference;
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

  @Valid
  public ColumnReference getRelatedField() {
    ensureFullyInitialized();
    return relatedField;
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
    List<String> errors = new ArrayList<String>(2);
    boolean hasNotParentAutocomplete = getRelatedField() == null && getRelatedTableReference() == null && getRelatedTableColumn() == null && getRelatedLinkTable() == null;
    if (!hasNotParentAutocomplete) {
      if (getRelatedField() == null || getRelatedTableReference() == null)
        errors.add("Autocomplete.IncorrectBinding");
      if (getRelatedTableColumn() != null && getRelatedLinkTable() != null) {
        errors.add("Autocomplete.BothLinkFieldsFilled");
      }
    }
    return errors;
  }
}
