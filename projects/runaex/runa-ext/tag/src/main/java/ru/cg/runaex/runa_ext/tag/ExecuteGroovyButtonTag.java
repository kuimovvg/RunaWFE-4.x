package ru.cg.runaex.runa_ext.tag;

import freemarker.template.TemplateModelException;
import org.apache.ecs.html.Button;
import ru.runa.wfe.security.AuthenticationException;
import ru.runa.wfe.security.AuthorizationException;
import ru.runa.wfe.task.TaskDoesNotExistException;

import ru.cg.runaex.components.bean.component.ComponentType;
import ru.cg.runaex.components.bean.component.ExecuteGroovyButton;
import ru.cg.runaex.components.util.ComponentUtil;
import ru.cg.runaex.database.context.DatabaseSpringContext;
import ru.cg.runaex.database.dao.MetadataDao;

/**
 * @author Kochetkov
 */
public class ExecuteGroovyButtonTag extends BaseFreemarkerTag<ExecuteGroovyButton> {
  private static final long serialVersionUID = 1L;

  @Override
  protected ComponentType getComponentType() {
    return ComponentType.EXECUTE_GROOVY_BUTTON;
  }

  @Override
  protected String executeToHtml(ExecuteGroovyButton component) throws TemplateModelException, AuthorizationException, AuthenticationException, TaskDoesNotExistException {
    logger.debug("executeGroovyButtonTag - " + component.getName());
    logger.debug("groovyScript - " + component.getGroovyScript());

    StringBuilder htmlBuilder = new StringBuilder();
    appendComponentJsReference(ExecuteGroovy, htmlBuilder);

    MetadataDao metadataDao = DatabaseSpringContext.getMetadataDao();
    Long scriptId = metadataDao.saveGroovyScript(ComponentUtil.replaceCharacters(component.getGroovyScript()));

    Button button = new Button();
    button.setID("groovy_button_" + System.nanoTime());
    button.addAttribute("script_id", scriptId);
    button.setClass("runaex btn btn-primary");
    button.setType("button");
    button.setValue(component.getName());
    button.setTagText(component.getName());
    button.setOnClick("groovyScriptBtnClickHandler(this)");
    htmlBuilder.append(button.toString());

    return htmlBuilder.toString();
  }
}