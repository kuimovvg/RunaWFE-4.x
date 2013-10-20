package ru.cg.runaex.esb.util;

import java.util.Date;
import java.util.GregorianCalendar;
import javax.xml.datatype.XMLGregorianCalendar;

import com.sun.org.apache.xerces.internal.jaxp.datatype.XMLGregorianCalendarImpl;

import ru.cg.runaex.esb.bean.Variable;

/**
 * @author urmancheev
 */
public final class VariableHelper {

  public static Variable createVariable(String name, Object value) {
    Variable variable = new Variable();
    variable.setName(name);

    if (value instanceof String)
      variable.setStringValue((String) value);
    else if (value instanceof Boolean)
      variable.setBooleanValue((Boolean) value);
    else if (value instanceof Long)
      variable.setLongValue((Long) value);
    else if (value instanceof Double)
      variable.setDoubleValue((Double) value);
    else if (value instanceof Date) {
      GregorianCalendar calendar = new GregorianCalendar();
      calendar.setTime((Date) value);

      XMLGregorianCalendar xmlDate = new XMLGregorianCalendarImpl(calendar);
      variable.setDateValue(xmlDate);
    }
    else if (value instanceof byte[])
      variable.setByteaValue((byte[]) value);
    else if (value != null)
      variable.setStringValue(value.toString());

    return variable;
  }

  public static Object getValue(Variable variable) {
    if (variable.getStringValue() != null)
      return variable.getStringValue();
    else if (variable.getBooleanValue() != null)
      return variable.getBooleanValue();
    else if (variable.getLongValue() != null)
      return variable.getLongValue();
    else if (variable.getDoubleValue() != null)
      return variable.getDoubleValue();
    else if (variable.getDateValue() != null)
      return variable.getDateValue();
    else
      return variable.getByteaValue();
  }
}
