package ru.cg.runaex.components.bean.component;

import java.util.ArrayList;
import java.util.List;
import javax.validation.constraints.NotNull;

import ru.cg.runaex.components.validation.ComponentWithCustomValidation;

/**
 * @author korablev
 */
public class HelpButton extends Component implements ComponentWithCustomValidation {
  private static final long serialVersionUID = -8674983927396820877L;

  private static final int NAME = 0;
  private static final int TEXT = 1;
  private static final int VISIBILITY_RULE = 2;
  private static final int WIDTH = 3;

  private Integer width;

  @Override
  public int getParametersNumber() {
    return 4;
  }

  @NotNull
  public String getName() {
    return getParameter(NAME);
  }

  @NotNull
  public String getText() {
    return getParameter(TEXT);
  }

  @Override
  protected int getVisibilityRuleParameterIndex() {
    return VISIBILITY_RULE;
  }

  public Integer getWidth() {
    ensureFullyInitialized();
    return width;
  }

  @Override
  protected void initLazyFields() {
    super.initLazyFields();    //TODO: implement this method
    if (getParameter(WIDTH) != null)
      try {
        width = Integer.valueOf(getParameter(WIDTH));
      }
      catch (NullPointerException ex) {

      }

  }

  @Override
  public List<String> customValidate() {
    List<String> constraintCodes = new ArrayList<String>(1);
    if (getParameter(WIDTH) != null)
      try {
        Integer.valueOf(getParameter(WIDTH));
      }
      catch (NumberFormatException ex) {
        constraintCodes.add("HelpButton.widthInvalidSyntax");
      }
    return constraintCodes;
  }
}
