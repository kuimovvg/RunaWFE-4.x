package ru.cg.runaex.database.dao;

import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.postgresql.jdbc4.Jdbc4Array;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.*;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;

import ru.cg.runaex.database.bean.TemplateUploadStatus;
import ru.cg.runaex.database.bean.model.ReportTemplate;
import ru.cg.runaex.database.dao.util.JdbcTemplateProvider;
import ru.cg.runaex.database.dao.util.QueryHelper;

/**
 * @author Kochetkov
 */
public class ReportDaoImpl implements ReportDao {

  private static final String[] acceptableSortFields = {"report_template_name"};
  private static final Pattern CONSTRAINT = Pattern.compile(".*(?:\"(uni_.+?)\").*", Pattern.DOTALL);

  private JdbcTemplateProvider jdbcTemplateProvider;

  public void setJdbcTemplateProvider(JdbcTemplateProvider jdbcTemplateProvider) {
    this.jdbcTemplateProvider = jdbcTemplateProvider;
  }

  private String getWrapQuotes(String str) {
    StringBuilder sb = new StringBuilder();
    sb.append("\"").append(str).append("\"");
    return sb.toString();
  }

  @Override
  public Map<String, byte[]> getReportTemplatesFromDb(String templateFileName) {
    StringBuilder sqlBuilder = new StringBuilder();
    sqlBuilder.append("WITH RECURSIVE temp1 AS (\n");
    sqlBuilder.append("SELECT T1.\"report_template_id\",T1.\"report_template_bytes\",T1.\"report_template_name\", T1.\"parent_report_template_id\"\n");
    sqlBuilder.append("FROM \"metadata\".\"report_templates\" T1 WHERE T1.\"report_template_name\" = ?\n");
    sqlBuilder.append("UNION SELECT T2.\"report_template_id\", T2.\"report_template_bytes\", T2.\"report_template_name\", T2.\"parent_report_template_id\"\n");
    sqlBuilder.append("FROM \"metadata\".\"report_templates\" T2 INNER JOIN temp1 ON(temp1.\"report_template_id\" = T2.\"parent_report_template_id\"))\n");
    sqlBuilder.append("SELECT \"report_template_name\",\"report_template_bytes\" FROM temp1");
    String sql = sqlBuilder.toString();

    ResultSetExtractor<Map<String, byte[]>> resultSetExtractor = new ResultSetExtractor<Map<String, byte[]>>() {
      @Override
      public Map<String, byte[]> extractData(ResultSet rs) throws SQLException, DataAccessException {
        Map<String, byte[]> reportTemplates = new HashMap<String, byte[]>();
        while (rs.next()) {
          reportTemplates.put(rs.getString("report_template_name"), rs.getBytes("report_template_bytes"));
        }
        return reportTemplates;
      }
    };
    return jdbcTemplateProvider.getMetadataTemplate().query(sql, new Object[] {templateFileName}, resultSetExtractor);
  }

  @Override
  public ArrayList<Map<String, ?>> getDataByStoredProcedure(Long processDefinitionId, String schema, String procedureName, String parameters) {
    StringBuilder sqlBuilder = new StringBuilder();
    sqlBuilder.append("SELECT * FROM ");
    sqlBuilder.append(getWrapQuotes(schema));
    sqlBuilder.append(".");
    sqlBuilder.append(getWrapQuotes(procedureName));
    sqlBuilder.append("(");
    sqlBuilder.append(parameters);
    sqlBuilder.append(")");

    String sql = sqlBuilder.toString();

    ResultSetExtractor<ArrayList<Map<String, ?>>> resultSetExtractor = new ResultSetExtractor<ArrayList<Map<String, ?>>>() {
      @Override
      public ArrayList<Map<String, ?>> extractData(ResultSet rs) throws SQLException, DataAccessException {
        ArrayList<Map<String, ?>> results = new ArrayList<Map<String, ?>>();
        while (rs.next()) {
          ResultSetMetaData rsMetaData = rs.getMetaData();
          int numberOfColumns = rsMetaData.getColumnCount();
          Map<String, Object> map = new HashMap<String, Object>();
          for (int i = 1; i <= numberOfColumns; i++) {
            Object rsObject = rs.getObject(i);
            if (rsObject instanceof Jdbc4Array) {
              rsObject = ((Jdbc4Array) rsObject).getArray();
            }
            map.put(rsMetaData.getColumnName(i), rsObject);
          }
          results.add(map);
        }
        return results;
      }
    };
    return jdbcTemplateProvider.getTemplate(processDefinitionId).query(sql, new Object[] {}, resultSetExtractor);
  }

