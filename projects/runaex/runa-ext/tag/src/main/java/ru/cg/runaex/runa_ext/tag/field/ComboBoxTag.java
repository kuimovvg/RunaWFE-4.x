package ru.cg.runaex.runa_ext.tag.field;

import java.util.Arrays;

import freemarker.template.TemplateModelException;
import org.apache.ecs.html.Input;
import org.apache.ecs.html.Option;
import org.apache.ecs.html.Select;
import ru.runa.wfe.security.AuthenticationException;
import ru.runa.wfe.security.AuthorizationException;
import ru.runa.wfe.task.TaskDoesNotExistException;

import ru.cg.runaex.components.bean.component.ComponentType;
import ru.cg.runaex.components.bean.component.field.ComboBox;
import ru.cg.runaex.components.bean.component.part.ColumnReference;
import ru.cg.runaex.components.bean.session.ObjectInfo;
import ru.cg.runaex.database.bean.transport.Data;
import ru.cg.runaex.database.bean.transport.TransportData;
import ru.cg.runaex.database.bean.transport.TransportDataSet;
import ru.cg.runaex.runa_ext.tag.BaseEditableFreemarkerTag;
import ru.cg.runaex.runa_ext.tag.utils.DefaultValueUtil;
import ru.cg.runaex.runa_ext.tag.bean.DefaultValue;

/**
 * @author Sabirov
 */
public class ComboBoxTag extends BaseEditableFreemarkerTag<ComboBox> {
  private static final long serialVersionUID = 6127881243438824647L;

  @Override
  protected ComponentType getComponentType() {
    return ComponentType.COMBO_BOX;
  }

  @Override
  protected String executeToHtml(ComboBox component) throws TemplateModelException, AuthorizationException, AuthenticationException, TaskDoesNotExistException {
    logger.debug("schema - " + component.getSchema());
    logger.debug("table - " + component.getTable());
    logger.debug("field - " + component.getField());
    logger.debug("references - " + component.getColumnReference());
    logger.debug("strEnableSpeechInput - " + component.getSpeechInput());
    logger.debug("defaultValue - " + component.getDefaultValue());

    String schema = component.getSchema();

    initObjectInfo(schema, component.getTable());

    String comboSchema = component.getColumnReference().getSchema();
    String comboTable = component.getColumnReference().getTable();
    String comboField = component.getColumnReference().getColumn();
    String comboId = comboTable + "_id"; //todo use id postfix from constants

    logger.debug("comboSchema - " + comboSchema);
    logger.debug("comboTable - " + comboTable);
    logger.debug("comboField - " + comboField);
    logger.debug("comboId - " + comboId);

    /**
     * Selected id in base table
     */
    Long selectedRowId = getSelectedRowId();
    DefaultValue defaultValue = getDefaultValue(selectedRowId, component, schema);

    TransportDataSet transportDataSet = getDataFromDB(comboSchema, comboTable, Arrays.asList(comboId, comboField), comboField, component.getSortOrder());

    Select select = new Select();
    select.setClass("runaex select");
    String fieldId = "combobox_" + System.nanoTime();
    select.setID(fieldId);
    select.setName(component.getField());
    select.addAttribute("data-schema", schema);
    select.addAttribute("data-table", component.getTable());
    select.addAttribute("column-reference", component.getColumnReference());

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
      if (defaultValue.getId() != null && defaultValue.getId().equals(id) || selectedRowId == null && name.equals(defaultValue.getValue())) { //todo code reuse
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
      select.setClass("runaex select speech check-error");
      speechInput = new Input();
      speechInput.setID(fieldId.concat("-speech"));
      speechInput.setClass("runaex combobox-speech select");
      speechInput.addAttribute("speech", "");
      speechInput.addAttribute("x-webkit-speech", "");
    }

    StringBuilder htmlBuilder = new StringBuilder();
    appendComponentCssReference(ComboBoxTag, htmlBuilder);

    if (component.isSpeechInputEnabled()) {
      select.addAttribute("speech_recognition_element", "true");
      appendJsReference(SpeechRecognition, htmlBuilder);
    }

    if (!isEditable) {
      select.addAttribute("disabled", "");
      if (speechInput != null) {
        speechInput.addAttribute("disabled", "");
      }
    }

    String validationHtml = getHtmlWithValidation(new WrapComponent(null, select, speechInput));
    htmlBuilder.append(validationHtml);


    if (component.isSpeechInputEnabled()) {
      setJsTemplateName(ComboBoxTag);
      addObjectToJs("fieldId", fieldId);
    }

    return htmlBuilder.toString();
  }

  private DefaultValue getDefaultValue(Long selectedRowId, ComboBox component, String schema) throws TemplateModelException {
    //todo code reuse
    Long selectId = null;
    Object defaultValue = DefaultValueUtil.getDefaultValue(component.getDefaultValue(), variableProvider);
    String defaultValueStr = String.valueOf(defaultValue);

    if (selectedRowId != null) {
      selectId = getValue(selectedRowId, schema, component.getTable(), component.getField(), null, Long.class, null);
    }
    else if (defaultValue instanceof ColumnReference) {
      ColumnReference defaultValueReference = (ColumnReference) defaultValue;
      String defaultValueReferenceSchema = defaultValueReference.getSchema();
      String defaultValueReferenceTable = defaultValueReference.getTable();
      String defaultValueReferenceColumn = defaultValueReference.getColumn();

      String referenceStr = component.getColumnReference().toString();

      ObjectInfo refObjectInfo = getObjectInfo(defaultValueReferenceSchema, defaultValueReferenceTable);
      if (refObjectInfo != null) {
        defaultValueStr = getValue(refObjectInfo.getId(), defaultValueReferenceSchema, defaultValueReferenceTable,
            defaultValueReferenceColumn, referenceStr, String.class, "");
      }
    }

    return new DefaultValue(selectId, defaultValueStr);
  }
}
