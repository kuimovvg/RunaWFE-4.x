package ru.cg.runaex.runa_ext.tag.template.exception;

/**
 * @author Абдулин Ильдар
 */
public class TemplateNotFoundException extends RuntimeException {

  public TemplateNotFoundException() {
  }

  public TemplateNotFoundException(String message) {
    super(message);
  }

  public TemplateNotFoundException(String message, Throwable cause) {
    super(message, cause);
  }

  public TemplateNotFoundException(Throwable cause) {
    super(cause);
  }
}
