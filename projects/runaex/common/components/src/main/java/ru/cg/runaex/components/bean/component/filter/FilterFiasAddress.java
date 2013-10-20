package ru.cg.runaex.components.bean.component.filter;

import java.util.ArrayList;
import java.util.List;
import javax.validation.constraints.NotNull;

import ru.cg.runaex.components.bean.component.EditableFieldImpl;
import ru.cg.runaex.components.bean.component.FiasComponent;
import ru.cg.runaex.components.bean.component.part.ColumnReference;
import ru.cg.runaex.components.bean.component.part.FiasObjectLevel;
import ru.cg.runaex.components.parser.FiasObjectLevelParser;
import ru.cg.runaex.components.validation.ComponentWithCustomValidation;
import ru.cg.runaex.components.validation.annotation.AssertNumber;
import ru.cg.runaex.components.validation.annotation.DatabaseStructureElement;
import ru.cg.runaex.components.validation.annotation.NotNullSchema;
import ru.cg.runaex.components.validation.helper.FiasValidationHelper;

/**
 * @author urmancheev
 */
public class FilterFiasAddress extends EditableFieldImpl implements FiasComponent, ComponentWithCustomValidation {
  private static final long serialVersionUID = 734764413853298831L;

  private static final int FIELD = 0;
  private static final int TABLE_ID = 1;
  private static final int MIN_LEVEL = 2;
  private static final int MAX_LEVEL = 3;
  private static final int DEFAULT_FILTER = 4;
  private static final int USAGE_ACTUAL = 5;
  private static final int FULL_FIELD = 6;
  private static final int VISIBILITY_RULE = 7;
  private static final int EDITABILITY_RULE = 8;

  private ColumnReference field;
  private String fullField;

  private Boolean usageActual;
  private FiasObjectLevel minObjectLevel;
  private FiasObjectLevel maxObjectLevel;

  @Override
  public int getParametersNumber() {
    return 9;
  }

  @Override
  protected void initLazyFields() {
    super.initLazyFields();

    field = parseColumnReferenceInitTerm(getParameter(FIELD));
    ColumnReference fullFieldReference = parseColumnReference(getParameter(FULL_FIELD));
    fullField = fullFieldReference != null ? fullFieldReference.getColumn() : null;

    minObjectLevel = FiasObjectLevelParser.convertObjectLevel(getMinLevel());
    maxObjectLevel = FiasObjectLevelParser.convertObjectLevel(getMaxLevel());
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
  @AssertNumber(allowFractional = false)
  public String getMinLevel() {
    return getParameter(MIN_LEVEL);
  }

  @NotNull
  @AssertNumber(allowFractional = false)
  public String getMaxLevel() {
    return getParameter(MAX_LEVEL);
  }

  public String getDefaultFilter() {
    return getParameter(DEFAULT_FILTER);
  }

  @DatabaseStructureElement
  public String getFullField() {
    ensureFullyInitialized();
    return fullField;
  }

  @NotNull
  public boolean getUsageActual() {
    if (usageActual == null) {
      String usageActualStr = getParameter(USAGE_ACTUAL);
      usageActual = usageActualStr != null ? Boolean.valueOf(usageActualStr) : true;
    }
    return usageActual;
  }

  public FiasObjectLevel getMinObjectLevel() {
    ensureFullyInitialized();
    return minObjectLevel;
  }

  public FiasObjectLevel getMaxObjectLevel() {
    ensureFullyInitialized();
    return maxObjectLevel;
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
    List<String> errorCodes = new ArrayList<String>(1);

    FiasValidationHelper.validateLevelCombination(getMinLevel(), getMaxLevel(), errorCodes);

    return errorCodes;
  }

}
