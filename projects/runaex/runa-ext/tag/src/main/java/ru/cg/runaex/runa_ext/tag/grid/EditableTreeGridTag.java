package ru.cg.runaex.runa_ext.tag.grid;

import java.util.*;

import freemarker.template.TemplateModelException;
import org.apache.ddlutils.model.Table;
import ru.runa.wfe.security.AuthenticationException;
import ru.runa.wfe.security.AuthorizationException;
import ru.runa.wfe.task.TaskDoesNotExistException;

import ru.cg.runaex.components.WfeRunaVariables;
import ru.cg.runaex.components.bean.component.ComponentType;
import ru.cg.runaex.components.bean.component.grid.EditableTreeGrid;
import ru.cg.runaex.components.bean.component.part.EditableTreeGridColumn;
import ru.cg.runaex.components.bean.component.part.GridColumn;
import ru.cg.runaex.database.bean.transport.TransportData;
import ru.cg.runaex.database.structure.bean.DatabaseStructure;

/**
 * @author Kochetkov
 */
public class EditableTreeGridTag extends BaseGridTag<EditableTreeGrid> {
  private static final long serialVersionUID = 1L;
  protected static final String extAllJs = "ext-all.js";
  protected static final String extAllCss = "ext-all.css";

  @Override
  protected ComponentType getComponentType() {
    return ComponentType.EDITABLE_TREE_GRID;
  }

  @Override
  protected String executeToHtml(EditableTreeGrid component) throws TemplateModelException, AuthorizationException, AuthenticationException, TaskDoesNotExistException {
    logger.debug("schema - " + component.getSchema());
    logger.debug("table - " + component.getTable());
    logger.debug("tableId - " + component.getTableId());

    String schema = component.getSchema();
    String gridElementId = "editable_tree_grid" + System.nanoTime();

    initObjectInfo(schema, component.getTable());
    addDefaultFilters(component);

    StringBuilder html = new StringBuilder();
    appendJsReference(extAllJs, html);
    appendComponentJsReference(FlexiGridCommon, html);
    appendCssReference(extAllCss, html);
    includeJsFiles(html);
    html.append(getEditableTreeGridHtmlTag(component, gridElementId));

    return html.toString();
  }

