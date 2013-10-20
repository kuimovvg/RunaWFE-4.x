package ru.cg.runaex.runa_ext.handler.webservice_call_handler;

import ru.cg.runaex.components.bean.component.part.ColumnReference;

/**
 * @author urmancheev
 */
public class DbParameter implements WebserviceCallParameter {
  private ColumnReference columnReference;
  private String parentElementXpath;

  public ColumnReference getColumnReference() {
    return columnReference;
  }

  public void setColumnReference(ColumnReference columnReference) {
    this.columnReference = columnReference;
  }

  @Override
  public String getParentElementXpath() {
    return parentElementXpath;
  }

  public void setParentElementXpath(String parentElementXpath) {
    this.parentElementXpath = parentElementXpath;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (!(o instanceof DbParameter)) return false;

    DbParameter parameter = (DbParameter) o;

    if (!parentElementXpath.equals(parameter.parentElementXpath)) return false;

    return true;
  }

  @Override
  public int hashCode() {
    return parentElementXpath.hashCode();
  }
}
