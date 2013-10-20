package ru.cg.runaex.runa_ext.tag;

import freemarker.template.TemplateModelException;

import ru.cg.runaex.components.bean.component.EditableField;
import ru.cg.runaex.runa_ext.tag.field.BaseFieldFreemarkerTag;

/**
 * @author Петров А.
 */
public abstract class BaseEditableFreemarkerTag<C extends EditableField> extends BaseFieldFreemarkerTag<C> {

  private static final long serialVersionUID = 1L;

  protected boolean isEditable = true;

  @Override
  protected void initAdditionalParameters(C component) throws TemplateModelException {
    super.initAdditionalParameters(component);

    String editableRuleGroovyScript = component.getEditabilityRule() != null ? component.getEditabilityRule().getGroovyScript() : null;
    if (editableRuleGroovyScript != null) {
      isEditable = executeGroovyRule(editableRuleGroovyScript);
    }
  }
}
