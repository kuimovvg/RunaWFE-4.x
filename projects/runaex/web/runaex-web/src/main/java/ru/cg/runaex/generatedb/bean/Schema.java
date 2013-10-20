package ru.cg.runaex.generatedb.bean;

import java.io.Serializable;
import java.text.MessageFormat;

import ru.cg.runaex.generatedb.GenerateDBImpl;


/**
 * @author Sabirov
 */
public class Schema extends GenerateDBImpl implements Serializable {
  private static final long serialVersionUID = -8659417965479861081L;

  private String name;
  private boolean isGenerate = true;

  public Schema() {
  }

  public Schema(String name) {
    this.name = name;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public boolean isGenerate() {
    return isGenerate;
  }

  public void setGenerate(boolean generate) {
    isGenerate = generate;
  }

  public boolean equals(Object obj) {
    if (this == obj)
      return true;
    if ((obj == null) || (obj.getClass() != this.getClass()))
      return false;

    Schema schema = (Schema) obj;
    return (getName() != null && getName().equals(schema.getName()));
  }

  @Override
  public int hashCode() {
    int hash = 7;
    hash = 31 * hash + name.hashCode();
    return hash;
  }

  @Override
  public String toString() {
    return "Schema - "+getName()+ ". ";
  }

  @Override
  public String getSQL() {
    if(!this.isGenerate()) {
      return "";
    }
    String template = super.getSQL();
    return MessageFormat.format(template, getName());
  }


  /**
   * Check empty field by name
   *
   * @return true if field name is empty
   */
  public boolean isEmpty() {
    return name == null || name.isEmpty();
  }
}
