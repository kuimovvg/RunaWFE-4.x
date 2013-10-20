package ru.cg.runaex.exceptions;

/**
 * @author urmancheev
 */
public class ProjectParseException extends Exception {
  private static final long serialVersionUID = 5698383423981796286L;

  public ProjectParseException(String message) {
    super(message);
  }

  public ProjectParseException(String message, Throwable cause) {
    super(message, cause);
  }
}
