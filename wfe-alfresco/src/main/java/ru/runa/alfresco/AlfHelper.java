package ru.runa.alfresco;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.alfresco.model.ContentModel;
import org.alfresco.service.ServiceRegistry;
import org.alfresco.service.cmr.dictionary.AssociationDefinition;
import org.alfresco.service.cmr.dictionary.ClassDefinition;
import org.alfresco.service.cmr.dictionary.PropertyDefinition;
import org.alfresco.service.cmr.repository.AssociationRef;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.ContentWriter;
import org.alfresco.service.cmr.repository.InvalidNodeRefException;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.StoreRef;
import org.alfresco.service.cmr.search.LimitBy;
import org.alfresco.service.cmr.search.ResultSet;
import org.alfresco.service.cmr.search.SearchParameters;
import org.alfresco.service.cmr.search.SearchService;
import org.alfresco.service.cmr.version.Version;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import ru.runa.alfresco.search.Search;
import ru.runa.alfresco.search.Search.Sorting;
import ru.runa.wfe.InternalApplicationException;

import com.google.common.base.Charsets;
import com.google.common.base.Objects;
import com.google.common.base.Preconditions;
import com.google.common.base.Throwables;

/**
 * Connector implementation using Alfresco Java API (used in same JVM).
 * 
 * @author dofs
 */
@SuppressWarnings("unchecked")
public class AlfHelper implements AlfConn {
    private static Log log = LogFactory.getLog(AlfHelper.class);

    private final ServiceRegistry registry;

    public AlfHelper(ServiceRegistry registry) {
        this.registry = registry;
    }

    public ServiceRegistry getRegistry() {
        return registry;
    }

    private NodeRef getNodeRef(AlfObject object) {
        return new NodeRef(object.getUuidRef());
    }

    public void addAspect(AlfObject object, QName aspectTypeName) throws InternalApplicationException {
        AlfTypeDesc desc = Mappings.getMapping(aspectTypeName.toString(), this);
        Map<QName, Serializable> props = new JavaObjectAccessor(object).getAlfrescoProperties(desc, true, false);
        registry.getNodeService().addAspect(getNodeRef(object), aspectTypeName, props);
    }

    public boolean hasAspect(AlfObject object, QName aspectTypeName) {
        return registry.getNodeService().getAspects(getNodeRef(object)).contains(aspectTypeName);
    }

    public ResultSet find(Search search) throws InternalApplicationException {
        try {
            SearchParameters sp = new SearchParameters();
            sp.setLanguage(SearchService.LANGUAGE_LUCENE);
            sp.addStore(search.getStore());
            sp.setQuery(search.toString());
            if (search.getLimit() != 0) {
                sp.setLimit(search.getLimit());
                sp.setLimitBy(LimitBy.FINAL_SIZE);
            }
            if (search.hasSorting()) {
                for (Sorting sorting : search.getSortings()) {
                    sp.addSort("@" + sorting.getName().toString(), sorting.isAscending());
                }
            }
            return registry.getSearchService().query(sp);
        } catch (Exception e) {
            log.error("Failed query: " + search);
            throw Throwables.propagate(e);
        }
    }

    public List<NodeRef> findObjectRefs(Search search) throws InternalApplicationException {
        ResultSet resultSet = null;
        try {
            resultSet = find(search);
            List<NodeRef> refs = resultSet.getNodeRefs();
            log.debug("Search " + search + " returns " + refs.size());
            return refs;
        } finally {
            if (resultSet != null) {
                resultSet.close();
            }
        }
    }

    @Override
    public <T extends AlfObject> List<T> findObjects(Search search) throws InternalApplicationException {
        List<NodeRef> refs = findObjectRefs(search);
        return (List<T>) loadObjects(refs);
    }

