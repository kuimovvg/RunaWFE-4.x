package ru.cg.runaex.exceptions;

/**
 * @author Bagautdinov
 */
public class BusinessProcessException extends Exception {

  public BusinessProcessException() {
  }

  public BusinessProcessException(String message) {
    super(message);
  }

  public BusinessProcessException(String message, Throwable cause) {
    super(message, cause);
  }

  public BusinessProcessException(Throwable cause) {
    super(cause);
  }
}
