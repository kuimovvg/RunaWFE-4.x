/*
 * Copyright (c) 2012.
 *
 * Class: DateTimePickerTag
 * Last modified: 07.09.12 9:43
 *
 * Author: Sabirov
 * Company Center
 */

package ru.cg.runaex.runa_ext.tag.field;

import java.util.Date;
import java.util.List;

import freemarker.template.TemplateModelException;
import org.apache.ecs.html.IMG;
import org.apache.ecs.html.Input;
import ru.runa.wfe.security.AuthenticationException;
import ru.runa.wfe.security.AuthorizationException;
import ru.runa.wfe.task.TaskDoesNotExistException;

import ru.cg.runaex.components.bean.component.ComponentType;
import ru.cg.runaex.components.bean.component.field.DateTimePicker;
import ru.cg.runaex.components.bean.component.part.ColumnReference;
import ru.cg.runaex.components.bean.component.part.DateType;
import ru.cg.runaex.runa_ext.tag.utils.DefaultValueUtil;
import ru.cg.runaex.runa_ext.tag.BaseEditableFreemarkerTag;
import ru.cg.runaex.runa_ext.tag.bean.DefaultValue;

/**
 * @author Sabirov
 */
public class DateTimePickerTag extends BaseEditableFreemarkerTag<DateTimePicker> {
  private static final long serialVersionUID = 9088009126846558691L;

  @Override
  protected ComponentType getComponentType() {
    return ComponentType.DATE_TIME_PICKER;
  }

  @Override
  protected String executeToHtml(DateTimePicker component) throws TemplateModelException, AuthorizationException, AuthenticationException, TaskDoesNotExistException {
    String schema = component.getSchema();

    initObjectInfo(schema, component.getTable());
    DefaultValue defaultValue = getDefaultValue(component, schema);

    boolean isDateTime = component.getType() == DateType.DATE_TIME;
    Input input = new Input(Input.TEXT, component.getField());
    String fieldId = "date_time_picker_" + System.nanoTime();
    input.setID(fieldId);
    input.setReadOnly(true);
    if (isDateTime)
      input.setClass("runaex sdtpt-datetimepicker fix-datetime check-error");
    else
      input.setClass("runaex sdtpt-datetimepicker fix-date check-error");
    input.addAttribute("data-schema", schema);
    input.addAttribute("data-table", component.getTable());
    if (defaultValue.getValue() != null) {
      input.setValue(defaultValue.getValue());
    }
    if (component.getPlaceHolder() != null) {
      input.addAttribute("placeholder", component.getPlaceHolder());
    }

    IMG img = new IMG("resources/images/datetime/cal.gif");
    img.setClass("runaex sdtpt-datetimepicker show-error");
    img.setOnClick("javascript:NewCssCal('".concat("resources/images/datetime/','").concat(fieldId).concat("','ddMMyyyy','dropdown',").concat(String.valueOf(isDateTime)).concat(",'24',true)"));

    StringBuilder htmlBuilder = new StringBuilder();
    appendComponentCssReference(DateTimePickerTag, htmlBuilder);
    appendComponentJsReference(DateTimePickerTag, htmlBuilder);

    if (!isEditable) {
      input.addAttribute("disabled", "");
      img.addAttribute("disabled", "");
    }

    String validationHtml = getHtmlWithValidation(new WrapComponent(null, input, img));
    htmlBuilder.append(validationHtml);

    return htmlBuilder.toString();
  }

  @Override
  protected StringBuilder getHtmlWithValidationDiv(List<WrapComponent> list) {
    StringBuilder sb = new StringBuilder();
    for (WrapComponent wrapComponent : list) {
      sb.append("<div class=\"control-group\"><div class=\"controls inline\">").
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

  private DefaultValue getDefaultValue(DateTimePicker component, String schema) throws TemplateModelException {
    String date = null;

    Long selectedRowId = getSelectedRowId();
    Object defaultValue = DefaultValueUtil.getDefaultValue(component.getDefaultValue(), variableProvider, Date.class);
    if (selectedRowId != null) {
      date = getValue(selectedRowId, schema, component.getTable(), component.getField(), null, String.class, null);
    }
    else if (component.getDefaultValue() != null) {
      if (defaultValue instanceof ColumnReference) {
        ColumnReference defaultValueReference = (ColumnReference) defaultValue;
        date = copyValue(defaultValueReference, String.class, "");
      }
      else
        date = String.valueOf(defaultValue);
    }

    return new DefaultValue(null, date);
  }
}
