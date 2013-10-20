package ru.cg.runaex.database.dao.util;

import ru.cg.runaex.database.util.TypeConverter;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Types;
import java.util.Date;

/**
 * @author urmancheev
 */
public class PreparedStatementSetter {

  public static void setValue(Object value, int sqlType, int index, PreparedStatement ps) throws SQLException {
    if (TypeConverter.isNull(value)) {
      ps.setNull(index, sqlType);
      return;
    }

    switch (sqlType) {
      case Types.NUMERIC:
        ps.setBigDecimal(index, TypeConverter.convertNumeric(value));
        break;
      case Types.INTEGER:
        ps.setInt(index, TypeConverter.convertInteger(value));
        break;
      case Types.SMALLINT:
        ps.setShort(index, TypeConverter.convertInteger(value).shortValue());
      case Types.TINYINT:
        ps.setByte(index, TypeConverter.convertInteger(value).byteValue());
      case Types.BIGINT:
        ps.setLong(index, TypeConverter.convertBigint(value));
        break;
      case Types.VARCHAR:
      case Types.CHAR:
      case Types.LONGVARCHAR:
        ps.setString(index, TypeConverter.convertChar(value));
        break;
//      case Types.NCHAR:
//      case Types.NVARCHAR:
//      case Types.LONGNVARCHAR:
//      case Types.CLOB:
//      break;
      case Types.BOOLEAN:
      case Types.BIT:
        ps.setBoolean(index, TypeConverter.convertBoolean(value));
        break;
      case Types.DATE:
        Date date = TypeConverter.convertDate(value);
        ps.setDate(index, new java.sql.Date(date.getTime()));
        break;
      case Types.TIME:
        date = TypeConverter.convertDate(value);
        ps.setTime(index, new java.sql.Time(date.getTime()));
        break;
      case Types.TIMESTAMP:
        date = TypeConverter.convertDate(value);
        ps.setTimestamp(index, new java.sql.Timestamp(date.getTime()));
        break;
      case Types.BINARY:
      case Types.VARBINARY:
      case Types.LONGVARBINARY:
        ps.setBytes(index, (byte[]) value);
        break;
      case Types.ARRAY:
        ps.setArray(index, TypeConverter.convertStringArray(value, ps)); //todo supports only varchar arrays
        break;
//      case Types.BLOB:
//        break;
    }
  }
















}
