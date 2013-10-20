package ru.cg.runaex.runa_ext.tag.rule;

import freemarker.template.TemplateModelException;
import ru.runa.wfe.security.AuthenticationException;
import ru.runa.wfe.security.AuthorizationException;
import ru.runa.wfe.task.TaskDoesNotExistException;

import ru.cg.runaex.components.bean.component.ComponentType;
import ru.cg.runaex.components.bean.component.rule.GroovyRule;
import ru.cg.runaex.runa_ext.tag.BaseFreemarkerTag;

/**
 * @author Петров А.
 */
public class GroovyRuleTag<C extends GroovyRule> extends BaseFreemarkerTag<C> {

  private static final long serialVersionUID = 1L;

  @Override
  protected ComponentType getComponentType() {
    return ComponentType.GROOVY_RULE;
  }

  @Override
  protected String executeToHtml(C component) throws TemplateModelException, AuthorizationException, AuthenticationException, TaskDoesNotExistException {
    return Boolean.toString(executeGroovyRule(component.getGroovyScript()));
  }
}
