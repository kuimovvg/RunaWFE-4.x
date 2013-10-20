package ru.cg.runaex.components.bean.component;

import ru.cg.runaex.components.bean.component.part.GroovyRuleComponentPart;

/**
 * @author Петров А.
 */
public interface EditableField extends IsComponent {

  GroovyRuleComponentPart getEditabilityRule();
}
