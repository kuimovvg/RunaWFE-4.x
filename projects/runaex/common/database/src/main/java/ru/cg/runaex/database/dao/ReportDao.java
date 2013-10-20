package ru.cg.runaex.database.dao;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.ConnectionCallback;

import ru.cg.runaex.database.bean.TemplateUploadStatus;
import ru.cg.runaex.database.bean.model.ReportTemplate;

/**
 * @author Kochetkov
 */
public interface ReportDao {
  Map<String, byte[]> getReportTemplatesFromDb(String templateFileName);

  ArrayList<Map<String, ?>> getDataByStoredProcedure(Long processDefinitionId, String schema, String procedureName, String parameters);

  Map<TemplateUploadStatus, Object> saveReportTemplate(Map<String, byte[]> templateFilesToSave, String parentTemplateName);

  int getTemplatesCount();

  List<ReportTemplate> loadTemplates(String sortName, String sortOrder, int i, Integer pageSize);

  void deleteTemplatesByIds(Long[] reportTemplateIds);

  <T> T execute(Long processDefinitionId, ConnectionCallback<T> callback) throws DataAccessException;

}
