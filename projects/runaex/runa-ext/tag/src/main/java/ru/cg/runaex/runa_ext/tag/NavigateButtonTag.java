package ru.cg.runaex.runa_ext.tag;

import freemarker.template.TemplateModelException;
import org.apache.ecs.html.Button;
import org.apache.ecs.html.I;
import ru.runa.wfe.security.AuthenticationException;
import ru.runa.wfe.security.AuthorizationException;
import ru.runa.wfe.task.TaskDoesNotExistException;

import ru.cg.runaex.components.bean.component.ComponentType;
import ru.cg.runaex.components.bean.component.NavigateButton;
import ru.cg.runaex.components.util.ComponentUtil;
import ru.cg.runaex.database.context.DatabaseSpringContext;
import ru.cg.runaex.database.dao.MetadataDao;

/**
 * Button for navigation between tasks.
 *
 * @author Donskoy, Sabirov
 */
public class NavigateButtonTag extends BaseFreemarkerTag<NavigateButton> {
  private static final long serialVersionUID = 1L;

  @Override
  protected ComponentType getComponentType() {
    return ComponentType.NAVIGATE_BUTTON;
  }

  @Override
  protected String executeToHtml(NavigateButton component) throws TemplateModelException, AuthorizationException, AuthenticationException, TaskDoesNotExistException {
    StringBuilder html = new StringBuilder();
    appendComponentCssReference(NavigateButtonTag, html);
    appendComponentJsReference(NavigationCommon, html);
    appendComponentJsReference(ExecuteGroovy, html);

    Button button = new Button();
    button.setID("navigate_button__" + System.nanoTime());
    String groovyScript = component.getGroovyScript();
    if (groovyScript != null) {
      MetadataDao metadataDao = DatabaseSpringContext.getMetadataDao();
      Long scriptId = metadataDao.saveGroovyScript(ComponentUtil.replaceCharacters(groovyScript));
      button.addAttribute("script_id", scriptId);
    }
    button.addAttribute("next-task", component.getNextTask());
    button.addAttribute("action", component.getActionStr());

    if (component.getTableId() != null) {
      button.addAttribute("data-tableId", component.getTableId());
      button.addAttribute("disabled", "");
    }
    button.setClass("runaex btn");
    button.setValue(component.getName());
    button.setOnClick("nbtClickHandler(this)");
    if (component.getWidth() != null)
      button.addAttribute("style", "width:" + component.getWidth() + "px");
    NavigateButton.Action action = component.getAction();

    I htmlI = new I();
    switch (action) {
      case ADD: {
        htmlI.setClass("icon-plus");
        break;
      }
      case CHANGE: {
        htmlI.setClass("icon-edit");
        break;
      }
    }
    button.setType("button");
    button.setTagText(htmlI.toString() + component.getName());
    html.append(button.toString());

    return html.toString();
  }
}