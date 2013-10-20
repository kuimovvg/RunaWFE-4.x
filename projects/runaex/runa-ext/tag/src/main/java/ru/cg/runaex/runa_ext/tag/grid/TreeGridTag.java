/*
 * Copyright (c) 2012.
 *
 * Class: TreeTag
 * Last modified: 19.09.12 11:37
 *
 * Author: Sabirov
 * Company Center
 */

package ru.cg.runaex.runa_ext.tag.grid;

import freemarker.template.TemplateModelException;
import ru.runa.wfe.security.AuthenticationException;
import ru.runa.wfe.security.AuthorizationException;
import ru.runa.wfe.task.TaskDoesNotExistException;

import java.util.Date;
import ru.cg.runaex.components.bean.component.ComponentType;
import ru.cg.runaex.components.bean.component.grid.TreeGrid;

public class TreeGridTag extends BaseTreeTag<TreeGrid> {
  private static final long serialVersionUID = -5695069041030621881L;
  private String btnReloadActive = null;

  @Override
  protected ComponentType getComponentType() {
    return ComponentType.TREE_GRID;
  }

  @Override
  protected String executeToHtml(TreeGrid component) throws TemplateModelException, AuthorizationException, AuthenticationException, TaskDoesNotExistException {
    logger.debug("treeGridId - " + component.getTableId());

    super.executeToHtml(component);

    html.append("<div id=\"").append(component.getTable()).append("\" style=\"height: 500px;\"></div>");

    btnReloadActive = "btnReloadActive_" + System.nanoTime();
    html.append("<button id=\"").append(btnReloadActive).append("\" class=\"btn btn-primary btn-small\">Reload active node...</button>"); //todo localization

    addTemplate(TreeGridTag);

    return html.toString();
  }

  @Override
  protected void addTemplate(String templateName) {
    super.addTemplate(templateName);
    addObjectToJs("btnReloadActive", btnReloadActive);
  }
}
