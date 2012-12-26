package ru.runa.alfresco;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import net.sf.ehcache.Cache;
import net.sf.ehcache.CacheManager;
import net.sf.ehcache.Element;

import org.alfresco.model.ContentModel;
import org.alfresco.service.cmr.repository.StoreRef;
import org.alfresco.webservice.authoring.VersionResult;
import org.alfresco.webservice.dictionary.ClassPredicate;
import org.alfresco.webservice.repository.Association;
import org.alfresco.webservice.repository.QueryResult;
import org.alfresco.webservice.repository.UpdateResult;
import org.alfresco.webservice.types.CML;
import org.alfresco.webservice.types.CMLAddAspect;
import org.alfresco.webservice.types.CMLAddChild;
import org.alfresco.webservice.types.CMLCreate;
import org.alfresco.webservice.types.CMLCreateAssociation;
import org.alfresco.webservice.types.CMLDelete;
import org.alfresco.webservice.types.CMLRemoveAspect;
import org.alfresco.webservice.types.CMLRemoveAssociation;
import org.alfresco.webservice.types.CMLRemoveChild;
import org.alfresco.webservice.types.CMLUpdate;
import org.alfresco.webservice.types.ClassDefinition;
import org.alfresco.webservice.types.ContentFormat;
import org.alfresco.webservice.types.NamedValue;
import org.alfresco.webservice.types.Node;
import org.alfresco.webservice.types.ParentReference;
import org.alfresco.webservice.types.Predicate;
import org.alfresco.webservice.types.PropertyDefinition;
import org.alfresco.webservice.types.Query;
import org.alfresco.webservice.types.Reference;
import org.alfresco.webservice.types.ResultSet;
import org.alfresco.webservice.types.ResultSetRow;
import org.alfresco.webservice.types.Store;
import org.alfresco.webservice.types.Version;
import org.alfresco.webservice.types.VersionHistory;
import org.alfresco.webservice.util.Constants;
import org.alfresco.webservice.util.Utils;
import org.alfresco.webservice.util.WebServiceFactory;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import ru.runa.alfresco.search.Search;
import ru.runa.wfe.ApplicationException;

import com.google.common.base.Charsets;

/**
 * Connector implementation using Alfresco web services.
 * 
 * @author dofs
 */
@SuppressWarnings("unchecked")
public class AlfSession implements AlfConn {
    private static Log log = LogFactory.getLog(AlfSession.class);
    private static CacheManager cacheManager = null;

    static {
        if (CacheSettings.isCacheEnabled()) {
            cacheManager = new CacheManager(CacheSettings.getConfigurationInputStream());
        }
    }

    public static CacheManager getCacheManager() {
        return cacheManager;
    }

    private Store getSpacesStore() {
        return new Store(Constants.WORKSPACE_STORE, "SpacesStore");
    }

    private Store toStore(StoreRef storeRef) {
        return new Store(storeRef.getProtocol(), storeRef.getIdentifier());
    }

    public void addAspect(AlfObject object, String aspectTypeName) throws ApplicationException {
        try {
            AlfSessionWrapper.sessionStart();
            AlfTypeDesc desc = Mappings.getMapping(aspectTypeName);
            loadClassDefinitionIfNeeded(desc);
            NamedValue[] props = new WSObjectAccessor(object).getAlfrescoProperties(desc, true, false);
            CMLAddAspect addAspect = new CMLAddAspect(aspectTypeName, props, object.getPredicate(), null);
            CML cml = new CML();
            cml.setAddAspect(new CMLAddAspect[] { addAspect });
            WebServiceFactory.getRepositoryService().update(cml);
        } catch (Exception e) {
            throw new ApplicationException(e);
        } finally {
            AlfSessionWrapper.sessionEnd();
        }
    }

    public void removeAspect(AlfObject object, String aspectTypeName) throws ApplicationException {
        try {
            AlfSessionWrapper.sessionStart();
            CMLRemoveAspect removeAspect = new CMLRemoveAspect(aspectTypeName, object.getPredicate(), null);
            CML cml = new CML();
            cml.setRemoveAspect(new CMLRemoveAspect[] { removeAspect });
            WebServiceFactory.getRepositoryService().update(cml);
        } catch (Exception e) {
            throw new ApplicationException(e);
        } finally {
            AlfSessionWrapper.sessionEnd();
        }
    }

