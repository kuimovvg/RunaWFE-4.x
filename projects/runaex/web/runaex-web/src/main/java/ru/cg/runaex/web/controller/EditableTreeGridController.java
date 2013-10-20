package ru.cg.runaex.web.controller;

import java.text.SimpleDateFormat;
import java.util.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import groovy.lang.Binding;
import groovy.lang.GroovyShell;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import ru.runa.wfe.security.AuthenticationException;
import ru.runa.wfe.security.AuthorizationException;
import ru.runa.wfe.task.TaskDoesNotExistException;
import ru.runa.wfe.task.dto.WfTask;

import ru.cg.fias.search.core.server.sphinx.SphinxException;
import ru.cg.runaex.components.WfeRunaVariables;
import ru.cg.runaex.core.DateFormat;
import ru.cg.runaex.database.bean.*;
import ru.cg.runaex.database.bean.model.MetadataEditableTreeGrid;
import ru.cg.runaex.database.bean.transport.ClassType;
import ru.cg.runaex.database.bean.transport.Data;
import ru.cg.runaex.database.bean.transport.TransportData;
import ru.cg.runaex.database.bean.transport.TransportDataSet;
import ru.cg.runaex.database.dao.BaseDao;
import ru.cg.runaex.database.util.GsonUtil;
import ru.cg.runaex.exceptions.EditableTreeGridException;
import ru.cg.runaex.generatedb.bean.Table;
import ru.cg.runaex.web.security.SecurityUtils;
import ru.cg.runaex.web.service.DatabaseMetadataService;
import ru.cg.runaex.web.service.RunaWfeService;

/**
 * @author Kochetkov
 */
@Controller
public class EditableTreeGridController {
  private Map<String, Boolean> editRuleMap = null;

  @Autowired
  private BaseDao baseDao;

  @Autowired
  private DatabaseMetadataService metadataService;

  @Autowired
  private RunaWfeService runaWfeService;

  private final static String ROOT_NODE = "root";
  private final static String PARENTS_SESSION_KEY = "PARENTS_SESSION_KEY";
  private final static String CHILDREN_SESSION_KEY = "CHILDREN_SESSION_KEY";

  @RequestMapping(value = "/editableTreeGrid", method = RequestMethod.GET)
  @ResponseBody
  public List<Map<String, Object>> loadData(
      @RequestParam("node") String node,
      @RequestParam("parentCol") String parentCol,
      @RequestParam("s") String schema,
      @RequestParam("t") String table,
      @RequestParam("tableId") String tableId,
      @RequestParam("fields") String strColumns,
      @RequestParam("linkColumns") String linkColumns,
      @RequestParam("processDefinitionId") Long processDefinitionId,
      HttpServletRequest request) throws SphinxException, AuthorizationException, AuthenticationException, TaskDoesNotExistException {

    MetadataEditableTreeGrid editableTreeGrid = getMetadataEditableTreeGrid(tableId);
    editableTreeGrid.getDependencyMatrix().refresh(processDefinitionId);
    strColumns += "," + schema + ":" + table + ":" + parentCol;
    String[] columnArray = strColumns.split(",");

    FullTableParam editableTreeGridParam = new FullTableParam(schema, table, columnArray, linkColumns, parentCol, tableId);
    String[] fields = Arrays.copyOf(columnArray, columnArray.length + 1);
    fields[columnArray.length] = parentCol;

    List<Map<String, Object>> rows = loadData(node, processDefinitionId, tableId, editableTreeGridParam, request);

    List<Map<String, Object>> newRows = new ArrayList<Map<String, Object>>();
    Map<Cell, Object> cache = new HashMap<Cell, Object>();
    for (Map<String, Object> row : rows) {
      newRows.add(recalculateRow(processDefinitionId, row, cache, editableTreeGrid.getDependencyMatrix(), editableTreeGridParam, request));
    }
    return render(processDefinitionId, editableTreeGridParam, newRows, editableTreeGrid, request);
  }

