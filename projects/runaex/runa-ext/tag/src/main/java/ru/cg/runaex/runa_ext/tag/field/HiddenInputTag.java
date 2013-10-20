package ru.cg.runaex.runa_ext.tag.field;

import freemarker.template.TemplateModelException;
import org.apache.ecs.html.Input;
import ru.runa.wfe.security.AuthenticationException;
import ru.runa.wfe.security.AuthorizationException;
import ru.runa.wfe.task.TaskDoesNotExistException;

import ru.cg.runaex.components.bean.component.ComponentType;
import ru.cg.runaex.components.bean.component.field.HiddenInput;
import ru.cg.runaex.runa_ext.tag.BaseFreemarkerTag;

/**
 * @author Петров А.
 */
public class HiddenInputTag extends BaseFreemarkerTag<HiddenInput> {
  private static final long serialVersionUID = 1L;

  @Override
  protected ComponentType getComponentType() {
    return ComponentType.HIDDEN_INPUT;
  }

  @Override
  protected String executeToHtml(HiddenInput component) throws TemplateModelException, AuthorizationException, AuthenticationException, TaskDoesNotExistException {
    logger.debug("schema - " + component.getSchema());
    logger.debug("table - " + component.getTable());
    logger.debug("field - " + component.getField());
    logger.debug("references - " + component.getColumnReference());
    logger.debug("value - " + component.getValue());

    String schema = component.getSchema();
    String value = component.getValue();

    initObjectInfo(schema, component.getTable());

    if (component.getColumnReference() != null) {
      Long previousValue = getIdByFieldValue(component.getColumnReference(), value);
      value = previousValue == null ? null : previousValue.toString();
    }

    Input input = new Input();
    input.setType(Input.HIDDEN);
    input.setClass("runaex save-hidden-input");
    input.setID("hidden_input_" + System.nanoTime());
    input.setName(component.getField());
    input.addAttribute("data-schema", schema);
    input.addAttribute("data-table", component.getTable());
    if (component.getColumnReference() != null)
      input.addAttribute("column-reference", component.getColumnReference());
    if (component.getValue() != null)
      input.addAttribute("str-value", component.getValue());
    input.addAttribute("current-time-as-default-value", component.isCurrentTimeAsDefaultValue());
    input.addAttribute("current-user-as-default-value", component.isCurrentUserAsDefaultValue());
    if (value != null) {
      input.setValue(value);
    }

    return input.toString();
  }
}
