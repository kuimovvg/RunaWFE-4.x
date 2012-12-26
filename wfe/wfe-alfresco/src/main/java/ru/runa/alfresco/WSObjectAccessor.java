package ru.runa.alfresco;

import java.beans.PropertyDescriptor;
import java.lang.reflect.Array;
import java.lang.reflect.Modifier;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.alfresco.util.ISO8601DateFormat;
import org.alfresco.webservice.types.NamedValue;
import org.alfresco.webservice.types.Reference;
import org.alfresco.webservice.util.Constants;
import org.alfresco.webservice.util.Utils;
import org.apache.commons.beanutils.PropertyUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Converts java properties from/to alfresco {@link NamedValue}.
 * 
 * @author dofs
 */
public class WSObjectAccessor {
    protected static Log log = LogFactory.getLog(WSObjectAccessor.class);
    public static final DateFormat ALF_DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.000Z");
    private final AlfObject alfObject;

    public WSObjectAccessor(AlfObject alfObject) {
        this.alfObject = alfObject;
    }

    @SuppressWarnings("unchecked")
    public void setProperty(AlfSerializerDesc desc, NamedValue prop) throws Exception {
        String fieldName = desc.getFieldName();
        if (desc.isNodeReference()) {
            alfObject.refFields.put(fieldName, prop.getValue());
            return;
        }
        PropertyDescriptor descriptor = PropertyUtils.getPropertyDescriptor(alfObject, fieldName);
        if (descriptor == null) {
            throw new IllegalArgumentException("No property '" + fieldName + "' found in " + getClass());
        }
        if (prop.getIsMultiValue()) {
            String[] stringValues = prop.getValues();
            Class<?> componentType = descriptor.getPropertyType().getComponentType();
            PropertyDescriptor propertyDescriptor = PropertyUtils.getPropertyDescriptor(alfObject, fieldName);
            Object propValue = null;
            if (propertyDescriptor.getPropertyType().isArray()) {
                propValue = Array.newInstance(componentType, stringValues.length);
                for (int i = 0; i < stringValues.length; i++) {
                    Object value = alfrescoToJava(stringValues[i], componentType);
                    Array.set(propValue, i, value);
                }
            } else if (List.class.isAssignableFrom(propertyDescriptor.getPropertyType())) {
                try {
                    if (stringValues != null) {
                        List<Object> list = null;
                        int modifiers = propertyDescriptor.getPropertyType().getModifiers();
                        if (!Modifier.isAbstract(modifiers)) {
                            list = (List<Object>) propertyDescriptor.getPropertyType().newInstance();
                        } else {
                            list = new ArrayList<Object>();
                        }
                        for (int i = 0; i < stringValues.length; i++) {
                            Object value = alfrescoToJava(stringValues[i], componentType);
                            list.add(value);
                        }
                        propValue = list;
                    }
                } catch (Throwable e) {
                    log.error("Unable to deserialize property to field " + propertyDescriptor.getName(), e);
                }
            } else {
                log.error("Unable to deserialize property to field " + propertyDescriptor.getName() + ": array or list expected.");
            }
            setTypedProperty(fieldName, propValue);
        } else {
            String stringValue = prop.getValue();
            Object value = alfrescoToJava(stringValue, descriptor.getPropertyType());
            setTypedProperty(fieldName, value);
        }
    }

    protected void setTypedProperty(String propertyName, Object value) throws Exception {
        PropertyUtils.setProperty(alfObject, propertyName, value);
    }

    private Object alfrescoToJava(String value, Class<?> fieldClass) throws Exception {
        if (value == null) {
            return null;
        }
        if (fieldClass == Long.class || fieldClass == long.class) {
            return Long.parseLong(value);
        }
        if (fieldClass == Integer.class || fieldClass == int.class) {
            return Integer.parseInt(value);
        }
        if (fieldClass == Boolean.class || fieldClass == boolean.class) {
            return Boolean.parseBoolean(value);
        }
        if (fieldClass == Calendar.class) {
            Calendar c = Calendar.getInstance();
            c.setTime(ISO8601DateFormat.parse(value));
            return c;
        }
        return value;
    }

    public NamedValue[] getAlfrescoProperties(AlfTypeDesc typeDesc, boolean all, boolean includeName) throws Exception {
        Map<String, NamedValue> props = new HashMap<String, NamedValue>();
        Collection<AlfSerializerDesc> propDescs;
        if (all) {
            propDescs = typeDesc.getAllDescs();
        } else {
            propDescs = new ArrayList<AlfSerializerDesc>();
            for (String fieldName : alfObject.getDirtyFieldNames(typeDesc)) {
                propDescs.add(typeDesc.getPropertyDescByFieldName(fieldName));
            }
        }
        for (AlfSerializerDesc desc : propDescs) {
            if (desc.getProperty() != null && !desc.getProperty().readOnly()) {
                NamedValue namedValue = getProperty(desc);
                props.put(namedValue.getName(), namedValue);
            }
        }
        if (includeName) {
            NamedValue namedValue = Utils.createNamedValue(Constants.PROP_NAME, alfObject.getNewObjectName(typeDesc));
            props.put(namedValue.getName(), namedValue);
        }
        return new ArrayList<NamedValue>(props.values()).toArray(new NamedValue[props.size()]);
    }

    public NamedValue getProperty(AlfSerializerDesc desc) throws Exception {
        String fieldName = desc.getFieldName();
        if (desc.isNodeReference()) {
            Object uuidObject = alfObject.refFields.get(fieldName);
            String uuidString;
            if (uuidObject instanceof Reference) {
                Reference reference = (Reference) uuidObject;
                uuidString = AlfSession.getUuidRef(reference);
            } else {
                uuidString = (String) uuidObject;
            }
            return Utils.createNamedValue(desc.getPropertyNameWithNamespace(), uuidString);
        }
        Object propValue = PropertyUtils.getProperty(alfObject, fieldName);
        if (propValue == null) {
            return Utils.createNamedValue(desc.getPropertyNameWithNamespace(), (String) null);
        }
        if (propValue.getClass().isArray()) {
            String[] result = new String[Array.getLength(propValue)];
            for (int i = 0; i < result.length; i++) {
                result[i] = Array.get(propValue, i).toString();
            }
            return Utils.createNamedValue(desc.getPropertyNameWithNamespace(), result);
        } else if (propValue instanceof List<?>) {
            List<?> list = (List<?>) propValue;
            String[] result = new String[list.size()];
            for (int i = 0; i < result.length; i++) {
                result[i] = list.get(i).toString();
            }
            return Utils.createNamedValue(desc.getPropertyNameWithNamespace(), result);
        } else {
            String formattedPropValue;
            if (propValue instanceof Calendar) {
                Calendar c = (Calendar) propValue;
                formattedPropValue = ISO8601DateFormat.format(c.getTime());
            } else {
                formattedPropValue = propValue.toString();
            }
            return Utils.createNamedValue(desc.getPropertyNameWithNamespace(), formattedPropValue);
        }
    }
}
