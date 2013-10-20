package ru.cg.runaex.exceptions;

import ru.cg.runaex.database.bean.FtlComponent;

/**
 * @author Петров А.
 */
public class ComponentException extends RuntimeException {

  private static final long serialVersionUID = 8734960701489806168L;

  protected FtlComponent ftlComponent;

  public ComponentException(FtlComponent ftlComponent) {
    this.ftlComponent = ftlComponent;
  }

  public FtlComponent getFtlComponent() {
    return ftlComponent;
  }
}
