package ru.cg.runaex.web.controller;

import java.text.SimpleDateFormat;
import java.util.*;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;

import ru.cg.runaex.components.UnicodeSymbols;
import ru.cg.runaex.core.DateFormat;
import ru.cg.runaex.database.bean.transport.ClassType;
import ru.cg.runaex.database.bean.transport.Data;
import ru.cg.runaex.database.bean.transport.TransportData;
import ru.cg.runaex.database.bean.transport.TransportDataSet;
import ru.cg.runaex.database.dao.BaseDao;
import ru.cg.runaex.web.json.ListMappingJacksonJsonView;

/**
 * @author Петров А.
 */
@Controller
public class TreeGridController {

  @Autowired
  private BaseDao baseDao;

  @RequestMapping(value = "/treegrid", method = RequestMethod.GET)
  public ModelAndView load(
      @RequestParam("schema") String schema,
      @RequestParam("table") String table,
      @RequestParam("fields") String strFields,
      @RequestParam("processDefinitionId") Long processDefinitionId,
      @RequestParam(value = "parentId", required = false) String parentId,
      HttpServletResponse response) {

    ModelAndView mv = new ModelAndView(new ListMappingJacksonJsonView());
    String[] tmp;
    String[] splitedFields = strFields.split(";");
    String[] fields = new String[splitedFields.length + 1];
    fields[0] = table.concat("_parent_id");
    int idx = 1;
    for (String f : splitedFields) {
      tmp = f.split(UnicodeSymbols.COMMA);
      String fieldName = "";
      /**
       * exist filed name
       */
      if (tmp.length == 1) {
        fieldName = tmp[0].trim();
      }
      /**
       * exist Display name and filed name
       */
      else if (tmp.length == 2 || (tmp.length == 4 && tmp[2].trim().isEmpty())) {
        fieldName = tmp[1].trim();
      }
      /**
       * exist Display name, filed name and references
       */
      else if (tmp.length == 4 && !tmp[2].trim().isEmpty()) {
        fieldName = tmp[1].trim() + ";" + tmp[2].trim();
      }
      fields[idx] = fieldName;
      ++idx;
    }

    String filterField = table.concat("_parent_id");
    if (parentId == null) {
      parentId = String.valueOf(Long.MIN_VALUE);
      filterField = table + "_parent_id_is_null";
    }

    List<Data> list = new ArrayList<Data>();
    Data filterObject1 = new Data();
    filterObject1.setTable(table);
    filterObject1.setField(filterField);
    filterObject1.setValue(parentId);
    list.add(filterObject1);
    TransportData filter = new TransportData(0, list);

    TransportDataSet transportDataSet = baseDao.getData(processDefinitionId, schema, table, fields, null, null,
        null, null, null, null, filter, null, null, null);
    mv.addObject(process(transportDataSet, table.concat("_id"), table.concat("_parent_id")));

    return mv;
  }

  private List<Map<String, Object>> process(TransportDataSet transportDataSet, String tableFieldId, String tableFieldParentId) {
    List<Map<String, Object>> result = new ArrayList<Map<String, Object>>();

    if (transportDataSet.getSets() == null) {
      return result;
    }

    for (TransportData transportData : transportDataSet.getSortSets()) {
      Map<String, Object> item = new HashMap<String, Object>();

      Data pkTableData = transportData.getData(tableFieldId);
      if (pkTableData != null)
        item.put("key", pkTableData.getValue());

      String title = "";
      Long parentId = null;
      for (Data data : transportData.getData()) {
        if (data.isPk()) {
          continue;
        }
        String field = data.getField();
        if (field.equals(tableFieldId))
          continue;
        if (field.equals(tableFieldParentId)) {
          parentId = (Long) data.getValue();
          continue;
        }
        Object value = data.getValue();
        if (value instanceof Date) {
          SimpleDateFormat dateFormat = ClassType.DATE == data.getClassType() ? DateFormat.getDateFormat() : DateFormat.getDateTimeFormat();
          value = dateFormat.format((Date) value);
        }
        if (value == null) {
          value = "";
        }
        title = title + "~" + value;
      }
      item.put("title", title);
      item.put("parentId", parentId);
      item.put("isLazy", true);
      item.put("children", new ArrayList());
      result.add(item);
    }

    return result;
  }
}
