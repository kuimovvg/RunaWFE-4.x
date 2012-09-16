package ru.runa.commons;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Set;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

// TODO move to ClassLoaderUtil
public class Loader {
    private static Log log = LogFactory.getLog(Loader.class);

    public static Object loadObject(String name, Object[] params) throws InstantiationException, NoSuchMethodException, InvocationTargetException,
            IllegalAccessException, ClassNotFoundException {
        Class clazz = Class.forName(name);
        Class[] paramType;
        if (params != null) {
            paramType = new Class[params.length];
            for (int i = 0; i < params.length; ++i) {
                paramType[i] = params[i].getClass();
            }
        } else {
            paramType = new Class[0];
            params = new Object[0];
        }
        Constructor constructor = null;
        Constructor[] constructors = clazz.getConstructors();
        constrLoop: for (Constructor constr : constructors) {
            Class[] types = constr.getParameterTypes();
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
    }

    public static Object loadObject(String name, Object[] params, boolean noThrow) {
        try {
            return loadObject(name, params);
        } catch (InvocationTargetException e) {
        } catch (NoSuchMethodException e) {
        } catch (IllegalAccessException e) {
        } catch (ClassNotFoundException e) {
        } catch (InstantiationException e) {
        }
        return null;
    }

    public static <T> Set<Class<? extends T>> findSubclassesAtDeploy(String[] jars, Class<T> searchTreeRoot) {
        String jbossDeployDir = System.getProperty("jboss.server.home.dir") + "/deploy/";
        for (int i = 0; i < jars.length; ++i) {
            jars[i] = jbossDeployDir + jars[i];
        }
        return findSubclasses(jars, searchTreeRoot);
    }

    public static <T> Set<Class<? extends T>> findSubclasses(String[] jars, Class<T> searchTreeRoot) {
        Set<Class<? extends T>> result = new HashSet<Class<? extends T>>();
        for (int i = 0; i < jars.length; i++) {
            try {
                JarFile jarFile = new JarFile(jars[i].trim());
                Enumeration<JarEntry> entries = jarFile.entries();
                while (entries.hasMoreElements()) {
                    // If we can't load class - just move to next class. 
                    try {
                        JarEntry entry = entries.nextElement();
                        String entryName = entry.getName();
                        if (entryName.endsWith(".class")) {
                            int lastIndexOfDotSymbol = entryName.lastIndexOf('.');
                            entryName = entryName.substring(0, lastIndexOfDotSymbol).replace('/', '.');

                            Class someClass = Class.forName(entryName);
                            if (searchTreeRoot.isAssignableFrom(someClass)) {
                                result.add(someClass);
                            }
                        }
                    } catch (Throwable e) {
                        //log.warn("Error on loading task handlers list." + e.getMessage());
                    }
                }
            } catch (Throwable e) {
                log.warn("Subclass search got error on file " + jars[i], e);
            }
        }
        return result;
    }
}
