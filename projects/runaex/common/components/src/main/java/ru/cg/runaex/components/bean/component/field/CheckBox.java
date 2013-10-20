package ru.cg.runaex.components.bean.component.field;

import java.util.ArrayList;
import java.util.List;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;

import ru.cg.runaex.components.GenerateFieldType;
import ru.cg.runaex.components.bean.component.ComponentWithSingleField;
import ru.cg.runaex.components.bean.component.EditableFieldImpl;
import ru.cg.runaex.components.bean.component.part.ColumnReference;
import ru.cg.runaex.components.bean.component.part.DefaultValueType;
import ru.cg.runaex.components.validation.ComponentWithCustomValidation;
import ru.cg.runaex.components.validation.annotation.DatabaseStructureElement;
import ru.cg.runaex.components.validation.annotation.NotNullSchema;

/**
 * @author urmancheev
 */
public class CheckBox extends EditableFieldImpl implements ComponentWithSingleField, ComponentWithCustomValidation {
  private static final long serialVersionUID = -6755130819543431151L;

  private static final int FIELD = 0;
  private static final int DEFAULT_VALUE = 1;
  private static final int COPY_FROM_REFERENCE = 2;
  private static final int VISIBILITY_RULE = 3;
  private static final int EDITABILITY_RULE = 4;

  private ColumnReference field;
  private ColumnReference copyFromReference;

  @Override
  public int getParametersNumber() {
    return 5;
  }

  @Override
  protected void initLazyFields() {
    super.initLazyFields();

    field = parseColumnReferenceInitTerm(getParameter(FIELD));
    copyFromReference = parseColumnReference(getCopyFrom());
  }

  @Override
  public GenerateFieldType getGenerateFieldType() {
    return GenerateFieldType.BOOLEAN;
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
  public String getDefaultValue() {
    return getParameter(DEFAULT_VALUE);
  }

  public String getCopyFrom() {
    return getParameter(COPY_FROM_REFERENCE);
  }

  @Valid
  public ColumnReference getCopyFromReference() {
    ensureFullyInitialized();
    return copyFromReference;
  }

  public boolean isUseDefaultValueFromDb() {
    DefaultValueType defaultValueType = convertDefaultValueType(getDefaultValue());
    return defaultValueType == DefaultValueType.FROM_DB;
  }

  public boolean isCheckedByDefault() {
    return "checked".equals(getDefaultValue());
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
    if ("fromDb".equals(getDefaultValue())) {
      if (getCopyFrom() == null || getCopyFrom().isEmpty())
        errorCodes.add("CheckBox.isEmptyCopyField");
    }
    return errorCodes;
  }
}
