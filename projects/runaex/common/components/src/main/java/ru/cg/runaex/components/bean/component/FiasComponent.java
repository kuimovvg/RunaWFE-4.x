package ru.cg.runaex.components.bean.component;

import ru.cg.runaex.components.bean.component.part.FiasObjectLevel;

/**
 * @author urmancheev
 */
public interface FiasComponent extends EditableField, ComponentWithSingleField {

  String getSchema();

  String getTable();

  @Override
  String getField();

  String getDefaultFilter();

  FiasObjectLevel getMinObjectLevel();

  FiasObjectLevel getMaxObjectLevel();

  boolean getUsageActual();

}
