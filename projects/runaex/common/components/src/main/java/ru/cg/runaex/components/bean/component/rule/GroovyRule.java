package ru.cg.runaex.components.bean.component.rule;

import ru.cg.runaex.components.bean.component.Component;
import ru.cg.runaex.components.bean.component.part.VisibilityRuleComponentPart;

/**
 * @author Петров А.
 */
public class GroovyRule extends Component {

  private static final long serialVersionUID = 1L;

  @Override
  public int getParametersNumber() {
    return 1;
  }

  public String getGroovyScript() {
    return getParameter(0);
  }

  @Override
  public VisibilityRuleComponentPart getVisibilityRule() {
    return VisibilityRuleComponentPart.ALWAYS_TRUE_RULE;
  }

  @Override
  protected int getVisibilityRuleParameterIndex() {
    return -1;
  }
}
