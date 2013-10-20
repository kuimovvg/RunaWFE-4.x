package ru.cg.runaex.components.bean.component.part;

import ru.cg.runaex.components.GpdRunaConfigComponent;

/**
 * @author Петров А.
 */
public class RequireRuleComponentPart extends GroovyRuleComponentPart {

  private static final long serialVersionUID = 1L;

  private boolean unconditionallyRequired;

  public RequireRuleComponentPart(boolean unconditionallyRequired) {
    super(null);
    this.unconditionallyRequired = unconditionallyRequired;
  }

  public RequireRuleComponentPart(String groovyScript) {
    super(groovyScript);
    this.unconditionallyRequired = false;
  }

  public boolean isUnconditionallyRequired() {
    return unconditionallyRequired;
  }

  public void setUnconditionallyRequired(boolean unconditionallyRequired) {
    this.unconditionallyRequired = unconditionallyRequired;
  }

  @Override
  public String toString() {
    if (groovyScript != null) {
      return groovyScript;
    }

    return unconditionallyRequired ? GpdRunaConfigComponent.REQUIRED : GpdRunaConfigComponent.NOT_REQUIRED;
  }

  @Override
  public RequireRuleComponentPart clone() throws CloneNotSupportedException {
    RequireRuleComponentPart clone = (RequireRuleComponentPart) super.clone();
    clone.setUnconditionallyRequired(unconditionallyRequired);
    return clone;
  }
}
