package ru.runa.alfresco;

import java.beans.PropertyDescriptor;
import java.io.Serializable;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.alfresco.model.ContentModel;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.namespace.QName;
import org.apache.commons.beanutils.PropertyUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import ru.runa.wfe.InternalApplicationException;
import ru.runa.wfe.commons.TypeConversionUtil;

import com.google.common.base.Throwables;

/**
 * Converts java properties from/to alfresco {@link Serializable}.
 * 
 * @author dofs
 */
@SuppressWarnings({ "unchecked", "rawtypes" })
public class JavaObjectAccessor {
    protected static Log log = LogFactory.getLog(JavaObjectAccessor.class);
    private final AlfObject alfObject;

    public JavaObjectAccessor(AlfObject alfObject) {
        this.alfObject = alfObject;
    }

    public void setProperty(AlfSerializerDesc desc, Serializable value) throws InternalApplicationException {
        try {
            String fieldName = desc.getFieldName();
            if (desc.isNodeReference()) {
                alfObject.refFields.put(fieldName, value);
                return;
            }
            PropertyDescriptor propertyDescriptor = PropertyUtils.getPropertyDescriptor(alfObject, fieldName);
            if (propertyDescriptor == null) {
                throw new IllegalArgumentException("No property '" + fieldName + "' found in " + getClass());
            }
            if (value instanceof List) {
                List list = (List) value;
                if (propertyDescriptor.getPropertyType().isArray()) {
                    Class componentClass = propertyDescriptor.getPropertyType().getComponentType();
                    Object array = Array.newInstance(componentClass, list.size());
                    for (int i = 0; i < list.size(); i++) {
                        Array.set(array, i, list.get(i));
                    }
                    setTypedProperty(fieldName, array);
                } else if (List.class.isAssignableFrom(propertyDescriptor.getPropertyType())) {
                    setTypedProperty(fieldName, new ArrayList(list));
                } else {
                    log.error("Unable to deserialize property to field " + propertyDescriptor.getName() + ": array or list expected.");
                }
            } else {
                Object v = TypeConversionUtil.convertTo(value, propertyDescriptor.getPropertyType());
                setTypedProperty(fieldName, v);
            }
        } catch (Exception e) {
            throw Throwables.propagate(e);
        }
    }

    protected void setTypedProperty(String propertyName, Object value) throws Exception {
        PropertyUtils.setProperty(alfObject, propertyName, value);
    }

    public Map<QName, Serializable> getAlfrescoProperties(AlfTypeDesc typeDesc, boolean all, boolean includeName) throws InternalApplicationException {
        Map<QName, Serializable> props = new HashMap<QName, Serializable>();
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
                props.put(desc.getPropertyQName(), getProperty(desc));
            }
        }
        if (includeName) {
            props.put(ContentModel.PROP_NAME, alfObject.getNewObjectName(typeDesc));
        }
        return props;
    }

    public Serializable getProperty(AlfSerializerDesc desc) throws InternalApplicationException {
        try {
            String fieldName = desc.getFieldName();
            if (desc.isNodeReference()) {
                return (NodeRef) alfObject.refFields.get(fieldName);
            }
            Object propValue = PropertyUtils.getProperty(alfObject, fieldName);
            if (propValue == null) {
                return null;
            }
            if (propValue.getClass().isArray()) {
                ArrayList list = new ArrayList();
                for (int i = 0; i < Array.getLength(propValue); i++) {
                    list.add(Array.get(propValue, i));
                }
                return list;
            } else if (propValue instanceof List) {
                // ?
                return (Serializable) propValue;
            } else {
                if (propValue instanceof Calendar) {
                    Calendar c = (Calendar) propValue;
                    return c.getTime();
                }
                return (Serializable) propValue;
            }
        } catch (Exception e) {
            throw Throwables.propagate(e);
        }
    }

}
