package ru.cg.runaex.components.bean.component.field;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import javax.validation.constraints.NotNull;

import ru.cg.runaex.components.bean.component.ComponentWithSingleField;
import ru.cg.runaex.components.bean.component.EditableFieldImpl;
import ru.cg.runaex.components.bean.component.part.ColumnReference;
import ru.cg.runaex.components.bean.component.part.DefaultValue;
import ru.cg.runaex.components.bean.component.part.*;
import ru.cg.runaex.components.parser.ComponentParser;
import ru.cg.runaex.components.validation.ComponentWithCustomValidation;
import ru.cg.runaex.components.validation.ValidationConstants;
import ru.cg.runaex.components.validation.annotation.*;

/**
 * @author urmancheev
 */
public class TextField extends EditableFieldImpl implements ComponentWithSingleField, ComponentWithCustomValidation {
  private static final long serialVersionUID = -2619663330826186742L;

  private static final int FIELD = 0;
  private static final int REGEX = 1;
  private static final int LENGTH = 2;
  private static final int REQUIRED = 3;
  private static final int PLACE_HOLDER = 4;
  private static final int TYPE = 5;
  private static final int SPEECH_INPUT = 6;
  private static final int DEFAULT_VALUE = 7;
  private static final int MASK_TYPE = 8;
  private static final int MASK = 9;
  private static final int VISIBILITY_RULE = 10;
  private static final int EDITABILITY_RULE = 11;

  private RequireRuleComponentPart requireRule;

  private Integer length;
  private DefaultValue defaultValue;

  private ColumnReference field;
  private boolean speechInputEnabled;
  private TextType type;


  private MaskType maskType;

  @Override
  public int getParametersNumber() {
    return 12;
  }

  @Override
  protected void initLazyFields() {
    super.initLazyFields();

    field = parseColumnReferenceInitTerm(getParameter(FIELD));
    speechInputEnabled = convertEnableSpeechInput(getSpeechInput());
    type = ComponentParser.convertTextType(getTypeStr());
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

  @Regex
  public String getRegex() {
    return getParameter(REGEX);
  }

  public String getPlaceHolder() {
    return getParameter(PLACE_HOLDER);
  }

  @NotNull
  public String getTypeStr() {
    return getParameter(TYPE);
  }

  @SpeechInput
  public String getSpeechInput() {
    return getParameter(SPEECH_INPUT);
  }

  public String getDefaultValueStr() {
    return getParameter(DEFAULT_VALUE);
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

  public MaskType getMaskType() {
    ensureFullyInitialized();
    return maskType;
  }

  public String getMask() {
    return getParameter(MASK);
  }

  public TextType getType() {
    ensureFullyInitialized();
    return type;
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
    List<String> errorCodes = new ArrayList<String>(4);

    if (isValueTypeNotValid())
      errorCodes.add("TextField.invalidType");
    if (!isDefaultValueValid())
      errorCodes.add("TextField.invalidDefaultValueType");

    if (MaskType.MANUAL == getMaskType() && getMask() == null)
      errorCodes.add("TextField.invalidMask");

    if (getParameter(MASK_TYPE) != null && getMaskType() == null)
      errorCodes.add("TextField.invalidMaskType");

    // при выборе email можно применять только маску с типом вручную
    if (TextType.EMAIL == getType() && getMaskType() != MaskType.MANUAL && getMaskType() != MaskType.NONE) {
      errorCodes.add("TextField.invalidMaskForEmail");
    }

    return errorCodes;
  }

  private boolean isValueTypeNotValid() {
    return getTypeStr() != null && getType() == null;
  }

  private boolean isDefaultValueValid() {
    boolean hasAllParameters = getType() != null && getDefaultValue() != null && getDefaultValue().getType() != DefaultValueType.NONE;

    boolean isValid = true;
    if (hasAllParameters && getDefaultValue().getType() == DefaultValueType.MANUAL) {
      if (getType() == TextType.EMAIL) {
        Matcher emailMatcher = ValidationConstants.EMAIL_PATTERN.matcher(getDefaultValueStr());
        if (!emailMatcher.matches())
          isValid = false;
      }
    }

    return isValid;
  }
}
