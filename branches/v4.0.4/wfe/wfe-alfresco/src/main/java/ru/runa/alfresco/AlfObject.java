package ru.runa.alfresco;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.webservice.types.Predicate;
import org.alfresco.webservice.types.Reference;
import org.apache.commons.beanutils.PropertyUtils;

import ru.runa.EqualsUtil;
import ru.runa.alfresco.anno.Property;
import ru.runa.alfresco.anno.Type;
import ru.runa.wfe.InternalApplicationException;
import ru.runa.wfe.commons.CalendarUtil;

import com.google.common.base.Throwables;

/**
 * Base class for all mappable objects.
 * 
 * @author dofs
 */
@Type(name = "content", prefix = "cm")
@SuppressWarnings({ "unchecked", "rawtypes" })
public class AlfObject implements Serializable {
    private final static long serialVersionUID = 197L;

    protected transient AlfConn conn;
    private Reference reference;
    private NodeRef nodeRef;
    private transient final Map<String, Object> initialFieldValues = new HashMap<String, Object>();
    protected Map<String, Object> refFields = new HashMap<String, Object>();
    protected Map<String, List<Object>> refCollections = new HashMap<String, List<Object>>();

    @Property(name = "name")
    private String objectName;
    @Property(name = "modified", readOnly = true)
    private Calendar lastUpdated;

    // protected boolean versionable;

    public void setLazyLoader(AlfConn lazyLoader) {
        conn = lazyLoader;
    }

    public void markInitialState(AlfTypeDesc mapping) {
        try {
            initialFieldValues.clear();
            for (AlfSerializerDesc desc : mapping.getAllDescs()) {
                if (desc.getProperty() == null || desc.getProperty().readOnly()) {
                    continue;
                }
                String fieldName = desc.getFieldName();
                if (desc.isNodeReference()) {
                    initialFieldValues.put(fieldName, refFields.get(fieldName));
                } else {
                    Object object = PropertyUtils.getProperty(this, fieldName);
                    if (object == null) {
                        initialFieldValues.put(fieldName, null);
                    } else if (object instanceof String) {
                        initialFieldValues.put(fieldName, object);
                    } else if (Number.class.isAssignableFrom(object.getClass())) {
                        initialFieldValues.put(fieldName, object);
                    } else if (object instanceof Boolean) {
                        initialFieldValues.put(fieldName, object);
                    } else if (object instanceof Calendar) {
                        initialFieldValues.put(fieldName, CalendarUtil.clone((Calendar) object));
                    } else if (object.getClass().isArray()) {
                        // arrays are immutable
                    } else if (List.class.isAssignableFrom(object.getClass())) {
                        initialFieldValues.put(fieldName, ((List) object).toArray());
                    } else {
                        System.err.println("No clone op supported for field " + fieldName);
                    }
                }
            }
        } catch (Exception e) {
            throw Throwables.propagate(e);
        }
    }

    public String getObjectName() {
        return objectName;
    }

    public void setObjectName(String objectName) {
        this.objectName = objectName;
    }

    public Calendar getLastUpdated() {
        return lastUpdated;
    }

    public void setLastUpdated(Calendar lastUpdated) {
        this.lastUpdated = lastUpdated;
    }

    public void updateObjectName() {
        AlfTypeDesc typeDesc = Mappings.getMapping(getClass());
        setObjectName(getNewObjectName(typeDesc));
    }

    protected String getNewObjectName(AlfTypeDesc typeDesc) {
        return null;
    }

    public Set<String> getDirtyFieldNames(AlfTypeDesc mapping) {
        try {
            Set<String> dirtyFieldNames = new HashSet<String>();
            for (String fieldName : initialFieldValues.keySet()) {
                Object initialObject = initialFieldValues.get(fieldName);
                Object currentObject;
                if (mapping.getPropertyDescByFieldName(fieldName).isNodeReference()) {
                    currentObject = refFields.get(fieldName);
                } else {
                    currentObject = PropertyUtils.getProperty(this, fieldName);
                    if (currentObject instanceof List) {
                        currentObject = ((List) currentObject).toArray();
                    }
                }
                if (!EqualsUtil.equals(initialObject, currentObject)) {
                    dirtyFieldNames.add(fieldName);
                }
            }
            return dirtyFieldNames;
        } catch (Exception e) {
            throw Throwables.propagate(e);
        }
    }