    public String createFolder(String rootFolderUUID, String folderName) throws ApplicationException {
        try {
            AlfSessionWrapper.sessionStart();
            ParentReference docParent = new ParentReference(getSpacesStore(), rootFolderUUID, null, Constants.ASSOC_CONTAINS, folderName);
            NamedValue[] props = new NamedValue[] { Utils.createNamedValue(Constants.PROP_NAME, folderName) };
            CMLCreate createDoc = new CMLCreate("ref1", docParent, null, ContentModel.ASSOC_CONTAINS.toString(), folderName,
                    ContentModel.TYPE_FOLDER.toString(), props);
            CML cml = new CML();
            cml.setCreate(new CMLCreate[] { createDoc });
            UpdateResult[] results = WebServiceFactory.getRepositoryService().update(cml);
            Reference docRef = results[0].getDestination();
            log.info("Created folder '" + folderName + "' with " + docRef.getUuid());
            return docRef.getUuid();
        } catch (Exception e) {
            throw new ApplicationException(e);
        } finally {
            AlfSessionWrapper.sessionEnd();
        }
    }

    public void createObject(AlfObject object) throws ApplicationException {
        String folderUUID = Mappings.getFolderUUID(object.getClass());
        createObject(folderUUID, object);
    }

    public void createObject(String folderUUID, AlfObject object) throws ApplicationException {
        try {
            AlfSessionWrapper.sessionStart();
            AlfTypeDesc typeDesc = Mappings.getMapping(object.getClass());
            loadClassDefinitionIfNeeded(typeDesc);
            String typeName = typeDesc.getAlfrescoTypeNameWithNamespace();
            String name = object.getNewObjectName(typeDesc);
            log.info("Creating new object " + name);
            ParentReference docParent = new ParentReference(getSpacesStore(), folderUUID, null, Constants.ASSOC_CONTAINS, name);
            NamedValue[] props = new WSObjectAccessor(object).getAlfrescoProperties(typeDesc, true, true);
            CMLCreate createDoc = new CMLCreate("ref1", docParent, null, null, null, typeName, props);
            CML cml = new CML();
            cml.setCreate(new CMLCreate[] { createDoc });
            UpdateResult[] results = WebServiceFactory.getRepositoryService().update(cml);
            object.markInitialState(typeDesc);
            Reference docRef = results[0].getDestination();
            object.setReference(docRef);
            log.info("Created object " + docRef.getUuid());
            Cache cache = getCache(object.getClass().getName());
            if (cache != null) {
                cache.put(new Element(object.getUuidRef(), object));
            }
        } catch (Exception e) {
            throw new ApplicationException(e);
        } finally {
            AlfSessionWrapper.sessionEnd();
        }
    }

    public void updateVersion(AlfObject object, String comment) throws ApplicationException {
        try {
            AlfSessionWrapper.sessionStart();
            AlfTypeDesc typeDesc = Mappings.getMapping(object.getClass());
            loadClassDefinitionIfNeeded(typeDesc);
            NamedValue[] comments = new NamedValue[1];
            comments[0] = new NamedValue("description", false, comment, null);
            VersionResult result = WebServiceFactory.getAuthoringService().createVersion(object.getPredicate(), comments, false);
            log.info("Version of " + object + " updated to " + result.getVersions()[0].getLabel());
            object.markInitialState(typeDesc);
        } catch (Exception e) {
            throw new ApplicationException(e);
        } finally {
            AlfSessionWrapper.sessionEnd();
        }
    }

    public void addAssociation(AlfObject source, AlfObject target, String associationName) throws ApplicationException {
        try {
            AlfSessionWrapper.sessionStart();
            CMLCreateAssociation createAssociation = new CMLCreateAssociation(source.getPredicate(), null, target.getPredicate(), null,
                    associationName);
            CML cml = new CML();
            cml.setCreateAssociation(new CMLCreateAssociation[] { createAssociation });
            WebServiceFactory.getRepositoryService().update(cml);
            log.info("Created association " + associationName);
        } catch (Exception e) {
            throw new ApplicationException(e);
        } finally {
            AlfSessionWrapper.sessionEnd();
        }
    }

    public void addChildAssociation(AlfObject source, AlfObject target, String associationName) throws ApplicationException {
        try {
            AlfSessionWrapper.sessionStart();
            ParentReference parentReference = new ParentReference(source.getReference().getStore(), source.getReference().getUuid(), null,
                    associationName, associationName);

            CMLAddChild addChild = new CMLAddChild();
            addChild.setTo(parentReference);
            addChild.setWhere(target.getPredicate());

            CML cml = new CML();
            cml.setAddChild(new CMLAddChild[] { addChild });

            WebServiceFactory.getRepositoryService().update(cml);
            log.info("Created child association " + associationName);
        } catch (Exception e) {
            throw new ApplicationException(e);
        } finally {
            AlfSessionWrapper.sessionEnd();
        }
    }

