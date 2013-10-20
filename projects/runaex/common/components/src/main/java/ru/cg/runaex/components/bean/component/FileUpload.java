package ru.cg.runaex.components.bean.component;

import javax.validation.constraints.NotNull;

import ru.cg.runaex.components.bean.component.part.EditabilityRuleComponentPart;
import ru.cg.runaex.components.bean.component.part.RequireRuleComponentPart;

/**
 * @author golovlyev
 */
public class FileUpload extends BaseFile implements EditableField {
  private static final long serialVersionUID = -2424136055278655405L;

  private static final int FIELD = 0;
  private static final int REQUIRED = 1;
  private static final int SIGN_COLUMN_NAME = 2;
  private static final int DEFAULT_VALUE = 3;
  private static final int VISIBILITY_RULE = 4;
  private static final int EDITABILITY_RULE = 5;

  private RequireRuleComponentPart requireRule;
  private EditabilityRuleComponentPart editabilityRule;

  @Override
  public int getParametersNumber() {
    return 6;
  }

  @Override
  protected void initLazyFields() {
    super.initLazyFields();

    requireRule = parseRequireRule(getParameter(REQUIRED));
    editabilityRule = parseEditabilityRule(getParameter(EDITABILITY_RULE));
  }

  @Override
  protected int getVisibilityRuleParameterIndex() {
    return VISIBILITY_RULE;
  }

  @NotNull
  public RequireRuleComponentPart getRequireRule() {
    ensureFullyInitialized();
    return requireRule;
  }

  @Override
  public EditabilityRuleComponentPart getEditabilityRule() {
    ensureFullyInitialized();
    return editabilityRule;
  }

  @Override
  protected int getFieldParameterIndex() {
    return FIELD;
  }

  @Override
  protected int getSignColumnNameParameterIndex() {
    return SIGN_COLUMN_NAME;
  }

  @Override
  protected int getDefaultValueParameterIndex() {
    return DEFAULT_VALUE;
  }
}
