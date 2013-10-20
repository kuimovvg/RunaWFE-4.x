package ru.cg.runaex.runa_ext.tag;

import java.io.Serializable;
import java.util.Collection;

import freemarker.template.TemplateModelException;
import ru.runa.wfe.security.AuthenticationException;
import ru.runa.wfe.security.AuthorizationException;
import ru.runa.wfe.task.TaskDoesNotExistException;

import ru.cg.runaex.components.bean.component.ComponentType;
import ru.cg.runaex.components.bean.component.Label;
import ru.cg.runaex.database.context.DatabaseSpringContext;
import ru.cg.runaex.runa_ext.tag.utils.FiasAddressUtils;

/**
 * Label
 *
 * @author Sabirov
 */
public class LabelTag extends BaseFreemarkerTag<Label> {
  private static final long serialVersionUID = 1L;

  @Override
  protected ComponentType getComponentType() {
    return ComponentType.LABEL;
  }

  @Override
  protected String executeToHtml(Label component) throws TemplateModelException, AuthorizationException, AuthenticationException, TaskDoesNotExistException {
    String schema = component.getSchema();

    initObjectInfo(schema, component.getTable());

    Object value = null;
    String field = component.getField();
    String fieldId = "label_" + System.nanoTime();
    Long selectedRowId = getSelectedRowId();
    if (selectedRowId != null) {
      String referenceStr = null;

      if (component.getColumnReference() != null)
        referenceStr = component.getColumnReference().toString();
      value = getValue(selectedRowId, schema, component.getTable(), field, referenceStr, field != null ? field.getClass() : Serializable.class, null);
      if (value instanceof String) {
        Collection<String> fiasColumns = DatabaseSpringContext.getMetadataDao().getFiasColumns(getProcessDefinitionId(), schema, component.getTable());
        if (fiasColumns.contains(component.getField())) {
          value = FiasAddressUtils.getAddressByGuId((String) value);
        }
      }
    }

    StringBuilder html = new StringBuilder();
    appendComponentCssReference(LabelTag, html);

    html.append("<label class=\"runaex lt-label\" id=\"").append(fieldId).append("\">");
    if (value != null) {
      html.append(value);
    }
    html.append(" </label>");

    return html.toString();
  }
}