  private Map<String, Object> recalculateRow(Long processDefinitionId, Map<String, Object> row, Map<Cell, Object> cache, DependencyMatrix dependencyMatrix, FullTableParam param, HttpServletRequest request) {
    Map<String, Object> newRow = new HashMap<String, Object>();
    for (String column : row.keySet()) {
      newRow.put(column, recalculateCell(processDefinitionId, column, row.get(column), new Cell((Long) row.get("id"), column), cache, dependencyMatrix, param, request));
    }
    return newRow;
  }

  private MetadataEditableTreeGrid getMetadataEditableTreeGrid(String tableId) {
    MetadataEditableTreeGrid editableTreeGrid = metadataService.getEditableTreeGrid(tableId);
    if (editableTreeGrid == null) {
      throw new EditableTreeGridException("Unknown tree grid with id " + tableId);
    }
    return editableTreeGrid;
  }

  private Object recalculateCell(Long processDefinitionId, String originalColumn, Object currentValue, Cell cell, Map<Cell, Object> cache, DependencyMatrix dependencyMatrix, FullTableParam param, HttpServletRequest request) {
    Dependency dependency = dependencyMatrix.getDependencies().get(cell);
    if (dependency == null)
      return currentValue;

    List<CellValue> cellValues = new ArrayList<CellValue>();

    List<Cell> loadCells = new ArrayList<Cell>();
    for (Cell dpCell : dependency.getDependency()) {
      if (cache.get(dpCell) == null) {
        loadCells.add(dpCell);
      }
    }
    cache.putAll(getCellsFromDB(processDefinitionId, loadCells, param, request));

    for (Cell dpCell : dependency.getDependency()) {
      if (cache.get(dpCell) == null) {
        return null;
      }
      cellValues.add(new CellValue(dpCell, cache.get(dpCell)));
    }

    Binding binding = new Binding();
    binding.setVariable(cell.getAlias(), null);
    for (CellValue cellValue : cellValues) {
      binding.setVariable(cellValue.getCell().getAlias(), cellValue.getValue());
    }

    String groovyScript = cell.getAlias() + "=" + dependency.getBusinessRule();

    GroovyShell groovyShell = getGroovyShell(binding);
    groovyShell.evaluate(groovyScript);
    Object newValue = binding.getVariable(cell.getAlias());
    if ((currentValue == null && newValue == null) || (currentValue != null && newValue != null && currentValue.equals(newValue)))
      return currentValue;
    cache.put(cell, newValue);
    baseDao.updateTreeCell(originalColumn, cell.getId(), newValue, processDefinitionId, getFilter(param.getTableId(), request));
    return newValue;
  }

  protected Map<Cell, Object> getCellsFromDB(Long processDefinitionId, List<Cell> loadCells, FullTableParam param, HttpServletRequest request) {
    if (loadCells == null || loadCells.isEmpty())
      return new HashMap<Cell, Object>();

    TransportData filter = getFilter(param.getTableId(), request);
    String[] columns = new String[loadCells.size() + 1];
    List<Long> ids = new ArrayList<Long>();
    for (int i = 0; i < loadCells.size(); i++) {
      for (String col : param.getColumns()) {
        if (new Cell(loadCells.get(i).getId(), col).equals(loadCells.get(i))) {
          columns[i] = col;
          break;
        }
      }
      ids.add(loadCells.get(i).getId());
    }
    String tableIdField = param.getTable() + Table.POSTFIX_TABLE_ID;
    filter.add(new Data(tableIdField + Data.IN_POSTFIX, ids, "Long"));
    columns[columns.length - 1] = tableIdField;
    TransportDataSet transportDataSet = baseDao.getData(processDefinitionId, param.getSchema(), param.getTable(), columns, null, null, null, null, null, null, filter, null, null, param.getLinkColumns());
    Map<Cell, Object> allLoadedCells = new HashMap<Cell, Object>();
    for (TransportData transportData : transportDataSet.getSets()) {
      Map<Cell, Object> loadedCells = new HashMap<Cell, Object>();
      Long id = null;
      for (Data data : transportData.getData()) {
        if (data.getField().equalsIgnoreCase(tableIdField)) {
          id = (Long) data.getValue();
          continue;
        }
        loadedCells.put(new Cell(-1l, data.getField()), data.getValue());
      }
      if (id == null)
        throw new RuntimeException("undefined id column");
      for (Cell cell : loadedCells.keySet()) {
        cell.setId(id);
      }
      allLoadedCells.putAll(loadedCells);
    }
    return allLoadedCells;
  }