    @Override
    public <T extends AlfObject> T loadObject(Object objectId) throws ApplicationException {
        if (objectId instanceof Version) {
            Reference ref = ((Version) objectId).getId();
            Predicate predicate = new Predicate(new Reference[] { ref }, ref.getStore(), null);
            return (T) loadObject(predicate);

        }
        String uuidRef = (String) objectId;
        return (T) loadObject(uuidRef, null);
    }

    @SuppressWarnings("rawtypes")
    @Override
    public void loadAssociation(Object ref, Collection collection, AlfSerializerDesc desc) throws ApplicationException {
        try {
            AlfSessionWrapper.sessionStart();
            Reference reference = (Reference) ref;
            QueryResult queryResult;
            boolean filter = true;
            if (desc.getAssoc().child()) {
                if (desc.getAssoc().source()) {
                    queryResult = WebServiceFactory.getRepositoryService().queryChildren(reference);
                } else {
                    queryResult = WebServiceFactory.getRepositoryService().queryParents(reference);
                }
            } else {
                filter = false;
                Association association = new Association(desc.getPropertyNameWithNamespace(), desc.getAssoc().source() ? "target" : "source");
                queryResult = WebServiceFactory.getRepositoryService().queryAssociated(reference, association);
            }
            if (queryResult.getResultSet().getTotalRowCount() > 0) {
                for (ResultSetRow row : queryResult.getResultSet().getRows()) {
                    boolean filterAccepted = false;
                    if (filter) {
                        for (NamedValue nv : row.getColumns()) {
                            if ("associationType".equals(nv.getName())) {
                                filterAccepted = desc.getPropertyNameWithNamespace().equals(nv.getValue());
                                break;
                            }
                        }
                    }
                    if (filterAccepted || !filter) {
                        log.debug("[tmp] row.getColumns() [size] = " + (row.getColumns() != null ? row.getColumns().length : "null"));
                        collection.add(loadObject(row.getNode().getId(), reference.getStore()));
                    }
                }
            }
        } catch (Exception e) {
            throw new ApplicationException(e);
        } finally {
            AlfSessionWrapper.sessionEnd();
        }
    }

    @Override
    public boolean updateObjectAssociations(AlfObject object) throws ApplicationException {
        try {
            boolean updated = false;
            AlfSessionWrapper.sessionStart();
            CML cml = new CML();
            List<CMLRemoveChild> removeChilds = new ArrayList<CMLRemoveChild>();
            List<CMLRemoveAssociation> removeAssociations = new ArrayList<CMLRemoveAssociation>();
            List<CMLAddChild> addChilds = new ArrayList<CMLAddChild>();
            List<CMLCreateAssociation> createAssociations = new ArrayList<CMLCreateAssociation>();
            Map<AlfSerializerDesc, List<Object>> assocToDelete = object.getAssocToDelete();
            for (AlfSerializerDesc desc : assocToDelete.keySet()) {
                List<Object> refs = assocToDelete.get(desc);
                for (Object ref : refs) {
                    Reference reference = (Reference) ref;
                    log.debug("Removing assoc " + reference.getUuid() + " from " + object);
                    Predicate where = new Predicate(new Reference[] { reference }, reference.getStore(), null);
                    if (desc.getAssoc().child()) {
                        CMLRemoveChild removeChild = new CMLRemoveChild(object.getReference(), null, where, null);
                        removeChilds.add(removeChild);
                        // TODO TEST WITHOUT THIS deleteObject((NodeRef) ref);
                    } else {
                        CMLRemoveAssociation removeAssociation = new CMLRemoveAssociation(object.getPredicate(), null, where, null,
                                desc.getPropertyNameWithNamespace());
                        removeAssociations.add(removeAssociation);
                    }
                    updated = true;
                }
            }
            Map<AlfSerializerDesc, List<Object>> assocToCreate = object.getAssocToCreate();
            for (AlfSerializerDesc desc : assocToCreate.keySet()) {
                List<Object> refs = assocToCreate.get(desc);
                for (Object ref : refs) {
                    Reference reference = (Reference) ref;
                    AlfObject target = loadObject(reference);
                    Predicate predicate = target.getPredicate();
                    log.debug("Adding assoc " + reference.getUuid() + " to " + object);
                    if (desc.getAssoc().child()) {
                        ParentReference parentReference = new ParentReference(object.getReference().getStore(), object.getReference().getUuid(),
                                null, desc.getPropertyNameWithNamespace(), target.getObjectName());
                        CMLAddChild addChild = new CMLAddChild(parentReference, null, desc.getPropertyNameWithNamespace(), target.getObjectName(),
                                predicate, null);
                        addChilds.add(addChild);
                    } else {
                        CMLCreateAssociation createAssociation = new CMLCreateAssociation(object.getPredicate(), null, predicate, null,
                                desc.getPropertyNameWithNamespace());
                        createAssociations.add(createAssociation);
                    }
                    updated = true;
                }
            }
            if (removeAssociations.size() > 0) {
                cml.setRemoveAssociation(removeAssociations.toArray(new CMLRemoveAssociation[removeAssociations.size()]));
            }
            if (removeChilds.size() > 0) {
                cml.setRemoveChild(removeChilds.toArray(new CMLRemoveChild[removeChilds.size()]));
            }
            if (addChilds.size() > 0) {
                cml.setAddChild(addChilds.toArray(new CMLAddChild[addChilds.size()]));
            }
            if (createAssociations.size() > 0) {
                cml.setCreateAssociation(createAssociations.toArray(new CMLCreateAssociation[createAssociations.size()]));
            }
            WebServiceFactory.getRepositoryService().update(cml);
            object.refCollections.clear();
            object.clearCollections();
            return updated;
        } catch (Exception e) {
            throw new ApplicationException(e);
        } finally {
            AlfSessionWrapper.sessionEnd();
        }
    }

