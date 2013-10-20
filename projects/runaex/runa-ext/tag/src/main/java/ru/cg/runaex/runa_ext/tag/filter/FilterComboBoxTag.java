/*
 * Copyright (c) 2012.
 *
 * Class: FilterComboBoxTag
 * Last modified: 06.09.12 10:31
 *
 * Author: Sabirov
 * Company Center
 */

package ru.cg.runaex.runa_ext.tag.filter;

import java.util.Arrays;

import freemarker.template.TemplateModelException;
import org.apache.ecs.html.Input;
import org.apache.ecs.html.Option;
import org.apache.ecs.html.Select;
import ru.runa.wfe.security.AuthenticationException;
import ru.runa.wfe.security.AuthorizationException;
import ru.runa.wfe.task.TaskDoesNotExistException;

import ru.cg.runaex.components.WfeRunaVariables;
import ru.cg.runaex.components.bean.component.ComponentType;
import ru.cg.runaex.components.bean.component.filter.FilterComboBox;
import ru.cg.runaex.database.bean.transport.Data;
import ru.cg.runaex.database.bean.transport.TransportData;
import ru.cg.runaex.database.bean.transport.TransportDataSet;
import ru.cg.runaex.runa_ext.tag.BaseEditableFreemarkerTag;

/**
 * @author Sabirov
 */
public class FilterComboBoxTag extends BaseEditableFreemarkerTag<FilterComboBox> {
  private static final long serialVersionUID = 8374182724291930501L;

  @Override
  protected ComponentType getComponentType() {
    return ComponentType.FILTER_COMBO_BOX;
  }

  @Override
  protected String executeToHtml(FilterComboBox component) throws TemplateModelException, AuthorizationException, AuthenticationException, TaskDoesNotExistException {
    logger.debug("schema - " + component.getSchema());
    logger.debug("table - " + component.getTable());
    logger.debug("field - " + component.getField());
    logger.debug("references - " + component.getColumnReference());
    logger.debug("tableId - " + component.getTableId());

    String schema = component.getSchema();
    String sortOrder = component.getSortOrder();
    if (sortOrder == null) {
      sortOrder = "asc";
    }
    String comboSchema = component.getColumnReference().getSchema();
    String comboTable = component.getColumnReference().getTable();
    String comboField = component.getColumnReference().getColumn();
    String comboId = comboTable + "_id"; //todo replace with constant

    initObjectInfo(schema, component.getTable());

    /**
     * Selected id in base table
     */
    Long selectId = null;
    if (component.getTableId() != null) {
      String filterKey = WfeRunaVariables.getFilterKeyVariable(component.getTableId());
      TransportData transportData = getVariableFromDb(getProcessInstanceId(), filterKey);
      logger.debug("filter transportFilterData - " + transportData);
      selectId = getValue(transportData, component.getField());
    }

    TransportDataSet transportDataSet = getDataFromDB(comboSchema, comboTable, Arrays.asList(comboId, comboField), comboField, sortOrder);


    Select select = new Select();
    select.setClass("runaex select speech");
    String fieldId = "filter_combobox_" + System.nanoTime();
    select.setID(fieldId);
    select.setName(component.getField());
    select.addAttribute("data-schema", schema);
    select.addAttribute("data-table", component.getTable());
    select.addAttribute("data-tableId", component.getTableId());

    Option emptyOption = new Option();
    emptyOption.setClass("runaex select-option");
    emptyOption.setValue("");
    select.addElement(emptyOption);

    Option option;
    boolean nonEmptyOptionSelected = false;
    for (TransportData transportData : transportDataSet.getSortSets()) {
      Data dataId = transportData.getData(comboId);
      Data dataValue = transportData.getData(comboField);

      logger.debug("dataId.getValue() - " + dataId.getValue());
      logger.debug("dataId.getValueClass() - " + dataId.getValueClass());
      logger.debug("dataId.getField() - " + dataId.getField());

      Long id = (Long) dataId.getValue();
      String name = (String) dataValue.getValue();

      option = new Option();
      option.setClass("runaex select-option");
      option.setValue(String.valueOf(id));
      if (selectId != null && selectId.equals(id) || selectId == null && name.equals(component.getDefaultValue())) {
        option.setSelected(true);
        nonEmptyOptionSelected = true;
      }
      option.setTagText("" + dataValue.getValue());
      select.addElement(option);

    }

    if (!nonEmptyOptionSelected) {
      emptyOption.setSelected(true);
    }

    Input speechInput = null;
    if (component.isSpeechInputEnabled()) {
      speechInput = new Input();
      speechInput.setID(fieldId.concat("-speech"));
      speechInput.setClass("runaex combobox-speech");
      speechInput.addAttribute("speech", "");
      speechInput.addAttribute("x-webkit-speech", "");
    }

    if (!isEditable) {
      select.addAttribute("disabled", "");
      if (speechInput != null) {
        speechInput.addAttribute("disabled", "");
      }
    }

    StringBuilder html = new StringBuilder();

    if (component.isSpeechInputEnabled()) {
      select.addAttribute("speech_recognition_element", "true");
      appendJsReference(SpeechRecognition, html);
    }

    appendComponentCssReference(ComboBoxTag, html);
    html.append(getHtmlWithValidation(new WrapComponent(null, select, speechInput)));

    if (component.isSpeechInputEnabled()) {
      setJsTemplateName(ComboBoxTag);
      addObjectToJs("fieldId", fieldId);
    }

    return html.toString();
  }

  private Long getValue(TransportData transportData, String field) throws TemplateModelException {
    Long selectId = null;
    logger.debug("transportData - " + transportData);
    if (transportData != null) {
      Data data = transportData.getData(field);
      logger.debug("data - " + data);
      if (data == null) {
        return null;
      }
      Object value = data.getValue();
      if (value instanceof Long) {
        selectId = (Long) data.getValue();
      }
      else if (value instanceof String && !((String) value).isEmpty()) {
        try {
          selectId = Long.valueOf(((String) value).trim());
        }
        catch (NumberFormatException ex) {
          logger.error(ex.toString(), ex);
          throw new TemplateModelException(ex.toString(), ex);
        }
      }
      logger.debug("selectId - " + selectId);
    }
    return selectId;
  }

}
