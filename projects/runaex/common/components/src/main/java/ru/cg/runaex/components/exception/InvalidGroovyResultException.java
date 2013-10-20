package ru.cg.runaex.components.exception;

/**
 * @author Петров А.
 */
public class InvalidGroovyResultException extends RuntimeException {

  private static final long serialVersionUID = 1L;

  public InvalidGroovyResultException() {
  }

  public InvalidGroovyResultException(String message) {
    super(message);
  }

  public InvalidGroovyResultException(String message, Throwable cause) {
    super(message, cause);
  }

  public InvalidGroovyResultException(Throwable cause) {
    super(cause);
  }
}