    private <T extends AlfObject> T loadObject(String uuid, Store store) throws ApplicationException {
        Reference ref = getReference(uuid, store);
        T result = (T) findInCache(ref.getUuid());
        if (result != null) {
            return result;
        }
        return (T) loadObject(ref);
    }

    public Reference getReference(String uuid, Store store) throws ApplicationException {
        String id;
        int li = uuid.lastIndexOf("/");
        if (li != -1) {
            String storeString = uuid.substring(0, li);
            id = uuid.substring(li + 1);
            store = toStore(new StoreRef(storeString));
        } else {
            id = uuid;
        }
        if (store == null) {
            store = getSpacesStore();
            log.warn("UUID does not contains store identifier, " + uuid);
            Thread.dumpStack();
        }
        return new Reference(store, id, null);
    }

    private <T extends AlfObject> T loadObject(Reference reference) throws ApplicationException {
        Predicate predicate = new Predicate(new Reference[] { reference }, reference.getStore(), null);
        return (T) loadObject(predicate);
    }

    private <T extends AlfObject> T loadObject(Predicate where) throws ApplicationException {
        try {
            AlfSessionWrapper.sessionStart();
            Node node = WebServiceFactory.getRepositoryService().get(where)[0];
            return (T) buildObject(node.getType(), node.getReference(), node.getProperties(), node.getAspects());
        } catch (Exception e) {
            Thread.dumpStack();
            log.warn("Unable to load object " + where.getNodes(0).getUuid(), e);
            return null;
        } finally {
            AlfSessionWrapper.sessionEnd();
        }
    }

    public NamedValue[] loadObjectProperties(Predicate where) throws ApplicationException {
        try {
            AlfSessionWrapper.sessionStart();
            Node node = WebServiceFactory.getRepositoryService().get(where)[0];
            return node.getProperties();
        } catch (Exception e) {
            Thread.dumpStack();
            log.warn("Unable to load object properties " + where.getNodes(0).getUuid(), e);
            return null;
        } finally {
            AlfSessionWrapper.sessionEnd();
        }
    }

    private <T extends AlfObject> T loadObject(Store store, ResultSetRow row) throws ApplicationException {
        if (row.getColumns() != null) {
            Reference reference = new Reference(store, row.getNode().getId(), null);
            return (T) buildObject(row.getNode().getType(), reference, row.getColumns(), row.getNode().getAspects());
        } else {
            return (T) loadObject(row.getNode().getId(), store);
        }
    }

    public static Cache getCache(String className) {
        if (cacheManager != null && cacheManager.cacheExists(className)) {
            return cacheManager.getCache(className);
        }
        return null;
    }

