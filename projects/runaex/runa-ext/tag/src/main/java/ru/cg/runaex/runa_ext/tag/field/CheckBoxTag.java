package ru.cg.runaex.runa_ext.tag.field;

import freemarker.template.TemplateModelException;
import org.apache.ecs.html.Input;
import ru.runa.wfe.security.AuthenticationException;
import ru.runa.wfe.security.AuthorizationException;
import ru.runa.wfe.task.TaskDoesNotExistException;

import ru.cg.runaex.components.bean.component.ComponentType;
import ru.cg.runaex.components.bean.component.field.CheckBox;
import ru.cg.runaex.runa_ext.tag.BaseEditableFreemarkerTag;

/**
 * @author Sabirov
 */
public class CheckBoxTag extends BaseEditableFreemarkerTag<CheckBox> {
  private static final long serialVersionUID = 8040396230319765437L;

  @Override
  protected ComponentType getComponentType() {
    return ComponentType.CHECK_BOX;
  }

  @Override
  protected String executeToHtml(CheckBox component) throws TemplateModelException, AuthorizationException, AuthenticationException, TaskDoesNotExistException {
    logger.debug("schema - " + component.getSchema());
    logger.debug("table - " + component.getTable());
    logger.debug("field - " + component.getField());

    String schema = component.getSchema();
    initObjectInfo(schema, component.getTable());
    Boolean defaultValue = getDefaultValue(component, schema);

    Input input = new Input(Input.CHECKBOX);
    input.setClass("runaex checkbox");
    input.setID("checkbox_" + System.nanoTime());
    input.setName(component.getField());
    input.addAttribute("data-schema", schema);
    input.addAttribute("data-table", component.getTable());
    if (defaultValue != null && defaultValue) {
      input.setChecked(true);
    }

    if (!isEditable) {
      input.addAttribute("disabled", "");
    }

    return getHtmlWithValidation(new WrapComponent(null, input));
  }

  private Boolean getDefaultValue(CheckBox component, String schema) throws TemplateModelException {
    Boolean defaultValue;

    Long selectedRowId = getSelectedRowId();
    if (selectedRowId != null)
      defaultValue = getValue(selectedRowId, schema, component.getTable(), component.getField(), null, Boolean.class, Boolean.FALSE);
    else if (component.isUseDefaultValueFromDb())
      defaultValue = copyValue(component.getCopyFromReference(), Boolean.class, Boolean.FALSE);
    else
      defaultValue = component.isCheckedByDefault();

    return defaultValue;
  }
}