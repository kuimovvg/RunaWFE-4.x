package ru.cg.runaex.runa_ext.tag.field;

import freemarker.template.TemplateModelException;
import org.apache.ecs.ConcreteElement;
import org.apache.ecs.html.Input;
import org.apache.ecs.html.TextArea;
import ru.runa.wfe.security.AuthenticationException;
import ru.runa.wfe.security.AuthorizationException;
import ru.runa.wfe.task.TaskDoesNotExistException;
import ru.cg.runaex.components.bean.component.ComponentType;
import ru.cg.runaex.components.bean.component.field.TextField;
import ru.cg.runaex.components.bean.component.part.ColumnReference;
import ru.cg.runaex.components.bean.component.part.MaskType;
import ru.cg.runaex.components.bean.component.part.TextType;
import ru.cg.runaex.runa_ext.tag.BaseEditableFreemarkerTag;
import ru.cg.runaex.runa_ext.tag.utils.DefaultValueUtil;


import java.util.List;

/**
 * Text field
 *
 * @author Sabirov
 */
public class TextFieldTag extends BaseEditableFreemarkerTag<TextField> {
  private static final long serialVersionUID = -842961648283120882L;

  @Override
  protected ComponentType getComponentType() {
    return ComponentType.TEXT_FIELD;
  }

  @Override
  protected String executeToHtml(TextField component) throws TemplateModelException, AuthorizationException, AuthenticationException, TaskDoesNotExistException {
    String schema = component.getSchema();

    initObjectInfo(schema, component.getTable());
    String defaultValue = getDefaultValue(component, schema);

    ConcreteElement input;
    Input textAreaSpeechInput = null;

    String fieldId = "textfield_" + System.nanoTime();
    if (component.getType() != TextType.TEXT_AREA) {
      input = new Input();
      input.setClass("runaex textfield speech");
      input.setID(fieldId);
      ((Input) input).setName(component.getField());
      input.addAttribute("data-schema", schema);
      input.addAttribute("data-table", component.getTable());
      ((Input) input).setType("text");

      if (defaultValue != null) {
        ((Input) input).setValue(defaultValue);
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
      input.setClass("runaex textfield speech");
      input.setID(fieldId);
      ((TextArea) input).setName(component.getField());
      input.addAttribute("type", Input.text);
      input.addAttribute("data-schema", schema);
      input.addAttribute("data-table", component.getTable());
      if (defaultValue != null) {
        input.setTagText(defaultValue);
      }
      if (component.getPlaceHolder() != null) {
        input.addAttribute("placeholder", component.getPlaceHolder());
      }

      if (component.isSpeechInputEnabled()) {
        input.setClass("runaex speech check-error");
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

    //attribute for recognizing on mouse and key events
    if (component.isSpeechInputEnabled()) {
      input.addAttribute("speech_recognition_element", "true");
      appendJsReference(SpeechRecognition, html);
    }

    appendComponentCssReference(TextFieldTag, html);
    appendComponentJsReference(TextFieldTag, html);

    html.append(getHtmlWithValidation(new WrapComponent(null, input, textAreaSpeechInput)));

    setJsTemplateName(TextFieldTag);
    addObjectToJs("fieldId", fieldId);
    addObjectToJs("speechAvailable", textAreaSpeechInput != null);

    String mask = component.getMask();
    if (component.getMaskType() != MaskType.NONE && MaskType.MANUAL != component.getMaskType()) {
      mask = component.getMaskType().getCode();
    }

    addObjectToJs("hasMask", mask != null);
    addObjectToJs("mask", mask != null ? mask : "");

    return html.toString();
  }

  private String getDefaultValue(TextField component, String schema) throws TemplateModelException {
    String defaultValueStr = "";
    Long selectedRowId = getSelectedRowId();
    if (selectedRowId != null) {
      defaultValueStr = getValue(selectedRowId, schema, component.getTable(), component.getField(), null, String.class, "");
    }
    else if (component.getDefaultValue() != null) {
      Object defaultValue = DefaultValueUtil.getDefaultValue(component.getDefaultValue(), variableProvider);
      if (defaultValue instanceof ColumnReference){
        ColumnReference defaultValueReference = (ColumnReference) defaultValue;
        defaultValueStr = copyValue(defaultValueReference, String.class, "");
      }
      else
        defaultValueStr = String.valueOf(defaultValue);
    }

    return defaultValueStr;
  }

  @Override
  protected StringBuilder getHtmlWithValidationDiv(List<WrapComponent> list) {
    StringBuilder sb = new StringBuilder();
    for (WrapComponent wrapComponent : list) {
      sb.append("<div class=\"control-group\"><div class=\"controls inline speech\">").
          append(wrapComponent.getElem1().toString());
      if (wrapComponent.getElem2() != null)
        sb.append(wrapComponent.getElem2().toString());
      if (wrapComponent.getLabel() != null)
        sb.append(wrapComponent.getLabel().toString());
      sb.append("</div>");
      sb.append("</div>");
    }
    return sb;
  }
}