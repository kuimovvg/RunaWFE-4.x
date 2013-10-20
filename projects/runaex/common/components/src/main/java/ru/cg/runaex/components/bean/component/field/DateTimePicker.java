package ru.cg.runaex.components.bean.component.field;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import javax.validation.constraints.NotNull;

import ru.cg.runaex.components.GenerateFieldType;
import ru.cg.runaex.components.bean.component.ComponentWithSingleField;
import ru.cg.runaex.components.bean.component.EditableFieldImpl;
import ru.cg.runaex.components.bean.component.part.*;
import ru.cg.runaex.components.parser.ComponentParser;
import ru.cg.runaex.components.validation.ComponentWithCustomValidation;
import ru.cg.runaex.components.validation.annotation.DatabaseStructureElement;
import ru.cg.runaex.components.validation.annotation.NotNullSchema;
import ru.cg.runaex.core.DateFormat;

/**
 * @author urmancheev
 */
public class DateTimePicker extends EditableFieldImpl implements ComponentWithCustomValidation, ComponentWithSingleField {
  private static final long serialVersionUID = 4048978323372205665L;

  private static final int FIELD = 0;
  private static final int REQUIRED = 1;
  private static final int PLACE_HOLDER = 2;
  private static final int TYPE = 3;
  private static final int DEFAULT_VALUE = 4;
  private static final int VISIBILITY_RULE = 5;
  private static final int EDITABILITY_RULE = 6;

  private RequireRuleComponentPart requireRule;
  private DefaultValue defaultValue;

  private ColumnReference field;
  private DateType type;

  @Override
  public int getParametersNumber() {
    return 7;
  }

  @Override
  protected void initLazyFields() {
    super.initLazyFields();
    field = parseColumnReferenceInitTerm(getParameter(FIELD));
    type = ComponentParser.convertDateType(getTypeStr());
    defaultValue = parseDefaultValue(getDefaultValueStr());
  }

  @Override
  public GenerateFieldType getGenerateFieldType() {
    if (getType() == DateType.DATE)
      return GenerateFieldType.DATE;
    else
      return GenerateFieldType.TIMESTAMP_WITHOUT_TIME_ZONE;
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

  public String getPlaceHolder() {
    return getParameter(PLACE_HOLDER);
  }

  @NotNull
  public String getTypeStr() {
    return getParameter(TYPE);
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

  public DateType getType() {
    ensureFullyInitialized();
    return type;
  }

  public DefaultValue getDefaultValue() {
    ensureFullyInitialized();
    return defaultValue;
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
    List<String> constraintCodes = new ArrayList<String>(2);

    if (isValueTypeNotValid())
      constraintCodes.add("DateTimePicker.invalidType");
    if (!isDefaultValueValid())
      constraintCodes.add("DateTimePicker.invalidDefaultValueType");

    return constraintCodes;
  }

  private boolean isValueTypeNotValid() {
    return getTypeStr() != null && getType() == null;
  }

  private boolean isDefaultValueValid() {
    boolean hasAllParameters = getType() != null && getDefaultValue() != null && getDefaultValue().getType() != DefaultValueType.NONE;

    boolean isValid = true;
    if (hasAllParameters && getDefaultValue().getType() == DefaultValueType.MANUAL) {
      SimpleDateFormat format;
      String defaultValue = getDefaultValue().getValue();
      switch (getType()) {
        case DATE:
          format = DateFormat.getDateFormat();
          break;
        case DATE_TIME:
          format = DateFormat.getDateTimeFormat();
          break;
        default:
          format = null;
      }
      format.setLenient(false);
      try {
        Date date = format.parse(defaultValue);
        if (!format.format(date).equals(defaultValue))
          isValid = false;
      }
      catch (ParseException e) {
        isValid = false;
      }
    }

    return isValid;
  }
}