    public <T extends AlfObject> T findUniqueObject(Search search) throws InternalApplicationException {
        List<T> objects = findObjects(search);
        search.setLimit(1);
        if (objects.size() > 1) {
            List<NodeRef> noderefs = new ArrayList<NodeRef>(objects.size());
            for (T t : objects) {
                noderefs.add(getNodeRef(t));
            }
            throw new InternalApplicationException("Search " + search + " returns not unique result: " + noderefs);
        }
        if (objects.size() == 1) {
            return objects.get(0);
        }
        return null;
    }

    @Override
    public <T extends AlfObject> T findObject(Search search) throws InternalApplicationException {
        ResultSet resultSet = null;
        try {
            resultSet = find(search);
            for (int i = 0; i < resultSet.length(); i++) {
                T t = (T) loadObject(resultSet.getNodeRef(i));
                if (t != null) {
                    return t;
                }
            }
            return null;
        } finally {
            if (resultSet != null) {
                resultSet.close();
            }
        }
    }

    public List<AlfObject> loadObjects(List<NodeRef> refs) throws InternalApplicationException {
        List<AlfObject> result = new ArrayList<AlfObject>(refs.size());
        for (NodeRef nodeRef : refs) {
            AlfObject object = loadObject(nodeRef);
            if (object != null) {
                result.add(object);
            }
        }
        return result;
    }

    @Override
    public <T extends AlfObject> T loadObject(String uuidRef) {
        return (T) loadObject(new NodeRef(uuidRef));
    }

    @Override
    public <T extends AlfObject> T loadObjectNotNull(String uuidRef) {
        T object = (T) loadObject(uuidRef);
        if (object == null) {
            throw new InternalApplicationException("Unable to load object by " + uuidRef);
        }
        return object;
    }

    @SuppressWarnings("rawtypes")
    @Override
    public void loadAssociation(String uuidRef, Collection collection, AlfSerializerDesc desc) throws InternalApplicationException {
        NodeRef nodeRef = new NodeRef(uuidRef);
        QName assocName = desc.getPropertyQName();
        if (desc.isChildAssociation()) {
            List<ChildAssociationRef> childAssocRefs;
            if (desc.isSourceAssociation()) {
                childAssocRefs = registry.getNodeService().getChildAssocs(nodeRef);
            } else {
                childAssocRefs = registry.getNodeService().getParentAssocs(nodeRef);
            }
            for (ChildAssociationRef assocRef : childAssocRefs) {
                if (assocRef.getTypeQName().equals(assocName)) {
                    collection.add(loadObject(desc.isSourceAssociation() ? assocRef.getChildRef() : assocRef.getParentRef()));
                }
            }
        } else {
            List<AssociationRef> assocRefs;
            if (desc.isSourceAssociation()) {
                assocRefs = registry.getNodeService().getTargetAssocs(nodeRef, assocName);
            } else {
                assocRefs = registry.getNodeService().getSourceAssocs(nodeRef, assocName);
            }
            for (AssociationRef assocRef : assocRefs) {
                if (assocRef.getTypeQName().equals(assocName)) {
                    collection.add(loadObject(desc.isSourceAssociation() ? assocRef.getTargetRef() : assocRef.getSourceRef()));
                }
            }
        }
    }

