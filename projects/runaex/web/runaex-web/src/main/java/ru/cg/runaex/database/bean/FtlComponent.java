package ru.cg.runaex.database.bean;

import ru.cg.runaex.components.bean.component.ComponentType;
import ru.cg.runaex.components.bean.component.IsComponent;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.io.Serializable;
import java.util.*;

/**
 * @author Петров А.
 */
public class FtlComponent implements Serializable {
  private static final long serialVersionUID = 3558257714245016648L;

  private IsComponent component;
  private String sourceTag;
  private String processName;
  private List<String> sourceForms = new ArrayList<String>();

  public FtlComponent(String processName, IsComponent component, String sourceTag) {
    this.processName = processName;
    this.component = component;
    this.sourceTag = sourceTag;
  }

  public <C extends IsComponent> C getComponent() {
    return (C) component;
  }

  public String getDefaultSchema() {
    return component.getDefaultSchema();
  }

  public ComponentType getComponentType() {
    return component.getComponentType();
  }

  public String getProcessName() {
    return processName;
  }

  public List<String> getSourceForms() {
    return sourceForms;
  }

  public void addForm(@NotNull @Size(min = 1) String form) {
    if (sourceForms == null) {
      sourceForms = new ArrayList<String>();
    }
    sourceForms.add(form);
  }

  public void addForms(@NotNull List<String> forms) {
    this.sourceForms.addAll(forms);
  }

  public String getFormsAsString() {
    StringBuilder sb = new StringBuilder();

    int index = 0;
    for (String form : sourceForms) {
      sb.append(form);
      if (index < sourceForms.size() - 1) {
        sb.append(", ");
      }
      ++index;
    }

    return sb.toString();
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (!(o instanceof FtlComponent)) return false;

    FtlComponent that = (FtlComponent) o;

    String defaultSchema = getDefaultSchema();
    if (defaultSchema != null ? !defaultSchema.equals(that.getDefaultSchema()) : that.getDefaultSchema() != null)
      return false;
    if (!sourceTag.equals(that.sourceTag)) return false;

    return true;
  }

  @Override
  public int hashCode() {
    int result = sourceTag.hashCode();
    result = 31 * result + (getDefaultSchema() != null ? getDefaultSchema().hashCode() : 0);
    return result;
  }
}
