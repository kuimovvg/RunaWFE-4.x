package ru.cg.runaex.runa_ext.tag.template.exception;

/**
 * @author Абдулин Ильдар
 */
public class TemplateProcessException extends RuntimeException {

  public TemplateProcessException() {
  }

  public TemplateProcessException(String message) {
    super(message);
  }

  public TemplateProcessException(String message, Throwable cause) {
    super(message, cause);
  }

  public TemplateProcessException(Throwable cause) {
    super(cause);
  }
}
