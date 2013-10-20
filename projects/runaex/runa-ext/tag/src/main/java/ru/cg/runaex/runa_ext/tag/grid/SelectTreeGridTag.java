package ru.cg.runaex.runa_ext.tag.grid;

import freemarker.template.TemplateModelException;
import ru.cg.runaex.components.bean.component.ComponentType;
import ru.cg.runaex.components.bean.component.grid.SelectTreeGrid;
import ru.cg.runaex.components.util.ComponentUtil;
import ru.runa.wfe.security.AuthenticationException;
import ru.runa.wfe.security.AuthorizationException;

/**
 * @author golovlyev
 */
public class SelectTreeGridTag extends BaseTreeTag<SelectTreeGrid> {
  private static final long serialVersionUID = 6550048848727022545L;

  @Override
  protected ComponentType getComponentType() {
    return ComponentType.SELECT_TREE_GRID;
  }

  @Override
  protected String executeToHtml(SelectTreeGrid component) throws TemplateModelException, AuthorizationException, AuthenticationException {
    logger.debug("saveTable - " + component.getSaveTable());
    logger.debug("saveColumn - " + component.getSaveField());

    super.executeToHtml(component);
    html.append("<div class=\"runaex check-error");
    if ("true".equals(ComponentUtil.createRequireRule(component.getRequireRule())))    //todo
      html.append(" dynatree-validate");
    html.append("\" id=\"").append(table).append("\" style=\"height: 300px;\"").append(" data-piid=\"").append(processInstanceId).append("\"")
        .append(" save-schema=\"").append(schema).append("\" save-table=\"").append(component.getSaveTable()).append("\" save-field=\"").append(component.getSaveField())
        .append("\"></div>");

    addTemplate(SelectTreeGridTag);

    return html.toString();
  }
}
