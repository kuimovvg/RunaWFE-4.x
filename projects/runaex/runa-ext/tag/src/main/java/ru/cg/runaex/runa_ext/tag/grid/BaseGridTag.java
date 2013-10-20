package ru.cg.runaex.runa_ext.tag.grid;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import com.google.gson.Gson;
import freemarker.template.TemplateModelException;
import org.apache.ddlutils.model.Table;
import ru.runa.wfe.security.AuthenticationException;
import ru.runa.wfe.security.AuthorizationException;
import ru.runa.wfe.task.TaskDoesNotExistException;

import ru.cg.runaex.components.FilterExpressions;
import ru.cg.runaex.components.UnicodeSymbols;
import ru.cg.runaex.components.WfeRunaVariables;
import ru.cg.runaex.components.bean.component.grid.GridComponent;
import ru.cg.runaex.components.bean.component.part.ColumnReference;
import ru.cg.runaex.components.bean.component.part.GridColumn;
import ru.cg.runaex.components.bean.session.ObjectInfo;
import ru.cg.runaex.database.bean.transport.Data;
import ru.cg.runaex.database.bean.transport.TransportData;
import ru.cg.runaex.database.context.DatabaseSpringContext;
import ru.cg.runaex.database.structure.bean.DatabaseStructure;
import ru.cg.runaex.runa_ext.tag.BaseFreemarkerTag;
import ru.cg.runaex.runa_ext.tag.bean.FilterValue;

/**
 * @author Петров А.
 */
public abstract class BaseGridTag<C extends GridComponent> extends BaseFreemarkerTag<C> {
  private static final long serialVersionUID = 8754334510052708136L;
  protected static final String FlexiGridPackJs = "flexigrid.pack.js";
  protected static final String FlexiGridPackCss = "flexigrid.pack.css";
  protected static final String RunaexFlexiGridCss = "runaex.flexigrid.css";
  private static final Pattern patternCurrentUser = Pattern.compile("=\\s*ТЕКУЩИЙ_ПОЛЬЗОВАТЕЛЬ");
  private static final Pattern patternSelectedRow = Pattern.compile("=\\s*SELECTED_ROW_ID");
  private static final Pattern patternCurrentRoleFromVariable = Pattern.compile("\\$.+");

  @Override
  protected String executeToHtml(C component) throws TemplateModelException, AuthorizationException, AuthenticationException, TaskDoesNotExistException {
    logger.debug("schema - " + component.getSchema());
    logger.debug("table - " + component.getTable());
    logger.debug("tableId - " + component.getTableId());

    String schema = component.getSchema();
    String gridElementId = "grid_" + System.nanoTime();

    initObjectInfo(schema, component.getTable());
    addDefaultFilters(component);

    StringBuilder html = new StringBuilder();
    appendCssReference(FlexiGridPackCss, html);
    appendCssReference(RunaexFlexiGridCss, html);
    appendJsReference(FlexiGridPackJs, html);
    appendComponentJsReference(FlexiGridCommon, html);
    includeJsFiles(html);
    appendInputFieldHtml(html, gridElementId);
    html.append(getFlexiGridHtmlTag(component, gridElementId));

    return html.toString();
  }

  private void appendInputFieldHtml(StringBuilder html, String gridElementId) {
    html.append("<div><input id=\"").append(gridElementId).append("\" type=\"text\" onkeyup=\"flexiGridFilterKeyListener(event, '").append(gridElementId).append("')\"></div>");
  }

  protected abstract String getColumnsTableSchema(C grid);

  protected abstract String getColumnsTable(C grid);