  protected List<Map<String, Object>> render(Long processDefinitionId, FullTableParam param, List<Map<String, Object>> treeRoots, MetadataEditableTreeGrid editableTreeGrid, HttpServletRequest request) {
    if (editRuleMap == null)
      editRuleMap = new HashMap<String, Boolean>();

    List<Map<String, Object>> treeRootsClone = new ArrayList<Map<String, Object>>();
    for (Map<String, Object> row : treeRoots) {
      Map<String, Object> newRow = new HashMap<String, Object>();
      treeRootsClone.add(newRow);
      for (String column : row.keySet()) {
        newRow.put(column, row.get(column));
      }
    }

    treeRoots = treeRootsClone;
    for (Map<String, Object> item : treeRoots) {
      Long id = (Long) item.get("id");
      Integer level = getLevel(processDefinitionId, id, param, request);
      Map<String, Object> cssColumns = new HashMap<String, Object>();
      for (String column : item.keySet()) {
        if ("leaf".equals(column) || "id".equals(column) || "parent_id".equals(column) || column.contains("_CSS"))
          continue;
        Binding binding = new Binding();
        binding.setVariable("col", column);
        binding.setVariable("id", id);
        binding.setVariable("row", item);
        binding.setVariable("level", level);

        GroovyShell groovyShell = new GroovyShell(binding);
        if (editableTreeGrid.getCssClass() != null) {
          String cssClass = (String) groovyShell.evaluate(editableTreeGrid.getCssClass());
          if (cssClass == null)
            cssClass = "";
          cssColumns.put(column + "_CSS", cssClass);
        }

        if (editableTreeGrid.getDependencyMatrix().getDependencies().get(new Cell(id, column)) != null) {
          editRuleMap.put(id.toString().concat(":").concat(column), Boolean.FALSE);
          continue;
        }
        String[] nameCol = column.split(":");
        groovyShell = new GroovyShell(binding);
        if (editableTreeGrid.getEditableRule() != null) {
          Boolean editableRule = (Boolean) groovyShell.evaluate(editableTreeGrid.getEditableRule());
          if (nameCol.length == 5) {
            String name = nameCol[4];
            for (String columnTmp : item.keySet()) {
              columnTmp = columnTmp.replace(WfeRunaVariables.LINK_COLUMN_PREFIX, "");
              if (columnTmp.equals(name))
                editRuleMap.put(id.toString().concat(":").concat(column), item.get(column) != null && Boolean.TRUE.equals(editableRule));
            }
          }
          else
            editRuleMap.put(id.toString().concat(":").concat(column), Boolean.TRUE.equals(editableRule));
        }
      }
      item.putAll(cssColumns);
    }
    return treeRoots;
  }

  protected Integer getLevel(Long processDefinitionId, Long id, FullTableParam param, HttpServletRequest request) {
    Map<Long, String> parents = (Map<Long, String>) request.getSession().getAttribute(PARENTS_SESSION_KEY);
    if (parents.get(id) == null) {
      Cell parentCell = new Cell(id, param.getParentCol());
      Map<Cell, Object> loadedValues = getCellsFromDB(processDefinitionId, Arrays.asList(parentCell), param, request);
      if (loadedValues.get(parentCell) == null) {
        parents.put(id, "root");
      }
      else {
        parents.put(id, loadedValues.get(parentCell).toString());
      }
    }
    if ("root".equals(parents.get(id))) {
      return 0;
    }
    Long parentId = Long.valueOf(parents.get(id));
    return getLevel(processDefinitionId, parentId, param, request) + 1;
  }

