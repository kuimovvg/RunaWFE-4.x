package ru.cg.runaex.components.bean.component.part;

/**
 * @author Петров А.
 */
public class VisibilityRuleComponentPart extends GroovyRuleComponentPart {

  public static final VisibilityRuleComponentPart ALWAYS_TRUE_RULE = new VisibilityRuleComponentPart("return true;");
  public static final VisibilityRuleComponentPart ALWAYS_FALSE_RULE = new VisibilityRuleComponentPart("return false;");

  private static final long serialVersionUID = 1L;

  public VisibilityRuleComponentPart(String groovyScript) {
    super(groovyScript);
  }

  @Override
  protected GroovyRuleComponentPart clone() throws CloneNotSupportedException {
    return super.clone();
  }
}
