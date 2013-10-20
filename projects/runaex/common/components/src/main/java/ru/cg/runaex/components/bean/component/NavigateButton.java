package ru.cg.runaex.components.bean.component;

import java.util.ArrayList;
import java.util.List;
import javax.validation.constraints.NotNull;

import ru.cg.runaex.components.validation.ComponentWithCustomValidation;
import ru.cg.runaex.components.validation.annotation.GroovyScriptSyntax;

/**
 * @author urmancheev
 */
public class NavigateButton extends Component implements ComponentWithCustomValidation {
  private static final long serialVersionUID = -7435663560647344528L;

  private static final int NAME = 0;
  private static final int NEXT_TASK = 1;
  private static final int ACTION = 2;
  private static final int TABLE_ID = 3;
  private static final int GROOVY_SCRIPT = 4;
  private static final int VISIBILITY_RULE = 5;
  private static final int WIDTH = 6;

  private Action action;
  private Integer width;

  @Override
  public int getParametersNumber() {
    return 7;
  }

  @Override
  protected void initLazyFields() {
    super.initLazyFields();

    action = getActionByCode(getActionStr());
    if (getParameter(WIDTH) != null)
      try {
        width = Integer.valueOf(getParameter(WIDTH));
      }
      catch (NumberFormatException ex) {
      }
  }

  @NotNull
  public String getName() {
    return getParameter(NAME);
  }

  @NotNull
  public String getNextTask() {
    return getParameter(NEXT_TASK);
  }

  public String getActionStr() {
    return getParameter(ACTION);
  }

  public String getTableId() {
    return getParameter(TABLE_ID);
  }

  @GroovyScriptSyntax
  public String getGroovyScript() {
    return getGroovyScriptParameter(GROOVY_SCRIPT);
  }

  @NotNull
  public Action getAction() {
    ensureFullyInitialized();
    return action;
  }

  public Integer getWidth() {
    ensureFullyInitialized();
    return width;
  }

  private Action getActionByCode(String code) {
    if ("other".equals(code))
      return Action.OTHER;
    if ("add".equals(code))
      return Action.ADD;
    if ("chg".equals(code))
      return Action.CHANGE;
    return null;
  }

  @Override
  protected int getVisibilityRuleParameterIndex() {
    return VISIBILITY_RULE;
  }

  @Override
  public List<String> customValidate() {
    List<String> constraintCodes = new ArrayList<String>(1);

    if (getParameter(WIDTH) != null)
      try {
        Integer.valueOf(getParameter(WIDTH));
      }
      catch (NumberFormatException ex) {
        constraintCodes.add("NavigationButton.widthInvalidSyntax");
      }

    return constraintCodes;
  }

  public enum Action {
    OTHER,
    ADD,
    CHANGE
  }
}