  @Override
  public Map<TemplateUploadStatus, Object> saveReportTemplate(Map<String, byte[]> templateFilesToSave, final String parentTemplateName) {
    JdbcTemplate metadataTemplate = jdbcTemplateProvider.getMetadataTemplate();
    HashMap<TemplateUploadStatus, Object> saveResults = new HashMap<TemplateUploadStatus, Object>();
    List<String> existingTemplates = new ArrayList<String>();
    if (templateFilesToSave != null && !templateFilesToSave.isEmpty()) {
      final String sql = "INSERT INTO metadata.report_templates (report_template_bytes, report_template_name, parent_report_template_id) VALUES (?, ?, ?);";
      int successfulSaveCount = 0;
      boolean unknownError = false;
      Long parentReportId = null;

      if (parentTemplateName != null) {
        final byte[] bytes = templateFilesToSave.get(parentTemplateName);
        try {
          KeyHolder keyHolder = new GeneratedKeyHolder();
          metadataTemplate.update(new PreparedStatementCreator() {
            @Override
            public PreparedStatement createPreparedStatement(Connection con) throws SQLException {
              PreparedStatement ps = con.prepareStatement(sql, new String[] {"report_template_id"});
              ps.setBytes(1, bytes);
              ps.setString(2, parentTemplateName);
              ps.setNull(3, Types.BIGINT);
              return ps;
            }
          }, keyHolder);
          parentReportId = keyHolder.getKey().longValue();
          templateFilesToSave.remove(parentTemplateName);
          successfulSaveCount++;
        }
        catch (Exception ex) {
          String constraint = getUniConstraintName(ex);
          if (constraint == null) {
            unknownError = true;
          }
          else {
            existingTemplates.add(parentTemplateName);
          }
        }
      }

      if ((parentTemplateName != null && parentReportId != null) || parentTemplateName == null) {
        for (String templateFileName : templateFilesToSave.keySet()) {
          try {
            byte[] templateFileBytes = templateFilesToSave.get(templateFileName);
            metadataTemplate.update(sql, new Object[] {templateFileBytes, templateFileName, parentReportId}, new int[] {Types.LONGVARBINARY, Types.VARCHAR, Types.BIGINT});
            successfulSaveCount++;
          }
          catch (Exception ex) {
            String constraint = getUniConstraintName(ex);
            if (constraint == null) {
              unknownError = true;
              break;
            }
            else {
              existingTemplates.add(templateFileName);
            }
          }
        }
      }

      saveResults.put(TemplateUploadStatus.SUCCESS, successfulSaveCount);
      saveResults.put(TemplateUploadStatus.UNKNOWN_ERROR, unknownError);

      if (existingTemplates.size() > 0) {
        saveResults.put(TemplateUploadStatus.UNIQUE_NAME_VIOLATION, existingTemplates);
      }
    }
    return saveResults;
  }

  private String getUniConstraintName(Exception ex) {
    String constraint = null;
    Matcher matcher = CONSTRAINT.matcher(ex.getMessage());
    if (matcher.find()) {
      constraint = matcher.group(1);
    }
    return constraint;
  }

  @Override
  public int getTemplatesCount() {
    String sql = "SELECT COUNT(*) FROM metadata.report_templates";
    return jdbcTemplateProvider.getMetadataTemplate().query(sql, new Long[] {}, new ResultSetExtractor<Integer>() {
      @Override
      public Integer extractData(ResultSet rs) throws SQLException, DataAccessException {
        return rs.next() ? rs.getInt(1) : 0;
      }
    });
  }

  @Override
  public <T> T execute(Long processDefinitionId, ConnectionCallback<T> callback) throws DataAccessException {
    return jdbcTemplateProvider.getTemplate(processDefinitionId).execute(callback);
  }

  @Override
  public List<ReportTemplate> loadTemplates(String sortField, String sortDir, int fromRow, Integer limit) {
    StringBuilder sql = new StringBuilder("select report_template_id, report_template_name from metadata.report_templates");
    QueryHelper.addOrder(sql, sortField, sortDir, acceptableSortFields);
    QueryHelper.addOffsetAndLimit(sql, fromRow, limit);
    return jdbcTemplateProvider.getMetadataTemplate().query(sql.toString(), new RowMapper<ReportTemplate>() {
      @Override
      public ReportTemplate mapRow(ResultSet rs, int rowNum) throws SQLException {
        return new ReportTemplate(rs.getLong("report_template_id"), rs.getString("report_template_name"));
      }
    });
  }

  @Override
  public void deleteTemplatesByIds(Long[] reportTemplateIds) {
    if (reportTemplateIds != null && reportTemplateIds.length != 0) {
      StringBuilder sqlBuilder = new StringBuilder("delete from metadata.report_templates where report_template_id in (");
      Object[] args = new Object[reportTemplateIds.length];
      int[] types = new int[reportTemplateIds.length];

      for (int i = 0; i < reportTemplateIds.length; i++) {
        sqlBuilder.append("?");
        if (i + 1 != reportTemplateIds.length) {
          sqlBuilder.append(", ");
        }
        args[i] = reportTemplateIds[i];
        types[i] = Types.BIGINT;
      }
      sqlBuilder.append(");");

      jdbcTemplateProvider.getMetadataTemplate().update(sqlBuilder.toString(), args, types);
    }
  }
}
