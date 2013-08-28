package ru.runa.alfresco;

import java.beans.PropertyDescriptor;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.alfresco.model.ContentModel;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.namespace.QName;
import org.apache.commons.beanutils.PropertyUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import ru.runa.wfe.InternalApplicationException;
import ru.runa.wfe.commons.ClassLoaderUtil;
import ru.runa.wfe.commons.TypeConversionUtil;

import com.google.common.base.Throwables;

/**
 * Converts java properties from/to alfresco {@link Serializable}.
 * 
 * @author dofs
 */
@SuppressWarnings({ "unchecked" })
public class JavaObjectAccessor {
    protected static Log log = LogFactory.getLog(JavaObjectAccessor.class);
    private final AlfObject alfObject;

    public JavaObjectAccessor(AlfObject alfObject) {
        this.alfObject = alfObject;
    }

    public void setProperty(AlfSerializerDesc desc, Serializable alfrescoValue) throws InternalApplicationException {
        try {
            String fieldName = desc.getFieldName();
            if (desc.isNodeReference()) {
                alfObject.setReferencePropertyUuid(fieldName, alfrescoValue != null ? alfrescoValue.toString() : null);
                return;
            }
            PropertyDescriptor propertyDescriptor = PropertyUtils.getPropertyDescriptor(alfObject, fieldName);
            if (propertyDescriptor == null) {
                throw new IllegalArgumentException("No property '" + fieldName + "' found in " + getClass());
            }
            Object javaValue = TypeConversionUtil.convertTo(propertyDescriptor.getPropertyType(), alfrescoValue);
            PropertyUtils.setProperty(alfObject, fieldName, javaValue);
        } catch (Exception e) {
            throw Throwables.propagate(e);
        }
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

    private Serializable getProperty(AlfSerializerDesc desc) throws InternalApplicationException {
        try {
            String fieldName = desc.getFieldName();
            if (desc.isNodeReference()) {
                String uuidRef = alfObject.getReferencePropertyUuid(fieldName, true);
                return uuidRef != null ? new NodeRef(uuidRef) : null;
            }
            Object javaValue = PropertyUtils.getProperty(alfObject, fieldName);
            if (desc.getDataType() != null) {
                Class<? extends Serializable> alfrescoType = (Class<? extends Serializable>) ClassLoaderUtil.loadClass(desc.getDataType());
                return TypeConversionUtil.convertTo(alfrescoType, javaValue);
            }
            return (Serializable) javaValue;
        } catch (Exception e) {
            throw Throwables.propagate(e);
        }
    }

}
