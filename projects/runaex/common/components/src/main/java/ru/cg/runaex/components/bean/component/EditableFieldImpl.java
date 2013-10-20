package ru.cg.runaex.components.bean.component;

import ru.cg.runaex.components.bean.component.part.EditabilityRuleComponentPart;

/**
 * @author Петров А.
 */
public abstract class EditableFieldImpl extends Component implements EditableField {

  private static final long serialVersionUID = 1L;

  private EditabilityRuleComponentPart editabilityRule;

  @Override
  protected void initLazyFields() {
    super.initLazyFields();

    editabilityRule = parseEditabilityRule(getParameter(getEditabilityRuleParameterIndex()));
  }

  protected abstract int getEditabilityRuleParameterIndex();

  @Override
  public EditabilityRuleComponentPart getEditabilityRule() {
    ensureFullyInitialized();
    return editabilityRule;
  }
}