    public static <T> T findInCache(Serializable uuid) {
        if (cacheManager != null) {
            for (String cacheName : cacheManager.getCacheNames()) {
                Cache cache = cacheManager.getCache(cacheName);
                Element element = cache.get(uuid);
                if (element != null) {
                    return (T) element.getValue();
                }
            }
        }
        return null;
    }

    public static void clearCaches() {
        if (cacheManager != null) {
            for (String cacheName : cacheManager.getCacheNames()) {
                Cache cache = cacheManager.getCache(cacheName);
                cache.removeAll();
            }
        }
    }

    public static String getUuidRef(Reference reference) {
        return reference.getStore().getScheme() + "://" + reference.getStore().getAddress() + "/" + reference.getUuid();
    }

    public <T extends AlfObject> T buildObject(String typeName, Reference reference, NamedValue[] props, String[] aspects)
            throws ApplicationException {
        try {
            AlfTypeDesc typeDesc = Mappings.getMapping(typeName);
            loadClassDefinitionIfNeeded(typeDesc);
            Cache cache = getCache(typeDesc.getJavaClassName());
            if (cache != null) {
                Element element = cache.get(getUuidRef(reference));
                if (element != null) {
                    return (T) element.getValue();
                }
            }
            Class<T> clazz = (Class<T>) Class.forName(typeDesc.getJavaClassName());
            T object = clazz.newInstance();
            object.setReference(reference);
            object.setLazyLoader(this);
            WSObjectAccessor accessor = new WSObjectAccessor(object);
            for (NamedValue prop : props) {
                AlfSerializerDesc propertyDesc = typeDesc.getPropertyDescByTypeName(prop.getName());
                if (propertyDesc != null) {
                    accessor.setProperty(propertyDesc, prop);
                }
            }
            object.markInitialState(typeDesc);
            if (cache != null) {
                cache.put(new Element(object.getUuidRef(), object));
            }
            return object;
        } catch (Exception e) {
            Thread.dumpStack();
            throw new ApplicationException(e);
        }
    }

    public Version[] getAllVersions(Predicate where) throws ApplicationException {
        try {
            AlfSessionWrapper.sessionStart();
            VersionHistory history = WebServiceFactory.getAuthoringService().getVersionHistory(where.getNodes()[0]);
            return history.getVersions();
        } catch (Exception e) {
            throw new ApplicationException(e);
        } finally {
            AlfSessionWrapper.sessionEnd();
        }
    }

    @Override
    public boolean updateObject(AlfObject object, boolean force) throws ApplicationException {
        try {
            AlfSessionWrapper.sessionStart();
            AlfTypeDesc typeDesc = Mappings.getMapping(object.getClass());
            loadClassDefinitionIfNeeded(typeDesc);
            WSObjectAccessor accessor = new WSObjectAccessor(object);
            NamedValue[] contentProps = accessor.getAlfrescoProperties(typeDesc, force, false);
            if (contentProps.length == 0) {
                log.info("Ignored object " + object + " to update (0) fields");
                return false;
            }
            log.info("Updating " + object + " (" + contentProps.length + ") fields");
            CMLUpdate update = new CMLUpdate(contentProps, object.getPredicate(), null);
            CML cml = new CML();
            cml.setUpdate(new CMLUpdate[] { update });
            WebServiceFactory.getRepositoryService().update(cml);
            Cache cache = getCache(object.getClass().getName());
            if (cache != null) {
                Element old = cache.get(object.getUuidRef());
                if (old != null) {
                    cache.replace(old, new Element(old.getKey(), object));
                }
            }
            // TODO use UpdateResult[] updateResults
            return true;
        } catch (Exception e) {
            Thread.dumpStack();
            throw new ApplicationException(e);
        } finally {
            AlfSessionWrapper.sessionEnd();
        }
    }

    @Override
    public boolean updateObject(AlfObject object, boolean force, String comment) throws ApplicationException {
        boolean updated = updateObject(object, force);
        if (updated) {
            // comment in 3.2
            updateVersion(object, comment);
        }
        return updated;
    }

    public void setContent(AlfObject object, byte[] content, String mimetype) throws ApplicationException {
        try {
            AlfSessionWrapper.sessionStart();
            log.info("Setting content to " + object);
            WebServiceFactory.getContentService().write(object.getReference(), Constants.PROP_CONTENT, content,
                    new ContentFormat(mimetype, Charsets.UTF_8.name()));
        } catch (Exception e) {
            throw new ApplicationException(e);
        } finally {
            AlfSessionWrapper.sessionEnd();
        }
    }