  protected String getEditableTreeGridHtmlTag(EditableTreeGrid grid, String gridElementId) throws TemplateModelException {
    StringBuilder html = new StringBuilder();

    Long processInstanceId = getProcessInstanceId();

    String schema = grid.getSchema();

    String heightWeight = grid.getHeightWeight();
    if (heightWeight == null) {
      heightWeight = "100";
    }
    String widthWeight = grid.getWidthWeight();
    if (widthWeight == null) {
      widthWeight = "100";
    }
    String nodeCount = grid.getNodeCount();
    String defaultSortName = getDefaultSortName(grid);
    String defaultSortOrder = grid.getSortOrder();
    if (defaultSortOrder == null)
      defaultSortOrder = "asc";
    int defaultElementsCountOnPage = 10;
    int defaultColumnWidth = 150;

    html.append("<div height-weight=\"").append(heightWeight)
        .append("\" width-weight=\"").append(widthWeight)
        .append("\" class=\"flexigrid-mark\" id=\"").append(gridElementId).append("\"")
        .append(" data-piid=\"").append(processInstanceId).append("\"")
        .append(" data-tableId=\"").append(grid.getTableId()).append("\"")
        .append(" data-schema=\"").append(schema).append("\" data-table=\"").append(grid.getTable()).append("\"");

    for (Parameter parameter : getFlexiGridTableAdditionalParameters(grid))
      html.append(" ").append(parameter.getName()).append("=\"").append(parameter.getValue()).append("\"");
    html.append("></div>");

    List<Map<String, Object>> columns = new ArrayList<Map<String, Object>>();
    List<Map<String, Object>> modelFields = new ArrayList<Map<String, Object>>();

    StringBuilder columnNames = new StringBuilder();
    StringBuilder linkColumns = new StringBuilder();
    boolean isFirst = true;
    String columnsSchema = getColumnsTableSchema(grid);

    Table columnsTable = DatabaseStructure.getTable(getProcessDefinitionId(), columnsSchema, getColumnsTable(grid));

    boolean isFirstLinkColumn = true;
    for (EditableTreeGridColumn linkColumn : grid.getLinkColumns()) {
      if (linkColumn.getColumnReference() != null && !linkColumns.toString().contains(linkColumn.getColumnReference().toString())) {
        if (!isFirstLinkColumn) {
          linkColumns.append(",");
        }
        linkColumns.append(linkColumn.getColumnReference().toString());
        isFirstLinkColumn = false;
      }
    }

    for (EditableTreeGridColumn column : grid.getColumns()) {
      if (!isFirst) {
        columnNames.append(", ");
      }
      String displayFieldName = column.getDisplayName();
      String fieldName = column.getDatabaseColumn();
      if (column.getColumnReference() != null) {
        fieldName = fieldName.concat(";").concat(column.getColumnReference().toString());
      }
      else {
        fieldName = grid.getSchema().concat(":").concat(grid.getTable()).concat(":").concat(fieldName);
      }

      boolean booleanColumn = DatabaseStructure.isBooleanColumn(columnsTable, column.getDatabaseColumn());
      boolean numberColumn = DatabaseStructure.isNumberColumn(columnsTable, column.getDatabaseColumn());

      Map<String, Object> fieldParams = new HashMap<String, Object>();
      Map<String, Object> columnParams = new HashMap<String, Object>();

      String correctFieldValue = fieldName.replace(";", "").replace(".", "");
      fieldParams.put("name", correctFieldValue);
      columnParams.put("dataIndex", correctFieldValue);

      fieldParams.put("type", columnsTable.getType().toString());
      columnNames.append(fieldName);

      columnParams.put("text", displayFieldName);
      columnParams.put("width", column.getWidth() == null ? defaultColumnWidth : Integer.valueOf(column.getWidth()));
      columnParams.put("sortable", true);

      if (booleanColumn) {
        columnParams.put("xtype", "checkcolumn");
      }

      if (isFirst) {
        columnParams.put("xtype", "treecolumn");
      }

      if (numberColumn) {
        columnParams.put("align", "right");
        columnParams.put("editor", "{'xtype': 'numberfield'}");
      }
      else {
        columnParams.put("align", "left");
        columnParams.put("editor", "{'xtype': 'textfield'}");
      }

      columnParams.put("renderer", WfeRunaVariables.FUNCTION_PREFIX + "renderCellStyle('" + fieldName + "_CSS', '" + column.getColumnFormat() + "')");

      columns.add(columnParams);
      modelFields.add(fieldParams);
      isFirst = false;
    }

    for (EditableTreeGridColumn column : grid.getLinkColumns()) {
      if (!columnNames.toString().isEmpty())
        columnNames.append(", ");

      String displayFieldName = column.getDisplayName();
      String fieldName = column.getDatabaseColumn();
      if (column.getColumnReference() != null) {
        /**
         * в разных таблицах наименования колонок могут совпадать
         * поэтому алиас в формате schema:maintable:dependtable:column:linkcolumn
         */
        fieldName = grid.getSchema().concat(":").concat(grid.getTable()).concat(":").
            concat(column.getColumnReference().getTable()).concat(":").concat(fieldName).
            concat(":").concat(column.getColumnReference().getColumn());
      }

      columnsTable = DatabaseStructure.getTable(getProcessDefinitionId(), columnsSchema, column.getColumnReference().getTable());

      Map<String, Object> fieldParams = new HashMap<String, Object>();
      Map<String, Object> columnParams = new HashMap<String, Object>();
      boolean booleanColumn = DatabaseStructure.isBooleanColumn(columnsTable, column.getDatabaseColumn());
      boolean numberColumn = DatabaseStructure.isNumberColumn(columnsTable, column.getDatabaseColumn());

      fieldParams.put("name", fieldName);
      columnParams.put("dataIndex", fieldName);
      fieldParams.put("type", columnsTable.getType().toString());
      columnNames.append(fieldName);
      columnParams.put("renderer", WfeRunaVariables.FUNCTION_PREFIX + "renderCellStyle('" + fieldName + "_CSS', " + column.getColumnFormat() + ")");

      if (booleanColumn) {
        columnParams.put("xtype", "checkcolumn");
      }
      columnParams.put("text", displayFieldName);
      columnParams.put("width", defaultColumnWidth);
      columnParams.put("sortable", true);

      if (numberColumn) {
        columnParams.put("align", "right");
        columnParams.put("editor", "{'xtype': 'numberfield'}");
      }
      else {
        columnParams.put("align", "left");
        columnParams.put("editor", "{'xtype': 'textfield'}");
      }

      columns.add(columnParams);
      modelFields.add(fieldParams);
    }

    setJsTemplateName(EditableTreeGridTag);
    addObjectToJs("heightWeight", Integer.valueOf(heightWeight));
    addObjectToJs("nodeCount", nodeCount != null ? Integer.valueOf(nodeCount) : Integer.valueOf(1));
    addObjectToJs("widthWeight", Integer.valueOf(widthWeight));
    addObjectToJs("gridElementId", gridElementId);
    addObjectToJs("isPaginationVisible", grid.isPaginationVisible());
    addObjectToJs("schema", schema);
    addObjectToJs("table", grid.getTable());
    addObjectToJs("isDependent", isDependent());

    String filterKey = WfeRunaVariables.getFilterKeyVariable(grid.getTableId());
    TransportData transportData = getVariableFromDb(processInstanceId, filterKey);
    boolean showClearFilterButton = transportData != null;
    addObjectToJs("pClearFilter", showClearFilterButton);
    addObjectToJs("sortname", defaultSortName);
    addObjectToJs("sortorder", defaultSortOrder);
    addObjectToJs("defaultElementsCountOnPage", defaultElementsCountOnPage);
    addObjectToJs("columns", columns);
    addObjectToJs("modelFields", modelFields);
    addObjectToJsWithoutFormatting("onRowClickHandler", getOnRowClickHandlerName());

    addObjectToJs("fields", columnNames.toString());
    addObjectToJs("linkColumns", linkColumns.toString());
    addObjectToJs("tableId", grid.getTableId());
    addObjectToJs("parentCol", grid.getParentColumn());

    return html.toString();
  }

  @Override
  protected GridColumn getGridColumn(EditableTreeGrid grid, String fieldNameFromFilter) {
    GridColumn column = super.getGridColumn(grid, fieldNameFromFilter);
    if (column == null) {
      for (EditableTreeGridColumn currentColumn : grid.getLinkColumns()) {
        if (currentColumn.getDatabaseColumn().equals(fieldNameFromFilter)) {
          column = currentColumn;
        }
      }
    }
    return column;
  }

  @Override
  protected String getColumnsTableSchema(EditableTreeGrid grid) {
    return grid.getSchema();
  }

  @Override
  protected String getColumnsTable(EditableTreeGrid grid) {
    return grid.getTable();
  }

  @Override
  protected StringBuilder includeJsFiles(StringBuilder htmlBuilder) {
    appendComponentJsReference(EditableTreeGridTag, htmlBuilder);
    return htmlBuilder;
  }

  @Override
  protected List<Parameter> getFlexiGridTableAdditionalParameters(EditableTreeGrid grid) {
    return Collections.emptyList();
  }

  @Override
  protected List<Parameter> getFlexiGridConfigAdditionalParameters(EditableTreeGrid grid) {
    return Collections.emptyList();
  }
}
