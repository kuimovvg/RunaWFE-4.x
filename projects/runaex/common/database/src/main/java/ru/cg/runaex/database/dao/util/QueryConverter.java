package ru.cg.runaex.database.dao.util;

import java.sql.Connection;
import java.sql.SQLException;

/**
 * @author golovlyev
 */
public final class QueryConverter {
  private final static String POSTGRESQL_DB = "PostgreSQL";
  private final static String POSTGRESQL_CHAR = "::character varying";

  private final static String ORACLE_CHAR = "TO_CHAR";

  public static String getQueryParam(Connection connection, String table, String field, String columnType) throws SQLException {

    //todo use Data Base Structure instead of columnMap after review RUNAEX-828

    StringBuilder sql = new StringBuilder();
    if (POSTGRESQL_DB.equals(connection.getMetaData().getDatabaseProductName())) {
      if ("timestamp".equals(columnType)) {
        sql.append("to_char(").append(table).append(".").append(field).append(", 'DD.MM.YYYY HH24:MI:SS')");
      }
      else
        sql.append(table).append(".").append(field).append(POSTGRESQL_CHAR);
      return sql.toString();
    }
    else {
      //todo for ORACLE DB change to_char for date, timezone
      sql.append(ORACLE_CHAR).append("(").append(table).append(".").append(field).append(")");
      return sql.toString();
    }
  }
}
