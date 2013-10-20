package ru.cg.runaex.components.bean.component.field;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import javax.validation.constraints.NotNull;

import ru.cg.runaex.components.bean.component.ComponentWithSingleField;
import ru.cg.runaex.components.bean.component.EditableFieldImpl;
import ru.cg.runaex.components.bean.component.part.*;
import ru.cg.runaex.components.parser.ComponentParser;
import ru.cg.runaex.components.validation.ComponentWithCustomValidation;
import ru.cg.runaex.components.validation.annotation.AssertNumber;
import ru.cg.runaex.components.validation.annotation.DatabaseStructureElement;
import ru.cg.runaex.components.validation.annotation.NotNullSchema;
import ru.cg.runaex.components.validation.annotation.SpeechInput;

/**
 * @author korablev
 */
public class NumberField extends EditableFieldImpl implements ComponentWithSingleField, ComponentWithCustomValidation {
  private static final long serialVersionUID = -4800786303756678414L;

  private static final int FIELD = 0;
  private static final int MAX_VALUE = 1;
  private static final int REQUIRED = 2;
  private static final int PLACE_HOLDER = 3;
  private static final int SPEECH_INPUT = 4;
  private static final int DEFAULT_VALUE = 5;
  private static final int MASK_TYPE = 6;
  private static final int VISIBILITY_RULE = 7;
  private static final int EDITABILITY_RULE = 8;

  private RequireRuleComponentPart requireRule;
  private DefaultValue defaultValue;

  private ColumnReference field;
  private boolean speechInputEnabled;

  private MaskType maskType;

  @Override
  public int getParametersNumber() {
    return 9;
  }

  @Override
  protected void initLazyFields() {
    super.initLazyFields();

    field = parseColumnReferenceInitTerm(getParameter(FIELD));
    speechInputEnabled = convertEnableSpeechInput(getSpeechInput());
    defaultValue = parseDefaultValue(getDefaultValueStr());
    maskType = ComponentParser.convertMaskType(getParameter(MASK_TYPE));
    if (maskType == null)
      maskType = MaskType.NONE;
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

  @SpeechInput
  public String getSpeechInput() {
    return getParameter(SPEECH_INPUT);
  }

  public String getDefaultValueStr() {
    return getParameter(DEFAULT_VALUE);
  }

  @AssertNumber(allowFractional = false)
  public String getMaxValueStr() {
    return getParameter(MAX_VALUE);
  }

  public Long getMaxValue() {
    if (getMaxValueStr() != null)
      return Long.valueOf(getMaxValueStr());
    return null;
  }

  @NotNull
  public RequireRuleComponentPart getRequireRule() {
    if (requireRule == null)
      requireRule = parseRequireRule(getParameter(REQUIRED));
    return requireRule;
  }

  public MaskType getMaskType() {
    ensureFullyInitialized();
    return maskType;
  }

  public boolean isSpeechInputEnabled() {
    ensureFullyInitialized();
    return speechInputEnabled;
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

  public List<String> customValidate() {
    List<String> errorCodes = new ArrayList<String>(2);

    if (getDefaultValue() != null && getDefaultValue().getType() == DefaultValueType.MANUAL) {
      try {
        new BigDecimal(getDefaultValue().getValue());
      } catch (NumberFormatException ex) {
        errorCodes.add("NumberField.invalidDefaultValueType");
      }
    }

    if (getParameter(MASK_TYPE) != null && getMaskType() == null)
      errorCodes.add("NumberField.invalidMaskType");

    return errorCodes;
  }
}
