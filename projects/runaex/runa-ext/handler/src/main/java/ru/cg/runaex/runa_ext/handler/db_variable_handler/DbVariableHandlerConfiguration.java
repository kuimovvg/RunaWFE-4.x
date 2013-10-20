package ru.cg.runaex.runa_ext.handler.db_variable_handler;

import java.io.Serializable;
import java.util.List;

/**
 * @author Петров А.
 */
public class DbVariableHandlerConfiguration implements Serializable {

  private static final long serialVersionUID = 1L;

  private List<DbVariableHandlerParameter> parameters;

  public List<DbVariableHandlerParameter> getParameters() {
    return parameters;
  }

  public void setParameters(List<DbVariableHandlerParameter> parameters) {
    this.parameters = parameters;
  }
}
