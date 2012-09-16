package ru.runa.commons;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Collection;
import java.util.HashSet;
import java.util.Map;

import org.hibernate.proxy.HibernateProxy;

public class JBPMLazyLoaderHelper {

    private final HashSet<Object> visited = new HashSet<Object>();

    private JBPMLazyLoaderHelper() {
    }

    public static Object forceLoading(Object object) {
        return new JBPMLazyLoaderHelper().load(object);
    }

    public static Object getImplementation(Object object) {
        if (object instanceof HibernateProxy) {
            return ((HibernateProxy) object).getHibernateLazyInitializer().getImplementation();
        }
        return object;
    }

    public static Class getClass(Object object) {
        if (object instanceof HibernateProxy) {
            return ((HibernateProxy) object).getHibernateLazyInitializer().getImplementation().getClass();
        }
        return object.getClass();
    }

    private Object load(Object object) {
        if (object == null) {
            return object;
        }
        if (visited.contains(object)) {
            return object;
        }
        visited.add(object);
        if (object instanceof Map) {
            for (Object inMap : ((Map) object).values()) {
                load(inMap);
            }
            return object;
        }
        if (object instanceof Collection) {
            for (Object inCollection : (Collection) object) {
                load(inCollection);
            }
            return object;
        }
        if (object instanceof HibernateProxy) {
            object = ((HibernateProxy) object).getHibernateLazyInitializer().getImplementation();
        }
        if (!object.getClass().getName().startsWith("ru.runa.bpm")) {
            return object;
        }

        Method[] methods = object.getClass().getMethods();
        for (Method method : methods) {
            if (!method.getName().startsWith("get")) {
                continue;
            }
            if (method.getParameterTypes() != null && method.getParameterTypes().length != 0) {
                continue;
            }
            try {
                if (method.getReturnType() != null) {
                    load(method.invoke(object, (Object[]) null));
                }
            } catch (IllegalAccessException e) {
            } catch (InvocationTargetException e) {
            }
        }
        loadFields(object, null);
        // If we found another instance of HibernateProxy class we need to load them.
        if (object instanceof HibernateProxy) {
            visited.remove(object);
        }
        return object;
    }

    private void loadFields(Object object, Class currentClass) {
        if (currentClass == null) {
            currentClass = object.getClass();
        }
        Field[] fields = currentClass.getDeclaredFields();
        for (Field field : fields) {
            field.setAccessible(true);
            try {
                if (field.get(object) instanceof HibernateProxy) {
                    field.set(object, ((HibernateProxy) field.get(object)).getHibernateLazyInitializer().getImplementation());
                }
                load(field.get(object));
            } catch (IllegalAccessException e) {
            }
        }
        if (currentClass.getSuperclass() != null) {
            loadFields(object, currentClass.getSuperclass());
        }
    }
}
