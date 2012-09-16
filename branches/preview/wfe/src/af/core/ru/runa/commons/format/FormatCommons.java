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

package ru.runa.commons.format;

import java.lang.reflect.Constructor;
import java.lang.reflect.Modifier;

import ru.runa.InternalApplicationException;

public class FormatCommons {

    public static WebFormat create(String className, String format) {
        try {
            Class formatClass = createFormatClass(className);
            for (Constructor constructor : formatClass.getConstructors()) {
                Class[] parameters = constructor.getParameterTypes();
                if (parameters.length == 1 && parameters[0] == String.class && isConstructorPublic(constructor)) {
                    return (WebFormat) constructor.newInstance(new Object[] { format });
                }
            }
            throw new InternalApplicationException(className + " does not have public constructor with single String parameter.");
        } catch (Exception e) {
            throw new InternalApplicationException("Unable to create format " + className, e);
        }
    }

    public static WebFormat create(String className) {
        try {
            Class formatClass = createFormatClass(className);
            for (Constructor constructor : formatClass.getConstructors()) {
                Class[] parameters = constructor.getParameterTypes();
                if (parameters.length == 0 && isConstructorPublic(constructor)) {
                    return (WebFormat) constructor.newInstance();
                }
            }
            throw new InternalApplicationException(className + " does not have public constructor without parameters.");
        } catch (Exception e) {
            throw new InternalApplicationException("Unable to create format " + className, e);
        }
    }

    private static boolean isConstructorPublic(Constructor constructor) {
        return (constructor.getModifiers() & Modifier.PUBLIC) == 0 ? false : true;
    }

    private static Class createFormatClass(String className) throws ClassNotFoundException {
        Class formatClass = Class.forName(className);
        if (!WebFormat.class.isAssignableFrom(formatClass)) {
            throw new ClassCastException(className + " is not assignable from " + WebFormat.class);
        }
        return formatClass;
    }
}
