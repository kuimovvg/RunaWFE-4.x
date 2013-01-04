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
import java.util.Date;

import javax.security.auth.Subject;

import ru.runa.wfe.InternalApplicationException;
import ru.runa.wfe.commons.ApplicationContextFactory;
import ru.runa.wfe.commons.CalendarUtil;
import ru.runa.wfe.commons.ClassLoaderUtil;
import ru.runa.wfe.commons.web.WebHelper;
import ru.runa.wfe.var.FileVariable;
import ru.runa.wfe.var.ISelectable;
import ru.runa.wfe.var.VariableDefinition;
import ru.runa.wfe.var.dto.WfVariable;

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

    public static VariableFormat create(VariableDefinition variableDefinition) {
        return create(variableDefinition.getFormatClassName());
    }

    public static VariableFormat create(WfVariable variable) {
        return create(variable.getDefinition());
    }

    private static boolean isConstructorPublic(Constructor<?> constructor) {
        return (constructor.getModifiers() & Modifier.PUBLIC) == 0 ? false : true;
    }

    private static Class<? extends VariableFormat> createFormatClass(String className) throws ClassNotFoundException {
        return (Class<? extends VariableFormat>) ClassLoaderUtil.loadClass(className);
    }

    public static String getVarOut(Object object, Subject subject, WebHelper webHelper, Long instanceId, String name, int listIndex, Object mapKey) {
        String value;
        if (object instanceof ISelectable) {
            value = ((ISelectable) object).getDisplayName();
        } else if (object instanceof Date) {
            value = CalendarUtil.formatDate((Date) object);
        } else if (object instanceof FileVariable) {
            value = FileFormat.getHtml((FileVariable) object, subject, webHelper, instanceId, name, listIndex, mapKey);
        } else if (object == null) {
            value = "";
        } else {
            value = String.valueOf(object);
        }
        return value;
    }

}
