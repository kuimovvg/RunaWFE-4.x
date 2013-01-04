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
package ru.runa.wfe.commons;

import java.lang.reflect.Array;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.List;

import ru.runa.wfe.ApplicationException;
import ru.runa.wfe.InternalApplicationException;
import ru.runa.wfe.user.Actor;
import ru.runa.wfe.user.Executor;
import ru.runa.wfe.user.Group;
import ru.runa.wfe.user.dao.ExecutorDAO;

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
                try {
                    Method valueOfMethod = classConvertTo.getMethod("valueOf", String.class);
                    return (T) valueOfMethod.invoke(null, object);
                } catch (NoSuchMethodException e) {
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
            if (List.class.isAssignableFrom(object.getClass()) && classConvertTo.isArray()) {
                List<?> list = (List<?>) object;
                Object array = Array.newInstance(classConvertTo.getComponentType(), list.size());
                for (int i = 0; i < list.size(); i++) {
                    Array.set(array, i, list.get(i));
                }
                return (T) array;
            }
            if (List.class.isAssignableFrom(classConvertTo)) {
                List result = new ArrayList();
                if (object.getClass().isArray()) {
                    int len = Array.getLength(object);
                    for (int i = 0; i < len; i++) {
                        result.add(Array.get(object, i));
                    }
                } else if (object instanceof Collection<?>) {
                    result.addAll((Collection<?>) object);
                } else {
                    result.add(object);
                }
                return (T) result;
            }
            if (object instanceof Date && classConvertTo == Calendar.class) {
                return (T) CalendarUtil.dateToCalendar((Date) object);
            }
            if (object instanceof String && (classConvertTo == Calendar.class || classConvertTo == Date.class)) {
                Date date;
                String formattedDate = (String) object;
                try {
                    date = CalendarUtil.convertToDate(formattedDate, CalendarUtil.DATE_WITH_HOUR_MINUTES_SECONDS_FORMAT);
                } catch (Exception e1) {
                    try {
                        date = CalendarUtil.convertToDate(formattedDate, CalendarUtil.DATE_WITH_HOUR_MINUTES_FORMAT);
                    } catch (Exception e2) {
                        try {
                            date = CalendarUtil.convertToDate(formattedDate, CalendarUtil.DATE_WITHOUT_TIME_FORMAT);
                        } catch (Exception e3) {
                            try {
                                date = CalendarUtil.convertToDate(formattedDate, CalendarUtil.HOURS_MINUTES_SECONDS_FORMAT);
                            } catch (Exception e4) {
                                try {
                                    date = CalendarUtil.convertToDate(formattedDate, CalendarUtil.HOURS_MINUTES_FORMAT);
                                } catch (Exception e5) {
                                    throw new InternalApplicationException("Unable to find datetime format for '" + formattedDate + "'");
                                }
                            }
                        }
                    }
                }
                if (classConvertTo == Calendar.class) {
                    return (T) CalendarUtil.dateToCalendar(date);
                }
                return (T) date;
            }
            if (object instanceof Actor) {
                // compatibility: client code expecting 'actorCode'
                Long actorCode = ((Actor) object).getCode();
                return convertTo(actorCode, classConvertTo);
            }
            if (object instanceof Group) {
                // compatibility: client code expecting 'groupCode'
                String groupCode = "G" + ((Group) object).getId();
                return convertTo(groupCode, classConvertTo);
            }
            if (object instanceof String && Executor.class.isAssignableFrom(classConvertTo)) {
                String s = (String) object;
                if (s.startsWith("G")) {
                    Long id = Long.valueOf(((String) object).substring(1));
                    return (T) ApplicationContextFactory.getExecutorDAO().getExecutor(id);
                }
                try {
                    Long code = convertTo(object, Long.class);
                    return (T) ApplicationContextFactory.getExecutorDAO().getActorByCode(code);
                } catch (Exception e) {
                    return (T) ApplicationContextFactory.getExecutorDAO().getExecutor(s);
                }
            }
        } catch (Exception e) {
            throw new InternalApplicationException(e);
        }
        throw new InternalApplicationException("No conversion found between '" + object.getClass() + "' and '" + classConvertTo
                + "'. Add yours or don't use this method.");
    }

    public static int getArraySize(Object value) {
        if (value.getClass().isArray()) {
            return Array.getLength(value);
        } else if (value instanceof List) {
            return ((List<?>) value).size();
        } else {
            throw new RuntimeException("Unsupported array type " + value.getClass());
        }
    }

    public static Object getArrayVariable(Object value, int index) {
        if (value.getClass().isArray()) {
            Object[] array = (Object[]) value;
            if (array.length > index) {
                return array[index];
            } else {
                throw new RuntimeException("Array has insufficient length, index = " + index);
            }
        } else if (value instanceof List) {
            List<?> list = (List<?>) value;
            if (list.size() > index) {
                return list.get(index);
            } else {
                throw new RuntimeException("List has insufficient size, index = " + index);
            }
        } else {
            throw new RuntimeException("Unsupported array type " + value.getClass());
        }
    }

    public static <T extends Executor> T toExecutor(ExecutorDAO executorDAO, Object object) {
        if (object == null) {
            return null;
        }
        try {
            if (object.toString().startsWith("ID")) {
                Long executorId = Long.parseLong(object.toString().substring(2));
                return (T) executorDAO.getExecutor(executorId);
            } else {
                Long actorCode = convertTo(object, Long.class);
                return (T) executorDAO.getActorByCode(actorCode);
            }
        } catch (Exception e1) {
            String executorIdentity = object.toString();
            if (executorDAO.isExecutorExist(executorIdentity)) {
                return (T) executorDAO.getExecutor(executorIdentity);
            }
            if (executorIdentity.startsWith("G")) {
                Long executorId = convertTo(executorIdentity.substring(1), Long.class);
                return (T) executorDAO.getExecutor(executorId);
            }
        }
        throw new ApplicationException("Unable to convert '" + object + "' to executor");
    }
}
