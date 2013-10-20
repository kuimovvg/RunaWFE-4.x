package ru.cg.runaex.database.factory;

import liquibase.database.Database;
import liquibase.exception.DatabaseException;
import liquibase.integration.commandline.CommandLineUtils;

import ru.cg.runaex.components.ConnectionInfo;

/**
 * @author Петров А.
 */
public class LiquibaseDatabaseFactoryImpl implements LiquibaseDatabaseFactory {

  private ConnectionInfo connectionInfo;

  public ConnectionInfo getConnectionInfo() {
    return connectionInfo;
  }

  public void setConnectionInfo(ConnectionInfo connectionInfo) {
    this.connectionInfo = connectionInfo;
  }

  @Override
  public Database createTargetDatabase(String schema) throws DatabaseException {
    return CommandLineUtils.createDatabaseObject(getClass().getClassLoader(), connectionInfo.getTargetDbUrl(), connectionInfo.getTargetDbUsername(), connectionInfo.getTargetDbPassword(), connectionInfo.getDriverClassName(), schema, null, null);
  }

  @Override
  public Database createReferenceDatabase(String schema) throws DatabaseException {
    return CommandLineUtils.createDatabaseObject(getClass().getClassLoader(), connectionInfo.getReferenceDbUrl(), connectionInfo.getReferenceDbUsername(), connectionInfo.getReferenceDbPassword(), connectionInfo.getDriverClassName(), schema, null, null);
  }
}
