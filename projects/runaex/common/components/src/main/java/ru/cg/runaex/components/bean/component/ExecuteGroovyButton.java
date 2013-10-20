package ru.cg.runaex.components.bean.component;

import javax.validation.constraints.NotNull;

import ru.cg.runaex.components.validation.annotation.GroovyScriptSyntax;

/**
 * @author Kochetkov
 */
public class ExecuteGroovyButton extends Component {
  private static final long serialVersionUID = 2272871521395891094L;

  private static final int NAME = 0;
  private static final int GROOVY_SCRIPT = 1;
  private static final int VISIBILITY_RULE = 2;

  @Override
  public int getParametersNumber() {
    return 3;
  }

  @NotNull
  public String getName() {
    return getParameter(NAME);
  }

  @NotNull
  @GroovyScriptSyntax
  public String getGroovyScript() {
    return getGroovyScriptParameter(GROOVY_SCRIPT);
  }

  @Override
  protected int getVisibilityRuleParameterIndex() {
    return VISIBILITY_RULE;
  }
}
