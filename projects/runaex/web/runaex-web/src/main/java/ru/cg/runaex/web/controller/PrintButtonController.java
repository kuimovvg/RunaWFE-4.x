package ru.cg.runaex.web.controller;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.net.URLEncoder;
import java.sql.Connection;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import net.sf.jasperreports.engine.JRException;
import net.sf.jasperreports.engine.JasperReport;
import net.sf.jasperreports.engine.data.JRMapCollectionDataSource;
import net.sf.jasperreports.engine.util.JRLoader;
import org.apache.commons.io.FileUtils;
import org.apache.poi.hssf.usermodel.HSSFRichTextString;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.CellRangeAddress;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.support.ResourceBundleMessageSource;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.ConnectionCallback;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import ru.runa.wfe.security.AuthenticationException;
import ru.runa.wfe.security.AuthorizationException;
import ru.runa.wfe.task.TaskDoesNotExistException;
import ru.runa.wfe.task.dto.WfTask;

import com.cg.jul.exporter.jasperreports.Exporter;
import com.cg.jul.exporter.jasperreports.ReportFormat;
import ru.cg.fias.search.core.server.bean.AddressSphinx;
import ru.cg.fias.search.core.server.component.reader.SphinxStrAddressByGuidReader;
import ru.cg.fias.search.core.server.sphinx.SphinxException;
import ru.cg.runaex.components.WfeRunaVariables;
import ru.cg.runaex.components.bean.component.part.Column;
import ru.cg.runaex.components.bean.component.part.StoredProcedure;
import ru.cg.runaex.components.bean.session.ObjectInfo;
import ru.cg.runaex.components.parser.StoredProcedureParser;
import ru.cg.runaex.core.DateFormat;
import ru.cg.runaex.database.bean.transport.ClassType;
import ru.cg.runaex.database.bean.transport.Data;
import ru.cg.runaex.database.bean.transport.TransportData;
import ru.cg.runaex.database.bean.transport.TransportDataSet;
import ru.cg.runaex.database.dao.BaseDao;
import ru.cg.runaex.database.dao.MetadataDao;
import ru.cg.runaex.database.dao.ReportDao;
import ru.cg.runaex.database.exception.DataAccessCommonException;
import ru.cg.runaex.database.util.GsonUtil;
import ru.cg.runaex.web.security.SecurityUtils;
import ru.cg.runaex.web.service.RunaWfeService;

/**
 * @author Петров А.
 */
@Controller
public class PrintButtonController {

  private static final Logger logger = LoggerFactory.getLogger(PrintButtonController.class);
  private static final Gson GSON = GsonUtil.getGsonObject();
  private static final String IS_IT_FIAS_PREFIX = "it_is_fias_address";
  private static final String SELECTED_OBJECT_ID = "SELECTED_OBJECT_ID";
  private static final String MAIN_DATA_SOURCE = "MAIN_DATA_SOURCE";

  @Autowired
  private BaseDao baseDao;
  @Autowired
  private MetadataDao metadataDao;
  @Autowired
  private ReportDao reportDao;
  @Autowired
  private RunaWfeService runaWfeService;
  @Autowired
  private SphinxStrAddressByGuidReader sphinxStrAddressByGuidReader;

  @Autowired
  @Qualifier("reportTemplateMessageSource")
  private ResourceBundleMessageSource messages;

