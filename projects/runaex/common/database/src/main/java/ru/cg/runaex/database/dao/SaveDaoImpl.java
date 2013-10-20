package ru.cg.runaex.database.dao;

import java.sql.*;
import java.util.List;

import org.apache.ddlutils.model.Column;
import org.apache.ddlutils.model.Table;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.ConnectionCallback;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;

import ru.cg.runaex.database.bean.transport.Data;
import ru.cg.runaex.database.bean.transport.LinkIds;
import ru.cg.runaex.database.bean.transport.SaveTransportData;
import ru.cg.runaex.database.dao.util.JdbcTemplateProvider;
import ru.cg.runaex.database.dao.util.PreparedStatementSetter;
import ru.cg.runaex.database.dao.util.QueryHelper;
import ru.cg.runaex.database.exception.DataAccessCommonException;
import ru.cg.runaex.database.structure.bean.DatabaseStructure;

/**
 * @author urmancheev
 */
public class SaveDaoImpl implements SaveDao {
  private final Logger logger = LoggerFactory.getLogger(getClass());

  private JdbcTemplateProvider templateProvider;

  public void setTemplateProvider(JdbcTemplateProvider templateProvider) {
    this.templateProvider = templateProvider;
  }

  @Override
  public Long saveData(Long processDefinitionId, SaveTransportData saveTransportData) throws DataAccessException {
    Long rowId;
    try {
      Table table = DatabaseStructure.getTable(processDefinitionId, saveTransportData.getSchema(), saveTransportData.getTable());

      boolean isFindParentId = false;  //needed for tree grids only?
      for (Column column : table.getColumns()) {
        if (column.getName().contains("_parent_id")) {
          isFindParentId = true;
        }
      }
      if (!isFindParentId) {
        Data dataParentId = saveTransportData.getData(saveTransportData.getTable() + "_parent_id");
        saveTransportData.getData().remove(dataParentId);
      }

      boolean insert = saveTransportData.getId() == null;
      if (insert) {
        String sql = getSqlForSave(saveTransportData);
        KeyHolder keyHolder = new GeneratedKeyHolder();

        templateProvider.getTemplate(processDefinitionId).update(new SavePreparedStatementCreator(sql, saveTransportData, table, true), keyHolder);
        rowId = keyHolder.getKey() != null ? keyHolder.getKey().longValue() : null;
      }
      else {
        String sql = getSqlForSave(saveTransportData);
        templateProvider.getTemplate(processDefinitionId).update(new SavePreparedStatementCreator(sql, saveTransportData, table, false));
        rowId = saveTransportData.getId();
      }

      return rowId;
    }
    catch (DataAccessException ex) {
      logger.error(ex.toString(), ex);
      throw ex;
    }
    catch (RuntimeException ex) {
      logger.error(ex.toString(), ex);
      throw ex;
    }
  }

  @Override
  public LinkIds saveAndLinkData(final Long processDefinitionId, final SaveTransportData saveTransportData, final SaveTransportData linkTransportData) throws DataAccessException {
    JdbcTemplate template = templateProvider.getTemplate(processDefinitionId);
    //TODO after RUNAEX-892 move this method to service and delete LinkIds
    try {
      return template.execute(new ConnectionCallback<LinkIds>() {
        @Override
        public LinkIds doInConnection(Connection connection) throws SQLException, DataAccessException {
          Long newObjectId;
          Long linkId;
          try {
            boolean autoCommit = connection.getAutoCommit();
            connection.setAutoCommit(false);

            newObjectId = saveLinked(processDefinitionId, saveTransportData, connection);

            Table linkTable = DatabaseStructure.getTable(processDefinitionId, linkTransportData.getSchema(), linkTransportData.getTable());
            Data attachableObjectData = new Data(saveTransportData.getTable().concat("_id"), newObjectId, "Long");
            linkTransportData.getData().add(attachableObjectData);
            linkId = findExistingLink(linkTable, linkTransportData, connection);

            if (linkId == null) {
              linkId = createLink(linkTable, linkTransportData, connection);
            }

            connection.commit();
            connection.setAutoCommit(autoCommit);
          }
          catch (Exception ex) {
            logger.error(ex.getMessage(), ex);
            throw new DataAccessCommonException(ex.getMessage(), ex);
          }
          finally {
            if (connection != null) {
              connection.close();
            }
          }
          return new LinkIds(linkId, newObjectId);
        }
      });
    }
    catch (DataAccessException ex) {
      logger.error(ex.toString(), ex);
      throw ex;
    }
    catch (RuntimeException ex) {
      logger.error(ex.toString(), ex);
      throw ex;
    }
  }

