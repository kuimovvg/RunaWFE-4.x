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
import org.alfresco.webservice.repository.RepositoryFault;
import org.alfresco.webservice.repository.UpdateResult;
import org.alfresco.webservice.types.AssociationDefinition;
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
import org.apache.axis.AxisFault;
import org.apache.axis.utils.XMLUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import ru.runa.ClassUtils;
import ru.runa.alfresco.search.Search;
import ru.runa.wfe.InternalApplicationException;

import com.google.common.base.Charsets;
import com.google.common.base.Objects;
import com.google.common.base.Throwables;
import com.google.common.collect.Lists;

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

    public Store getSpacesStore() {
        return new Store(Constants.WORKSPACE_STORE, "SpacesStore");
    }

    private Store toStore(StoreRef storeRef) {
        return new Store(storeRef.getProtocol(), storeRef.getIdentifier());
    }

    public Predicate getPredicate(AlfObject alfObject) {
        return getPredicate(alfObject.getUuidRef());
    }

    private Predicate getPredicate(String uuidRef) {
        Reference reference = getReference(uuidRef, null);
        return new Predicate(new Reference[] { reference }, reference.getStore(), null);
    }

    private Reference getReference(AlfObject alfObject) {
        return getReference(alfObject.getUuidRef(), null);
    }

    private Reference getReference(String uuid, Store store) {
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

    public void addAspect(AlfObject object, String aspectTypeName) throws InternalApplicationException {
        try {
            AlfSessionWrapper.sessionStart();
            AlfTypeDesc desc = Mappings.getMapping(aspectTypeName);
            loadClassDefinitionIfNeeded(desc);
            NamedValue[] props = new WSObjectAccessor(object).getAlfrescoProperties(desc, true, false);
            CMLAddAspect addAspect = new CMLAddAspect(aspectTypeName, props, getPredicate(object), null);
            CML cml = new CML();
            cml.setAddAspect(new CMLAddAspect[] { addAspect });
            WebServiceFactory.getRepositoryService().update(cml);
        } catch (Exception e) {
            throw propagate(e);
        } finally {
            AlfSessionWrapper.sessionEnd();
        }
    }

    public void removeAspect(AlfObject object, String aspectTypeName) throws InternalApplicationException {
        try {
            AlfSessionWrapper.sessionStart();
            CMLRemoveAspect removeAspect = new CMLRemoveAspect(aspectTypeName, getPredicate(object), null);
            CML cml = new CML();
            cml.setRemoveAspect(new CMLRemoveAspect[] { removeAspect });
            WebServiceFactory.getRepositoryService().update(cml);
        } catch (Exception e) {
            throw propagate(e);
        } finally {
            AlfSessionWrapper.sessionEnd();
        }
    }

    public String createFolder(String rootFolderUUID, String folderName) throws InternalApplicationException {
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
            throw propagate(e);
        } finally {
            AlfSessionWrapper.sessionEnd();
        }
    }

    public void createObject(AlfObject object) throws InternalApplicationException {
        String folderUUID = Mappings.getFolderUUID(object.getClass());
        createObject(folderUUID, object);
    }

    public void createObject(String folderUUID, AlfObject object) throws InternalApplicationException {
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
            object.markPropertiesInitialState(typeDesc);
            Reference docRef = results[0].getDestination();
            object.setUuidRef(getUuidRef(docRef));
            log.info("Created object " + docRef.getUuid());
            Cache cache = getCache(ClassUtils.getImplClass(object.getClass()).getName());
            if (cache != null) {
                cache.put(new Element(object.getUuidRef(), object));
            }
        } catch (Exception e) {
            throw propagate(e);
        } finally {
            AlfSessionWrapper.sessionEnd();
        }
    }

    public void updateVersion(AlfObject object, String comment) throws InternalApplicationException {
        try {
            AlfSessionWrapper.sessionStart();
            AlfTypeDesc typeDesc = Mappings.getMapping(object.getClass());
            loadClassDefinitionIfNeeded(typeDesc);
            NamedValue[] comments = new NamedValue[1];
            comments[0] = new NamedValue("description", false, comment, null);
            VersionResult result = WebServiceFactory.getAuthoringService().createVersion(getPredicate(object), comments, false);
            log.info("Version of " + object + " updated to " + result.getVersions()[0].getLabel());
            object.markPropertiesInitialState(typeDesc);
        } catch (Exception e) {
            throw propagate(e);
        } finally {
            AlfSessionWrapper.sessionEnd();
        }
    }

    public void addAssociation(AlfObject source, AlfObject target, String associationName) throws InternalApplicationException {
        try {
            AlfSessionWrapper.sessionStart();
            CMLCreateAssociation createAssociation = new CMLCreateAssociation(getPredicate(source), null, getPredicate(target), null, associationName);
            CML cml = new CML();
            cml.setCreateAssociation(new CMLCreateAssociation[] { createAssociation });
            WebServiceFactory.getRepositoryService().update(cml);
            log.info("Created association " + associationName);
        } catch (Exception e) {
            throw propagate(e);
        } finally {
            AlfSessionWrapper.sessionEnd();
        }
    }

    public void addChildAssociation(AlfObject source, AlfObject target, String associationName) throws InternalApplicationException {
        try {
            AlfSessionWrapper.sessionStart();
            Reference reference = getReference(source);
            ParentReference parentReference = new ParentReference(reference.getStore(), reference.getUuid(), null, associationName, associationName);

            CMLAddChild addChild = new CMLAddChild();
            addChild.setTo(parentReference);
            addChild.setWhere(getPredicate(target));

            CML cml = new CML();
            cml.setAddChild(new CMLAddChild[] { addChild });

            WebServiceFactory.getRepositoryService().update(cml);
            log.info("Created child association " + associationName);
        } catch (Exception e) {
            throw propagate(e);
        } finally {
            AlfSessionWrapper.sessionEnd();
        }
    }

    @Override
    public <T extends AlfObject> T loadObject(String uuidRef) throws InternalApplicationException {
        return (T) loadObject(uuidRef, null, false);
    }

    @Override
    public <T extends AlfObject> T loadObjectNotNull(String uuidRef) throws InternalApplicationException {
        T object = (T) loadObject(uuidRef);
        if (object == null) {
            throw new InternalApplicationException("Unable to load object by " + uuidRef);
        }
        return object;
    }

    @SuppressWarnings("rawtypes")
    @Override
    public void loadAssociation(String uuidRef, Collection collection, AlfSerializerDesc desc) throws InternalApplicationException {
        try {
            AlfSessionWrapper.sessionStart();
            Reference reference = getReference(uuidRef, null);
            QueryResult queryResult;
            boolean filter = true;
            if (desc.isChildAssociation()) {
                if (desc.isSourceAssociation()) {
                    queryResult = WebServiceFactory.getRepositoryService().queryChildren(reference);
                } else {
                    queryResult = WebServiceFactory.getRepositoryService().queryParents(reference);
                }
            } else {
                filter = false;
                Association association = new Association(desc.getPropertyNameWithNamespace(), desc.isSourceAssociation() ? "target" : "source");
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
                        try {
                            AlfObject object = loadObject(reference.getStore(), row, true);
                            collection.add(object);
                        } catch (Exception e) {
                            throw new RuntimeException(desc.getAssoc() + " in " + uuidRef, e);
                        }
                    }
                }
            }
        } catch (Exception e) {
            throw propagate(e);
        } finally {
            AlfSessionWrapper.sessionEnd();
        }
    }

    @Override
    public boolean updateObjectAssociations(AlfObject object) throws InternalApplicationException {
        try {
            boolean updated = false;
            AlfSessionWrapper.sessionStart();
            CML cml = new CML();
            List<CMLRemoveChild> removeChilds = new ArrayList<CMLRemoveChild>();
            List<CMLRemoveAssociation> removeAssociations = new ArrayList<CMLRemoveAssociation>();
            List<CMLAddChild> addChilds = new ArrayList<CMLAddChild>();
            List<CMLCreateAssociation> createAssociations = new ArrayList<CMLCreateAssociation>();
            Map<AlfSerializerDesc, List<String>> assocToDelete = object.getAssocToDelete();
            for (Map.Entry<AlfSerializerDesc, List<String>> entry : assocToDelete.entrySet()) {
                for (String uuidRef : entry.getValue()) {
                    log.debug("Removing assoc " + uuidRef + " from " + object);
                    Predicate where = getPredicate(uuidRef);
                    if (entry.getKey().isChildAssociation()) {
                        CMLRemoveChild removeChild = new CMLRemoveChild(getReference(object), null, where, null);
                        removeChilds.add(removeChild);
                    } else {
                        CMLRemoveAssociation removeAssociation = new CMLRemoveAssociation(getPredicate(object), null, where, null, entry.getKey()
                                .getPropertyNameWithNamespace());
                        removeAssociations.add(removeAssociation);
                    }
                    updated = true;
                }
            }
            Map<AlfSerializerDesc, List<String>> assocToCreate = object.getAssocToCreate();
            for (Map.Entry<AlfSerializerDesc, List<String>> entry : assocToCreate.entrySet()) {
                for (String uuidRef : entry.getValue()) {
                    Reference reference = getReference(uuidRef, null);
                    AlfObject target = loadObject(reference, true);
                    Predicate predicate = getPredicate(target);
                    log.debug("Adding assoc " + reference.getUuid() + " to " + object);
                    if (entry.getKey().isChildAssociation()) {
                        Reference cref = getReference(object);
                        ParentReference parentReference = new ParentReference(cref.getStore(), cref.getUuid(), null, entry.getKey()
                                .getPropertyNameWithNamespace(), target.getObjectName());
                        CMLAddChild addChild = new CMLAddChild(parentReference, null, entry.getKey().getPropertyNameWithNamespace(),
                                target.getObjectName(), predicate, null);
                        addChilds.add(addChild);
                    } else {
                        CMLCreateAssociation createAssociation = new CMLCreateAssociation(getPredicate(object), null, predicate, null, entry.getKey()
                                .getPropertyNameWithNamespace());
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
            object.markCollectionsInitialState();
            return updated;
        } catch (Exception e) {
            throw propagate(e);
        } finally {
            AlfSessionWrapper.sessionEnd();
        }
    }

    private <T extends AlfObject> T loadObject(String uuid, Store store, boolean throwError) throws InternalApplicationException {
        Reference ref = getReference(uuid, store);
        T result = (T) findInCache(ref.getUuid());
        if (result != null) {
            return result;
        }
        return (T) loadObject(ref, throwError);
    }

    private <T extends AlfObject> T loadObject(Reference reference, boolean throwError) throws InternalApplicationException {
        Predicate predicate = new Predicate(new Reference[] { reference }, reference.getStore(), null);
        return (T) loadObject(predicate, throwError);
    }

    private <T extends AlfObject> T loadObject(Predicate where, boolean throwError) throws InternalApplicationException {
        try {
            AlfSessionWrapper.sessionStart();
            Node node = WebServiceFactory.getRepositoryService().get(where)[0];
            return (T) buildObject(node.getType(), node.getReference(), node.getProperties(), node.getAspects());
        } catch (Exception e) {
            log.warn("Unable to load object " + where.getNodes(0).getUuid());
            if (throwError) {
                throw propagate(e);
            }
            return null;
        } finally {
            AlfSessionWrapper.sessionEnd();
        }
    }

    public NamedValue[] loadObjectProperties(String uuidRef) throws InternalApplicationException {
        try {
            AlfSessionWrapper.sessionStart();
            Predicate where = getPredicate(uuidRef);
            Node node = WebServiceFactory.getRepositoryService().get(where)[0];
            return node.getProperties();
        } catch (Exception e) {
            Thread.dumpStack();
            log.warn("Unable to load object properties " + uuidRef, e);
            return null;
        } finally {
            AlfSessionWrapper.sessionEnd();
        }
    }

    private <T extends AlfObject> T loadObject(Store store, ResultSetRow row, boolean throwError) throws InternalApplicationException {
        if (row.getColumns() != null) {
            Reference reference = new Reference(store, row.getNode().getId(), null);
            return (T) buildObject(row.getNode().getType(), reference, row.getColumns(), row.getNode().getAspects());
        } else {
            return (T) loadObject(row.getNode().getId(), store, throwError);
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

    private String getUuidRef(Reference reference) {
        return reference.getStore().getScheme() + "://" + reference.getStore().getAddress() + "/" + reference.getUuid();
    }

    public <T extends AlfObject> T buildObject(String typeName, Reference reference, NamedValue[] props, String[] aspects)
            throws InternalApplicationException {
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
            T object = (T) AlfObjectFactory.create(typeDesc.getJavaClassName());
            object.setLazyLoader(this);
            object.setUuidRef(getUuidRef(reference));
            WSObjectAccessor accessor = new WSObjectAccessor(object);
            for (NamedValue prop : props) {
                AlfSerializerDesc propertyDesc = typeDesc.getPropertyDescByTypeName(prop.getName());
                if (propertyDesc != null) {
                    accessor.setProperty(propertyDesc, prop);
                }
            }
            object.markPropertiesInitialState(typeDesc);
            if (cache != null) {
                cache.put(new Element(object.getUuidRef(), object));
            }
            return object;
        } catch (Exception e) {
            Thread.dumpStack();
            throw propagate(e);
        }
    }

    public <T extends AlfObject> List<T> getVersionedObjects(T object, int limit) throws InternalApplicationException {
        try {
            AlfSessionWrapper.sessionStart();
            Predicate where = getPredicate(object);
            VersionHistory history = WebServiceFactory.getAuthoringService().getVersionHistory(where.getNodes()[0]);
            Version[] versions = history.getVersions();
            List<T> result = Lists.newArrayList();
            // [0] element is current version
            if (versions.length > 1) {
                for (int i = 1; i < versions.length; i++) {
                    if (i > limit) {
                        break;
                    }
                    result.add((T) loadObject(versions[i].getId(), true));
                }
            }
            return result;
        } catch (Exception e) {
            throw propagate(e);
        } finally {
            AlfSessionWrapper.sessionEnd();
        }
    }

    @Override
    public boolean updateObject(AlfObject object, boolean force) throws InternalApplicationException {
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
            CMLUpdate update = new CMLUpdate(contentProps, getPredicate(object), null);
            CML cml = new CML();
            cml.setUpdate(new CMLUpdate[] { update });
            WebServiceFactory.getRepositoryService().update(cml);
            Cache cache = getCache(ClassUtils.getImplClass(object.getClass()).getName());
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
            throw propagate(e);
        } finally {
            AlfSessionWrapper.sessionEnd();
        }
    }

    @Override
    public boolean updateObject(AlfObject object, boolean force, String comment) throws InternalApplicationException {
        boolean updated = updateObject(object, force);
        if (updated) {
            // comment in 3.2
            updateVersion(object, comment);
        }
        return updated;
    }

    public void setContent(AlfObject object, byte[] content, String mimetype) throws InternalApplicationException {
        try {
            AlfSessionWrapper.sessionStart();
            log.info("Setting content to " + object);
            WebServiceFactory.getContentService().write(getReference(object), Constants.PROP_CONTENT, content,
                    new ContentFormat(mimetype, Charsets.UTF_8.name()));
        } catch (Exception e) {
            throw propagate(e);
        } finally {
            AlfSessionWrapper.sessionEnd();
        }
    }

    @Override
    public void deleteObject(AlfObject object) throws InternalApplicationException {
        if (object != null) {
            deleteObject(getReference(object));
            Cache cache = getCache(ClassUtils.getImplClass(object.getClass()).getName());
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
    public void deleteObject(Reference reference) throws InternalApplicationException {
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
                throw propagate(e);
            } finally {
                AlfSessionWrapper.sessionEnd();
            }
        }
    }

    @Override
    public <T extends AlfObject> T findObject(Search search) throws InternalApplicationException {
        List<T> objects = findObjects(search);
        if (objects.size() > 0) {
            return objects.get(0);
        }
        return null;
    }

    private ResultSetRow[] findObjectRows(Store store, Search search) throws InternalApplicationException {
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
            throw propagate(e);
        } finally {
            AlfSessionWrapper.sessionEnd();
        }
    }

    public <T extends AlfObject> List<T> findObjects(Store store, String luceneQuery) throws InternalApplicationException {
        try {
            AlfSessionWrapper.sessionStart();
            Query query = new Query("lucene", luceneQuery);
            QueryResult queryResult = WebServiceFactory.getRepositoryService().query(store, query, false);
            ResultSet resultSet = queryResult.getResultSet();
            ResultSetRow[] rows;
            if (resultSet.getTotalRowCount() > 0) {
                rows = resultSet.getRows();
            } else {
                rows = new ResultSetRow[0];
            }
            List<T> result = new ArrayList<T>(rows.length);
            for (ResultSetRow row : rows) {
                result.add((T) loadObject(store, row, true));
            }
            return result;
        } catch (Exception e) {
            throw propagate(e);
        } finally {
            AlfSessionWrapper.sessionEnd();
        }
    }

    @Override
    public <T extends AlfObject> List<T> findObjects(Search search) throws InternalApplicationException {
        try {
            AlfSessionWrapper.sessionStart();
            Store store = new Store(search.getStore().getProtocol(), search.getStore().getIdentifier());
            ResultSetRow[] rows = findObjectRows(store, search);
            List<T> result = new ArrayList<T>(rows.length);
            for (ResultSetRow row : rows) {
                result.add((T) loadObject(store, row, true));
            }
            return result;
        } catch (Exception e) {
            throw propagate(e);
        } finally {
            AlfSessionWrapper.sessionEnd();
        }
    }

    private void loadClassDefinitionIfNeeded(AlfTypeDesc typeDesc) throws InternalApplicationException {
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
            if (definition.getAssociations() != null) {
                for (AssociationDefinition associationDefinition : definition.getAssociations()) {
                    AlfSerializerDesc desc = typeDesc.getPropertyDescByTypeName(associationDefinition.getName());
                    if (desc != null) {
                        desc.setTitle(associationDefinition.getTitle());
                        desc.setChildAssociation(associationDefinition.isIsChild());
                        desc.setSourceAssociation(!Objects.equal(associationDefinition.getTargetClass(), definition.getName()));
                    } else {
                        log.debug("No property found in mapping for " + associationDefinition.getName());
                    }
                }
            }
            typeDesc.setClassDefinitionLoaded(true);
        } catch (Exception e) {
            throw propagate(e);
        } finally {
            AlfSessionWrapper.sessionEnd();
        }
    }

    public static RuntimeException propagate(Exception exception) {
        if (exception instanceof AxisFault) {
            AxisFault fault = (AxisFault) exception;
            log.error(fault.dumpToString());
            String message;
            if (fault instanceof RepositoryFault) {
                message = ((RepositoryFault) fault).getMessage1();
            } else if (fault.getFaultDetails().length > 0) {
                message = XMLUtils.getInnerXMLString(fault.getFaultDetails()[0]);
            } else {
                message = fault.dumpToString();
            }
            throw new InternalApplicationException(message);
        }
        throw Throwables.propagate(exception);
    }
}
