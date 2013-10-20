package ru.cg.runaex.exceptions;

/**
 * @author Абдулин Ильдар
 */
public class DuplicateColumnWithDifferentType extends RuntimeException {
  private static final long serialVersionUID = -6168911733297239891L;

  public DuplicateColumnWithDifferentType() {
  }

  public DuplicateColumnWithDifferentType(String message) {
    super(message);
  }

  public DuplicateColumnWithDifferentType(String message, Throwable cause) {
    super(message, cause);
  }

  public DuplicateColumnWithDifferentType(Throwable cause) {
    super(cause);
  }
}
