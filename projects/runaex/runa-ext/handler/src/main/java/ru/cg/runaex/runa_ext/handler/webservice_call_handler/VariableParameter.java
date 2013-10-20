package ru.cg.runaex.runa_ext.handler.webservice_call_handler;

/**
 * @author urmancheev
 */
public class VariableParameter implements WebserviceCallParameter {
  private String variableName;
  private String parentElementXpath;

  public VariableParameter(String variableName, String parentElementXpath) {
    this.variableName = variableName;
    this.parentElementXpath = parentElementXpath;
  }

  public String getVariableName() {
    return variableName;
  }

  @Override
  public String getParentElementXpath() {
    return parentElementXpath;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (!(o instanceof VariableParameter)) return false;

    VariableParameter that = (VariableParameter) o;

    if (!parentElementXpath.equals(that.parentElementXpath)) return false;

    return true;
  }

  @Override
  public int hashCode() {
    return parentElementXpath.hashCode();
  }
}
