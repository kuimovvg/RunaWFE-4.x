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
import java.util.Properties;

import ru.runa.wfe.InternalApplicationException;

import com.google.common.base.Preconditions;
import com.google.common.base.Throwables;

/**
 * Utils.
 */
public class ClassLoaderUtil {

    public static Class<?> loadClass(String className, Class<?> callingClass) throws ClassNotFoundException {
        try {
            className = BackCompatibilityClassNames.getClassName(className);
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

    @SuppressWarnings("unchecked")
    public static <T extends Object> T instantiate(String className) {
        return (T) instantiate(loadClass(className));
    }

    public static <T extends Object> T instantiate(Class<T> clazz) {
        try {
            return clazz.newInstance();
        } catch (Exception e) {
            throw Throwables.propagate(e);
        }
    }

    public static Properties getPropertiesNotNull(String resource) {
        Properties properties = new Properties();
        try {
            InputStream is = getAsStreamNotNull(resource, ClassLoaderUtil.class);
            properties.load(is);
            is.close();
        } catch (IOException e) {
            throw new InternalApplicationException("couldn't load properties file '" + resource + "'", e);
        }
        return properties;
    }

    private static URL getAsURL(String resourceName, Class<?> callingClass) {
        Preconditions.checkNotNull(resourceName, "resourceName");
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
        if (url == null) {
            url = callingClass.getResource(resourceName);
        }
        if ((url == null) && (resourceName != null) && (resourceName.charAt(0) != '/')) {
            return getAsURL('/' + resourceName, callingClass);
        }
        return url;
    }

    public static URL getAsURLNotNull(String resourceName, Class<?> callingClass) {
        URL url = getAsURL(resourceName, callingClass);
        if (url == null) {
            throw new InternalApplicationException("No resource found by '" + resourceName + "'");
        }
        return url;
    }

    /**
     * Get resource as stream.
     * 
     * @param resourceName
     *            classpath resource name
     * @param callingClass
     *            package of this class will be inspected for resources
     * @return resource stream or <code>null</code>
     */
    public static InputStream getAsStream(String resourceName, Class<?> callingClass) {
        URL url = getAsURL(resourceName, callingClass);
        try {
            return url != null ? url.openStream() : null;
        } catch (IOException e) {
            return null;
        }
    }

    /**
     * Get resource as stream.
     * 
     * @param resourceName
     *            classpath resource name
     * @param callingClass
     *            package of this class will be inspected for resources
     * @return resource stream
     */
    public static InputStream getAsStreamNotNull(String resourceName, Class<?> callingClass) {
        InputStream stream = getAsStream(resourceName, callingClass);
        if (stream == null) {
            throw new InternalApplicationException("No resource found by '" + resourceName + "'");
        }
        return stream;
    }

    public static Object instantiate(String className, Object... params) {
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
            throw Throwables.propagate(e);
        }
    }

}
