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
import org.apache.ecs.html.TextArea;
import ru.runa.wfe.security.AuthenticationException;
import ru.runa.wfe.security.AuthorizationException;
import ru.runa.wfe.task.TaskDoesNotExistException;

import ru.cg.runaex.components.WfeRunaVariables;
import ru.cg.runaex.components.bean.component.ComponentType;
import ru.cg.runaex.components.bean.component.filter.FilterField;
import ru.cg.runaex.components.bean.component.part.TextType;
import ru.cg.runaex.database.bean.transport.TransportData;
import ru.cg.runaex.runa_ext.tag.BaseEditableFreemarkerTag;

/**
 * Text field
 *
 * @author Sabirov
 */
public class FilterFieldTag extends BaseEditableFreemarkerTag<FilterField> {
  private static final long serialVersionUID = -205823550814657911L;
  private static final Log logger = LogFactory.getLog(FilterFieldTag.class);

  @Override
  protected ComponentType getComponentType() {
    return ComponentType.FILTER_FIELD;
  }

  @Override
  protected String executeToHtml(FilterField component) throws TemplateModelException, AuthorizationException, AuthenticationException, TaskDoesNotExistException {
    logger.debug("schema - " + component.getSchema());
    logger.debug("table - " + component.getTable());
    logger.debug("field - " + component.getField());
    logger.debug("type - " + component.getValueType());
    logger.debug("length - " + component.getLength());
    logger.debug("tableId - " + component.getTableId());
    logger.debug("comparison - " + component.getComparison());

    String schema = component.getSchema();

    initObjectInfo(schema, component.getTable());

    String value = "";
    String fieldId = "filter_field_" +System.nanoTime();
    String filterField = component.getField() + "_" + component.getComparisonStr();

    if (component.getTableId() != null) {
      String filterKey = WfeRunaVariables.getFilterKeyVariable(component.getTableId());
      TransportData transportData = getVariableFromDb(getProcessInstanceId(), filterKey);
      logger.debug("filter transportFilterData - " + transportData);
      value = getValue(String.class, transportData, filterField, component.getDefaultValue());
    }

    ConcreteElement input;
    Input textAreaSpeechInput = null;

    if (component.getValueType() != TextType.TEXT_AREA) {
      input = new Input();
      input.setID(fieldId);
      input.setClass("runaex textfield speech");
      ((Input) input).setName(filterField);
      input.addAttribute("data-schema", schema);
      input.addAttribute("data-table", component.getTable());
      input.addAttribute("data-tableId", component.getTableId());
      ((Input) input).setType("text");
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
    }
    else {
      input = new TextArea();
      input.setID(fieldId);
      input.setClass("runaex textfield");
      ((TextArea) input).setName(filterField);
      input.addAttribute("data-schema", schema);
      input.addAttribute("data-table", component.getTable());
      input.addAttribute("data-tableId", component.getTableId());
      if (value != null) {
        input.setTagText(value);
      }
      if (component.getPlaceHolder() != null) {
        input.addAttribute("placeholder", component.getPlaceHolder());
      }

      if (component.isSpeechInputEnabled()) {
        input.setClass("runaex speech");
        textAreaSpeechInput = new Input();
        textAreaSpeechInput.setID(fieldId.concat("-speech"));
        textAreaSpeechInput.setClass("runaex textarea-speech textfield check-error");
        textAreaSpeechInput.addAttribute("speech", "");
        textAreaSpeechInput.addAttribute("x-webkit-speech", "");
      }
    }

    if (!isEditable) {
      input.addAttribute("disabled", "");
      if (textAreaSpeechInput != null) {
        textAreaSpeechInput.addAttribute("disabled", "");
      }
    }

    StringBuilder html = new StringBuilder();

    if (component.isSpeechInputEnabled()) {
      input.addAttribute("speech_recognition_element", "true");
      appendJsReference(SpeechRecognition, html);
    }

    appendComponentCssReference(TextFieldTag, html);
    appendComponentJsReference(TextFieldTag, html);

    html.append(getHtmlWithValidation(new WrapComponent(null, input, textAreaSpeechInput)));

    if (textAreaSpeechInput != null) {
      setJsTemplateName(TextFieldTag);
      addObjectToJs("fieldId", fieldId);
      addObjectToJs("speechAvailable", textAreaSpeechInput != null);
    }
    addObjectToJs("hasMask", false);
    addObjectToJs("mask", "");

    return html.toString();
  }
}