  private Long saveLinked(Long processDefinitionId, SaveTransportData saveTransportData, Connection connection) throws SQLException {
    Long newObjectId = null;
    Table table = DatabaseStructure.getTable(processDefinitionId, saveTransportData.getSchema(), saveTransportData.getTable());

    boolean isFindParentId = false;  //needed for tree grids only?
    for (Column column : table.getColumns()) {
      if (column.getName().contains("_parent_id")) {
        isFindParentId = true;
      }
    }
    if (!isFindParentId) {
      Data dataParentId = saveTransportData.getData(saveTransportData.getTable() + "_parent_id");
      saveTransportData.getData().remove(dataParentId);
    }

    int autoGeneratedKeys = Statement.NO_GENERATED_KEYS;
    if (saveTransportData.getId() == null) {
      autoGeneratedKeys = Statement.RETURN_GENERATED_KEYS;
    }
    else {
      newObjectId = saveTransportData.getId();
    }

    String sql = getSqlForSave(saveTransportData);

    PreparedStatement savePs = null;
    ResultSet generatedKeys = null;
    try {
      savePs = connection.prepareStatement(sql, autoGeneratedKeys);
      int i = 1;
      for (Data data : saveTransportData.getData()) {
        Column column = table.findColumn(data.getField());
        PreparedStatementSetter.setValue(data.getValue(), column.getTypeCode(), i, savePs);
        i++;
      }

      int affectedRows = savePs.executeUpdate();
      if (affectedRows == 0) {
        throw new SQLException("Creating object failed, no rows affected.");
      }
      generatedKeys = savePs.getGeneratedKeys();
      if (generatedKeys.next()) {
        newObjectId = generatedKeys.getLong(1);
      }
    }
    finally {
      if (generatedKeys != null)
        generatedKeys.close();
      if (savePs != null)
        savePs.close();
    }
    return newObjectId;
  }

  private Long findExistingLink(Table linkTable, SaveTransportData linkTransportData, Connection connection) throws SQLException {
    Long linkId = null;

    StringBuilder sql = new StringBuilder();
    sql.append(" select ")
        .append(QueryHelper.getWrapQuotes(linkTransportData.getTable().concat("_id")))
        .append(" from ")
        .append(QueryHelper.wrapTableReference(linkTransportData.getSchema(), linkTransportData.getTable()))
        .append(" where ");

    List<Data> dataList = linkTransportData.getData();
    for (int i = 0; i < dataList.size(); ++i) {
      Data data = dataList.get(i);
      sql.append(QueryHelper.getWrapQuotes(data.getField()))
          .append(" = ?");
      if (i < dataList.size() - 1) {
        sql.append(" and ");
      }
    }
    sql.append(" limit 1");

    PreparedStatement linkExistsPs = null;
    ResultSet linkExistsResult = null;
    try {
      linkExistsPs = connection.prepareStatement(sql.toString());
      int i = 1;
      for (Data data : linkTransportData.getData()) {
        Column column = linkTable.findColumn(data.getField());
        PreparedStatementSetter.setValue(data.getValue(), column.getTypeCode(), i, linkExistsPs);
        i++;
      }
      linkExistsResult = linkExistsPs.executeQuery();
      if (linkExistsResult.next()) {
        linkId = linkExistsResult.getLong(1);
      }
    }
    finally {
      if (linkExistsResult != null)
        linkExistsResult.close();
      if (linkExistsPs != null)
        linkExistsPs.close();
    }

    return linkId;
  }

