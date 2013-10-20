package ru.cg.runaex.runa_ext.tag.exception;

import freemarker.template.TemplateModelException;

/**
 * @author Kochetkov
 */
public class DefaultValueGettingException extends TemplateModelException {
  private static final long serialVersionUID = 1109404956129785010L;

  public DefaultValueGettingException(Exception cause) {
    super(cause);
  }
}
