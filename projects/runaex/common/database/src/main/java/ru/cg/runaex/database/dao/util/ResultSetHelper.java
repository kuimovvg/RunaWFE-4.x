package ru.cg.runaex.database.dao.util;

import java.sql.ResultSet;
import java.sql.SQLException;

import org.postgresql.jdbc4.Jdbc4Array;

/**
 * @author urmancheev
 */
public class ResultSetHelper {

  public static Object getValue(ResultSet resultSet, int columnIndex) throws SQLException {
    Object value = resultSet.getObject(columnIndex);
    return transformValue(value);
  }

  public static Object getValue(ResultSet resultSet, String columnAlias) throws SQLException {
    Object value = resultSet.getObject(columnAlias);
    return transformValue(value);
  }

  private static Object transformValue(Object value) throws SQLException {
    if (value instanceof Jdbc4Array) {
      value = ((Jdbc4Array) value).getArray();
    }
    return value;
  }
}
