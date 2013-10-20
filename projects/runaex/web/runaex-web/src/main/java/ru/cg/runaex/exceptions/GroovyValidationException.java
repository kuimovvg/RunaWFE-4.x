package ru.cg.runaex.exceptions;

/**
 * @author golovlyev
 */
public class GroovyValidationException extends Exception {
  private static final long serialVersionUID = -8329271987712443587L;

  public GroovyValidationException() {
  }

  public GroovyValidationException(String message) {
    super(message);
  }

  public GroovyValidationException(String message, Throwable cause) {
    super(message, cause);
  }

  public GroovyValidationException(Throwable cause) {
    super(cause);
  }
}
