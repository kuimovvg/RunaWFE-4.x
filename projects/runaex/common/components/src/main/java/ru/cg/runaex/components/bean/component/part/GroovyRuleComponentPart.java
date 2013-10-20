package ru.cg.runaex.components.bean.component.part;

import java.io.Serializable;

/**
 * @author Петров А.
 */
public class GroovyRuleComponentPart implements Serializable, Cloneable {

  public static final GroovyRuleComponentPart ALWAYS_TRUE_RULE = new GroovyRuleComponentPart("return true;");

  private static final long serialVersionUID = 1L;

  protected String groovyScript;

  public GroovyRuleComponentPart(String groovyScript) {
    this.groovyScript = groovyScript;
  }

  public String getGroovyScript() {
    return groovyScript;
  }

  public void setGroovyScript(String groovyScript) {
    this.groovyScript = groovyScript;
  }

  @Override
  protected GroovyRuleComponentPart clone() throws CloneNotSupportedException {
    GroovyRuleComponentPart clone = (GroovyRuleComponentPart) super.clone();
    clone.setGroovyScript(groovyScript);
    return clone;
  }

  @Override
  public String toString() {
    return String.valueOf(groovyScript);
  }
}
