package ru.cg.runaex.runa_ext.tag.field;

import freemarker.template.TemplateModelException;
import org.apache.commons.lang.StringUtils;
import org.apache.ecs.html.Input;
import ru.runa.wfe.security.AuthenticationException;
import ru.runa.wfe.security.AuthorizationException;
import ru.runa.wfe.task.TaskDoesNotExistException;

import ru.cg.runaex.components.bean.component.ComponentType;
import ru.cg.runaex.components.bean.component.field.Autocomplete;
import ru.cg.runaex.components.bean.component.part.ColumnReference;
import ru.cg.runaex.components.bean.session.ObjectInfo;
import ru.cg.runaex.runa_ext.tag.BaseEditableFreemarkerTag;
import ru.cg.runaex.runa_ext.tag.utils.DefaultValueUtil;
import ru.cg.runaex.runa_ext.tag.BaseFreemarkerTag;
import ru.cg.runaex.runa_ext.tag.bean.DefaultValue;

/**
 * @author Sabirov
 */
public class AutocompleteTag extends BaseEditableFreemarkerTag<Autocomplete> {
  private static final long serialVersionUID = -6134034816403136495L;

  private static final String varMinSymbols = "3";
  private static final String varQueryDelay = "300";

  @Override
  protected ComponentType getComponentType() {
    return ComponentType.AUTOCOMPLETE;
  }