  @RequestMapping(value = "/print", method = RequestMethod.POST)
  public void print(@RequestParam(value = "schema", required = false) String schema,
                    @RequestParam(value = "table", required = false) String table,
                    @RequestParam(value = "columns", required = false) String columns,
                    @RequestParam(value = "tableId", required = false) String tableId,
                    @RequestParam(value = "object1", required = false) String object1,
                    @RequestParam(value = "object2", required = false) String object2,
                    @RequestParam(value = "sortname", required = false) String sortName,
                    @RequestParam(value = "sortorder", required = false) String sortOrder,
                    @RequestParam(value = "formData", required = false) String formData,
                    @RequestParam(value = "processDefinitionId", required = false) Long processDefinitionId,
                    @RequestParam(value = "templateFileName", required = false) String templateFileName,
                    @RequestParam(value = "reportType", required = false) String reportType,
                    @RequestParam(value = "storedProcedures", required = false) String storedProcedures,
                    HttpServletRequest request, HttpServletResponse response) throws AuthorizationException, AuthenticationException, TaskDoesNotExistException, IOException, JRException, SphinxException {

    File report = null;
    Workbook workbook = null;
    ReportFormat reportFormat = null;
    try {
      Long taskId = (Long) request.getSession().getAttribute(TasksController.CURRENT_TASK_ID);
      WfTask task = runaWfeService.getTask(SecurityUtils.getCurrentRunaUser(), taskId);
      Long processInstanceId = task.getProcessId();
      String taskName = task.getName();
      String processName = task.getDefinitionName();
      if (reportType != null) {
        reportFormat = ReportFormat.valueOf(reportType);
      }

      ObjectInfo selectedObjectInfo = null;
      String strSelectedObjectInfo = baseDao.getVariableFromDb(processInstanceId, WfeRunaVariables.SELECTED_OBJECT_INFO);
      if (strSelectedObjectInfo != null && !strSelectedObjectInfo.isEmpty())
        selectedObjectInfo = GsonUtil.getObjectFromJson(strSelectedObjectInfo, ObjectInfo.class);

      boolean templateExists = templateFileName != null && reportType != null;
      if (formData != null) {
        if (templateExists) {
          report = exportFormByJasper(processDefinitionId, formData, templateFileName, reportFormat, storedProcedures, selectedObjectInfo);
        }
        else {
          workbook = exportFormToCSV(formData, taskName, processName);
        }
      }
      else {
        String[] splitedColumns = columns.split(",");
        String[] dbColumns = new String[splitedColumns.length];
        String[] displayColumns = new String[splitedColumns.length];

        for (int i = 0; i < splitedColumns.length; ++i) {
          String[] splitedColumn = splitedColumns[i].split("::");
          dbColumns[i] = splitedColumn[0];
          displayColumns[i] = splitedColumn[1];
        }

        TransportDataSet transportDataSet = getTransportDataSet(processDefinitionId, schema, table, tableId, object1, object2, sortName, sortOrder, processInstanceId, dbColumns);

        if (templateExists) {
          report = exportTableByJasper(processDefinitionId, schema, table, templateFileName, reportFormat, dbColumns, displayColumns, transportDataSet, storedProcedures, selectedObjectInfo);
        }
        else {
          workbook = exportTableToCSV(processDefinitionId, schema, table, dbColumns, displayColumns, transportDataSet);
        }
      }
      if (report != null) {
        byte[] bytes = FileUtils.readFileToByteArray(report);
        response.setContentType(reportFormat.getContentType());
        response.setHeader("Content-Disposition", "attachment; filename=".concat(URLEncoder.encode(report.getName(), "UTF-8")));
        response.getOutputStream().write(bytes);
      }

      if (workbook != null) {
        String fileName;
        if (tableId != null) {
          fileName = table;
        }
        else {
          fileName = processName + "_" + taskName;
        }
        response.setContentType("text/csv");
        response.setHeader("Content-Disposition", "attachment; filename=".concat(URLEncoder.encode(fileName, "UTF-8")).concat("_").concat(getFileDateFormat().format(new Date())).concat(".csv"));
        workbook.write(response.getOutputStream());
      }
    }
    finally {
      if (report != null) {
        report.delete();
      }
    }
  }

  /**
   * Generate jasper report by form data
   *
   * @param formData           - data of clients Form
   * @param templateFileName   - name of report template
   * @param reportFormat       - format of report
   * @param storedProcedures   - name of stored procedures
   * @param selectedObjectInfo - editable form's id from parent table
   * @return File of report
   * @throws IOException
   * @throws JRException
   */
  private File exportFormByJasper(Long processDefinitionId, String formData, String templateFileName, ReportFormat reportFormat,
                                  String storedProcedures, ObjectInfo selectedObjectInfo) throws IOException, JRException, SphinxException {
    HashMap<String, Object> formDataMap = GSON.fromJson(formData, new TypeToken<HashMap<String, ?>>() {
    }.getType());
    ArrayList<Map<String, ?>> maps = new ArrayList<Map<String, ?>>();
    maps.add(formDataMap);
    return createReport(processDefinitionId, templateFileName, reportFormat, maps, storedProcedures, selectedObjectInfo);
  }

  /**
   * Generate jasper report by table
   *
   * @param templateFileName   - name of report template
   * @param reportFormat       - format of report
   * @param dbColumns          - columns in DB
   * @param displayColumns     - columns that are displayed (from user)
   * @param transportDataSet   - table's dataset
   * @param storedProcedures   - name of stored procedures
   * @param selectedObjectInfo - editable form's id from parent table
   * @return File of report
   * @throws IOException
   * @throws JRException
   */

