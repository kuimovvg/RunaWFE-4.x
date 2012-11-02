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

package ru.runa.wfe.var.format;

import java.lang.reflect.Constructor;
import java.lang.reflect.Modifier;

import ru.runa.wfe.InternalApplicationException;
import ru.runa.wfe.commons.ApplicationContextFactory;
import ru.runa.wfe.commons.ClassLoaderUtil;

public class FormatCommons {

    public static VariableFormat create(String className, String format) {
        try {
            Class<? extends VariableFormat> formatClass = createFormatClass(className);
            for (Constructor<?> constructor : formatClass.getConstructors()) {
                Class<?>[] parameters = constructor.getParameterTypes();
                if (parameters.length == 1 && parameters[0] == String.class && isConstructorPublic(constructor)) {
                    return (VariableFormat) constructor.newInstance(new Object[] { format });
                }
            }
            throw new InternalApplicationException(className + " does not have public constructor with single String parameter.");
        } catch (Exception e) {
            throw new InternalApplicationException("Unable to create format " + className, e);
        }
    }

    public static VariableFormat create(String className) {
        return ApplicationContextFactory.createAutowiredBean(className);
    }

    private static boolean isConstructorPublic(Constructor<?> constructor) {
        return (constructor.getModifiers() & Modifier.PUBLIC) == 0 ? false : true;
    }

    private static Class<? extends VariableFormat> createFormatClass(String className) throws ClassNotFoundException {
        return (Class<? extends VariableFormat>) ClassLoaderUtil.loadClass(className);
    }
}