  protected void addDefaultFilters(C grid) throws TemplateModelException {
    if (grid.getFilter() == null) {
      return;
    }
    String strFilters = grid.getFilter();
    String tableName = getColumnsTable(grid);
    String tableId = grid.getTableId();

    strFilters = strFilters.replaceAll("&gt;", ">");
    strFilters = strFilters.replaceAll("&lt;", "<");

    String[] splitedStrFilters = strFilters.split(";");
    List<String> processedStrFilters = new ArrayList<String>();
    StringBuilder sbFilter = new StringBuilder();
    for (String str : splitedStrFilters) {
      if (!str.endsWith("\\")) {
        sbFilter.append(str);
        processedStrFilters.add(sbFilter.toString().trim());
        sbFilter = new StringBuilder();
      }
      else {
        sbFilter.append(str.substring(0, str.length() - 1).concat(";"));
      }
    }

    List<Data> filterDatas = new ArrayList<Data>(processedStrFilters.size());
    for (String strFilter : processedStrFilters) {
      if (patternCurrentUser.matcher(strFilter).find()) {
        Data filterData = new Data();
        String fieldPostfix = strFilter.contains("!=") ? "_ne" : "_eq";
        String columnName = patternCurrentUser.matcher(strFilter).replaceAll("");
        columnName = columnName.replaceAll("!", "");
        filterData.setTable(tableName);
        filterData.setField(columnName.trim() + fieldPostfix);
        filterData.setValue(WfeRunaVariables.CURRENT_USER_CODE);
        filterDatas.add(filterData);
      }
      else if (patternSelectedRow.matcher(strFilter).find()) {
        Data filterData = new Data();
        String fieldPostfix = strFilter.contains("!=") ? "_ne" : "_eq";
        String columnName = patternSelectedRow.matcher(strFilter).replaceAll("");
        String[] tmpColumnName = columnName.split(UnicodeSymbols.POINT);
        if (tmpColumnName.length == 2) {
          columnName = tmpColumnName[1];
          filterData.setTable(tmpColumnName[0].trim());
        }
        columnName = columnName.replaceAll("!", "");
        filterData.setField(columnName.trim() + fieldPostfix);
        filterData.setValueClass("Long");
        String jsonData = DatabaseSpringContext.getBaseDao().getVariableFromDb(getProcessInstanceId(), WfeRunaVariables.SELECTED_OBJECT_INFO);
        if (jsonData != null && !jsonData.isEmpty())
          filterData.setValue(new Gson().fromJson(jsonData, ObjectInfo.class).getId());
        filterDatas.add(filterData);
      }
      else if (patternCurrentRoleFromVariable.matcher(strFilter).find()) {
        String leftPartWithSign = patternCurrentRoleFromVariable.matcher(strFilter).replaceAll("");
        FilterValue filterValue = getFilterValue(strFilter);
        String fieldName = filterValue.getFieldName();
        String suffix = leftPartWithSign.replaceAll(fieldName, "").trim();
        String variableName = strFilter.replaceAll(leftPartWithSign + "\\$", "").trim();

        Object value = variableProvider.getValueNotNull(variableName);
        if (value != null) {
          Data filterData = new Data();
          filterData.setTable(tableName);
          filterData.setField(fieldName + parseToCompactionSuffix(suffix));
          filterData.setValue(value);
          filterDatas.add(filterData);
        }
      }
      else {
        FilterValue filterValue = getFilterValue(strFilter);
        String fieldNameFromFilter = filterValue.getFieldName();
        GridColumn column = getGridColumn(grid, fieldNameFromFilter);
        ColumnReference columnReference = column.getColumnReference();
        String[] fieldParts = fieldNameFromFilter.split(UnicodeSymbols.POINT);
        String field;
        String table;
        if (fieldParts.length == 2) {
          table = fieldParts[0].trim();
          field = fieldParts[1].trim();
        }
        else {
          field = fieldParts[0].trim();
          table = tableName;
        }

        strFilter = strFilter.substring(fieldNameFromFilter.length(), strFilter.length()).trim();
        String comparsionSuffix = parseToCompactionSuffix(strFilter);
        strFilter = filterValue.getValue();
        Data filterData = new Data();
        filterData.setTable(table);
        filterData.setField(field.concat(comparsionSuffix));
        if ("_in".equals(comparsionSuffix) || "_notin".equals(comparsionSuffix)) {
          List<Object> filterValues = new ArrayList<Object>();
          strFilter = strFilter.substring(1, strFilter.length() - 1);
          for (String strFilterValue : strFilter.split(",")) {
            strFilterValue = strFilterValue.trim();
            if (columnReference != null) {
              Long idByFieldValue = getIdByFieldValue(columnReference.getSchema(), columnReference.getTable(), columnReference.getColumn(), strFilterValue);
              filterValues.add(idByFieldValue);
            }
            else {
              filterValues.add(FilterExpressions.parseExpression(strFilterValue));
            }
          }
          filterData.setValue(filterValues);
        }
        else {
          if (columnReference != null) {
            Long idByFieldValue = getIdByFieldValue(columnReference.getSchema(), columnReference.getTable(), columnReference.getColumn(), strFilter);
            filterData.setValue(idByFieldValue);
          }
          else {
            filterData.setValue(FilterExpressions.parseExpression(strFilter));
          }
        }
        filterDatas.add(filterData);
      }
    }

    String defaultFilterKey = WfeRunaVariables.getDefaultFilterKeyVariable(tableId);
    addVariablesFromDb(getProcessInstanceId(), defaultFilterKey, GSON.toJson(new TransportData(0, filterDatas)));
  }

