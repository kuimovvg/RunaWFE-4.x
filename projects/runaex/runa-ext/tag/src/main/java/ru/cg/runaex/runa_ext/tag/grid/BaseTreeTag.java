package ru.cg.runaex.runa_ext.tag.grid;

import freemarker.template.TemplateModelException;
import ru.cg.runaex.components.UnicodeSymbols;
import ru.cg.runaex.components.bean.component.grid.BaseTree;
import ru.cg.runaex.runa_ext.tag.field.BaseFieldFreemarkerTag;
import ru.runa.wfe.security.AuthenticationException;
import ru.runa.wfe.security.AuthorizationException;
import ru.runa.wfe.task.TaskDoesNotExistException;

/**
 * @author golovlyev
 */
public abstract class BaseTreeTag<C extends BaseTree> extends BaseFieldFreemarkerTag<C> {
  private static final long serialVersionUID = -8398540724268033004L;

  protected static final String JqueryCookie = "jquery.cookie.js";
  protected static final String JqueryDynaTree = "jquery.dynatree.js";
  protected static final String DynaTreeCss = "dynatree.css";
  protected static final String UiDynaTreeCss = "ui.dynatree.css";

  protected String schema = null;
  protected String table = null;
  protected String fields = null;
  protected Long processInstanceId = null;
  protected StringBuilder html = null;

  @Override
  protected String executeToHtml(C component) throws TemplateModelException, AuthorizationException, AuthenticationException, TaskDoesNotExistException {
    logger.debug("schema - " + component.getSchema());
    logger.debug("table - " + component.getTable());
    logger.debug("fields - " + component.getColumns());

    schema = component.getSchema();
    table = component.getTable();
    fields = component.getColumnsStr();
    processInstanceId = getProcessInstanceId();

    initObjectInfo(schema, table);

    StringBuilder header = new StringBuilder();
    header.append("<ul class=\"dynatree-container\" style=\"padding-left: 50px;\">");

    String[] tmp;
    String[] strFields = fields.split(";");
    for (String f : strFields) {
      tmp = f.split(UnicodeSymbols.COMMA);
      String displayField = "";
      /**
       * exist field name
       */
      if (tmp.length == 1) {
        displayField = tmp[0].trim();
      }
      /**
       * exist Display name and field name
       */
      else if (tmp.length == 2) {
        displayField = tmp[0].trim();
      }
      /**
       * exist Display name, field name and references
       */
      else if (tmp.length >= 3) {
        displayField = tmp[0].trim();
      }
      header.append("<span class=\"td\">");
      header.append(displayField);
      header.append("</span>");
    }
    header.append("</ul>");

    html = new StringBuilder();
    appendCssReference(UiDynaTreeCss, html);
    appendCssReference(DynaTreeCss, html);
    appendJsReference(JqueryCookie, html);
    appendJsReference(JqueryDynaTree, html);
    appendComponentJsReference(TreeGridTag, html);

    html.append(header.toString());
    return html.toString();
  }

  protected void addTemplate(String templateName) {
    setJsTemplateName(templateName);
    addObjectToJs("table", table);
    addObjectToJs("schema", schema);
    addObjectToJs("fields", fields);
    addObjectToJs("processInstanceId", String.valueOf(processInstanceId));
  }
}