  private File exportTableByJasper(Long processDefinitionId, String schema, String table, String templateFileName, ReportFormat reportFormat,
                                   String[] dbColumns, String[] displayColumns, TransportDataSet transportDataSet,
                                   String storedProcedures, ObjectInfo selectedObjectInfo) throws IOException, JRException, SphinxException {
    Iterator iter = transportDataSet.getSets().iterator();
    ArrayList<Map<String, ?>> maps = new ArrayList<Map<String, ?>>();
    Object oValue;
    Collection<String> fiasAddressColumns = metadataDao.getFiasColumns(processDefinitionId, schema, table);
    while (iter.hasNext()) {
      TransportData data = (TransportData) iter.next();
      HashMap<String, Object> rowMap = new HashMap<String, Object>();
      for (int j = 0; j < displayColumns.length; ++j) {
        String dbColumnName = dbColumns[j];
        oValue = data.getData(dbColumnName).getValue();
        if (dbColumnName.contains(";")) {
          dbColumnName = dbColumnName.split(";")[0];
        }
        if (oValue != null && oValue instanceof Date) {
          String value = ClassType.DATETIME == data.getData(dbColumnName).getClassType() ? DateFormat.getDateTimeFormat().format(oValue) : DateFormat.getDateFormat().format(oValue);
          rowMap.put(dbColumnName, value);
        }
        else {
          if (oValue instanceof String && fiasAddressColumns.contains(dbColumnName)) {
            oValue = getFiasAddress((String) oValue);
          }
          rowMap.put(dbColumnName, oValue);
        }
      }
      maps.add(rowMap);
    }

    return createReport(processDefinitionId, templateFileName, reportFormat, maps, storedProcedures, selectedObjectInfo);
  }

  /**
   * Get table's dataset with all user's settings fo further export
   */
  private TransportDataSet getTransportDataSet(Long processDefinitionId, String schema, String table, String tableId,
                                               String object1, String object2, String sortName,
                                               String sortOrder, Long processInstanceId, String[] dbColumns) {
    Data filterObject1 = null;
    if (object1 != null && !object1.isEmpty() && object2 != null && !object2.isEmpty()) {
      Long selectedObject1Id = null;
      String selectedObject1Table = null;

      Map<Long, String> objectInfoMap = baseDao.getObjectInfoFromDb(processInstanceId);
      for (Long key : objectInfoMap.keySet()) {
        ObjectInfo tmpObjectInfo = GSON.fromJson(objectInfoMap.get(key), ObjectInfo.class);
        if (object1.equals(tmpObjectInfo.toString())) {
          selectedObject1Id = tmpObjectInfo.getId();
          selectedObject1Table = tmpObjectInfo.getTable();
          break;
        }
      }

      filterObject1 = new Data();
      filterObject1.setTable(table);
      filterObject1.setField(selectedObject1Table + "_id");
      filterObject1.setValue(selectedObject1Id);
      filterObject1.setValueClass("Long");
    }

    String filterKey = WfeRunaVariables.getFilterKeyVariable(tableId);
    TransportData filter = GsonUtil.getObjectFromJson(baseDao.getVariableFromDb(processInstanceId, filterKey), TransportData.class);
    if (filter == null) {
      filter = new TransportData();
    }
    TransportData defaultFilter = GsonUtil.getObjectFromJson(baseDao.getVariableFromDb(processInstanceId, WfeRunaVariables.getDefaultFilterKeyVariable(tableId)), TransportData.class);
    if (defaultFilter != null) {
      filter.getData().addAll(defaultFilter.getData());
    }
    if (filterObject1 != null) {
      filter.add(filterObject1);
    }

    if (object1 != null && object1.trim().isEmpty()) {
      object1 = null;
    }
    if (object2 != null && object2.trim().isEmpty()) {
      object2 = null;
    }

    return baseDao.getData(processDefinitionId, schema, table, dbColumns, null, null,
        sortName, sortOrder, null, null, filter, object1, object2, null);
  }

