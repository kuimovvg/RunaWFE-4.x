package ru.cg.runaex.web.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.view.json.MappingJacksonJsonView;
import ru.cg.fias.search.core.server.bean.AddressSphinx;
import ru.cg.fias.search.core.server.component.reader.SphinxStrAddressByGuidReader;
import ru.cg.fias.search.core.server.sphinx.SphinxException;
import ru.cg.runaex.components.UnicodeSymbols;
import ru.cg.runaex.components.WfeRunaVariables;
import ru.cg.runaex.components.bean.session.ObjectInfo;
import ru.cg.runaex.core.DateFormat;
import ru.cg.runaex.database.bean.transport.ClassType;
import ru.cg.runaex.database.bean.transport.Data;
import ru.cg.runaex.database.bean.transport.TransportData;
import ru.cg.runaex.database.bean.transport.TransportDataSet;
import ru.cg.runaex.web.security.SecurityUtils;
import ru.cg.runaex.database.dao.BaseDao;
import ru.cg.runaex.database.dao.MetadataDao;
import ru.cg.runaex.database.util.GsonUtil;
import ru.cg.runaex.web.service.RunaWfeService;
import ru.runa.wfe.security.AuthenticationException;
import ru.runa.wfe.security.AuthorizationException;
import ru.runa.wfe.task.TaskDoesNotExistException;
import ru.runa.wfe.task.dto.WfTask;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.regex.Pattern;

/**
 * @author Петров А.
 */
@Controller
public class FlexiGridController {
  private final Logger logger = LoggerFactory.getLogger(getClass());

  private static final Pattern pattern = Pattern.compile("\\w*\\-\\w*\\-\\w*\\-\\w*\\-\\w*");

  @Autowired
  private RunaWfeService runaWfeService;
  @Autowired
  private BaseDao baseDao;
  @Autowired
  private MetadataDao metadataDao;
  @Autowired
  private SphinxStrAddressByGuidReader sphinxStrAddressByGuidReader;

  @RequestMapping(value = "/flexigrid", method = RequestMethod.POST)
  public ModelAndView loadData(
      @RequestParam("page") Integer page,
      @RequestParam("rp") Integer pageSize,
      @RequestParam("sortname") String sortName,
      @RequestParam("sortorder") String sortOrder,
      @RequestParam("query") String query,
      @RequestParam("qtype") String qType,
      @RequestParam("s") String schema,
      @RequestParam("t") String table,
      @RequestParam("tableId") String tableId,
      @RequestParam("clearfilter") Boolean clearFilter,
      @RequestParam("columns") String strColumns,
      @RequestParam("processDefinitionId") Long processDefinitionId,
      @RequestParam(value = "dependent", required = false) Boolean dependent,
      @RequestParam(value = "object1", required = false) String object1,
      @RequestParam(value = "object2", required = false) String object2,
      HttpServletRequest request, HttpServletResponse response) throws SphinxException, AuthorizationException, AuthenticationException, TaskDoesNotExistException {

    HttpSession session = request.getSession();

    if (clearFilter == null) {
      clearFilter = Boolean.FALSE;
    }

    String[] fields = strColumns.split(",");
    logger.debug("strFields - " + Arrays.toString(fields));

    int from = (page - 1) * pageSize;
    TransportData filterData = new TransportData();
    Long taskId = (Long) session.getAttribute(TasksController.CURRENT_TASK_ID);
    WfTask taskStub = runaWfeService.getTask(SecurityUtils.getCurrentRunaUser(), taskId);
    Long processInstanceId = taskStub.getProcessId();
    String filterKey = WfeRunaVariables.getFilterKeyVariable(tableId);
    if (!clearFilter) {
      filterData = GsonUtil.getObjectFromJson(baseDao.getVariableFromDb(processInstanceId, filterKey), TransportData.class);
    }
    else {
      baseDao.removeVariableFromDb(filterKey, processInstanceId);
    }

    /**
     * add filter on link table
     */
    if (object1 != null && !object1.isEmpty() && (object2 != null && !object2.isEmpty() || dependent != null && dependent)) {
      Long selectedObject1Id = null;
      String selectedObject1Table = null;

      Map<Long, String> objectInfoMap = baseDao.getObjectInfoFromDb(processInstanceId);
      for (Long key : objectInfoMap.keySet()) {
        ObjectInfo tmpObjectInfo = GsonUtil.getObjectFromJson(objectInfoMap.get(key), ObjectInfo.class);
        if (object1.equals(tmpObjectInfo.toString())) {
          selectedObject1Id = tmpObjectInfo.getId();
          selectedObject1Table = tmpObjectInfo.getTable();
          break;
        }
      }

      List<Data> list = new ArrayList<Data>();
      Data filterObject1 = new Data();
      filterObject1.setTable(table);
      filterObject1.setField(selectedObject1Table + "_id");
      filterObject1.setValue(selectedObject1Id);
      filterObject1.setValueClass("Long");
      list.add(filterObject1);
      filterData = new TransportData(0, list);
    }

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

    TransportDataSet transportDataSet;
    if (dependent == null || !dependent)
      transportDataSet = baseDao.getData(processDefinitionId, schema, table, fields, from, pageSize,
          sortName, sortOrder, query, qType, filterData, object1, object2, null);
    else {
      transportDataSet = baseDao.getData(processDefinitionId, schema, table, fields, from, pageSize,
          sortName, sortOrder, query, qType, filterData, object1, null, null);
    }

    int size = transportDataSet.getRowCounts() != null ? transportDataSet.getRowCounts() : 0;
    ModelAndView mv = new ModelAndView(new MappingJacksonJsonView());
    mv.addObject("page", page);
    mv.addObject("total", size);
    mv.addObject("rows", process(transportDataSet, table + "_id", metadataDao.getFiasColumns(processDefinitionId, schema, table)));

    return mv;
  }

