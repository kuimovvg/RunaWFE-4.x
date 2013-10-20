package ru.cg.runaex.runa_ext.tag;

import freemarker.template.TemplateModelException;
import ru.runa.wfe.security.AuthenticationException;
import ru.runa.wfe.security.AuthorizationException;
import ru.runa.wfe.task.TaskDoesNotExistException;

import ru.cg.runaex.components.bean.component.ComponentType;
import ru.cg.runaex.components.bean.component.NavigationTreeMenu;
import ru.cg.runaex.components.parser.NavigationTreeMenuParser;

/**
 * @author Петров А.
 */
public class NavigationTreeMenuTag extends BaseFreemarkerTag<NavigationTreeMenu> {
  private static final long serialVersionUID = 1L;

  private static final int SUBTREE_MARGIN = 15;

  @Override
  protected ComponentType getComponentType() {
    return ComponentType.NAVIGATION_TREE_MENU;
  }

  @Override
  protected String executeToHtml(NavigationTreeMenu component) throws TemplateModelException, AuthorizationException, AuthenticationException, TaskDoesNotExistException {
    StringBuilder html = new StringBuilder();
    appendComponentCssReference(NavigationTreeMenuTag, html);
    appendComponentJsReference(NavigationCommon, html);
    appendJsReference("ddaccordion.js", html);
    appendComponentJsReference(NavigationTreeMenuTag, html);

    NavigationTreeMenu.Node treeRoot = NavigationTreeMenuParser.buildTree(component.getNodes());
    html.append(buildTree(treeRoot, component.getTitle()));

    return html.toString();
  }

  /**
   * Build tree
   *
   * @param root Root element of tree
   * @return Html
   */
  private String buildTree(NavigationTreeMenu.Node root, String header) {
    StringBuilder html = new StringBuilder();

    html.append("<div class=\"runaex arrowlistmenu\"><h3 class=\"menuheader expandable\">")
        .append(header != null ? header : "").append("</h3><ul class=\"categoryitems\">");
    for (NavigationTreeMenu.Node node : root.getChildNodes()) {
      html.append(buildSubtree(node, 1));
    }
    html.append("</ul></div>");
    return html.toString();
  }

  private String buildSubtree(NavigationTreeMenu.Node subtreeRoot, int level) {
    StringBuilder html = new StringBuilder();
    if (subtreeRoot.getChildNodes().isEmpty()) {
      html.append("<li><a onclick=\"nbtClickHandler(this)\" next-task=\"").append(subtreeRoot.getTask()).append("\" action=\"other\">").append(subtreeRoot.getName()).append("</a></li>");
    }
    else {
      html.append("<li><a class=\"subexpandable\" next-task=\"").append(subtreeRoot.getChildNodes()).append("\" action=\"other\">").append(subtreeRoot.getName()).append("</a>")
          .append("<ul class=\"subcategoryitems\" style=\"margin-left: ").append(SUBTREE_MARGIN * level).append("px\">"); //TODO calculate margin
      for (NavigationTreeMenu.Node node : subtreeRoot.getChildNodes()) {
        html.append(buildSubtree(node, level + 1));
      }
      html.append("</ul></li>");
    }

    return html.toString();
  }

}
