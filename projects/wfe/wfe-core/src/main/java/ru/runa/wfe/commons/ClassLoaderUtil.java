/*
 * JBoss, Home of Professional Open Source
 * Copyright 2005, JBoss Inc., and individual contributors as indicated
 * by the @authors tag. See the copyright.txt in the distribution for a
 * full listing of individual contributors.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */
package ru.runa.wfe.commons;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Constructor;
import java.net.URL;
import java.util.Map;
import java.util.Properties;

import ru.runa.wfe.InternalApplicationException;

import com.google.common.base.Preconditions;
import com.google.common.collect.Maps;

/**
 * Utils.
 */
public class ClassLoaderUtil {
    /**
     * This map contains substitution for loaded classes (back compatibility on class loading).
     */
    private static Map<String, String> bcc = Maps.newHashMap();
    static {
        // formats renamed
        bcc.put("org.jbpm.web.formgen.format.DoubleFormat", "ru.runa.wfe.var.format.DoubleFormat");
        bcc.put("org.jbpm.web.formgen.format.DefaultFormat", "ru.runa.wfe.var.format.StringFormat");
        bcc.put("ru.runa.wf.web.forms.format.ArrayListFormat", "ru.runa.wfe.var.format.ArrayListFormat");
        bcc.put("ru.runa.wf.web.forms.format.BooleanFormat", "ru.runa.wfe.var.format.BooleanFormat");
        bcc.put("ru.runa.wf.web.forms.format.DateFormat", "ru.runa.wfe.var.format.DateFormat");
        bcc.put("ru.runa.wf.web.forms.format.DateTimeFormat", "ru.runa.wfe.var.format.DateTimeFormat");
        bcc.put("ru.runa.wf.web.forms.format.DoubleFormat", "ru.runa.wfe.var.format.DoubleFormat");
        bcc.put("ru.runa.wf.web.forms.format.FileFormat", "ru.runa.wfe.var.format.FileFormat");
        bcc.put("ru.runa.wf.web.forms.format.LongFormat", "ru.runa.wfe.var.format.LongFormat");
        bcc.put("ru.runa.wf.web.forms.format.StringArrayFormat", "ru.runa.wfe.var.format.StringArrayFormat");
        bcc.put("ru.runa.wf.web.forms.format.StringFormat", "ru.runa.wfe.var.format.StringFormat");
        bcc.put("ru.runa.wf.web.forms.format.TimeFormat", "ru.runa.wfe.var.format.TimeFormat");
        bcc.put("ru.runa.wf.web.forms.format.TimeWithSecondsFormat", "ru.runa.wfe.var.format.TimeWithSecondsFormat");
        // assignment handler renamed
        bcc.put("ru.runa.wf.jbpm.delegation.assignment.AssignmentHandler", "ru.runa.wfe.handler.assign.WfAssignmentHandler");
        // decision handler renamed
        bcc.put("ru.runa.wf.jbpm.delegation.decision.BSFDecisionHandler", "ru.runa.wfe.handler.decision.BSFDecisionHandler");
        // action handlers renamed
        bcc.put("ru.runa.wf.jbpm.delegation.action.SetSubProcessPermissionsActionHandler", "ru.runa.wfe.handler.action.EmptyActionHandler");
        bcc.put("ru.runa.wf.jbpm.delegation.action.BotInvokerActionHandler", "ru.runa.service.bot.handler.BotInvokerActionHandler");
        bcc.put("ru.runa.wfe.bp.commons.ExecuteFormulaActionHandler", "ru.runa.wfe.handler.action.var.FormulaActionHandler");
        bcc.put("ru.runa.wf.swimlane.AssignSwimlaneActionHandler", "ru.runa.wfe.handler.action.user.AssignSwimlaneActionHandler");
        bcc.put("ru.runa.wf.users.ActorNameActionHandler", "ru.runa.wfe.handler.action.user.ActorNameActionHandler");
        bcc.put("ru.runa.wf.var.AddObjectToListActionHandler", "ru.runa.wfe.handler.action.var.AddObjectToListActionHandler");
        bcc.put("ru.runa.wf.var.FormulaActionHandler", "ru.runa.wfe.handler.action.var.FormulaActionHandler");
        bcc.put("ru.runa.wf.var.RemoveObjectFromListActionHandler", "ru.runa.wfe.handler.action.var.RemoveObjectFromListActionHandler");
        bcc.put("ru.runa.wf.var.SortListActionHandler", "ru.runa.wfe.handler.action.var.SortListActionHandler");
        bcc.put("ru.runa.wf.BSHActionHandler", "ru.runa.wfe.handler.action.BSHActionHandler");
        bcc.put("ru.runa.wf.CreateOptionActionHandler", "ru.runa.wfe.handler.action.CreateOptionActionHandler");
        bcc.put("ru.runa.wf.EmailTaskNotifierActionHandler", "ru.runa.wfe.handler.action.EmailTaskNotifierActionHandler");
        bcc.put("ru.runa.wf.EscalationActionHandler", "ru.runa.wfe.handler.action.EscalationActionHandler");
        bcc.put("ru.runa.wf.GroovyActionHandler", "ru.runa.wfe.handler.action.GroovyActionHandler");
        bcc.put("ru.runa.wf.SendEmailActionHandler", "ru.runa.wfe.handler.action.SendEmailActionHandler");
        bcc.put("ru.runa.wf.SQLActionHandler", "ru.runa.wfe.handler.action.SQLActionHandler");
        // org functions renamed
        bcc.put("ru.runa.wfe.af.organizationfunction.ChiefFunction", "ru.runa.wfe.os.func.ChiefFunction");
        bcc.put("ru.runa.wfe.af.organizationfunction.ChiefRecursiveFunction", "ru.runa.wfe.os.func.ChiefRecursiveFunction");
        bcc.put("ru.runa.wfe.af.organizationfunction.DemoChiefFunction", "ru.runa.wfe.os.func.DemoChiefFunction");
        bcc.put("ru.runa.wfe.af.organizationfunction.DirectorFunction", "ru.runa.wfe.os.func.DirectorFunction");
        bcc.put("ru.runa.wfe.af.organizationfunction.ExecutorByCodeFunction", "ru.runa.wfe.os.func.ExecutorByCodeFunction");
        bcc.put("ru.runa.wfe.af.organizationfunction.ExecutorByNameFunction", "ru.runa.wfe.os.func.ExecutorByNameFunction");
        bcc.put("ru.runa.wfe.af.organizationfunction.SQLFunction", "ru.runa.wfe.os.func.SQLFunction");
        bcc.put("ru.runa.wfe.af.organizationfunction.SubordinateFunction", "ru.runa.wfe.os.func.SubordinateFunction");
        bcc.put("ru.runa.wfe.af.organizationfunction.SubordinateRecursiveFunction", "ru.runa.wfe.os.func.SubordinateRecursiveFunction");
    }