  private Long createLink(Table linkTable, SaveTransportData linkTransportData, Connection connection) throws SQLException {
    Long linkId = null;

    StringBuilder sql = new StringBuilder();
    sql.append(" insert into ").append(QueryHelper.wrapTableReference(linkTransportData.getSchema(), linkTransportData.getTable())).append("(");
    boolean isFirst = true;
    StringBuilder values = new StringBuilder();
    for (Data data : linkTransportData.getData()) {
      if (!isFirst) {
        sql.append(", ");
        values.append(", ");
      }
      sql.append(QueryHelper.getWrapQuotes(data.getField()));
      values.append("?");
      isFirst = false;
    }
    sql.append(") values (").append(values.toString()).append(")");

    PreparedStatement linkPs = null;
    ResultSet generatedKeys = null;
    try {
      linkPs = connection.prepareStatement(sql.toString(), Statement.RETURN_GENERATED_KEYS);
      int i = 1;
      for (Data data : linkTransportData.getData()) {
        Column column = linkTable.findColumn(data.getField());
        PreparedStatementSetter.setValue(data.getValue(), column.getTypeCode(), i, linkPs);
        i++;
      }

      int affectedRows = linkPs.executeUpdate();
      if (affectedRows == 0) {
        throw new SQLException("Creating object failed, no rows affected.");
      }
      generatedKeys = linkPs.getGeneratedKeys();
      if (generatedKeys.next()) {
        linkId = generatedKeys.getLong(1);
      }
    }
    finally {
      if (generatedKeys != null)
        generatedKeys.close();
      if (linkPs != null)
        linkPs.close();
    }

    return linkId;
  }

  private String getSqlForSave(SaveTransportData saveTransportData) {
    StringBuilder sql = new StringBuilder();
    if (saveTransportData.getId() == null) {
      sql.append(" insert into ").append(QueryHelper.wrapTableReference(saveTransportData.getSchema(), saveTransportData.getTable())).append("(");

      boolean isFirst = true;
      StringBuilder values = new StringBuilder();
      for (Data data : saveTransportData.getData()) {
        if (!isFirst) {
          sql.append(", ");
          values.append(", ");
        }
        sql.append(QueryHelper.getWrapQuotes(data.getField()));
        values.append("?");
        isFirst = false;
      }

      sql.append(") values (").append(values.toString()).append(")");
    }
    else {
      sql.append(" update ").append(QueryHelper.wrapTableReference(saveTransportData.getSchema(), saveTransportData.getTable())).append(" set ");
      boolean isFirst = true;
      for (Data data : saveTransportData.getData()) {
        if (!isFirst) {
          sql.append(", ");
        }
        sql.append(QueryHelper.getWrapQuotes(data.getField())).append("=?");
        isFirst = false;
      }
      sql.append(" where ").append("\"").append(saveTransportData.getTable()).append("_id").append("\"").append("=").append(saveTransportData.getId());
    }
    logger.debug("sql - " + sql.toString());
    return sql.toString();
  }

  private static class SavePreparedStatementCreator implements org.springframework.jdbc.core.PreparedStatementCreator {
    private String sql;
    private SaveTransportData saveTransportData;
    private Table table;
    private boolean insert;

    private SavePreparedStatementCreator(String sql, SaveTransportData saveTransportData, Table table, boolean insert) {
      this.sql = sql;
      this.saveTransportData = saveTransportData;
      this.table = table;
      this.insert = insert;
    }

    @Override
    public PreparedStatement createPreparedStatement(Connection con) throws SQLException {
      PreparedStatement ps;
      if (insert)
        ps = con.prepareStatement(sql, new String[] {saveTransportData.getTable() + "_id"});
      else
        ps = con.prepareStatement(sql);

      int i = 1;
      for (Data data : saveTransportData.getData()) {
        Column column = table.findColumn(data.getField());
        PreparedStatementSetter.setValue(data.getValue(), column.getTypeCode(), i, ps);
        i++;
      }

      return ps;
    }
  }

}
