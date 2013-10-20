/*
 * Copyright (c) 2012.
 *
 * Class: FilterFieldTag
 * Last modified: 06.09.12 10:31
 *
 * Author: Sabirov
 * Company Center
 */

package ru.cg.runaex.runa_ext.tag.filter;

import freemarker.template.TemplateModelException;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.ecs.ConcreteElement;
import org.apache.ecs.html.Input;
import ru.runa.wfe.security.AuthenticationException;
import ru.runa.wfe.security.AuthorizationException;
import ru.runa.wfe.task.TaskDoesNotExistException;

import ru.cg.runaex.components.WfeRunaVariables;
import ru.cg.runaex.components.bean.component.ComponentType;
import ru.cg.runaex.components.bean.component.filter.FilterNumberField;
import ru.cg.runaex.components.bean.component.part.MaskType;
import ru.cg.runaex.database.bean.transport.TransportData;
import ru.cg.runaex.runa_ext.tag.BaseEditableFreemarkerTag;


public class FilterNumberFieldTag extends BaseEditableFreemarkerTag<FilterNumberField> {

  private static final long serialVersionUID = 995563345340393346L;
  private static final Log logger = LogFactory.getLog(FilterNumberFieldTag.class);

  @Override
  protected ComponentType getComponentType() {
    return ComponentType.FILTER_NUMBER_FIELD;
  }

  @Override
  protected String executeToHtml(FilterNumberField component) throws TemplateModelException, AuthorizationException, AuthenticationException, TaskDoesNotExistException {
    logger.debug("schema - " + component.getSchema());
    logger.debug("table - " + component.getTable());
    logger.debug("field - " + component.getField());
    logger.debug("tableId - " + component.getTableId());
    logger.debug("comparison - " + component.getComparison());

    String value = "";
    String schema = component.getSchema();
    String filterField = component.getField() + "_" + component.getComparisonStr();
    initObjectInfo(schema, component.getTable());

    if (component.getTableId() != null) {
      String filterKey = WfeRunaVariables.getFilterKeyVariable(component.getTableId());
      TransportData transportData = getVariableFromDb(getProcessInstanceId(), filterKey);
      logger.debug("filter transportFilterData - " + transportData);
      value = getValue(String.class, transportData, filterField, component.getDefaultValue());
    }

    ConcreteElement input = new Input();
    String fieldId = "filter_number_field_" + System.nanoTime();
    input.setID(fieldId);
    input.setClass("runaex textfield speech");
    ((Input) input).setName(filterField);
    input.addAttribute("data-schema", schema);
    input.addAttribute("data-table", component.getTable());
    input.addAttribute("data-tableId", component.getTableId());
    ((Input) input).setType("number");

    if (value != null) {
      ((Input) input).setValue(value);
    }

    if (component.getPlaceHolder() != null) {
      input.addAttribute("placeholder", component.getPlaceHolder());
    }

    if (component.isSpeechInputEnabled()) {
      input.addAttribute("speech", "");
      input.addAttribute("x-webkit-speech", "");
    }

    if (!isEditable) {
      input.addAttribute("disabled", "");
    }

    StringBuilder html = new StringBuilder();

    if (component.isSpeechInputEnabled()) {
      input.addAttribute("speech_recognition_element", "true");
      appendJsReference(SpeechRecognition, html);
    }

    appendComponentCssReference(TextFieldTag, html);
    appendComponentJsReference(TextFieldTag, html);

    setJsTemplateName(NumberFieldTag);
    addObjectToJs("fieldId", fieldId);
    addObjectToJs("mask",  MaskType.NUMBER.getCode());

    html.append(getHtmlWithValidation(new WrapComponent(null, input)));

    return html.toString();
  }
}