package ru.cg.runaex.runa_ext.tag.filter;

import freemarker.template.TemplateModelException;
import org.apache.ecs.html.Input;
import ru.cg.runaex.runa_ext.tag.BaseFreemarkerTag;
import ru.runa.wfe.security.AuthenticationException;
import ru.runa.wfe.security.AuthorizationException;
import ru.runa.wfe.task.TaskDoesNotExistException;

import ru.cg.runaex.components.WfeRunaVariables;
import ru.cg.runaex.components.bean.component.ComponentType;
import ru.cg.runaex.components.bean.component.filter.FilterAutocomplete;
import ru.cg.runaex.database.bean.transport.TransportData;
import ru.cg.runaex.runa_ext.tag.BaseEditableFreemarkerTag;
import ru.cg.runaex.runa_ext.tag.bean.DefaultValue;

public class FilterAutocompleteTag extends BaseEditableFreemarkerTag<FilterAutocomplete> {
  private static final long serialVersionUID = 7711209443466545850L;

  @Override
  protected ComponentType getComponentType() {
    return ComponentType.FILTER_AUTOCOMPLETE;
  }

  @Override
  protected String executeToHtml(FilterAutocomplete component) throws TemplateModelException, AuthorizationException, AuthenticationException, TaskDoesNotExistException {
    logger.debug("schema - " + component.getSchema());
    logger.debug("table - " + component.getTable());
    logger.debug("field - " + component.getField());
    logger.debug("references - " + component.getColumnReference());
    logger.debug("tableId - " + component.getTableId());
    logger.debug("defaultValue - " + component.getDefaultValue());
    logger.debug("relatedField - " + component.getRelatedFieldReference());
    logger.debug("relatedTable - " + component.getRelatedTableReference());
    logger.debug("relatedTableColumn - " + component.getRelatedTableColumn());
    logger.debug("relatedLinkTable - " + component.getRelatedLinkTableReference());

    String schema = component.getSchema();
    String relatedTableColumn = component.getRelatedTableColumn();
    if (component.getRelatedTableReference() != null && (relatedTableColumn == null || component.getRelatedLinkTableReference() != null))
      relatedTableColumn = component.getRelatedLinkTableReference().getTable().concat("_id");  //todo use id postfix from constants

    String varMinSymbols = "3";
    String varQueryDelay = "300";

    String sortOrder = component.getSortOrder();
    if (sortOrder == null)
      sortOrder = "asc";


    initObjectInfo(schema, component.getTable());

    /**
     * Selected id in base table
     */
    DefaultValue selectedValue = getDefaultValue(component);

    Input input = new Input(Input.TEXT);
    String fieldId = "filter_autocomplete_" + System.nanoTime();
    input.setID(fieldId);
    input.setName(fieldId);
    input.setClass("runaex ac-input");
    if (selectedValue.getValue() != null) {
      input.setValue(selectedValue.getValue());
    }
    else if (component.getRelatedFieldReference() != null) {
      input.addAttribute("disabled", "disabled");
    }
    if (component.getPlaceHolder() != null) {
      input.addAttribute("placeholder", component.getPlaceHolder());
    }
    if (component.isSpeechInputEnabled()) {
      input.addAttribute("speech", "");
      input.addAttribute("x-webkit-speech", "");
      input.addAttribute("speech_recognition_element", "true");
    }

    Input inputHidden = new Input(Input.HIDDEN);
    inputHidden.setClass("runaex ac-hidden-input");
    String hiddenFieldId = "filter_autocomplete_hidden_" + System.nanoTime();
    inputHidden.setID(hiddenFieldId);
    inputHidden.setName(hiddenFieldId);
    inputHidden.addAttribute("data-schema", schema);
    inputHidden.addAttribute("data-table", component.getTable());
    inputHidden.addAttribute("data-tableId", component.getTableId());
    if (selectedValue.getId() != null) {
      inputHidden.setValue(String.valueOf(selectedValue.getId()));
    }
    else if (component.getRelatedFieldReference() != null) {
      inputHidden.addAttribute("disabled", "disabled");
    }

    if (component.getRelatedFieldReference() != null) {
      String relatedFieldStr = component.getRelatedFieldReference().toString();
      input.addAttribute("related-field", relatedFieldStr);
      inputHidden.addAttribute("related-field", relatedFieldStr);
    }
    if (relatedTableColumn != null) {
      input.addAttribute("related-table-column", relatedTableColumn);
      inputHidden.addAttribute("related-table-column", relatedTableColumn);
    }
    if (component.getRelatedLinkTableReference() != null) {
      String relatedLinkTableStr = component.getRelatedLinkTableReference().toString();
      input.addAttribute("related-link-table", relatedLinkTableStr);
      inputHidden.addAttribute("related-link-table", relatedLinkTableStr);
    }

    if (!isEditable) {
      input.addAttribute("disabled", "");
      inputHidden.addAttribute("disabled", "");
    }

    StringBuilder htmlBuilder = new StringBuilder();
    appendComponentCssReference(AutocompleteTag, htmlBuilder);
    appendCssReference(JqueryUiAutoCompleteCss, htmlBuilder);

    if (component.isSpeechInputEnabled()) {
      appendJsReference(SpeechRecognition, htmlBuilder);
    }

    String validationHtml = getHtmlWithValidation(new BaseFreemarkerTag.WrapComponent(null, input, inputHidden));
    htmlBuilder.append(validationHtml);

    String referenceStr = component.getColumnReference().toString();

    setJsTemplateName(AutocompleteTag);
    addObjectToJs("schema", schema);
    addObjectToJs("table", component.getTable());
    addObjectToJs("fieldId", fieldId, false);
    addObjectToJs("hiddenFieldId", hiddenFieldId, false);
    addObjectToJs("field", component.getField(), false);
    addObjectToJs("varMinSymbols", varMinSymbols);
    addObjectToJs("varQueryDelay", varQueryDelay);
    addObjectToJs("sortOrder", sortOrder);
    addObjectToJs("isSpeechInputEnabled", component.isSpeechInputEnabled());
    addObjectToJs("referenceStr", referenceStr);

    return htmlBuilder.toString();
  }

  private DefaultValue getDefaultValue(FilterAutocomplete component) throws TemplateModelException {
    Long selectedId = null;
    String selectedValue = null;

    String autocompleteSchema = component.getColumnReference().getSchema();
    String autocompleteTable = component.getColumnReference().getTable();
    String autocompleteField = component.getColumnReference().getColumn();

    Long processInstanceId = getProcessInstanceId();
    if (component.getTableId() != null) {
      String filterKey = WfeRunaVariables.getFilterKeyVariable(component.getTableId());
      TransportData transportData = getVariableFromDb(processInstanceId,filterKey);
      logger.debug("filter transportFilterData - " + transportData);
      selectedId = getValue(Long.class, transportData, component.getField(), null);
      if (selectedId != null) {
        selectedValue = getValue(selectedId, autocompleteSchema, autocompleteTable, autocompleteField, null, String.class, "");
      }
      else if (component.getDefaultValue() != null) {
        selectedId = getIdByFieldValue(autocompleteSchema, autocompleteTable, autocompleteField, component.getDefaultValue());
        if (selectedId != null) {
          selectedValue = component.getDefaultValue();
        }
      }
    }

    return new DefaultValue(selectedId, selectedValue);
  }
}