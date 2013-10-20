package ru.cg.runaex.exceptions;

/**
 * @author Абдулин Ильдар
 */
public class EditableTreeGridException extends RuntimeException {

  private static final long serialVersionUID = 5890502774207685445L;

  public EditableTreeGridException() {
  }

  public EditableTreeGridException(String message) {
    super(message);
  }

  public EditableTreeGridException(String message, Throwable cause) {
    super(message, cause);
  }

  public EditableTreeGridException(Throwable cause) {
    super(cause);
  }
}
