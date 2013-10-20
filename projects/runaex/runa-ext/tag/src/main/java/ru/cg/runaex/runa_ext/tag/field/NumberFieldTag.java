package ru.cg.runaex.runa_ext.tag.field;

import java.util.Locale;

import freemarker.template.TemplateModelException;
import org.apache.ecs.html.Input;
import ru.runa.wfe.security.AuthenticationException;
import ru.runa.wfe.security.AuthorizationException;
import ru.runa.wfe.task.TaskDoesNotExistException;

import ru.cg.runaex.components.bean.component.ComponentType;
import ru.cg.runaex.components.bean.component.field.NumberField;
import ru.cg.runaex.components.bean.component.part.ColumnReference;
import ru.cg.runaex.components.bean.component.part.MaskType;
import ru.cg.runaex.runa_ext.tag.utils.DefaultValueUtil;
import ru.cg.runaex.runa_ext.tag.BaseEditableFreemarkerTag;

/**
 * @author korablev
 */
public class NumberFieldTag extends BaseEditableFreemarkerTag<NumberField> {
  private static final long serialVersionUID = 3476019849363098516L;

  @Override
  protected ComponentType getComponentType() {
    return ComponentType.NUMBER_FIELD;
  }

  @Override
  protected String executeToHtml(NumberField component) throws TemplateModelException, AuthorizationException, AuthenticationException, TaskDoesNotExistException {
    String schema = component.getSchema();

    initObjectInfo(schema, component.getTable());
    String defaultValue = getDefaultValue(component, schema);

    Input input = new Input();
    input.setClass("runaex textfield speech");
    String fieldId = "number_field_" + System.nanoTime();
    input.setID(fieldId);
    input.setName(component.getField());
    input.addAttribute("data-schema", schema);
    input.addAttribute("data-table", component.getTable());
    input.setType("text");

    if (defaultValue != null) {
      input.setValue(defaultValue);
    }
    if (component.getPlaceHolder() != null) {
      input.addAttribute("placeholder", component.getPlaceHolder());
    }
    if (component.isSpeechInputEnabled()) {
      input.addAttribute("speech", "");
      input.addAttribute("x-webkit-speech", "");
      input.addAttribute("", resourceBundleMessageSource.getMessage("verifyError", null, Locale.ROOT));
      input.setClass("runaex speech check-error");
      input.addAttribute("speech_recognition_element", "true");
    }

    if (!isEditable) {
      input.addAttribute("disabled", "");
    }

    StringBuilder html = new StringBuilder();
    appendComponentCssReference(TextFieldTag, html);
    appendComponentJsReference(TextFieldTag, html);
    html.append(input);

    setJsTemplateName(NumberFieldTag);
    addObjectToJs("fieldId", fieldId);
    String maskType = MaskType.NUMBER.getCode();
    if (component.getMaskType() != MaskType.NONE)
      maskType = component.getMaskType().getCode();

    addObjectToJs("mask", maskType);

    return html.toString();
  }

  private String getDefaultValue(NumberField component, String schema) throws TemplateModelException {
    String defaultValueStr = "";
    Long selectedRowId = getSelectedRowId();
    if (selectedRowId != null) {
      defaultValueStr = getValue(selectedRowId, schema, component.getTable(), component.getField(), null, String.class, "");
    }
    else if (component.getDefaultValue() != null) {
      Object defaultValue = DefaultValueUtil.getDefaultValue(component.getDefaultValue(), variableProvider, Number.class);
      if (defaultValue instanceof ColumnReference) {
        ColumnReference defaultValueReference = (ColumnReference) defaultValue;
        defaultValueStr = copyValue(defaultValueReference, String.class, "");
      }
      else
        defaultValueStr = String.valueOf(defaultValue);
    }

    return defaultValueStr;
  }
}
