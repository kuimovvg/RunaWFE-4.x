package ru.cg.runaex.database.structure.bean;

import java.sql.Types;
import java.util.List;
import java.util.Map;

import org.apache.ddlutils.model.Database;
import org.apache.ddlutils.model.Schema;
import org.apache.ddlutils.model.Table;

/**
 * @author golovlyev
 */
public class DatabaseStructure {
  private static Map<Long, Database> databasesByProcess = null;

  public static void setDbObjectMap(Map<Long, Database> databasesByProcess) {
    if (DatabaseStructure.databasesByProcess == null)
      DatabaseStructure.databasesByProcess = databasesByProcess;
    else
      DatabaseStructure.databasesByProcess.putAll(databasesByProcess);
  }

  /**
   * Find table by name
   *
   * @throws IllegalArgumentException if table or schema doesn't exist
   */
  public static Table getTable(Long processDefinitionId, String schemaName, String tableName) throws IllegalArgumentException {
    Schema schema = getSchema(processDefinitionId, schemaName);
    Table table = schema.getTable(tableName);
    if (table != null) {
      return table;
    }
    throw new IllegalArgumentException("Couldn't find table " + tableName + " in schema " + schemaName + " for process with definition id " + processDefinitionId);
  }

  /**
   * Finds schema by name
   *
   * @throws IllegalArgumentException if schema doesn't exist
   */
  private static Schema getSchema(Long processDefinitionId, String schemaName) throws IllegalArgumentException {
    Database database = databasesByProcess.get(processDefinitionId);
    if (database == null)
      database = databasesByProcess.get(null);

    List<Schema> schemas = database.getSchemas();
    for (Schema schema : schemas) {
      if (schema.getName().equals(schemaName))
        return schema;
    }
    throw new IllegalArgumentException("Couldn't find schema " + schemaName + " for process with definition id " + processDefinitionId);
  }

  public static boolean isBooleanColumn(Table table, String columnName) {
    int sqlType = table.findColumn(columnName).getTypeCode();
    return sqlType == Types.BOOLEAN || sqlType == Types.BIT;
  }

  public static boolean isNumberColumn(Table table, String columnName) {
    int sqlType = table.findColumn(columnName).getTypeCode();
    return sqlType == Types.NUMERIC || sqlType == Types.BIGINT;
  }
}