  protected GridColumn getGridColumn(C grid, String fieldNameFromFilter) {
    GridColumn column = null;
    for (GridColumn currentColumn : grid.getColumns()) {
      if (currentColumn.getDatabaseColumn().equals(fieldNameFromFilter)) {
        column = currentColumn;
      }
    }
    return column;
  }

  private String parseToCompactionSuffix(String strFilter) {
    String comparsionSuffix = null;
    if (strFilter.startsWith(">=")) {
      comparsionSuffix = Data.GE_POSTFIX;
    }
    else if (strFilter.startsWith("<=")) {
      comparsionSuffix = Data.LE_POSTFIX;
    }
    else if (strFilter.startsWith("!=")) {
      comparsionSuffix = Data.NE_POSTFIX;
    }
    else if (strFilter.startsWith("=")) {
      comparsionSuffix = Data.EQ_POSTFIX;
    }
    else if (strFilter.startsWith("<")) {
      comparsionSuffix = Data.LT_POSTFIX;
    }
    else if (strFilter.startsWith(">")) {
      comparsionSuffix = Data.GT_POSTFIX;
    }
    else if (strFilter.toLowerCase().startsWith("в")) {
      comparsionSuffix = Data.IN_POSTFIX;
    }
    else if (strFilter.toLowerCase().startsWith("нев")) {
      comparsionSuffix = Data.NOT_IN_POSTFIX;
    }
    return comparsionSuffix;
  }

  private FilterValue getFilterValue(String strFilter) {
    String[] split = strFilter.split("[=!<>]+|\\sВ\\s|\\sНЕВ\\s");
    FilterValue filterValue = new FilterValue();
    filterValue.setFieldName(split[0].trim());
    filterValue.setValue(split[1].trim());
    return filterValue;
  }

  protected abstract StringBuilder includeJsFiles(StringBuilder html);

