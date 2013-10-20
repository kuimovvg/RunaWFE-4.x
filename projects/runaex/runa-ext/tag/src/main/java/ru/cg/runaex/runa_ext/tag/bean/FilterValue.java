package ru.cg.runaex.runa_ext.tag.bean;

/**
 * @author Kochetkov
 */
public class FilterValue {
  private String fieldName;
  private String value;

  public String getFieldName() {
    return fieldName;
  }

  public void setFieldName(String column) {
    this.fieldName = column;
  }

  public String getValue() {
    return value;
  }

  public void setValue(String value) {
    this.value = value;
  }
}