    public static Class<?> loadClass(String className, Class<?> callingClass) throws ClassNotFoundException {
        try {
            if (bcc.containsKey(className)) {
                className = bcc.get(className);
            }
            return Thread.currentThread().getContextClassLoader().loadClass(className);
        } catch (ClassNotFoundException e) {
            try {
                return Class.forName(className);
            } catch (ClassNotFoundException ex) {
                try {
                    return ClassLoaderUtil.class.getClassLoader().loadClass(className);
                } catch (ClassNotFoundException exc) {
                    return callingClass.getClassLoader().loadClass(className);
                }
            }
        }
    }

    public static Class<?> loadClass(String className) {
        try {
            return loadClass(className, ClassLoaderUtil.class);
        } catch (ClassNotFoundException e) {
            throw new InternalApplicationException("class not found '" + className + "'", e);
        }
    }

    public static <T extends Object> T instantiate(String className) {
        try {
            return (T) instantiate(loadClass(className));
        } catch (Exception e) {
            throw new InternalApplicationException(e);
        }
    }

    public static <T extends Object> T instantiate(Class<T> clazz) {
        try {
            return clazz.newInstance();
        } catch (Exception e) {
            throw new InternalApplicationException(e);
        }
    }

    public static Properties getProperties(String resource) {
        Properties properties = new Properties();
        try {
            InputStream is = getResourceAsStream(resource, ClassLoaderUtil.class);
            Preconditions.checkNotNull(is, "Unable to load properties " + resource);
            properties.load(is);
            is.close();
        } catch (IOException e) {
            throw new InternalApplicationException("couldn't load properties file '" + resource + "'", e);
        }
        return properties;
    }

    public static URL getResource(String resourceName, Class<?> callingClass) {
        ClassLoader loader = Thread.currentThread().getContextClassLoader();
        URL url = null;
        while ((loader != null) && (url == null)) {
            url = loader.getResource(resourceName);
            loader = loader.getParent();
        }
        if (url == null) {
            loader = ClassLoaderUtil.class.getClassLoader();
            url = loader.getResource(resourceName);
        }
        if (url == null) {
            loader = callingClass.getClassLoader();
            if (loader != null) {
                url = loader.getResource(resourceName);
            }
        }
        if ((url == null) && (resourceName != null) && (resourceName.charAt(0) != '/')) {
            return getResource('/' + resourceName, callingClass);
        }
        return url;
    }

    public static InputStream getResourceAsStream(String resourceName, Class<?> callingClass) {
        URL url = getResource(resourceName, callingClass);
        try {
            return (url != null) ? url.openStream() : null;
        } catch (IOException e) {
            return null;
        }
    }

    public static Object instantiate(String className, Object[] params) {
        try {
            Class<?> clazz = loadClass(className);
            Class<?>[] paramType;
            if (params != null) {
                paramType = new Class[params.length];
                for (int i = 0; i < params.length; ++i) {
                    paramType[i] = params[i].getClass();
                }
            } else {
                paramType = new Class[0];
                params = new Object[0];
            }
            Constructor<?> constructor = null;
            Constructor<?>[] constructors = clazz.getConstructors();
            constrLoop: for (Constructor<?> constr : constructors) {
                Class<?>[] types = constr.getParameterTypes();
                if (types.length != paramType.length) {
                    continue;
                }
                for (int i = 0; i < types.length; ++i) {
                    if (!types[i].isAssignableFrom(params[i].getClass())) {
                        continue constrLoop;
                    }
                }
                constructor = constr;
            }
            if (constructor == null) {
                constructor = clazz.getConstructor(paramType);
            }
            return constructor.newInstance(params);
        } catch (Exception e) {
            throw new InternalApplicationException(e);
        }
    }

}
