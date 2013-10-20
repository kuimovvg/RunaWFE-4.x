package ru.cg.runaex.database.bean.transport;

import java.io.Serializable;
import java.lang.reflect.Array;
import java.math.BigDecimal;
import java.sql.Timestamp;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ru.cg.runaex.core.DateFormat;

/**
 * Date: 17.08.12
 * Time: 12:55
 * <p/>
 * todo разобраться почему есть таблица и колонка, но нет схемы
 *
 * @author Sabirov
 */
public class Data implements Serializable {
  private static final long serialVersionUID = 6677483851061471215L;

  private static final Logger logger = LoggerFactory.getLogger(Data.class);

  public static final String EQ_POSTFIX = "_eq";
  public static final String NE_POSTFIX = "_ne";
  public static final String LT_POSTFIX = "_lt";
  public static final String LE_POSTFIX = "_le";
  public static final String GT_POSTFIX = "_gt";
  public static final String GE_POSTFIX = "_ge";
  public static final String IS_NULL_POSTFIX = "_is_null";
  public static final String IN_POSTFIX = "_in";
  public static final String NOT_IN_POSTFIX = "_notin";

  private String field;
  private AtomicReference<Object> value = new AtomicReference<Object>();
  private String valueClass;
  private boolean isPk = false;

  /**
   * additional filter data
   */
  private String schema;
  private String table;

  public Data() {
  }

  public Data(String field, Object value, String valueClass) {
    this.field = field;
    this.value.set(value);
    this.valueClass = valueClass;
  }

  public String getField() {
    return field;
  }

  public void setField(String field) {
    this.field = field;
  }

  public boolean isPk() {
    return isPk;
  }

  public void setPk(boolean pk) {
    isPk = pk;
  }

  public ClassType getClassType() {
    return ClassType.valueOfBySimpleName(valueClass);
  }

  public Object getValue() {
    Object oValue = this.value.get();

    if (oValue == null) {
      return null;
    }

    ClassType classType = getClassType();
    if (valueClass == null || valueClass.isEmpty()) {
      return oValue.toString();
    }

    if (oValue instanceof List) {
      List lValue = (List) oValue;
      List<Object> result = new ArrayList<Object>();
      for (Object o : lValue) {
        result.add(convertObjectValue(o, classType));
      }
      return result;
    }
    else if (oValue.getClass().isArray()) {
      //if array of Objects
      if (oValue instanceof Object[]) {
        Object[] arrValue = (Object[]) oValue;
        Class<?> arrayElementsType = arrValue.getClass().getComponentType();
        Object[] result = (Object[]) Array.newInstance(arrayElementsType, arrValue.length);
        for (int i = 0; i < arrValue.length; ++i) {
          result[i] = convertObjectValue(arrValue[i], classType);
        }
        return result;
      }
      //if array of primitives
      else {
        return oValue;
      }
    }

    return convertObjectValue(oValue, classType);
  }

  public void setValue(Object value) {
    if (value instanceof Timestamp) {
      value = DateFormat.getDateTimeFormat().format((Timestamp) value);
    }
    else if (value instanceof Date) {
      value = DateFormat.getDateFormat().format((Date) value);
    }
    else if (value instanceof Long) {
      value = value.toString();
    }
    this.value.set(value);
  }

  public String getValueClass() {
    return valueClass;
  }

  public void setValueClass(String valueClass) {
    this.valueClass = valueClass;
  }

  /**
   * If field is comparison field how add postfix: "_is_null",  "_eq", "_lt", "_le", "_gt" and "_ge"
   *
   * @return true if field is comparison
   */
  public boolean isComparisonField() {
    return field != null && (field.lastIndexOf(IS_NULL_POSTFIX) != -1 || field.lastIndexOf(LT_POSTFIX) != -1 ||
        field.lastIndexOf(LE_POSTFIX) != -1 || field.lastIndexOf(GT_POSTFIX) != -1 ||
        field.lastIndexOf(GE_POSTFIX) != -1 || field.lastIndexOf(EQ_POSTFIX) != -1);
  }

  /**
   * If field is comparison field how add postfix: "_eq", "_lt", "_le", "_gt" and "_ge"
   *
   * @return actual field name without this postfix
   */
  public String getFieldWithoutComparison() {
    if (field == null) {
      return null;
    }

    if (field.lastIndexOf(IS_NULL_POSTFIX) != -1) {
      return field.substring(0, field.length() - 8);
    }
    else if (field.lastIndexOf(LT_POSTFIX) != -1 || field.lastIndexOf(LE_POSTFIX) != -1 ||
        field.lastIndexOf(GT_POSTFIX) != -1 || field.lastIndexOf(GE_POSTFIX) != -1 ||
        field.lastIndexOf(EQ_POSTFIX) != -1 || field.lastIndexOf(IN_POSTFIX) != -1 ||
        field.lastIndexOf(NE_POSTFIX) != -1) {
      return field.substring(0, field.length() - 3);
    }
    else if (field.lastIndexOf(NOT_IN_POSTFIX) != -1) {
      return field.substring(0, field.length() - 6);
    }
    return field;
  }

