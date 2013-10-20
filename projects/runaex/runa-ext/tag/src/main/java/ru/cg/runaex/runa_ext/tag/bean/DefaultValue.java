package ru.cg.runaex.runa_ext.tag.bean;

/**
 * @author urmancheev
 */
public class DefaultValue {
  private final Long id;
  private final String value;

  public DefaultValue(Long id, String value) {
    this.id = id;
    this.value = value;
  }

  public Long getId() {
    return id;
  }

  public String getValue() {
    return value;
  }
}
