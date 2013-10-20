package ru.cg.runaex.web.controller;

import java.io.IOException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.Arrays;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import ru.cg.runaex.components.util.FileUploadComponentHelper;
import ru.cg.runaex.database.bean.transport.Data;
import ru.cg.runaex.database.bean.transport.TransportData;
import ru.cg.runaex.database.bean.transport.TransportDataSet;
import ru.cg.runaex.database.dao.BaseDao;
import ru.cg.runaex.generatedb.bean.Table;

/**
 * @author Петров А.
 */
@Controller
public class DownloadFileController {
  @Autowired
  private BaseDao baseDao;

  @RequestMapping(value = "/downloadfile", method = RequestMethod.POST)
  public void download(
      @RequestParam("id") Long id,
      @RequestParam("schema") String schema,
      @RequestParam("table") String table,
      @RequestParam("field") String field,
      @RequestParam("processDefinitionId") Long processDefinitionId,
      HttpServletResponse response) throws IOException {

    schema = URLDecoder.decode(schema, "UTF-8");
    table = URLDecoder.decode(table, "UTF-8");
    field = URLDecoder.decode(field, "UTF-8");
    String nameColumn = FileUploadComponentHelper.getNameColumn(field);
    String dataColumn = FileUploadComponentHelper.getDataColumn(field);

    Data filterData = new Data();
    filterData.setField(table.concat(Table.POSTFIX_TABLE_ID));
    filterData.setValue(id);
    TransportData filterTransportData = new TransportData(0, Arrays.asList(filterData));

    String[] fields = new String[] {nameColumn, dataColumn};
    TransportDataSet dataSet = baseDao.getData(processDefinitionId, schema, table, fields,
        0, 1, null, null, null, null, filterTransportData, null, null, null);

    if (dataSet.getRowCounts() == 0) {
      response.getWriter().print("File not found");
      return;
    }

    TransportData data = dataSet.getSortSets().get(0);
    byte[] filedata = (byte[]) data.getData(dataColumn).getValue();
    String filename = (String) data.getData(nameColumn).getValue();
    response.setHeader("Content-Disposition", "attachment; filename=".concat(URLEncoder.encode(filename, "UTF-8")));
    response.getOutputStream().write(filedata);
  }
}
