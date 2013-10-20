package ru.cg.runaex.components.bean.component.part;

import java.io.Serializable;
import javax.validation.constraints.NotNull;

import ru.cg.runaex.components.validation.NotNullChecks;

/**
 * @author Kochetkov
 */
@ru.cg.runaex.components.validation.annotation.DefaultValue
public class DefaultValue implements Serializable {
  private static final long serialVersionUID = -1335829536729690837L;

  @NotNull(groups = NotNullChecks.class)
  private DefaultValueType type;

  @NotNull(groups = NotNullChecks.class)
  private String value;

  public DefaultValue(DefaultValueType type, String value) {
    this.type = type;
    this.value = value;
  }

  public DefaultValueType getType() {
    return type;
  }

  public void setType(DefaultValueType type) {
    this.type = type;
  }

  public String getValue() {
    return value;
  }

  public void setValue(String defaultValue) {
    this.value = defaultValue;
  }

  @Override
  public String toString() {
    final StringBuilder sb = new StringBuilder();
    sb.append(type);
    sb.append(".").append(value);
    return sb.toString();
  }
}
