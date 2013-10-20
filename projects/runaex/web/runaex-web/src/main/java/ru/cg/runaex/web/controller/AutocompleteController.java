package ru.cg.runaex.web.controller;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;

import ru.cg.runaex.components.UnicodeSymbols;
import ru.cg.runaex.database.bean.transport.Data;
import ru.cg.runaex.database.bean.transport.TransportData;
import ru.cg.runaex.database.bean.transport.TransportDataSet;
import ru.cg.runaex.database.dao.BaseDao;
import ru.cg.runaex.web.json.ListMappingJacksonJsonView;

/**
 * @author Петров А.
 */
@Controller
public class AutocompleteController {

  private static final Logger logger = LoggerFactory.getLogger(AutocompleteController.class);

  @Autowired
  private BaseDao baseDao;

  @RequestMapping(value = "/autocomplete", method = RequestMethod.POST)
  public ModelAndView load(
      @RequestParam("references") String references,
      @RequestParam("query") String query,
      @RequestParam("sortorder") String sortOrder,
      @RequestParam("processDefinitionId") Long processDefinitionId,
      @RequestParam(value = "relatedFieldValue", required = false) String relatedFieldValue,
      @RequestParam(value = "relatedLinkTable", required = false) String relatedLinkTable,
      @RequestParam(value = "relatedTableColumn", required = false) String relatedTableColumn,
      HttpServletResponse response) {
    if (query != null && !query.isEmpty() && query.lastIndexOf(UnicodeSymbols.STAR) == -1) {
      query = query.concat("*");
    }

    String refSchema, refTable, refField, refPkTableField;
    String[] tmp = references.split(UnicodeSymbols.POINT);
    refSchema = tmp[0];
    refTable = tmp[1];
    refField = tmp[2];
    refPkTableField = refTable + "_id";
    logger.debug("refSchema - " + refSchema);
    logger.debug("refTable - " + refTable);
    logger.debug("refField - " + refField);
    logger.debug("refPkTableField - " + refPkTableField);

    String[] refFields = new String[] {refField, refPkTableField};
    logger.debug("refFields - " + refFields);

    TransportData filterData = new TransportData();
    if (relatedTableColumn != null && relatedFieldValue != null) {
      Data relatedFieldData = new Data();
      relatedFieldData.setValueClass("Long");
      relatedFieldData.setValue(Long.valueOf(relatedFieldValue));
      relatedFieldData.setField(relatedTableColumn);
      if (relatedLinkTable != null) {
        String[] splitedRelatedLinkTable = relatedLinkTable.split("[.]");
        String relatedLinkTableName = splitedRelatedLinkTable.length > 1 ? splitedRelatedLinkTable[1] : splitedRelatedLinkTable[0];
        relatedFieldData.setTable(relatedLinkTableName);
        // Trick. Swap tables to correct inner join (main table is referenced table, joined is link table)
        relatedLinkTable = (refSchema != null && !refSchema.isEmpty() ? refSchema.concat(".") : "").concat(refTable);
        refSchema = splitedRelatedLinkTable.length > 1 ? splitedRelatedLinkTable[0] : null;
        refTable = relatedLinkTableName;
      }
      else {
        relatedFieldData.setTable(refTable);
      }
      filterData.add(relatedFieldData);
    }

    TransportDataSet transportDataSet = baseDao.getData(processDefinitionId, refSchema, refTable, refFields, null, null, refField, sortOrder,
        query, refField, filterData, "dummy", relatedLinkTable, null);

    ModelAndView mv = new ModelAndView(new ListMappingJacksonJsonView());
    mv.addObject(process(transportDataSet, refPkTableField, refField));
    return mv;
  }

  /**
   * Получить список даныхх
   *
   * @param transportDataSet - данные
   * @param refPkTableId     - наименование поля ид ссылочной таблицы
   * @param refField         - наименование поля ссылочной таблицы
   * @return возвращает список даныхх
   */
  private static List<Map<String, String>> process(TransportDataSet transportDataSet, String refPkTableId, String refField) {
    List<Map<String, String>> result = new ArrayList<Map<String, String>>(transportDataSet.getSets().size());

    if (transportDataSet.getSets() == null) {
      return result;
    }

    for (TransportData transportData : transportDataSet.getSortSets()) {
      Map<String, String> item = new HashMap<String, String>();
      Data id = transportData.getData(refPkTableId);
      Data value = transportData.getData(refField);
      item.put("id", "" + id.getValue());
      item.put("label", "" + value.getValue());
      item.put("value", "" + value.getValue());
      result.add(item);
    }

    return result;
  }
}
