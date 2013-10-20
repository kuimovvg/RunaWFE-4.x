/*
 * Copyright (c) 2012.
 *
 * Class: FilterRadioButtonGroupTag
 * Last modified: 06.09.12 10:31
 *
 * Author: Sabirov
 * Company Center
 */

package ru.cg.runaex.runa_ext.tag.filter;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

import freemarker.template.TemplateModelException;
import org.apache.ecs.html.Input;
import org.apache.ecs.html.Label;
import ru.runa.wfe.security.AuthenticationException;
import ru.runa.wfe.security.AuthorizationException;
import ru.runa.wfe.task.TaskDoesNotExistException;

import ru.cg.runaex.components.WfeRunaVariables;
import ru.cg.runaex.components.bean.component.ComponentType;
import ru.cg.runaex.components.bean.component.filter.FilterRadioButtonGroup;
import ru.cg.runaex.database.bean.transport.Data;
import ru.cg.runaex.database.bean.transport.TransportData;
import ru.cg.runaex.database.bean.transport.TransportDataSet;
import ru.cg.runaex.runa_ext.tag.BaseEditableFreemarkerTag;

/**
 * @author Sabirov
 */
public class FilterRadioButtonGroupTag extends BaseEditableFreemarkerTag<FilterRadioButtonGroup> {
  private static final long serialVersionUID = 8418300870996546468L;

  @Override
  protected ComponentType getComponentType() {
    return ComponentType.FILTER_RADIO_BUTTON_GROUP;
  }

  @Override
  protected String executeToHtml(FilterRadioButtonGroup component) throws TemplateModelException, AuthorizationException, AuthenticationException, TaskDoesNotExistException {
    logger.debug("schema - " + component.getSchema());
    logger.debug("table - " + component.getTable());
    logger.debug("field - " + component.getField());
    logger.debug("references - " + component.getColumnReference());
    logger.debug("tableId - " + component.getTableId());

    String schema = component.getSchema();
    String sortOrder = component.getSortOrder();
    if (sortOrder == null)
      sortOrder = "asc";
    String radioSchema = component.getColumnReference().getSchema();
    String radioTable = component.getColumnReference().getTable();
    String radioField = component.getColumnReference().getColumn();
    String radioId = radioTable + "_id"; //todo use constant

    initObjectInfo(schema, component.getTable());

    /**
     * Selected id in base table
     */
    Long selectedId = null;
    if (component.getTableId() != null) {
      String filterKey = WfeRunaVariables.getFilterKeyVariable(component.getTableId());
      TransportData transportData = getVariableFromDb(getProcessInstanceId(), filterKey);
      logger.debug("filter transportFilterData - " + transportData);
      selectedId = getValue(Long.class, transportData, component.getField(), null);
    }

    TransportDataSet transportDataSet = getDataFromDB(radioSchema, radioTable, Arrays.asList(radioId, radioField), radioField, sortOrder);

    List<WrapComponent> list = new ArrayList<WrapComponent>();

    for (TransportData transportData : transportDataSet.getSortSets()) {
      Data dataId = transportData.getData(radioId);
      Data dataValue = transportData.getData(radioField);

      logger.debug("dataId.getValue() - " + dataId.getValue());
      logger.debug("dataId.getValueClass() - " + dataId.getValueClass());
      logger.debug("dataId.getField() - " + dataId.getField());

      Long id = (Long) dataId.getValue();
      String name = (String) dataValue.getValue();

      Input input = new Input();
      input.setClass("runaex radio");
      input.setType(Input.RADIO);
      input.setName(component.getField());
      input.addAttribute("data-schema", schema);
      input.addAttribute("data-table", component.getTable());
      input.addAttribute("data-tableId", component.getTableId());
      if (id != null) {
        input.setValue(String.valueOf(id));
      }
      if (selectedId != null && selectedId.equals(id) || selectedId == null && name.equals(component.getDefaultValue())) {
        input.setChecked(true);
      }
      Label label = new Label();
      label.setClass("runaex radio-label");
      label.setTagText(dataValue.getValue().toString());

      if (!isEditable) {
        input.addAttribute("disabled", "");
      }

      list.add(new WrapComponent(label, input));
    }

    Input input = new Input();
    input.setClass("runaex radio");
    input.setType(Input.RADIO);
    input.setName(component.getField());
    input.setValue("null");

    if (selectedId == null) {
      input.setChecked(true);
    }

    Label label = new Label();
    label.setClass("runaex radio-label");
    label.setTagText(resourceBundleMessageSource.getMessage("nothingSelected", null, Locale.ROOT));
    list.add(new WrapComponent(label, input));

    StringBuilder html = new StringBuilder();
    appendComponentCssReference(RadioButtonGroupTag, html);

    String validation = getHtmlWithValidation(list);
    html.append(validation);

    return html.toString();
  }
}