  protected GroovyShell getGroovyShell(Binding binding) {
    return new GroovyShell(binding);
  }

  protected TransportData getFilter(String tableId, HttpServletRequest request) {
    HttpSession session = request.getSession();
    TransportData filterData;
    Long taskId = (Long) session.getAttribute(TasksController.CURRENT_TASK_ID);
    WfTask taskStub = runaWfeService.getTask(SecurityUtils.getCurrentRunaUser(), taskId);
    Long processInstanceId = taskStub.getProcessId();
    String filterKey = WfeRunaVariables.getFilterKeyVariable(tableId);
    filterData = GsonUtil.getObjectFromJson(baseDao.getVariableFromDb(processInstanceId, filterKey), TransportData.class);


    String jsonData = baseDao.getVariableFromDb(processInstanceId, WfeRunaVariables.getDefaultFilterKeyVariable(tableId));
    TransportData defaultFilterData = GsonUtil.getObjectFromJson(jsonData, TransportData.class);
    if (filterData == null) {
      filterData = defaultFilterData;
    }
    if (defaultFilterData != null) {
      for (Data data : defaultFilterData.getData())
        if (WfeRunaVariables.CURRENT_USER_CODE.equals(data.getValue())) {
          data.setValue(SecurityUtils.getCurrentUser().getFullName());
          break;
        }
      filterData.getData().addAll(defaultFilterData.getData());
    }
    if (filterData == null)
      filterData = new TransportData();
    return filterData;
  }

  protected List<Map<String, Object>> loadData(String node, Long processDefinitionId, String tableId, FullTableParam param, HttpServletRequest request) {

    String schema = param.getSchema();
    String table = param.getTable();
    String[] fields = param.getColumns();
    String linkColumns = param.getLinkColumns();
    String parentCol = param.getParentCol();

    HttpSession session = request.getSession();
    Map<String, List<Long>> children = (Map<String, List<Long>>) session.getAttribute(CHILDREN_SESSION_KEY);
    Map<Long, String> parents = (Map<Long, String>) session.getAttribute(CHILDREN_SESSION_KEY);
    if (children == null) {
      children = new HashMap<String, List<Long>>();
    }
    if (parents == null)
      parents = new HashMap<Long, String>();

    TransportData filterData = getFilter(tableId, request);
    if (ROOT_NODE.equals(node)) {
      filterData.add(new Data(parentCol + Data.IS_NULL_POSTFIX, Long.MIN_VALUE, "Long"));
      children.clear();
      parents.clear();
    }
    else {
      filterData.add(new Data(parentCol + Data.EQ_POSTFIX, Long.valueOf(node), "Long"));
    }

    List<Long> childrenList = children.get(node);
    if (childrenList == null) {
      childrenList = new ArrayList<Long>();
      children.put(node, childrenList);
    }

    TransportDataSet transportDataSet = baseDao.getData(processDefinitionId, schema, table, fields, null, null,
        null, null, null, null, filterData, null, null, linkColumns);

    List<Map<String, Object>> values = new ArrayList<Map<String, Object>>();
    String tableFieldId = table + Table.POSTFIX_TABLE_ID;
    for (TransportData transportData : transportDataSet.getSets()) {
      Map<String, Object> item = new HashMap<String, Object>();

      Data pkTableData = transportData.getData(tableFieldId);
      if (pkTableData == null) {
        throw new EditableTreeGridException(tableFieldId + " does not exist, table must have primary key with name " + tableFieldId);
      }
      Long id = (Long) pkTableData.getValue();
      item.put("id", id);
      childrenList.add(id);
      parents.put(id, node);
      item.putAll(dataToMap(table, transportData));
      item.put("leaf", false);
      values.add(item);
    }
    session.setAttribute(CHILDREN_SESSION_KEY, children);
    session.setAttribute(PARENTS_SESSION_KEY, parents);
    return values;
  }

