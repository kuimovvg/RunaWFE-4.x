package ru.cg.runaex.database.dao.util;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Collection;

import org.apache.commons.lang.StringUtils;

import ru.cg.runaex.components.bean.component.part.ColumnReference;
import ru.cg.runaex.components.bean.component.part.TableReference;

/**
 * @author urmancheev
 */
public final class QueryHelper {

  public static void addOrder(StringBuilder sql, String sortField, String sortDir, String[] acceptableSortFields) {
    if (sortField == null || sortDir == null)
      return;

    if (!"asc".equalsIgnoreCase(sortDir) && !"desc".equalsIgnoreCase(sortDir))
      return;

    boolean validSortField = false;
    for (String acceptableField : acceptableSortFields) {
      if (acceptableField.equalsIgnoreCase(sortField)) {
        validSortField = true;
        break;
      }
    }
    if (!validSortField)
      return;

    sql.append(" order by ").append(sortField).append(" ").append(sortDir);
  }

  public static void addOffsetAndLimit(StringBuilder sql, int fromRow, int maxResults) {
    sql.append(" offset ").append(fromRow);
    sql.append(" limit ").append(maxResults);
  }

  public static String preparePlaceHolders(int length) {
    return StringUtils.repeat("?, ", length - 1) + "?";
  }

  public static void setArrayValues(int startIndex, PreparedStatement preparedStatement, Collection collection) throws SQLException {
    int i = startIndex;
    for (Object value : collection) {
      preparedStatement.setObject(i, value);
      i++;
    }
  }

  public static String wrapColumnReference(ColumnReference reference) {
    return wrapColumnReference(reference.getSchema(), reference.getTable(), reference.getColumn());
  }

  public static String wrapColumnReference(String schema, String table, String column) {
    return wrapTableReference(schema, table) + "." + getWrapQuotes(column);
  }

  public static String wrapTableReference(TableReference reference) {
    return wrapTableReference(reference.getSchema(), reference.getTable());
  }

  public static String wrapTableReference(String schema, String table) {
    return getWrapQuotes(schema) + "." + getWrapQuotes(table);
  }

  public static String getWrapQuotes(String str) {
    StringBuilder sb = new StringBuilder();
    sb.append("\"").append(str).append("\"");
    return sb.toString();
  }
}
