/*
 * Copyright (c) 2012.
 *
 * Class: FilterCheckBoxTag
 * Last modified: 06.09.12 10:31
 *
 * Author: Sabirov
 * Company Center
 */

package ru.cg.runaex.runa_ext.tag.filter;

import freemarker.template.TemplateModelException;
import org.apache.ecs.html.Input;
import ru.runa.wfe.security.AuthenticationException;
import ru.runa.wfe.security.AuthorizationException;
import ru.runa.wfe.task.TaskDoesNotExistException;

import ru.cg.runaex.components.WfeRunaVariables;
import ru.cg.runaex.components.bean.component.ComponentType;
import ru.cg.runaex.components.bean.component.filter.FilterCheckbox;
import ru.cg.runaex.database.bean.transport.TransportData;
import ru.cg.runaex.runa_ext.tag.BaseEditableFreemarkerTag;

/**
 * @author Sabirov
 */
public class FilterCheckBoxTag extends BaseEditableFreemarkerTag<FilterCheckbox> {
  private static final long serialVersionUID = 3453594863082575030L;

  @Override
  protected ComponentType getComponentType() {
    return ComponentType.FILTER_CHECKBOX;
  }

  @Override
  protected String executeToHtml(FilterCheckbox component) throws TemplateModelException, AuthorizationException, AuthenticationException, TaskDoesNotExistException {
    logger.debug("schema - " + component.getSchema());
    logger.debug("table - " + component.getTable());
    logger.debug("field - " + component.getField());
    logger.debug("tableId - " + component.getTableId());

    String schema = component.getSchema();

    initObjectInfo(schema, component.getTable());

    Boolean selected = false;

    if (component.getTableId() != null) {
      String filterKey = WfeRunaVariables.getFilterKeyVariable(component.getTableId());
      TransportData transportData = getVariableFromDb(getProcessInstanceId(), filterKey);
      logger.debug("filter transportFilterData - " + transportData);
      selected = getValue(Boolean.class, transportData, component.getField(), "checked".equals(component.getDefaultValue()));
    }

    Input input = new Input(Input.CHECKBOX);
    input.setClass("runaex schkbx-checkbox");
    input.setID("filter_checkbox_" + System.nanoTime());
    input.setName(component.getField());
    input.addAttribute("data-schema", schema);
    input.addAttribute("data-table", component.getTable());
    input.addAttribute("data-tableId", component.getTableId());
    if (selected != null && Boolean.TRUE.equals(selected)) {
      input.setChecked(true);
    }

    if (!isEditable) {
      input.addAttribute("disabled", "");
    }

    return getHtmlWithValidation(new WrapComponent(null, input));
  }
}
