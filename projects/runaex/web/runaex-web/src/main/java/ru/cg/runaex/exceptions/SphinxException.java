package ru.cg.runaex.exceptions;

/**
 * @author Абдулин Ильдар
 */
public class SphinxException extends Exception {

  public SphinxException(String message) {
    super(message);
  }

  public SphinxException(String message, Throwable cause) {
    super(message, cause);
  }
}
