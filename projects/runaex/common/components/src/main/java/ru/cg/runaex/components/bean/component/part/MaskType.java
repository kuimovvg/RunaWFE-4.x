package ru.cg.runaex.components.bean.component.part;

/**
 * Типы готовых маск
 *
 * @author Абдулин Ильдар
 */
public enum MaskType {
  NONE("NONE"),
  MONETARY("MONETARY"),
  MONETARY_EXTENDED("MONETARY_EXTENDED"),
  MANUAL("MANUAL"),
  NUMBER("NUMBER");

  private String code;

  MaskType(String code) {
    this.code = code;
  }

  public String getCode() {
    return code;
  }
}
