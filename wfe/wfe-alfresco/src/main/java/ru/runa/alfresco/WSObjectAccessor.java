package ru.runa.alfresco;

import java.beans.PropertyDescriptor;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.alfresco.util.ISO8601DateFormat;
import org.alfresco.webservice.types.NamedValue;
import org.alfresco.webservice.types.Reference;
import org.alfresco.webservice.util.Constants;
import org.alfresco.webservice.util.Utils;
import org.apache.commons.beanutils.PropertyUtils;

import ru.runa.wfe.commons.ITypeConvertor;
import ru.runa.wfe.commons.TypeConversionUtil;

/**
 * Converts java properties from/to alfresco {@link NamedValue}.
 * 
 * Limitations: list of strings only supported.
 * 
 * @author dofs
 */
@SuppressWarnings("unchecked")
public class WSObjectAccessor {
    private final AlfObject alfObject;
    private static final ISO8601DateConverter DATE_CONVERTER = new ISO8601DateConverter();

    public WSObjectAccessor(AlfObject alfObject) {
        this.alfObject = alfObject;
    }

    public void setProperty(AlfSerializerDesc desc, NamedValue prop) throws Exception {
        String fieldName = desc.getFieldName();
        if (desc.isNodeReference()) {
            alfObject.refFields.put(fieldName, prop.getValue());
            return;
        }
        PropertyDescriptor propertyDescriptor = PropertyUtils.getPropertyDescriptor(alfObject, fieldName);
        if (propertyDescriptor == null) {
            throw new IllegalArgumentException("No property '" + fieldName + "' found in " + getClass());
        }
        Object alfrescoValue;
        if (prop.getIsMultiValue()) {
            alfrescoValue = prop.getValues();
        } else {
            alfrescoValue = prop.getValue();
        }
        Object javaValue = TypeConversionUtil.convertTo(alfrescoValue, propertyDescriptor.getPropertyType(), DATE_CONVERTER, null);
        PropertyUtils.setProperty(alfObject, fieldName, javaValue);
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

    private NamedValue getProperty(AlfSerializerDesc desc) throws Exception {
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
        Object javaValue = PropertyUtils.getProperty(alfObject, fieldName);
        if (javaValue != null && (javaValue.getClass().isArray() || javaValue instanceof Collection<?>)) {
            String[] alfrescoStrings = TypeConversionUtil.convertTo(javaValue, String[].class, DATE_CONVERTER, null);
            return Utils.createNamedValue(desc.getPropertyNameWithNamespace(), alfrescoStrings);
        } else {
            String alfrescoString = TypeConversionUtil.convertTo(javaValue, String.class, DATE_CONVERTER, null);
            return Utils.createNamedValue(desc.getPropertyNameWithNamespace(), alfrescoString);
        }
    }

    private static class ISO8601DateConverter implements ITypeConvertor {

        @Override
        public <T> T convertTo(Object object, Class<T> classConvertTo) {
            if (object instanceof String) {
                if (classConvertTo == Date.class || classConvertTo == Calendar.class) {
                    synchronized (ISO8601DateFormat.class) {
                        Date date = ISO8601DateFormat.parse((String) object);
                        if (classConvertTo == Calendar.class) {
                            Calendar c = Calendar.getInstance();
                            c.setTime(date);
                            return (T) c;
                        }
                        return (T) date;
                    }
                }
            }
            return null;
        }

    }

}
