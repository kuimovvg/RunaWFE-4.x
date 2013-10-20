package ru.cg.runaex.runa_ext.tag;

import freemarker.template.TemplateModelException;
import org.apache.ecs.html.Input;
import ru.runa.wfe.security.AuthenticationException;
import ru.runa.wfe.security.AuthorizationException;
import ru.runa.wfe.task.TaskDoesNotExistException;

import ru.cg.runaex.components.bean.component.ComponentType;
import ru.cg.runaex.components.bean.component.HelpButton;

/**
 * @author korablev
 */
public class HelpButtonTag extends BaseFreemarkerTag<HelpButton> {
  private static final long serialVersionUID = -7526050525005111590L;

  @Override
  protected ComponentType getComponentType() {
    return ComponentType.HELP_BUTTON;
  }

  @Override
  protected String executeToHtml(HelpButton component) throws TemplateModelException, AuthorizationException, AuthenticationException, TaskDoesNotExistException {
    StringBuilder html = new StringBuilder();

    String fieldId = "button_" + System.nanoTime();
    Input input = new Input(Input.BUTTON, fieldId);
    input.setID(fieldId);
    input.setClass("runaex btn btn-info");
    input.setTitle(component.getName());
    input.addAttribute("is-help-popover-open", false);
    input.addAttribute("help-text", component.getText());

    input.setValue(component.getName());
    input.setOnClick("helpClickHandler(this)");
    if (component.getWidth() != null)
      input.addAttribute("style", "width:" + component.getWidth() + "px;");

    appendComponentJsReference(HelpButtonTag, html);
    html.append(input.toString());
    return html.toString();
  }
}