    @Override
    public boolean updateObjectAssociations(AlfObject object) throws InternalApplicationException {
        boolean updated = false;
        Map<AlfSerializerDesc, List<String>> assocToDelete = object.getAssocToDelete();
        for (Map.Entry<AlfSerializerDesc, List<String>> entry : assocToDelete.entrySet()) {
            for (String uuidRef : entry.getValue()) {
                log.debug("Removing assoc " + uuidRef + " from " + object.getUuidRef());
                if (entry.getKey().isChildAssociation()) {
                    registry.getNodeService().removeChild(getNodeRef(object), new NodeRef(uuidRef));
                } else {
                    registry.getNodeService().removeAssociation(getNodeRef(object), new NodeRef(uuidRef), entry.getKey().getPropertyQName());
                }
            }
            updated = true;
        }
        Map<AlfSerializerDesc, List<String>> assocToCreate = object.getAssocToCreate();
        for (Map.Entry<AlfSerializerDesc, List<String>> entry : assocToCreate.entrySet()) {
            for (String uuidRef : entry.getValue()) {
                log.debug("Adding assoc " + uuidRef + " to " + object.getUuidRef());
                if (entry.getKey().isChildAssociation()) {
                    addChildAssociation(object, uuidRef, entry.getKey().getPropertyQName());
                } else {
                    registry.getNodeService().createAssociation(getNodeRef(object), new NodeRef(uuidRef), entry.getKey().getPropertyQName());
                }
            }
            updated = true;
        }
        object.markCollectionsInitialState();
        return updated;
    }

    public void addChildAssociation(AlfObject object, String uuidRef, QName assocName) {
        Preconditions.checkState(uuidRef != null, "Save object before adding to association.");
        registry.getNodeService().addChild(getNodeRef(object), new NodeRef(uuidRef), assocName, assocName);
    }

    public <T extends AlfObject> T loadObject(NodeRef nodeRef) throws InternalApplicationException {
        try {
            if (!registry.getNodeService().exists(nodeRef)) {
                log.warn("Node does not exists: " + nodeRef);
                return null;
            }
            Map<QName, Serializable> properties = registry.getNodeService().getProperties(nodeRef);
            return (T) buildObject(nodeRef, registry.getNodeService().getType(nodeRef), properties);
        } catch (InvalidNodeRefException e) {
            // transaction will rolled-back
            log.warn(e);
            return null;
        }
    }

    public AlfObject buildObject(NodeRef nodeRef, QName type, Map<QName, Serializable> properties) throws InternalApplicationException {
        try {
            AlfTypeDesc typeDesc = Mappings.getMapping(type.toString(), this);
            AlfObject object = AlfObjectFactory.create(typeDesc.getJavaClassName(), this, nodeRef.toString());
            JavaObjectAccessor accessor = new JavaObjectAccessor(object);
            for (QName propName : properties.keySet()) {
                AlfSerializerDesc propertyDesc = typeDesc.getPropertyDescByTypeName(propName.toString());
                if (propertyDesc != null) {
                    accessor.setProperty(propertyDesc, properties.get(propName));
                }
            }
            object.markPropertiesInitialState(typeDesc);
            return object;
        } catch (Exception e) {
            throw Throwables.propagate(e);
        }
    }

    public void createObject(AlfObject object) throws InternalApplicationException {
        String folderUUID = Mappings.getFolderUUID(object.getClass());
        NodeRef dirRef = new NodeRef(StoreRef.STORE_REF_WORKSPACE_SPACESSTORE, folderUUID);
        createObject(object, dirRef);
    }

