package ru.cg.runaex.components.bean.component.grid;

import javax.validation.constraints.NotNull;

/**
 * @author urmancheev
 */
public class TreeGrid extends BaseTree {
  private static final long serialVersionUID = 3777165254144944707L;

  private static final int TABLE_ID = 2;

  @Override
  protected void initLazyFields() {
    super.initLazyFields();
  }

  @NotNull
  public String getTableId() {
    return getParameter(TABLE_ID);
  }
}