  /**
   * Generate the workbook object by table data
   *
   * @param schema           - Схема
   * @param table            - Таблица
   * @param dbColumns        - columns in DB
   * @param displayColumns   - columns that are displayed (from user)
   * @param transportDataSet - table's dataset    @return The workbook object to send to response
   * @throws IOException
   */
  private Workbook exportTableToCSV(Long processDefinitionId, String schema, String table, String[] dbColumns,
                                    String[] displayColumns, TransportDataSet transportDataSet) throws IOException, SphinxException {
    Collection<String> fiasAddressColumns = metadataDao.getFiasColumns(processDefinitionId, schema, table);
    Workbook wb = new HSSFWorkbook();
    Sheet sheet = wb.createSheet(messages.getMessage("list1", null, Locale.ROOT));

    Iterator iter = transportDataSet.getSets().iterator();
    int currentRow = 1;
    Cell cell;
    Object oValue;
    while (iter.hasNext()) {
      Row row = sheet.createRow(currentRow);
      TransportData data = (TransportData) iter.next();
      for (int j = 0; j < displayColumns.length; ++j) {
        cell = row.createCell(j);
        oValue = data.getData(dbColumns[j]).getValue();
        if (oValue != null) {
          if (oValue instanceof Date) {
            cell.setCellValue(ClassType.TIMESTAMP == data.getData(dbColumns[j]).getClassType() ? DateFormat.getDateTimeFormat().format(oValue) : DateFormat.getDateFormat().format(oValue));
          }
          else if (oValue instanceof String) {
            if (fiasAddressColumns.contains(dbColumns[j])) {
              oValue = getFiasAddress((String) oValue);
            }
            cell.setCellValue((String) oValue);
          }
          else {
            cell.setCellValue(oValue.toString());
          }
        }
      }
      ++currentRow;
    }

    Font boldFont = wb.createFont();
    boldFont.setBoldweight(Font.BOLDWEIGHT_BOLD);
    Row header = sheet.createRow(0);
    for (
        int i = 0;
        i < displayColumns.length; ++i)

    {
      Cell headerCell = header.createCell(i);
      RichTextString richTextString = new HSSFRichTextString(displayColumns[i]);
      richTextString.applyFont(boldFont);
      headerCell.setCellValue(richTextString);
      sheet.autoSizeColumn(i);
    }

    return wb;
  }

  /**
   * Generate the workbook object by form data
   *
   * @param formData    - data of clients Form
   * @param processName - name of current process (need to generate header)
   * @param taskName    - name of current task (need to generate header)
   * @return The workbook object to send to response
   * @throws IOException
   */
  private Workbook exportFormToCSV(String formData, String processName, String taskName) throws IOException {
    HashMap<String, String> formDataMap = GSON.fromJson(formData, new TypeToken<HashMap<String, String>>() {
    }.getType());
    Workbook wb = new HSSFWorkbook();
    Sheet sheet = wb.createSheet(messages.getMessage("list1", null, Locale.ROOT));

    int currentRow = 1;
    Cell cell;
    String oValue;
    for (String dbColumn : formDataMap.keySet()) {
      oValue = formDataMap.get(dbColumn);
      Row row = sheet.createRow(currentRow);
      cell = row.createCell(0);
      cell.setCellValue(dbColumn);
      cell = row.createCell(1);
      if (oValue != null) {
        cell.setCellValue(oValue);
      }
      ++currentRow;
    }

    Font boldFont = wb.createFont();
    boldFont.setBoldweight(Font.BOLDWEIGHT_BOLD);
    Row header = sheet.createRow(0);

    Cell headerCell = header.createCell(0);
    RichTextString richTextString = new HSSFRichTextString(processName);
    richTextString.applyFont(boldFont);
    headerCell.setCellValue(richTextString);
    sheet.autoSizeColumn(0);

    headerCell = header.createCell(1);
    richTextString = new HSSFRichTextString(taskName);
    richTextString.applyFont(boldFont);
    headerCell.setCellValue(richTextString);

    sheet.autoSizeColumn(1);
    sheet.addMergedRegion(new CellRangeAddress(0, 0, 0, 1));

    return wb;
  }

