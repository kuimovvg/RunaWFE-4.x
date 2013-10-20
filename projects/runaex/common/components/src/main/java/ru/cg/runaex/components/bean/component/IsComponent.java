package ru.cg.runaex.components.bean.component;

import java.io.Serializable;

import ru.cg.runaex.components.bean.component.part.GroovyRuleComponentPart;

/**
 * @author urmancheev
 */
public interface IsComponent extends Serializable {
  String getDefaultSchema();

  ComponentType getComponentType();

  String getComponentName();

  GroovyRuleComponentPart getVisibilityRule();
}
