package ru.cg.runaex.runa_ext.tag;

import freemarker.template.TemplateModelException;
import org.apache.ecs.html.Button;
import org.apache.ecs.html.I;
import ru.runa.wfe.security.AuthenticationException;
import ru.runa.wfe.security.AuthorizationException;
import ru.runa.wfe.task.TaskDoesNotExistException;

import ru.cg.runaex.components.bean.component.ComponentType;
import ru.cg.runaex.components.bean.component.SignAndSaveButton;

/**
 * @author Kochetkov
 */
public class SignAndSaveButtonTag extends BaseFreemarkerTag<SignAndSaveButton> {
  private static final long serialVersionUID = 1L;

  @Override
  protected ComponentType getComponentType() {
    return ComponentType.SIGN_AND_SAVE_BUTTON;
  }

  @Override
  protected String executeToHtml(SignAndSaveButton component) throws TemplateModelException, AuthorizationException, AuthenticationException, TaskDoesNotExistException {
    logger.debug("signAndSaveButtonName - " + component.getName());
    logger.debug("signAndSaveButtonTable - " + component.getTable());
    logger.debug("signAndSaveButtonTable - " + component.getTable());


    StringBuilder htmlBuilder = new StringBuilder();
    appendComponentCssReference(ActionButtonTag, htmlBuilder);
    appendComponentJsReference(ExtractFormData, htmlBuilder);
    appendComponentJsReference(SignAndSaveButtonTag, htmlBuilder);

    Button button = new Button();
    button.setID("sign_button_" +System.nanoTime());
    button.setClass("runaex btn btn-primary");
    button.addAttribute("data-schema", component.getSchema());
    button.addAttribute("data-table", component.getTable());
    button.addAttribute("sign-field", component.getSignField());
    button.addAttribute("data-field", component.getDataField());
    button.setType("button");
    button.setValue(component.getName());
    if (component.getWidth() != null)
      button.addAttribute("style", "width:" + component.getWidth() + "px;");
    I htmlI = new I();
    htmlI.setClass("icon-ok");
    button.setTagText(htmlI.toString() + component.getName());
    button.setOnClick("signAndSaveClickHandler(this)");
    htmlBuilder.append(button.toString());

    return htmlBuilder.toString();
  }
}