  protected String getFlexiGridHtmlTag(C grid, String gridElementId) throws TemplateModelException {
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
    String defaultSortName = getDefaultSortName(grid);
    String defaultSortOrder = grid.getSortOrder();
    if (defaultSortOrder == null)
      defaultSortOrder = "asc";
    int defaultElementsCountOnPage = 10;
    int defaultColumnWidth = 150;

    html.append("<table height-weight=\"").append(heightWeight)
        .append("\" width-weight=\"").append(widthWeight)
        .append("\" class=\"flexigrid-mark\" id=\"flexigrid-").append(gridElementId).append("\"")
        .append(" data-piid=\"").append(processInstanceId).append("\"")
        .append(" data-tableId=\"").append(grid.getTableId()).append("\"")
        .append(" data-schema=\"").append(schema).append("\" data-table=\"").append(grid.getTable()).append("\"");

    for (Parameter parameter : getFlexiGridTableAdditionalParameters(grid))
      html.append(" ").append(parameter.getName()).append("=\"").append(parameter.getValue()).append("\"");
    html.append("></table>");

    List<Map<String, Object>> columns = new ArrayList<Map<String, Object>>();

    StringBuilder columnNames = new StringBuilder();
    boolean isFirst = true;
    String columnsSchema = getColumnsTableSchema(grid);
    Table columnsTable = DatabaseStructure.getTable(getProcessDefinitionId(), columnsSchema, getColumnsTable(grid));

    for (GridColumn column : grid.getColumns()) {
      if (!isFirst) {
        columnNames.append(", ");
      }
      String displayFieldName = column.getDisplayName();
      String fieldName = column.getDatabaseColumn();
      if (column.getColumnReference() != null) {
        fieldName = fieldName.concat(";").concat(column.getColumnReference().toString());
      }
      boolean booleanColumn = DatabaseStructure.isBooleanColumn(columnsTable, column.getDatabaseColumn());
      Map<String, Object> columnParams = new HashMap<String, Object>();
      columnParams.put("display", displayFieldName);
      columnParams.put("name", fieldName);
      columnParams.put("width", column.getWidth() != null ? Integer.valueOf(column.getWidth()) : defaultColumnWidth);
      columnParams.put("sortable", true);
      columnParams.put("align", "center");
      if (booleanColumn)
        columnParams.put("type", "checkbox");
      columnNames.append(fieldName);

      columns.add(columnParams);
      isFirst = false;
    }

    List<Map<String, Object>> params = new ArrayList<Map<String, Object>>();

    Map<String, Object> param = new HashMap<String, Object>();
    param.put("name", "columns");
    param.put("value", columnNames.toString());
    params.add(param);

    param = new HashMap<String, Object>();
    param.put("name", "tableId");
    param.put("value", grid.getTableId());
    params.add(param);

    param = new HashMap<String, Object>();
    param.put("name", "schema");
    param.put("value", schema);
    params.add(param);

    param = new HashMap<String, Object>();
    param.put("name", "table");
    param.put("value", grid.getTable());
    params.add(param);

    for (Parameter parameter : getFlexiGridConfigAdditionalParameters(grid)) {
      param = new HashMap<String, Object>();
      param.put("name", parameter.getName());
      param.put("value", parameter.getValue());
      params.add(param);
    }

    setJsTemplateName(FlexiGridTag);
    addObjectToJs("heightWeight", Integer.valueOf(heightWeight));
    addObjectToJs("widthWeight", Integer.valueOf(widthWeight));
    addObjectToJs("gridElementId", gridElementId, false);
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
    addObjectToJs("colModel", columns);
    addObjectToJsWithoutFormatting("onRowClickHandler", getOnRowClickHandlerName());
    addObjectToJs("params", params);

    return html.toString();
  }

  protected abstract List<Parameter> getFlexiGridTableAdditionalParameters(C grid);

  protected abstract List<Parameter> getFlexiGridConfigAdditionalParameters(C grid);

  protected boolean isDependent() {
    return false;
  }

  protected String getOnRowClickHandlerName() {
    return "onRowClickHandler";
  }

  protected String getDefaultSortName(GridComponent grid) {
    if (grid.getSortColumn() != null)
      return grid.getSortColumn();
    else
      return grid.getColumns().get(0).getDatabaseColumn();
  }

  protected static class Parameter {
    private final String name;
    private final Object value;

    public Parameter(String name, Object value) {
      this.name = name;
      this.value = value;
    }

    public String getName() {
      return name;
    }

    public Object getValue() {
      return value;
    }
  }
}