  private List<Map<String, Object>> process(TransportDataSet transportDataSet, String tableFieldId, Collection<String> addressColumns) throws SphinxException {
    List<Map<String, Object>> result = new ArrayList<Map<String, Object>>();

    if (transportDataSet.getSets() == null) {
      return result;
    }

    for (TransportData transportData : transportDataSet.getSortSets()) {
      Map<String, Object> item = new HashMap<String, Object>();

      Data pkTableData = transportData.getData(tableFieldId);
      if (pkTableData != null)
        item.put("id", pkTableData.getValue());

      Map<String, Object> cell = new HashMap<String, Object>(transportData.getData().size());
      for (Data data : transportData.getData()) {
        if (data.isPk()) {
          item.put("id", data.getValue());
          continue;
        }
        String field = data.getField();
        if (field.equals(tableFieldId))
          continue;

        Object value = data.getValue();
        if (value instanceof Date) {
          SimpleDateFormat dateFormat = ClassType.DATE == data.getClassType() ? DateFormat.getDateFormat() : DateFormat.getDateTimeFormat();
          value = dateFormat.format((Date) value);
        }

        if (value != null && addressColumns.contains(field) && value instanceof String) {
          try {
            if (pattern.matcher((String) value).matches()) {
              List<AddressSphinx> addressSphinxes = sphinxStrAddressByGuidReader.search((String) value, 1);
              if (addressSphinxes != null && addressSphinxes.size() == 1) {
                value = SphinxStrAddressByGuidReader.fullAddress(addressSphinxes.get(0));
              }
              else {
                logger.error("sphinxStrAddressByGuidReader return zero or null " + value);
              }
            }
          }
          catch (SphinxException e) {
            logger.error(e.getMessage(), e);
            throw e;
          }
        }

        if (value instanceof BigDecimal) {
          value = ((BigDecimal) value).toPlainString();
        }

        cell.put(field, value);
      }
      item.put("cell", cell);
      result.add(item);
    }

    return result;
  }

  @RequestMapping(value = "/saveSelectedRow", method = RequestMethod.POST)
  public void saveSelectedRow(@RequestParam("piid") Long piid,
                              @RequestParam("rowId") Long rowId,
                              @RequestParam("schema") String schema,
                              @RequestParam("table") String table,
                              @RequestParam(value = "field", required = false) String field,
                              @RequestParam(value = "baseObject", required = false) String baseObject,
                              @RequestParam(value = "object2", required = false) String object2,
                              @RequestParam(value = "link", required = false) String link,
                              HttpServletResponse response) throws IOException {

    if (rowId == null) {
      baseDao.removeVariableFromDb(WfeRunaVariables.SELECTED_OBJECT_INFO, piid);
      return;
    }

    String selectedSchemaRow = schema.trim();
    String selectedTableRow = table.trim();
    String selectedTableBaseObjectRow = baseObject != null ? baseObject.trim() : null;
    String selectedTableAttachableObjectRow = object2 != null ? object2.trim() : null;
    String trimmedLink = link != null ? link.trim() : null;

    if (trimmedLink != null) {
      selectedTableAttachableObjectRow = selectedSchemaRow + "." + selectedTableRow;
      String[] tmp = trimmedLink.split(UnicodeSymbols.POINT);
      selectedSchemaRow = tmp[0];
      selectedTableRow = tmp[1];
    }

    ObjectInfo selectedObjectInfo = new ObjectInfo();
    selectedObjectInfo.setId(rowId);
    selectedObjectInfo.setSchema(selectedSchemaRow);
    selectedObjectInfo.setTable(selectedTableRow);
    if (field != null)
      selectedObjectInfo.setSelectTreeGridfield(field.trim());
    if (!WfeRunaVariables.isEmpty(selectedTableBaseObjectRow)) {
      String[] tmp = new String[0];
      if (selectedTableBaseObjectRow != null) {
        tmp = selectedTableBaseObjectRow.split(UnicodeSymbols.POINT);
      }
      ObjectInfo base = new ObjectInfo();
      base.setSchema(tmp[0]);
      base.setTable(tmp[1]);
      selectedObjectInfo.setBase(base);
    }
    if (!WfeRunaVariables.isEmpty(selectedTableAttachableObjectRow)) {
      String[] tmp = new String[0];
      if (selectedTableAttachableObjectRow != null) {
        tmp = selectedTableAttachableObjectRow.split(UnicodeSymbols.POINT);
      }
      ObjectInfo attachable = new ObjectInfo();
      attachable.setSchema(tmp[0]);
      attachable.setTable(tmp[1]);
      attachable.setId(rowId);
      selectedObjectInfo.setAttachable(attachable);
      selectedObjectInfo.setId(null);
    }

    if (!selectedObjectInfo.isEmpty())
      baseDao.addVariableToDb(piid, WfeRunaVariables.SELECTED_OBJECT_INFO, GsonUtil.toJson(selectedObjectInfo));
  }
}
