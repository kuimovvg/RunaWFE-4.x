package ru.runa.alfresco;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.alfresco.model.ContentModel;
import org.alfresco.service.ServiceRegistry;
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

    public void addAspect(AlfObject object, QName aspectTypeName) throws InternalApplicationException {
        AlfTypeDesc desc = Mappings.getMapping(aspectTypeName.toString());
        loadClassDefinitionIfNeeded(desc);
        Map<QName, Serializable> props = new JavaObjectAccessor(object).getAlfrescoProperties(desc, true, false);
        registry.getNodeService().addAspect(object.getNodeRef(), aspectTypeName, props);
    }

    public boolean hasAspect(AlfObject object, QName aspectTypeName) {
        return registry.getNodeService().getAspects(object.getNodeRef()).contains(aspectTypeName);
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
                noderefs.add(t.getNodeRef());
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
    public <T extends AlfObject> T loadObject(Object objectId) {
        try {
            return (T) loadObject((NodeRef) objectId);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @SuppressWarnings("rawtypes")
    @Override
    public void loadAssociation(Object ref, Collection collection, AlfSerializerDesc desc) throws InternalApplicationException {
        NodeRef nodeRef = (NodeRef) ref;
        QName assocName = desc.getPropertyQName();
        if (desc.getAssoc().child()) {
            List<ChildAssociationRef> childAssocRefs;
            if (desc.getAssoc().source()) {
                childAssocRefs = registry.getNodeService().getChildAssocs(nodeRef);
            } else {
                childAssocRefs = registry.getNodeService().getParentAssocs(nodeRef);
            }
            for (ChildAssociationRef assocRef : childAssocRefs) {
                if (assocRef.getTypeQName().equals(assocName)) {
                    collection.add(loadObject(desc.getAssoc().source() ? assocRef.getChildRef() : assocRef.getParentRef()));
                }
            }
        } else {
            List<AssociationRef> assocRefs;
            if (desc.getAssoc().source()) {
                assocRefs = registry.getNodeService().getTargetAssocs(nodeRef, assocName);
            } else {
                assocRefs = registry.getNodeService().getSourceAssocs(nodeRef, assocName);
            }
            for (AssociationRef assocRef : assocRefs) {
                if (assocRef.getTypeQName().equals(assocName)) {
                    collection.add(loadObject(desc.getAssoc().source() ? assocRef.getTargetRef() : assocRef.getSourceRef()));
                }
            }
        }
    }

    @Override
    public boolean updateObjectAssociations(AlfObject object) throws InternalApplicationException {
        boolean updated = false;
        Map<AlfSerializerDesc, List<Object>> assocToDelete = object.getAssocToDelete();
        for (AlfSerializerDesc desc : assocToDelete.keySet()) {
            List<Object> refs = assocToDelete.get(desc);
            for (Object ref : refs) {
                log.debug("Removing assoc " + ref + " from " + object.getNodeRef());
                if (desc.getAssoc().child()) {
                    registry.getNodeService().removeChild(object.getNodeRef(), (NodeRef) ref);
                } else {
                    registry.getNodeService().removeAssociation(object.getNodeRef(), (NodeRef) ref, desc.getPropertyQName());
                }
            }
            updated = true;
        }
        Map<AlfSerializerDesc, List<Object>> assocToCreate = object.getAssocToCreate();
        for (AlfSerializerDesc desc : assocToCreate.keySet()) {
            List<Object> refs = assocToCreate.get(desc);
            for (Object ref : refs) {
                log.debug("Adding assoc " + ref + " to " + object.getNodeRef());
                if (desc.getAssoc().child()) {
                    registry.getNodeService().addChild(object.getNodeRef(), (NodeRef) ref, desc.getPropertyQName(), desc.getPropertyQName());
                } else {
                    registry.getNodeService().createAssociation(object.getNodeRef(), (NodeRef) ref, desc.getPropertyQName());
                }
            }
            updated = true;
        }
        object.refCollections.clear();
        object.clearCollections();
        return updated;
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
            AlfTypeDesc typeDesc = Mappings.getMapping(type.toString());
            loadClassDefinitionIfNeeded(typeDesc);
            Class<? extends AlfObject> clazz = (Class<? extends AlfObject>) Class.forName(typeDesc.getJavaClassName());
            AlfObject object = clazz.newInstance();
            object.setNodeRef(nodeRef);
            object.setLazyLoader(this);
            JavaObjectAccessor accessor = new JavaObjectAccessor(object);
            for (QName propName : properties.keySet()) {
                AlfSerializerDesc propertyDesc = typeDesc.getPropertyDescByTypeName(propName.toString());
                if (propertyDesc != null) {
                    accessor.setProperty(propertyDesc, properties.get(propName));
                }
            }
            object.markInitialState(typeDesc);
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
        AlfTypeDesc typeDesc = Mappings.getMapping(object.getClass());
        loadClassDefinitionIfNeeded(typeDesc);
        log.debug("Creating new object of type " + object.getClass().getName() + " in " + folderRef);
        String name = object.getNewObjectName(typeDesc);
        object.setObjectName(name);
        Map<QName, Serializable> props = new JavaObjectAccessor(object).getAlfrescoProperties(typeDesc, true, true);
        ChildAssociationRef ref = registry.getNodeService().createNode(folderRef, ContentModel.ASSOC_CONTAINS,
                QName.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, name), QName.createQName(typeDesc.getAlfrescoTypeNameWithNamespace()),
                props);
        object.setNodeRef(ref.getChildRef());
        object.markInitialState(typeDesc);
        object.setLazyLoader(this);
        log.debug("Created object " + object);
    }

    @Override
    public boolean updateObject(AlfObject object, boolean forceAllProps) throws InternalApplicationException {
        AlfTypeDesc typeDesc = Mappings.getMapping(object.getClass());
        loadClassDefinitionIfNeeded(typeDesc);
        JavaObjectAccessor accessor = new JavaObjectAccessor(object);
        Map<QName, Serializable> props = accessor.getAlfrescoProperties(typeDesc, forceAllProps, false);
        if (props.size() == 0) {
            log.debug("Ignored object " + object + " to update (0) fields");
            return false;
        }
        log.debug("Updating " + object + " (" + props.size() + ") fields");
        for (QName name : props.keySet()) {
            registry.getNodeService().setProperty(object.getNodeRef(), name, props.get(name));
        }
        object.markInitialState(typeDesc);
        return true;
    }

    @Override
    public boolean updateObject(AlfObject object, boolean forceAllProps, String comment) throws InternalApplicationException {
        boolean updated = updateObject(object, forceAllProps);
        if (updated) {
            Map<String, Serializable> versionDetails = new HashMap<String, Serializable>(1);
            versionDetails.put(Version.PROP_DESCRIPTION, comment);
            registry.getVersionService().createVersion(object.getNodeRef(), versionDetails);
        }
        return updated;
    }

    @Override
    public void deleteObject(AlfObject object) throws InternalApplicationException {
        deleteObject(object.getNodeRef());
    }

    private void deleteObject(NodeRef nodeRef) throws InternalApplicationException {
        if (!registry.getNodeService().exists(nodeRef)) {
            log.warn("No object exists: " + nodeRef);
            return;
        }
        registry.getNodeService().deleteNode(nodeRef);
    }

    public void setContent(AlfObject object, String content, String mimetype) throws InternalApplicationException {
        log.info("Setting content to " + object);
        ContentWriter writer = registry.getContentService().getWriter(object.getNodeRef(), ContentModel.PROP_CONTENT, true);
        writer.setEncoding(Charsets.UTF_8.name());
        writer.setMimetype(mimetype);
        writer.putContent(content);
    }

    private void loadClassDefinitionIfNeeded(AlfTypeDesc typeDesc) throws InternalApplicationException {
        if (typeDesc.isClassDefinitionLoaded()) {
            return;
        }
        log.info("Loading definition for " + typeDesc.getAlfrescoTypeNameWithPrefix());
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
            PropertyDefinition d = registry.getDictionaryService().getProperty(desc.getPropertyQName());
            if (d != null) {
                desc.setTitle(d.getTitle());
                desc.setDataType(d.getDataType().getJavaClassName());
                desc.setDefaultValue(d.getDefaultValue());
            } else {
                log.debug("No property found in Alfresco for " + desc.getPropertyQName());
            }
        }
        typeDesc.setClassDefinitionLoaded(true);
    }

}
