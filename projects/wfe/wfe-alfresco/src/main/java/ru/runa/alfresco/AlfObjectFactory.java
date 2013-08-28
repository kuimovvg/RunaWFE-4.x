package ru.runa.alfresco;

import java.beans.Introspector;
import java.lang.reflect.Method;
import java.util.Collection;
import java.util.Map;

import net.sf.cglib.proxy.Callback;
import net.sf.cglib.proxy.CallbackFilter;
import net.sf.cglib.proxy.Enhancer;
import net.sf.cglib.proxy.MethodInterceptor;
import net.sf.cglib.proxy.MethodProxy;
import net.sf.cglib.proxy.NoOp;

import org.apache.commons.beanutils.BeanUtils;

import ru.runa.wfe.commons.ClassLoaderUtil;

import com.google.common.base.Throwables;
import com.google.common.collect.Maps;

@SuppressWarnings("unchecked")
public class AlfObjectFactory {
    private static Map<Class<? extends AlfObject>, Enhancer> ENHANCERS = Maps.newHashMap();

    public static synchronized AlfObject create(String javaClassName) {
        Class<AlfObject> clazz = (Class<AlfObject>) ClassLoaderUtil.loadClass(javaClassName);
        return create(clazz);
    }

    public static synchronized <T extends AlfObject> T create(Class<T> clazz) {
        if (!ENHANCERS.containsKey(clazz)) {
            Enhancer enhancer = new Enhancer();
            enhancer.setSuperclass(clazz);
            enhancer.setCallbackFilter(new AlfObjectCallbackFilter(clazz));
            enhancer.setCallbacks(new Callback[] { NoOp.INSTANCE, new GetAlfObjectInterceptor(), new SetAlfObjectInterceptor(),
                    new GetAlfObjectsCollectionInterceptor() });
            ENHANCERS.put(clazz, enhancer);
        }
        T alfObject = (T) ENHANCERS.get(clazz).create();
        return alfObject;
    }

    public static class AlfObjectCallbackFilter implements CallbackFilter {
        private final Class<? extends AlfObject> alfObjectClass;

        public AlfObjectCallbackFilter(Class<? extends AlfObject> alfObjectClass) {
            this.alfObjectClass = alfObjectClass;
        }

        @Override
        public int accept(Method method) {
            try {
                String propertyName = getFieldName(method.getName());
                AlfSerializerDesc desc = Mappings.getMapping(alfObjectClass).getPropertyDescByFieldName(propertyName);
                if (desc == null) {
                    return 0;
                }
                if (desc.getProperty() != null && desc.getProperty().noderef()) {
                    if (method.getName().startsWith("get")) {
                        return 1;
                    }
                    if (method.getName().startsWith("set")) {
                        return 2;
                    }
                }
                if (desc.getAssoc() != null && method.getName().startsWith("get")) {
                    return 3;
                }
                return 0;
            } catch (Exception e) {
                throw Throwables.propagate(e);
            }
        }

    }

    private static String getFieldName(String methodName) {
        return Introspector.decapitalize(methodName.substring(3));
    }

    public static class GetAlfObjectInterceptor implements MethodInterceptor {

        @Override
        public Object intercept(Object object, Method method, Object[] objects, MethodProxy methodProxy) throws Throwable {
            AlfObject result = (AlfObject) methodProxy.invokeSuper(object, objects);
            if (result == null) {
                AlfObject alfObject = (AlfObject) object;
                String propertyName = getFieldName(method.getName());
                String uuidRef = alfObject.getReferencePropertyUuid(propertyName, false);
                if (uuidRef == null) {
                    return null;
                }
                if (uuidRef.equals(alfObject.getUuidRef())) {
                    result = alfObject;
                } else {
                    result = alfObject.conn.loadObjectNotNull(uuidRef);
                }
                BeanUtils.setProperty(alfObject, propertyName, result);
            }
            return result;
        }
    }

    public static class SetAlfObjectInterceptor implements MethodInterceptor {

        @Override
        public Object intercept(Object object, Method method, Object[] objects, MethodProxy methodProxy) throws Throwable {
            String propertyName = getFieldName(method.getName());
            AlfObject alfObject = (AlfObject) object;
            AlfObject param = (AlfObject) objects[0];
            alfObject.setReferencePropertyUuid(propertyName, param != null ? param.getUuidRef() : null);
            return methodProxy.invokeSuper(object, objects);
        }
    }

    public static class GetAlfObjectsCollectionInterceptor implements MethodInterceptor {

        @Override
        public Object intercept(Object object, Method method, Object[] objects, MethodProxy methodProxy) throws Throwable {
            Collection<AlfObject> result = (Collection<AlfObject>) methodProxy.invokeSuper(object, objects);
            if (result.isEmpty()) {
                AlfObject alfObject = (AlfObject) object;
                String fieldName = getFieldName(method.getName());
                AlfTypeDesc typeDesc = Mappings.getMapping(alfObject.getClass());
                AlfSerializerDesc desc = typeDesc.getPropertyDescByFieldName(fieldName);
                if (desc == null) {
                    throw new NullPointerException("No association defined for " + fieldName);
                }
                alfObject.loadCollection(desc, result);
            }
            return result;
        }
    }

}