    public String getUuidRef() {
        if (reference != null) {
            return AlfSession.getUuidRef(reference);
        }
        if (nodeRef != null) {
            return nodeRef.toString();
        }
        throw new InternalApplicationException("No uuid found in " + this);
    }

    public Reference getReference() {
        return reference;
    }

    private Object getRef() {
        return nodeRef != null ? nodeRef : reference;
    }

    public NodeRef getNodeRef() {
        return nodeRef;
    }

    public String getId() {
        return nodeRef.getId();
    }

    public Predicate getPredicate() {
        return new Predicate(new Reference[] { reference }, reference.getStore(), null);
    }

    public void setReference(Reference reference) {
        this.reference = reference;
    }

    public void setNodeRef(NodeRef nodeRef) {
        this.nodeRef = nodeRef;
    }

    protected void setObjectProperty(String fieldName, AlfObject alfObject) {
        Object objectId = null;
        if (alfObject != null) {
            objectId = alfObject.getRef();
        }
        refFields.put(fieldName, objectId);
    }

    protected <T extends AlfObject> T lazyLoadObject(String fieldName) {
        Object objectId = refFields.get(fieldName);
        if (objectId == null) {
            return null;
        }
        if (objectId.equals(getUuidRef()) || objectId.equals(nodeRef)) {
            return (T) this;
        }
        return (T) conn.loadObjectNotNull(objectId);
    }

    protected void lazyLoadCollection(String fieldName, Collection<? extends AlfObject> collection) {
        if (conn == null) {
            return;
        }
        AlfTypeDesc typeDesc = Mappings.getMapping(getClass());
        AlfSerializerDesc desc = typeDesc.getPropertyDescByFieldName(fieldName);
        if (desc == null) {
            throw new NullPointerException("No association defined for field " + fieldName);
        }
        conn.loadAssociation(getRef(), collection, desc);
        List<Object> assocIds = new ArrayList<Object>();
        for (AlfObject alfObject : collection) {
            assocIds.add(alfObject.getRef());
        }
        refCollections.put(fieldName, assocIds);
    }

    protected void clearCollections() {
    }

    public Map<AlfSerializerDesc, List<Object>> getAssocToCreate() {
        try {
            AlfTypeDesc typeDesc = Mappings.getMapping(getClass());
            Map<AlfSerializerDesc, List<Object>> result = new HashMap<AlfSerializerDesc, List<Object>>();
            for (String fieldName : refCollections.keySet()) {
                List<Object> assocResults = new ArrayList<Object>();
                List<Object> assocIds = refCollections.get(fieldName);
                Collection<AlfObject> objects = (Collection<AlfObject>) PropertyUtils.getProperty(this, fieldName);
                for (AlfObject alfObject : objects) {
                    if (alfObject.getRef() == null) {
                        throw new RuntimeException("Save object before adding to association.");
                    }
                    if (!assocIds.contains(alfObject.getRef())) {
                        assocResults.add(alfObject.getRef());
                    }
                }
                if (assocResults.size() > 0) {
                    result.put(typeDesc.getPropertyDescByFieldName(fieldName), assocResults);
                }
            }
            return result;
        } catch (Exception e) {
            throw Throwables.propagate(e);
        }
    }

    public Map<AlfSerializerDesc, List<Object>> getAssocToDelete() {
        try {
            AlfTypeDesc typeDesc = Mappings.getMapping(getClass());
            Map<AlfSerializerDesc, List<Object>> result = new HashMap<AlfSerializerDesc, List<Object>>();
            for (String fieldName : refCollections.keySet()) {
                List<Object> assocIds = new ArrayList<Object>(refCollections.get(fieldName));
                Collection<AlfObject> objects = (Collection<AlfObject>) PropertyUtils.getProperty(this, fieldName);
                for (AlfObject alfObject : objects) {
                    if (alfObject.getRef() == null) {
                        throw new RuntimeException("Save object before deleting from association.");
                    }
                    assocIds.remove(alfObject.getRef());
                }
                if (assocIds.size() > 0) {
                    result.put(typeDesc.getPropertyDescByFieldName(fieldName), assocIds);
                }
            }
            return result;
        } catch (Exception e) {
            throw Throwables.propagate(e);
        }
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        return hashCode() == obj.hashCode();
    }

    @Override
    public int hashCode() {
        if (getRef() != null) {
            return getUuidRef().hashCode();
        }
        return super.hashCode();
    }

    @Override
    public String toString() {
        return getObjectName();
    }
}
