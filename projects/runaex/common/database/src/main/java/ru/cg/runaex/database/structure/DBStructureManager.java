package ru.cg.runaex.database.structure;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.sql.DataSource;

import org.apache.ddlutils.Platform;
import org.apache.ddlutils.PlatformFactory;
import org.apache.ddlutils.model.Database;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import ru.cg.runaex.database.dao.MetadataDao;
import ru.cg.runaex.database.dao.util.JdbcTemplateProvider;
import ru.cg.runaex.database.structure.bean.DatabaseStructure;

/**
 * @author golovlyev
 */
@Component
public class DBStructureManager implements InitializingBean {
  @Autowired
  private JdbcTemplateProvider jdbcTemplateProvider;
  @Autowired
  private MetadataDao metadataDao;

  public synchronized void initDbStructure(List<Long> processDefinitionIds) throws SQLException {
    //For getting default datasource it's need to put null in a list
    processDefinitionIds.add(null);

    Map<Long, Database> databasesByProcess = new HashMap<Long, Database>();
    Map<DataSource, Database> databasesByDataSource = new HashMap<DataSource, Database>();

    for (Long processId : processDefinitionIds) {
      DataSource dataSource = jdbcTemplateProvider.getTemplate(processId).getDataSource();
      Database database = databasesByDataSource.get(dataSource);

      if (database == null) {
        database = initDatabaseModel(dataSource);
        databasesByProcess.put(processId, database);
        databasesByDataSource.put(dataSource, database);
      }
      else {
        databasesByProcess.put(processId, database);
      }
    }
    DatabaseStructure.setDbObjectMap(databasesByProcess);
  }

  private Database initDatabaseModel(DataSource dataSource) throws SQLException {
    Connection connection = dataSource.getConnection();
    Platform platform = PlatformFactory.createNewPlatformInstance(dataSource);

    ResultSet rs = null;
    try {
      DatabaseMetaData dmd = connection.getMetaData();
      rs = dmd.getSchemas();
      List<String> schemaNames = new ArrayList<String>();
      while (rs.next()) {
        String schema = rs.getString("TABLE_SCHEM");
        schemaNames.add(schema);
      }
      return platform.readModelFromDatabase(connection, null, null, schemaNames, null);
    }
    finally {
      if (rs != null) {
        rs.close();
      }
      platform.returnConnection(connection);
      connection.close();
    }
  }

  @Override
  public void afterPropertiesSet() throws Exception {
    this.initDbStructure(metadataDao.getAllProcessDefinitionsByDbConnections());
  }
}