  public boolean isFieldComparisonEq() {
    return field != null && field.lastIndexOf(EQ_POSTFIX) != -1;
  }

  public boolean isFieldComparisonNe() {
    return field != null && field.lastIndexOf(NE_POSTFIX) != -1;
  }

  public boolean isFieldComparisonLt() {
    return field != null && field.lastIndexOf(LT_POSTFIX) != -1;
  }

  public boolean isFieldComparisonLe() {
    return field != null && field.lastIndexOf(LE_POSTFIX) != -1;
  }

  public boolean isFieldComparisonGt() {
    return field != null && field.lastIndexOf(GT_POSTFIX) != -1;
  }

  public boolean isFieldComparisonGe() {
    return field != null && field.lastIndexOf(GE_POSTFIX) != -1;
  }

  public boolean isFieldComparisonIsNull() {
    return field != null && field.lastIndexOf(IS_NULL_POSTFIX) != -1;
  }

  public boolean isFieldComparisonIn() {
    return field != null && field.lastIndexOf(IN_POSTFIX) != -1;
  }

  public boolean isFieldComparisonNotIn() {
    return field != null && field.lastIndexOf(NOT_IN_POSTFIX) != -1;
  }

  public String getSchema() {
    return schema;
  }

  public void setSchema(String schema) {
    this.schema = schema;
  }

  public String getTable() {
    return table;
  }

  public void setTable(String table) {
    this.table = table;
  }

  private Object convertObjectValue(Object oValue, ClassType classType) {
    switch (classType) {
      case LONG:
      case INT8:
      case INT4:
        if (oValue instanceof Double) {
          oValue = ((Double) oValue).longValue();
        }
        else if (oValue instanceof Float) {
          oValue = ((Float) oValue).longValue();
        }
        else if (oValue instanceof Integer) {
          oValue = ((Integer) oValue).longValue();
        }
        else if (oValue instanceof String) {
          String sValue = ((String) oValue).trim();
          if (!sValue.isEmpty() && !"null".equals(sValue)) {
            oValue = Long.valueOf(sValue);
          }
          else {
            oValue = null;
          }
        }
        break;
      case BIG_DECIMAL:
        if (oValue instanceof Double) {
          oValue = BigDecimal.valueOf((Double) oValue);
        }
        else if (oValue instanceof Float) {
          oValue = BigDecimal.valueOf(((Float) oValue).doubleValue());
        }
        else if (oValue instanceof Integer) {
          oValue = new BigDecimal((Integer) oValue);
        }
        else if (oValue instanceof Long) {
          oValue = new BigDecimal((Long) oValue);
        }
        else if (oValue instanceof String) {
          String sValue = ((String) oValue).trim();
          if (!sValue.isEmpty() && !"null".equals(sValue)) {
            oValue = new BigDecimal(sValue);
          }
          else {
            oValue = null;
          }
        }
        break;
      case INTEGER:
        if (oValue instanceof Double) {
          oValue = ((Double) oValue).intValue();
        }
        else if (oValue instanceof Float) {
          oValue = ((Float) oValue).intValue();
        }
        else if (oValue instanceof Long) {
          oValue = ((Long) oValue).intValue();
        }
        else if (oValue instanceof String) {
          String sValue = ((String) oValue).trim();
          if (!sValue.isEmpty()) {
            oValue = Integer.valueOf(sValue);
          }
          else {
            oValue = null;
          }
        }
        break;
      case STRING:
      case VARCHAR:
        oValue = oValue != null ? String.valueOf(oValue).trim() : null;
        break;
      case BOOLEAN:
      case BOOL:
        if (oValue instanceof String) {
          String sValue = ((String) oValue).trim();
          if (!sValue.isEmpty()) {
            oValue = Boolean.valueOf(sValue);
          }
        }
        break;
      case DATE:
      case DATETIME:
      case TIMESTAMP:
      case TIMESTAMP_WITH_TIMEZONE:
        if (oValue instanceof String) {
          String sValue = ((String) oValue).trim();
          if (!sValue.isEmpty()) {
            try {
              oValue = DateFormat.getDateTimeFormat().parse(sValue);
            }
            catch (ParseException e) {
              logger.warn(e.getMessage(), e);
              try {
                oValue = DateFormat.getDateFormat().parse(sValue);
              }
              catch (ParseException e1) {
                logger.error(e.getMessage(), e1);
                throw new RuntimeException(e);
              }
            }
          }
          else {
            oValue = null;
          }
        }
        else if (oValue instanceof Timestamp) {
          oValue = new Date(((Timestamp) oValue).getTime());
        }
        break;
    }
    return oValue;
  }
}
