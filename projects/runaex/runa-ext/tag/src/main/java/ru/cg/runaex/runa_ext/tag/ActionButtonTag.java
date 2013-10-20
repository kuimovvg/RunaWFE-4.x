package ru.cg.runaex.runa_ext.tag;

import freemarker.template.TemplateModelException;
import org.apache.ecs.html.Button;
import org.apache.ecs.html.I;
import ru.runa.wfe.security.AuthenticationException;
import ru.runa.wfe.security.AuthorizationException;
import ru.runa.wfe.task.TaskDoesNotExistException;

import ru.cg.runaex.components.WfeRunaVariables;
import ru.cg.runaex.components.bean.component.ActionButton;
import ru.cg.runaex.components.bean.component.ComponentType;
import ru.cg.runaex.components.bean.session.LinkTableInfo;
import ru.cg.runaex.components.util.ComponentUtil;
import ru.cg.runaex.database.context.DatabaseSpringContext;
import ru.cg.runaex.database.dao.MetadataDao;

/**
 * Action button
 *
 * @author Sabirov
 */
public class ActionButtonTag extends BaseFreemarkerTag<ActionButton> {
  private static final long serialVersionUID = 1L;

  @Override
  protected ComponentType getComponentType() {
    return ComponentType.ACTION_BUTTON;
  }

  @Override
  protected String executeToHtml(ActionButton component) throws TemplateModelException, AuthorizationException, AuthenticationException, TaskDoesNotExistException {
    logger.debug("actionButtonName - " + component.getName());
    logger.debug("actionButtonValue - " + component.getAction());
    logger.debug("nextTask - " + component.getNextTask());

    if (component.shouldHaveLinkInfo()) {
      saveLinkInfoToDb(component.getLinkInfo());
    }

    StringBuilder htmlBuilder = new StringBuilder();
    appendComponentCssReference(ActionButtonTag, htmlBuilder);
    appendComponentJsReference(ExtractFormData, htmlBuilder);
    appendComponentJsReference(ActionButtonTag, htmlBuilder);
    appendComponentJsReference(ExecuteGroovy, htmlBuilder);

    Button button = new Button();

    String groovyScript = component.getGroovyScript();
    if (groovyScript != null) {
      MetadataDao metadataDao = DatabaseSpringContext.getMetadataDao();
      Long scriptId = metadataDao.saveGroovyScript(ComponentUtil.replaceCharacters(groovyScript));
      button.addAttribute("script_id", scriptId);
    }
    button.setID("action_button_" + String.valueOf(System.nanoTime()));
    button.setClass("runaex btn btn-primary");
    ActionButton.Action action = component.getAction();
    button.addAttribute("action", component.getActionStr());
    if (component.getNextTask() != null) {
      button.addAttribute("next-task", component.getNextTask());
    }
    if (component.getTableId() != null) {
      button.addAttribute("data-tableId", component.getTableId());
      button.addAttribute("disabled", "");
    }

    I htmlI = new I();
    switch (action) {
      case SAVE: {
        htmlI.setClass("icon-ok");
        break;
      }
      case DELETE: {
        htmlI.setClass("icon-trash");
        break;
      }
      case FIND: {
        htmlI.setClass("icon-search");
        break;
      }
      case CANCEL: {
        htmlI.setClass("icon-remove");
        break;
      }
    }

    button.setType("button");
    button.setTagText(htmlI.toString() + component.getName());
    button.setValue(component.getName());
    button.setOnClick("sbtClickHandler(this)");
    if (component.getWidth() != null)
      button.addAttribute("style", "width:" + component.getWidth() + "px;");
    htmlBuilder.append(button.toString());

    return htmlBuilder.toString();
  }

  private void saveLinkInfoToDb(ActionButton.LinkInfo linkInfo) throws TemplateModelException, AuthorizationException, TaskDoesNotExistException, AuthenticationException {
    String dependentTableSchema = linkInfo.getDependentTableReference().getSchema();
    String mainTableSchema = linkInfo.getMainTableReference().getSchema();

    String dependentTable = linkInfo.getDependentTableReference().getTable();
    String mainTable = linkInfo.getMainTableReference().getTable();

    LinkTableInfo linkTableInfo = new LinkTableInfo(dependentTableSchema, dependentTable, mainTableSchema, mainTable);
    String jsonLinkTableInfo = GSON.toJson(linkTableInfo);

    Long processInstanceId = getProcessInstanceId();
    DatabaseSpringContext.getComponentDbServices().getBaseDaoService().addVariableToDb(processInstanceId, WfeRunaVariables.LINK_TABLE_INFO, jsonLinkTableInfo);
  }
}