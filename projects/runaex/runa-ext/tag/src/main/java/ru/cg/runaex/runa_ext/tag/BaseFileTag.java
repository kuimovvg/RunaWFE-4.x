package ru.cg.runaex.runa_ext.tag;

import java.util.Arrays;
import java.util.Locale;

import freemarker.template.TemplateModelException;

import ru.cg.runaex.components.bean.component.BaseFile;
import ru.cg.runaex.components.bean.component.part.ColumnReference;
import ru.cg.runaex.components.bean.session.ObjectInfo;
import ru.cg.runaex.components.util.FileUploadComponentHelper;
import ru.cg.runaex.database.bean.transport.TransportData;
import ru.cg.runaex.runa_ext.tag.field.BaseFieldFreemarkerTag;

/**
 * @author golovlyev
 */
public abstract class BaseFileTag<C extends BaseFile> extends BaseFieldFreemarkerTag<C> {
  private static final long serialVersionUID = 1L;

  protected String schema = null;
  protected String table = null;
  protected String fileName = null;
  protected byte[] contentFile = null;
  protected String signColumnName = null;
  protected Long selectedRowId = null;
  protected StringBuilder html = null;

  @Override
  protected String executeToHtml(C component) throws TemplateModelException {
    BaseFreemarkerTag.logger.debug("schema - " + component.getSchema());
    BaseFreemarkerTag.logger.debug("table - " + component.getTable());
    BaseFreemarkerTag.logger.debug("field - " + component.getField());

    schema = component.getSchema();
    table = component.getTable();
    signColumnName = component.getSignColumnName();

    initObjectInfo(schema, table);
    getValue(component);

    html = new StringBuilder();
    appendComponentCssReference(BaseFreemarkerTag.FileUploadTag, html);
    appendComponentJsReference(BaseFreemarkerTag.FileUploadTag, html);
    appendJsReference("jquery.download.js", html);
    html.append("<div class=\"inline-controls\">");
    setJsTemplateName(BaseFreemarkerTag.FileUploadTag);
    addObjectToJs("fieldId", component.getField(), false);

    addObjectToJs("id", selectedRowId == null ? 0L : selectedRowId);
    addObjectToJs("schema", schema);
    addObjectToJs("table", table);
    addObjectToJs("signColumnName", signColumnName == null ? "" : signColumnName);
    addObjectToJs("errorDuringFileSignVerifyMsg", BaseFreemarkerTag.resourceBundleMessageSource.getMessage("errorDuringFileSignVerifyMsg", null, Locale.ROOT));
    addObjectToJs("signVerifyRequired", false);

    return html.toString();
  }
  private void getValue(C component) throws TemplateModelException {
    selectedRowId = getSelectedRowId();
    if (component.isDefaultValueSet()) {
      fileName = copyValue(component.getDefaultValueReferenceName(), String.class, "");
      contentFile = copyValue(component.getDefaultValueReferenceFile(), byte[].class, null);
    }
    else if (selectedRowId != null) {
      String filenameColumn = FileUploadComponentHelper.getNameColumn(component.getField());
      String dataColumn = FileUploadComponentHelper.getDataColumn(component.getField());

      TransportData transportData = getValue(selectedRowId, schema, table, Arrays.asList(dataColumn, filenameColumn));
      fileName = (String) transportData.getData(filenameColumn).getValue();
      contentFile = (byte[]) transportData.getData(dataColumn).getValue();
    }
  }

  protected void putReferenceValueAsCurrent(C component) throws TemplateModelException {
    ColumnReference columnReferenceName = component.getDefaultValueReferenceName();
    ObjectInfo objectInfo = getObjectInfo(columnReferenceName.getSchema(), columnReferenceName.getTable());
    if (objectInfo != null) {
      schema = objectInfo.getSchema();
      table = objectInfo.getTable();
      selectedRowId = objectInfo.getId();
    }
  }

  protected String getField(C component) {
    if (component.isDefaultValueSet())
      return component.getDefaultValueReferenceName().getColumn();
    return component.getField();
  }

  protected void appendReferences() {
    appendComponentCssReference(BaseFreemarkerTag.FileUploadTag, html);
    appendComponentJsReference(BaseFreemarkerTag.FileUploadTag, html);
    appendJsReference("jquery.download.js", html);
  }
}