    @Override
    public void deleteObject(AlfObject object) throws ApplicationException {
        if (object != null) {
            deleteObject(object.getReference());
            Cache cache = getCache(object.getClass().getName());
            if (cache != null) {
                cache.remove(object.getUuidRef());
            }
        }
    }

    /**
     * @deprecated cache is not notified
     * @param reference
     * @throws Exception
     */
    @Deprecated
    public void deleteObject(Reference reference) throws ApplicationException {
        if (reference != null) {
            try {
                AlfSessionWrapper.sessionStart();
                log.info("Deleting object " + reference.getUuid());
                Predicate predicate = new Predicate(new Reference[] { reference }, reference.getStore(), null);
                CMLDelete delete = new CMLDelete(predicate);
                CML cml = new CML();
                cml.setDelete(new CMLDelete[] { delete });
                WebServiceFactory.getRepositoryService().update(cml);
            } catch (Exception e) {
                throw new ApplicationException(e);
            } finally {
                AlfSessionWrapper.sessionEnd();
            }
        }
    }

    @Override
    public <T extends AlfObject> T findObject(Search search) throws ApplicationException {
        List<T> objects = findObjects(search);
        if (objects.size() > 0) {
            return objects.get(0);
        }
        return null;
    }

    private ResultSetRow[] findObjectRows(Store store, Search search) throws ApplicationException {
        try {
            AlfSessionWrapper.sessionStart();
            String luceneQuery = search.toString();
            Query query = new Query("lucene", luceneQuery);
            QueryResult queryResult = WebServiceFactory.getRepositoryService().query(store, query, false);
            ResultSet resultSet = queryResult.getResultSet();
            ResultSetRow[] rows;
            if (resultSet.getTotalRowCount() > 0) {
                rows = resultSet.getRows();
            } else {
                rows = new ResultSetRow[0];
            }
            log.debug("Search " + query.getStatement() + " returns " + rows.length);
            return rows;
        } catch (Exception e) {
            throw new ApplicationException(e);
        } finally {
            AlfSessionWrapper.sessionEnd();
        }
    }

    @Override
    public <T extends AlfObject> List<T> findObjects(Search search) throws ApplicationException {
        try {
            AlfSessionWrapper.sessionStart();
            Store store = new Store(search.getStore().getProtocol(), search.getStore().getIdentifier());
            ResultSetRow[] rows = findObjectRows(store, search);
            List<T> result = new ArrayList<T>(rows.length);
            for (ResultSetRow row : rows) {
                result.add((T) loadObject(store, row));
            }
            return result;
        } catch (Exception e) {
            throw new ApplicationException(e);
        } finally {
            AlfSessionWrapper.sessionEnd();
        }
    }

    private void loadClassDefinitionIfNeeded(AlfTypeDesc typeDesc) throws ApplicationException {
        if (typeDesc.isClassDefinitionLoaded()) {
            return;
        }
        try {
            AlfSessionWrapper.sessionStart();
            log.info("Loading definition for " + typeDesc.getAlfrescoTypeNameWithPrefix());
            ClassPredicate types;
            ClassPredicate aspects;
            if (typeDesc.isAspect()) {
                types = new ClassPredicate(new String[] {}, false, false);
                aspects = new ClassPredicate(new String[] { typeDesc.getAlfrescoTypeNameWithPrefix() }, false, false);
            } else {
                types = new ClassPredicate(new String[] { typeDesc.getAlfrescoTypeNameWithPrefix() }, false, false);
                aspects = new ClassPredicate(new String[] {}, false, false);
            }
            ClassDefinition definition = WebServiceFactory.getDictionaryService().getClasses(types, aspects)[0];
            typeDesc.setTitle(definition.getTitle());
            PropertyDefinition[] propertyDefinitions = definition.getProperties();
            for (PropertyDefinition propertyDefinition : propertyDefinitions) {
                AlfSerializerDesc desc = typeDesc.getPropertyDescByTypeName(propertyDefinition.getName());
                if (desc != null) {
                    desc.setTitle(propertyDefinition.getTitle());
                    desc.setDataType(propertyDefinition.getDataType());
                    desc.setDefaultValue(propertyDefinition.getDefaultValue());
                } else {
                    log.debug("No property found in mapping for " + propertyDefinition.getName());
                }
            }
            typeDesc.setClassDefinitionLoaded(true);
        } catch (Exception e) {
            throw new ApplicationException(e);
        } finally {
            AlfSessionWrapper.sessionEnd();
        }
    }

}