  private Map<String, Object> dataToMap(String table, TransportData transportData) {
    String tableFieldId = table + Table.POSTFIX_TABLE_ID;
    Map<String, Object> item = new HashMap<String, Object>();
    for (Data data : transportData.getData()) {
      String field = data.getField();
      if (field.equals(tableFieldId)) {
        item.put("id", data.getValue());
        continue;
      }

      Object value = data.getValue();

      if (value instanceof Date) {
        SimpleDateFormat dateFormat = ClassType.DATE == data.getClassType() ? DateFormat.getDateFormat() : DateFormat.getDateTimeFormat();
        value = dateFormat.format((Date) value);
      }
      item.put(field.replace(";", "").replace(".", ""), value);
    }
    return item;
  }

  public String getOriginalColumn(Cell cell, String[] columns) {
    for (String column : columns) {
      if (new Cell(cell.getId(), column).equals(cell)) {
        return column;
      }
    }
    throw new RuntimeException("original column for " + cell + " not found");
  }

  protected List<CellValue> setNewValueToCell(Long processDefinitionId, Cell cell, Object newValue, DependencyMatrix dependencyMatrix, FullTableParam param, HttpServletRequest request) {
    Map<Cell, Object> cache = new HashMap<Cell, Object>();
    List<CellValue> changes = new ArrayList<CellValue>();
    if (dependencyMatrix.getDependencies().get(cell) != null)
      throw new RuntimeException("nobody can change cell with business rules, do you understand me?:) NOBODY!!!!!!!!!!!!!!!!");

    baseDao.updateTreeCell(getOriginalColumn(cell, param.getColumns()), cell.getId(), newValue, processDefinitionId, getFilter(param.getTableId(), request));
    for (Cell mainCell : dependencyMatrix.getDependencies().keySet()) {
      if (dependencyMatrix.getDependencies().get(mainCell).getDependency().contains(cell)) {
        Object value = recalculateCell(processDefinitionId, getOriginalColumn(mainCell, param.getColumns()), Float.MIN_VALUE, mainCell, cache, dependencyMatrix, param, request);
        changes.add(new CellValue(new Cell(mainCell.getId(), getOriginalColumn(mainCell, param.getColumns()), true), value));
      }
    }
    return changes;
  }

  @RequestMapping(value = "/checkTreeCellEdit", method = RequestMethod.GET)
  @ResponseBody
  public boolean isCellEditable(
      @RequestParam("column") String column,
      @RequestParam("rowId") String rowId,
      HttpServletRequest request) {
    return (Boolean.TRUE.equals(editRuleMap.get(rowId.concat(":").concat(column))));
  }

  @RequestMapping(value = "/updateTreeCellValue", method = RequestMethod.GET)
  @ResponseBody
  public List<CellValue> updateTreeCellValue(
      @RequestParam("parentCol") String parentCol,
      @RequestParam("s") String schema,
      @RequestParam("t") String table,
      @RequestParam("fields") String strColumns,
      @RequestParam("linkColumns") String linkColumns,
      @RequestParam("column") String column,
      @RequestParam("rowId") Long rowId,
      @RequestParam("value") String value,
      @RequestParam("originalValue") String originalValue,
      @RequestParam("tableId") String tableId,
      @RequestParam("processDefinitionId") Long processDefinitionId,
      HttpServletRequest request) {
    if (!value.equals(originalValue)) {
      String[] columnArray = strColumns.split(",");
      for (int i = 0; i < columnArray.length; i++)
        columnArray[i] = columnArray[i].trim();

      FullTableParam editableTreeGridParam = new FullTableParam(schema, table, columnArray, linkColumns, parentCol, tableId);
      DependencyMatrix dependencyMatrix = getMetadataEditableTreeGrid(tableId).getDependencyMatrix();
      dependencyMatrix.refresh(processDefinitionId);
      return setNewValueToCell(processDefinitionId, new Cell(rowId, column), value, dependencyMatrix, editableTreeGridParam, request);
    }
    return null;
  }
}


//e.grid.getStore().getNodeById(100).set('town_planning:df_tep_assign:tepname','ss'):