package ru.cg.runaex.components.bean.component;

import ru.cg.runaex.components.validation.annotation.AssertNumber;

/**
 * @author golovlyev
 */
public class FileView extends BaseFile {

  private static final long serialVersionUID = 6498681835544851118L;

  private static final int FIELD = 0;
  private static final int IMAGE_WIDTH = 1;
  private static final int SIGN_COLUMN_NAME = 2;
  private static final int DEFAULT_VALUE = 3;
  private static final int VISIBILITY_RULE = 4;

  @Override
  public int getParametersNumber() {
    return 5;
  }

  @AssertNumber(allowFractional = false)
  public String getImageWidth() {
    return getParameter(IMAGE_WIDTH);
  }

  @Override
  protected int getVisibilityRuleParameterIndex() {
    return VISIBILITY_RULE;
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