  /**
   * Creates report by jasper api
   *
   * @param templateFileName   - name of report template
   * @param reportFormat       - format of report
   * @param maps               - collection of data for the report
   * @param storedProcedures   - name of stored procedures
   * @param selectedObjectInfo - editable form's id from parent table
   * @return File of report
   * @throws IOException
   * @throws JRException
   */
  @SuppressWarnings("unchecked")
  private File createReport(Long processDefinitionId, String templateFileName, final ReportFormat reportFormat,
                            ArrayList<Map<String, ?>> maps, String storedProcedures,
                            ObjectInfo selectedObjectInfo) throws IOException, JRException, SphinxException {
    final HashMap<String, Object> exportParams = new HashMap<String, Object>();
    ArrayList<Map<String, ?>> dataForReport = new ArrayList<Map<String, ?>>();

    Long selectedObjectInfoId = null;
    if (selectedObjectInfo != null) {
      selectedObjectInfoId = selectedObjectInfo.getId();
    }
    exportParams.put(SELECTED_OBJECT_ID, selectedObjectInfoId);

    if (storedProcedures != null) {
      List<StoredProcedure> storedProcedureList = StoredProcedureParser.parseStoredProcedures(storedProcedures, null);
      for (StoredProcedure storedProcedure : storedProcedureList) {
        String schema = storedProcedure.getSchema();
        String procedureName = storedProcedure.getProcedureName();

        StringBuilder parametersBuilder = new StringBuilder();
        String selectedObjId = selectedObjectInfoId != null ? String.valueOf(selectedObjectInfoId) : "null";
        parametersBuilder.append(selectedObjId).append(", ");
        for (Column param : storedProcedure.getParameters()) {
          parametersBuilder.append(param.getName()).append(", ");
        }
        parametersBuilder.delete(parametersBuilder.length() - 2, parametersBuilder.length());

        ArrayList<Map<String, ?>> dataByStoredProcedure = reportDao.getDataByStoredProcedure(processDefinitionId, schema, procedureName, parametersBuilder.toString());
        if (dataByStoredProcedure != null) {
          for (Map map : dataByStoredProcedure) {
            List<String> replaceColumns = new ArrayList<String>();
            for (Object column : map.keySet()) {
              if (column.toString().startsWith(IS_IT_FIAS_PREFIX) && map.get(column) instanceof String) {
                replaceColumns.add(column.toString());
              }
            }
            for (String replaceColumn : replaceColumns) {
              map.put(replaceColumn, getFiasAddress(map.get(replaceColumn).toString()));
            }
          }
        }
        exportParams.put(procedureName, new JRMapCollectionDataSource(dataByStoredProcedure));
      }
    }

    if (maps != null) {
      dataForReport.addAll(maps);
    }

    final JRMapCollectionDataSource dataSource = new JRMapCollectionDataSource(dataForReport);
    exportParams.put(MAIN_DATA_SOURCE, dataSource);

    String jbossTempDirectory = System.getProperty("jboss.server.temp.dir");
    Map<String, byte[]> reportTemplatesFromDb = reportDao.getReportTemplatesFromDb(templateFileName);

    if (reportTemplatesFromDb.size() == 0) {
      throw new RuntimeException(messages.getMessage("templateNotFound", null, Locale.ROOT));
    }
    byte[] mainReportBytes = reportTemplatesFromDb.get(templateFileName);
    reportTemplatesFromDb.remove(templateFileName);

    for (String subReportName : reportTemplatesFromDb.keySet()) {
      ByteArrayInputStream subReportInputStream = new ByteArrayInputStream(reportTemplatesFromDb.get(subReportName));
      JasperReport subReport = (JasperReport) JRLoader.loadObject(subReportInputStream);
      exportParams.put(subReportName, subReport);
    }

    String createDate = getFileDateFormat().format(new Date());
    final String fileName = templateFileName + "_" + createDate;
    final Exporter exporter = new Exporter(jbossTempDirectory);
    final ByteArrayInputStream mainReportStream = new ByteArrayInputStream(mainReportBytes);
    String reportFileName = reportDao.execute(processDefinitionId, new ConnectionCallback<String>() {
      @Override
      public String doInConnection(Connection con) throws SQLException, DataAccessException {
        try {
          return exporter.export(exportParams, con, reportFormat, mainReportStream, fileName);
        }
        catch (Exception ex) {
          throw new DataAccessCommonException(ex.getMessage(), ex);
        }
      }
    });

    return new File(reportFileName);
  }

  private String getFiasAddress(String fiasGuId) throws SphinxException {
    List<AddressSphinx> addressSphinxes = sphinxStrAddressByGuidReader.search(fiasGuId, 1);
    if (addressSphinxes.size() == 1) {
      fiasGuId = SphinxStrAddressByGuidReader.fullAddress(addressSphinxes.get(0));
    }
    else {
      logger.error("sphinxStrAddressByGuidReader returned more than one result " + fiasGuId);
    }
    return fiasGuId;
  }

  private SimpleDateFormat getFileDateFormat() {
    return DateFormat.getDateTimeFormat();
  }
}
