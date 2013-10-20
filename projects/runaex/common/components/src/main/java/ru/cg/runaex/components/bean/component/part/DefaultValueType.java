package ru.cg.runaex.components.bean.component.part;

/**
 * @author urmancheev
 */
public enum DefaultValueType {
  NONE(-1),
  MANUAL(0),
  FROM_DB(1),
  EXECUTE_GROOVY(2);

  private final int value;

  private DefaultValueType(int value) {
    this.value = value;
  }

  public int getValue() {
    return value;
  }
}