  @Override
  protected String executeToHtml(Autocomplete component) throws TemplateModelException, AuthorizationException, AuthenticationException, TaskDoesNotExistException {
    BaseFreemarkerTag.logger.debug("schema - " + component.getSchema());
    BaseFreemarkerTag.logger.debug("table - " + component.getTable());
    BaseFreemarkerTag.logger.debug("field - " + component.getField());
    BaseFreemarkerTag.logger.debug("references - " + component.getColumnReference());
    BaseFreemarkerTag.logger.debug("placeholder - " + component.getPlaceHolder());
    BaseFreemarkerTag.logger.debug("relatedTable - " + component.getRelatedTableReference());
    BaseFreemarkerTag.logger.debug("relatedTableColumn - " + component.getRelatedTableColumn());
    BaseFreemarkerTag.logger.debug("relatedLinkTable - " + component.getRelatedLinkTable());
    BaseFreemarkerTag.logger.debug("defaultValue - " + component.getDefaultValue());

    String schema = component.getSchema();
    StringBuilder htmlBuilder = new StringBuilder();
    String relatedTableColumn = component.getRelatedTableColumn();
    if (component.getRelatedTableReference() != null && (relatedTableColumn == null || component.getRelatedLinkTable() != null))
      relatedTableColumn = component.getRelatedTableReference().getTable().concat("_id");  //todo use id postfix from constants

    String sortOrder = component.getSortOrder();
    if (sortOrder == null)
      sortOrder = "asc";

    initObjectInfo(schema, component.getTable());

    /**
     * Selected id in base table
     */
    DefaultValue defaultValueInBaseTable = getSelectedInBaseTable(component, schema);

    Input input = new Input(Input.TEXT);
    String fieldId = "autocomplete_" + System.nanoTime();
    input.setID(fieldId);
    input.setName(component.getField() + "_ac-name");
    input.setClass("runaex ac-input check-error");
    if (defaultValueInBaseTable.getValue() != null) {
      input.setValue(defaultValueInBaseTable.getValue());
    }
    else if (component.getRelatedField() != null) {
      input.addAttribute("disabled", "disabled");
    }
    if (component.getPlaceHolder() != null) {
      input.addAttribute("placeholder", component.getPlaceHolder());
    }
    if (component.isSpeechInputEnabled()) {
      input.addAttribute("speech", "");
      input.addAttribute("x-webkit-speech", "");
      input.addAttribute("speech_recognition_element", "true");
      appendJsReference(SpeechRecognition, htmlBuilder);
    }

    Input inputHidden = new Input(Input.HIDDEN);
    String hiddenFieldId = "autocomplete_hidden_" + System.nanoTime();
    inputHidden.setClass("runaex ac-hidden-input");
    inputHidden.setID(hiddenFieldId);
    inputHidden.setName(component.getField());
    inputHidden.addAttribute("data-schema", schema);
    inputHidden.addAttribute("data-table", component.getTable());
    inputHidden.addAttribute("column-reference", component.getColumnReference());

    if (defaultValueInBaseTable.getId() != null) {
      inputHidden.setValue(String.valueOf(defaultValueInBaseTable.getId()));
    }
    else if (component.getRelatedField() != null) {
      inputHidden.addAttribute("disabled", "disabled");
    }

    if (component.getRelatedField() != null) {
      String relatedFieldStr = component.getRelatedField().toString();
      input.addAttribute("related-field", relatedFieldStr);
      inputHidden.addAttribute("related-field", relatedFieldStr);
    }
    if (relatedTableColumn != null) {
      input.addAttribute("related-table-column", relatedTableColumn);
      inputHidden.addAttribute("related-table-column", relatedTableColumn);
    }
    if (component.getRelatedLinkTable() != null) {
      String relatedLinkTableStr = component.getRelatedLinkTable().toString();
      input.addAttribute("related-link-table", relatedLinkTableStr);
      inputHidden.addAttribute("related-link-table", relatedLinkTableStr);
    }

    if (!isEditable) {
      input.addAttribute("disabled", "");
      if (!inputHidden.hasAttribute("disabled")) {
        inputHidden.addAttribute("disabled", "");
      }
    }

    appendComponentJsReference(BaseFreemarkerTag.AutocompleteTag, htmlBuilder);
    appendCssReference(BaseFreemarkerTag.JqueryUiAutoCompleteCss, htmlBuilder);

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

  private DefaultValue getSelectedInBaseTable(Autocomplete component, String schema) throws TemplateModelException {
    String autocompleteSchema = component.getColumnReference().getSchema();
    String autocompleteTable = component.getColumnReference().getTable();
    String autocompleteField = component.getColumnReference().getColumn();

    BaseFreemarkerTag.logger.debug("comboSchema - " + autocompleteSchema);
    BaseFreemarkerTag.logger.debug("comboTable - " + autocompleteTable);
    BaseFreemarkerTag.logger.debug("comboField - " + autocompleteField);

    Long selectedId = null;
    String selectedValue = null;

    Long selectedRowId = getSelectedRowId();
    if (selectedRowId != null) {
      selectedId = getValue(selectedRowId, schema, component.getTable(), component.getField(), null, Long.class, null);
      if (selectedId != null) {
        selectedValue = getValue(selectedId, autocompleteSchema, autocompleteTable, autocompleteField, null, String.class, null);
      }
    }
    else if (component.getDefaultValue() != null) {
      Object defaultValue = DefaultValueUtil.getDefaultValue(component.getDefaultValue(), variableProvider);
      if (defaultValue instanceof ColumnReference) {
        ColumnReference defaultValueReference = (ColumnReference) defaultValue;
        String defaultValueReferenceSchema = defaultValueReference.getSchema();
        String defaultValueReferenceTable = defaultValueReference.getTable();
        String defaultValueReferenceColumn = defaultValueReference.getColumn();

        ObjectInfo refObjectInfo = getObjectInfo(defaultValueReferenceSchema, defaultValueReferenceTable);
        if (refObjectInfo != null) {
          selectedId = getValue(refObjectInfo.getId(), defaultValueReferenceSchema, defaultValueReferenceTable, defaultValueReferenceColumn, null, Long.class, null);
          selectedValue = getValue(selectedId, autocompleteSchema, autocompleteTable, autocompleteField, null, String.class, "");
        }
      }
      else {
        String defaultValueStr = String.valueOf(defaultValue);
        selectedId = getIdByFieldValue(autocompleteSchema, autocompleteTable, autocompleteField, defaultValueStr);
        if (selectedId != null) {
          selectedValue = defaultValueStr;
        }
      }
    }

    selectedValue = StringUtils.trimToNull(selectedValue);
    return new DefaultValue(selectedId, selectedValue);
  }
}