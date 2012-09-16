/*
 * This file is part of the RUNA WFE project.
 * 
 * This program is free software; you can redistribute it and/or 
 * modify it under the terms of the GNU Lesser General Public License 
 * as published by the Free Software Foundation; version 2.1 
 * of the License. 
 * 
 * This program is distributed in the hope that it will be useful, 
 * but WITHOUT ANY WARRANTY; without even the implied warranty of 
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the 
 * GNU Lesser General Public License for more details. 
 * 
 * You should have received a copy of the GNU Lesser General Public License 
 * along with this program; if not, write to the Free Software 
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA.
 */
package ru.runa.commons;

import java.lang.reflect.Array;
import java.lang.reflect.Method;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import ru.runa.InternalApplicationException;

public class TypeConversionUtil {

    @SuppressWarnings("unchecked")
    public static <T> T convertTo(Object object, Class<T> classConvertTo) {
        try {
            if (object == null || (object instanceof String && ((String) object).length() == 0)) {
                if (classConvertTo == long.class) {
                    return (T) new Long(0);
                } else if (classConvertTo == int.class) {
                    return (T) new Integer(0);
                } else if (classConvertTo == byte.class) {
                    return (T) new Byte((byte) 0);
                } else if (classConvertTo == double.class) {
                    return (T) new Double(0);
                } else if (classConvertTo == float.class) {
                    return (T) new Float(0);
                } else if (classConvertTo == boolean.class) {
                    return (T) Boolean.FALSE;
                } else {
                    return null;
                }
            }
            if (classConvertTo.isAssignableFrom(object.getClass())) {
                return (T) object;
            }
            if (classConvertTo == String.class) {
                return (T) object.toString();
            }
            if (object instanceof String) {
                // try to use 'valueOf(String)'
                Method valueOfMethod = classConvertTo.getMethod("valueOf", String.class);
                if (valueOfMethod != null) {
                    return (T) valueOfMethod.invoke(null, object);
                }
            }
            if (object instanceof Object[] && classConvertTo == String[].class) {
                Object[] source = (Object[]) object;
                String[] result = new String[source.length];
                int i = 0;
                for (Object o : source) {
                    result[i++] = o.toString();
                }
                return (T) result;
            }
            if (object instanceof Number && Number.class.isAssignableFrom(classConvertTo)) {
                Number n = (Number) object;
                if (classConvertTo == Long.class) {
                    return (T) new Long(n.longValue());
                }
                if (classConvertTo == Integer.class) {
                    return (T) new Integer(n.intValue());
                }
                if (classConvertTo == Byte.class) {
                    return (T) new Byte(n.byteValue());
                }
                if (classConvertTo == Double.class) {
                    return (T) new Double(n.doubleValue());
                }
                if (classConvertTo == Float.class) {
                    return (T) new Float(n.floatValue());
                }
            }
            if (object.getClass().isArray() && classConvertTo == List.class) {
                int len = Array.getLength(object);
                List<Object> result = new ArrayList<Object>(len);
                for (int i = 0; i < len; i++) {
                    result.add(Array.get(object, i));
                }
                return (T) result;
            }
            if (List.class.isAssignableFrom(object.getClass()) && classConvertTo == String[].class) {
                List<?> list = (List<?>) object;
                String[] result = new String[list.size()];
                for (int i = 0; i < list.size(); i++) {
                    result[i] = list.get(i).toString();
                }
                return (T) result;
            }
//            if (classConvertTo == List.class) {
//                List result = new ArrayList();
//                if (object.getClass().isArray()) {
//                    int len = Array.getLength(object);
//                    for (int i = 0; i < len; i++) {
//                        result.add(Array.get(object, i));
//                    }
//                } else {
//                    result.add(object);
//                }
//                return (T) result;
//            }
            if (object instanceof Date && classConvertTo == Calendar.class) {
                return (T) CalendarUtil.dateToCalendar((Date) object);
            }
            if (object instanceof String && classConvertTo == Calendar.class) {
                DateFormat format;
                String formattedDate = (String) object;
                if (formattedDate.length() == 10) {
                    format = CalendarUtil.DATE_WITHOUT_TIME_FORMAT;
                } else {
                    format = CalendarUtil.DATE_WITH_TIME_FORMAT;
                }
                Date date = format.parse(formattedDate);
                return (T) CalendarUtil.dateToCalendar(date);
            }
        } catch (Exception e) {
            throw new InternalApplicationException(e);
        }
        throw new InternalApplicationException("No conversion found between '" + object.getClass() + "' and '" + classConvertTo
                + "'. Add yours or don't use this method.");
    }
}