    public void createObject(AlfObject object, NodeRef folderRef) throws InternalApplicationException {
        AlfTypeDesc typeDesc = Mappings.getMapping(object.getClass(), this);
        log.debug("Creating new object of type " + object.getClass().getName() + " in " + folderRef);
        String name = object.getNewObjectName(typeDesc);
        object.setObjectName(name);
        Map<QName, Serializable> props = new JavaObjectAccessor(object).getAlfrescoProperties(typeDesc, true, true);
        ChildAssociationRef ref = registry.getNodeService().createNode(folderRef, ContentModel.ASSOC_CONTAINS,
                QName.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, name), QName.createQName(typeDesc.getAlfrescoTypeNameWithNamespace()),
                props);
        object.setUuidRef(ref.getChildRef().toString());
        object.markPropertiesInitialState(typeDesc);
        object.setLazyLoader(this);
        log.debug("Created object " + object);
    }

    @Override
    public boolean updateObject(AlfObject object, boolean forceAllProps) throws InternalApplicationException {
        AlfTypeDesc typeDesc = Mappings.getMapping(object.getClass(), this);
        JavaObjectAccessor accessor = new JavaObjectAccessor(object);
        Map<QName, Serializable> props = accessor.getAlfrescoProperties(typeDesc, forceAllProps, false);
        if (props.size() == 0) {
            log.debug("Ignored object " + object + " to update (0) fields");
            return false;
        }
        log.debug("Updating " + object + " (" + props.size() + ") fields");
        for (QName name : props.keySet()) {
            registry.getNodeService().setProperty(getNodeRef(object), name, props.get(name));
        }
        object.markPropertiesInitialState(typeDesc);
        return true;
    }

    @Override
    public boolean updateObject(AlfObject object, boolean forceAllProps, String comment) throws InternalApplicationException {
        boolean updated = updateObject(object, forceAllProps);
        if (updated) {
            Map<String, Serializable> versionDetails = new HashMap<String, Serializable>(1);
            versionDetails.put(Version.PROP_DESCRIPTION, comment);
            registry.getVersionService().createVersion(getNodeRef(object), versionDetails);
        }
        return updated;
    }

    @Override
    public void deleteObject(AlfObject object) throws InternalApplicationException {
        deleteObject(getNodeRef(object));
    }

    private void deleteObject(NodeRef nodeRef) throws InternalApplicationException {
        if (!registry.getNodeService().exists(nodeRef)) {
            log.warn("No object exists: " + nodeRef);
            return;
        }
        registry.getNodeService().deleteNode(nodeRef);
    }

    public void setContent(AlfObject object, String content, String mimetype) {
        log.info("Setting content to " + object);
        ContentWriter writer = registry.getContentService().getWriter(getNodeRef(object), ContentModel.PROP_CONTENT, true);
        writer.setEncoding(Charsets.UTF_8.name());
        writer.setMimetype(mimetype);
        writer.putContent(content);
    }

    @Override
    public void initializeTypeDefinition(AlfTypeDesc typeDesc) {
        if (typeDesc.isClassDefinitionLoaded()) {
            return;
        }
        log.info("Loading definition for " + typeDesc);
        QName typeName = QName.createQName(typeDesc.getAlfrescoTypeNameWithNamespace());
        ClassDefinition definition;
        if (typeDesc.isAspect()) {
            definition = registry.getDictionaryService().getAspect(typeName);
        } else {
            definition = registry.getDictionaryService().getClass(typeName);
        }
        if (definition == null) {
            throw new NullPointerException("No definition loaded for " + typeName);
        }
        typeDesc.setTitle(definition.getTitle());
        for (AlfSerializerDesc desc : typeDesc.getAllDescs()) {
            if (desc.getProperty() != null) {
                PropertyDefinition propertyDefinition = registry.getDictionaryService().getProperty(desc.getPropertyQName());
                if (propertyDefinition == null) {
                    throw new InternalApplicationException("No property found in Alfresco for " + desc + " of type " + typeDesc);
                }
                desc.setTitle(propertyDefinition.getTitle());
                if (propertyDefinition.isMultiValued()) {
                    desc.setDataType(List.class.getName());
                } else {
                    desc.setDataType(propertyDefinition.getDataType().getJavaClassName());
                }
                desc.setDefaultValue(propertyDefinition.getDefaultValue());
            }
            if (desc.getAssoc() != null) {
                AssociationDefinition associationDefinition = registry.getDictionaryService().getAssociation(desc.getPropertyQName());
                if (associationDefinition == null) {
                    throw new InternalApplicationException("No association found in Alfresco for " + desc + " of type " + typeDesc);
                }
                desc.setTitle(associationDefinition.getTitle());
                desc.setChildAssociation(associationDefinition.isChild());
                desc.setSourceAssociation(!Objects.equal(associationDefinition.getTargetClass().getName(), definition.getName()));
            }
        }
        typeDesc.setClassDefinitionLoaded(true);
    }

}
