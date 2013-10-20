package ru.cg.runaex.database.util;

import org.apache.ddlutils.model.Column;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.cg.runaex.core.DateFormat;
import ru.cg.runaex.database.dao.util.PreparedStatementSetter;

import java.math.BigDecimal;
import java.sql.*;
import java.text.ParseException;
import java.util.Collection;
import java.util.Date;

/**
 * @author Bagautdinov
 */
public class TypeConverter {
  private static final Logger logger = LoggerFactory.getLogger(PreparedStatementSetter.class);

  public static boolean isNull(Object value) {
    boolean isNull = value == null;
    if (value instanceof String) {
      String str = ((String) value).trim();
      isNull = str.isEmpty() || "null".equals(str);
    }
    return isNull;
  }

  public static BigDecimal convertNumeric(Object value) {
    BigDecimal convertedValue;
    if (value instanceof Double) {
      convertedValue = BigDecimal.valueOf((Double) value);
    }
    else if (value instanceof Float) {
      convertedValue = BigDecimal.valueOf(((Float) value).doubleValue());
    }
    else if (value instanceof Integer) {
      convertedValue = new BigDecimal((Integer) value);
    }
    else if (value instanceof Long) {
      convertedValue = new BigDecimal((Long) value);
    }
    else if (value instanceof String) {
      String sValue = ((String) value).trim();
      convertedValue = new BigDecimal(sValue);
    }
    else
      convertedValue = (BigDecimal) value;
    return convertedValue;
  }

  public static Integer convertInteger(Object value) {
    Integer convertedValue;
    if (value instanceof Double) {
      convertedValue = ((Double) value).intValue();
    }
    else if (value instanceof Float) {
      convertedValue = ((Float) value).intValue();
    }
    else if (value instanceof Long) {
      convertedValue = ((Long) value).intValue();
    }
    else if (value instanceof String) {
      String sValue = ((String) value).trim();
      convertedValue = Integer.valueOf(sValue);
    }
    else
      convertedValue = (Integer) value;
    return convertedValue;
  }

  public static Long convertBigint(Object value) {
    Long convertedValue;
    if (value instanceof Double) {
      convertedValue = ((Double) value).longValue();
    }
    else if (value instanceof Float) {
      convertedValue = ((Float) value).longValue();
    }
    else if (value instanceof Integer) {
      convertedValue = ((Integer) value).longValue();
    }
    else if (value instanceof String) {
      String sValue = ((String) value).trim();
      convertedValue = Long.valueOf(sValue);
    }
    else
      convertedValue = (Long) value;
    return convertedValue;
  }

  public static Boolean convertBoolean(Object value) {
    Boolean convertedValue;
    if (value instanceof String) {
      String sValue = ((String) value).trim();
      convertedValue = Boolean.valueOf(sValue);
    }
    else
      convertedValue = (Boolean) value;
    return convertedValue;
  }

  public static String convertChar(Object value) {
    return String.valueOf(value).trim();
  }

  public static Date convertDate(Object value) {
    Date convertedValue = null;
    if (value instanceof String) {
      String sValue = ((String) value).trim();
      if (!sValue.isEmpty()) {
        try {
          convertedValue = DateFormat.getDateTimeFormat().parse(sValue);
        }
        catch (ParseException e) {
          logger.error(e.getMessage(), e);
          try {
            convertedValue = DateFormat.getDateFormat().parse(sValue);
          }
          catch (ParseException e1) {
            logger.error(e.getMessage(), e1);
            throw new RuntimeException(e);
          }
        }
      }
    }
    else if (value instanceof Timestamp) {
      convertedValue = new Date(((Timestamp) value).getTime());
    }
    else
      convertedValue = (Date) value;
    return convertedValue;
  }

  public static Array convertStringArray(Object value, PreparedStatement ps) throws SQLException {
    Object[] elements;
    if (value instanceof Collection)
      elements = ((Collection) value).toArray();
    else //if (value instanceof Object[])
      elements = (Object[]) value;
    return ps.getConnection().createArrayOf("varchar", elements);
  }

  public static Object convertByType(Column column, Object value) {
    if (column != null || TypeConverter.isNull(value))
      return value;
    else {
      switch (column.getTypeCode()) {
        case Types.DATE:
        case Types.TIME:
        case Types.TIMESTAMP:
          return TypeConverter.convertDate(value);
        case Types.NUMERIC:
          return TypeConverter.convertNumeric(value);
        case Types.INTEGER:
        case Types.SMALLINT:
        case Types.TINYINT:
          return TypeConverter.convertInteger(value);
        case Types.BIGINT:
          return TypeConverter.convertBigint(value);
        case Types.VARCHAR:
        case Types.CHAR:
        case Types.LONGVARCHAR:
          return TypeConverter.convertChar(value);
        case Types.BOOLEAN:
        case Types.BIT:
          return TypeConverter.convertBoolean(value);
        case Types.BINARY:
        case Types.VARBINARY:
        case Types.LONGVARBINARY:
          return TypeConverter.convertBoolean(value.toString().getBytes());
      }
    }
    return null;
  }
}
