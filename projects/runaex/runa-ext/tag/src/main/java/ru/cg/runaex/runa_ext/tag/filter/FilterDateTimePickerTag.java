/*
 * Copyright (c) 2012.
 *
 * Class: FilterDateTimePickerTag
 * Last modified: 06.09.12 10:31
 *
 * Author: Sabirov
 * Company Center
 */

package ru.cg.runaex.runa_ext.tag.filter;

import freemarker.template.TemplateModelException;
import org.apache.ecs.html.IMG;
import org.apache.ecs.html.Input;
import ru.runa.wfe.security.AuthenticationException;
import ru.runa.wfe.security.AuthorizationException;
import ru.runa.wfe.task.TaskDoesNotExistException;

import ru.cg.runaex.components.WfeRunaVariables;
import ru.cg.runaex.components.bean.component.ComponentType;
import ru.cg.runaex.components.bean.component.filter.FilterDateTimePicker;
import ru.cg.runaex.components.bean.component.part.DateType;
import ru.cg.runaex.database.bean.transport.TransportData;
import ru.cg.runaex.runa_ext.tag.BaseEditableFreemarkerTag;

public class FilterDateTimePickerTag extends BaseEditableFreemarkerTag<FilterDateTimePicker> {
  private static final long serialVersionUID = 5414033691847897433L;

  @Override
  protected ComponentType getComponentType() {
    return ComponentType.FILTER_DATE_TIME_PICKER;
  }

  @Override
  protected String executeToHtml(FilterDateTimePicker component) throws TemplateModelException, AuthorizationException, AuthenticationException, TaskDoesNotExistException {
    logger.debug("schema - " + component.getSchema());
    logger.debug("table - " + component.getTable());
    logger.debug("field - " + component.getField());
    logger.debug("tableId - " + component.getTableId());
    logger.debug("comparison - " + component.getComparison());

    String schema = component.getSchema();

    initObjectInfo(schema, component.getTable());

    String date = null;
    String filterField = component.getField() + "_" + component.getComparisonStr();

    if (component.getTableId() != null) {
      String filterKey = WfeRunaVariables.getFilterKeyVariable(component.getTableId());
      TransportData transportData = getVariableFromDb(getProcessInstanceId(), filterKey);
      logger.debug("filter transportFilterData - " + transportData);
      date = getValue(String.class, transportData, filterField, component.getDefaultValue());
    }

    Input input = new Input(Input.TEXT);
    String filterId = "filter_date_time_picker_" + System.nanoTime();
    input.setID(filterId);
    input.setName(filterField);
    input.setReadOnly(true);
    input.setClass("runaex sdtpt-datetimepicker");
    input.addAttribute("data-schema", schema);
    input.addAttribute("data-table", component.getTable());
    input.addAttribute("data-tableId", component.getTableId());
    if (date != null) {
      input.setValue(date);
    }
    if (component.getPlaceHolder() != null) {
      input.addAttribute("placeholder", component.getPlaceHolder());
    }

    StringBuilder scr = new StringBuilder();
    scr.append("resources/images/datetime/cal.gif");
    IMG img = new IMG(scr.toString());
    img.setClass("runaex sdtpt-datetimepicker");
    boolean isDateTime = component.getType() == DateType.DATE_TIME;
    img.setOnClick("javascript:NewCssCal('','" + filterId + "','ddMMyyyy','dropdown'," + isDateTime + ",'24',true)");

    if (!isEditable) {
      input.addAttribute("disabled", "");
      img.addAttribute("disabled", "");
    }

    StringBuilder html = new StringBuilder();
    appendComponentCssReference(DateTimePickerTag, html);
    appendComponentJsReference(DateTimePickerTag, html);

    html.append(getHtmlWithValidation(new WrapComponent(null, input, img)));

    return html.toString();
  }
}
