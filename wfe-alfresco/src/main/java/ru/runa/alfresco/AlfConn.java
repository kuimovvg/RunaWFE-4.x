package ru.runa.alfresco;

import java.util.Collection;
import java.util.List;

import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.webservice.types.Reference;

import ru.runa.alfresco.search.Search;

/**
 * Connector interface to Alfresco repository.
 * 
 * @author dofs
 */
public interface AlfConn {

    /**
     * Load object from Alfresco repository.
     * 
     * @param objectId
     *            Type depends on implementation and can be {@link NodeRef} or
     *            {@link String}.
     * @return loaded object or <code>null</code>
     */
    public <T extends AlfObject> T loadObject(Object objectId);

    /**
     * Loads association from Alfresco repository.
     * 
     * @param ref
     *            Type depends on implementation and can be {@link NodeRef} or
     *            {@link Reference}.
     * @param collection
     *            container for association objects.
     * @param desc
     *            descriptor.
     */
    @SuppressWarnings("rawtypes")
    public void loadAssociation(Object ref, Collection collection, AlfSerializerDesc desc);

    /**
     * Finds object in Alfresco repository.
     * 
     * @param <T>
     *            result type
     * @param search
     *            valid Lucene query in object presentation.
     * @return found object or <code>null</code>
     */
    public <T extends AlfObject> T findObject(Search search);

    /**
     * Finds objects in Alfresco repository.
     * 
     * @param <T>
     *            result type
     * @param search
     *            valid Lucene query in object presentation.
     * @return list of found objects
     */
    public <T extends AlfObject> List<T> findObjects(Search search);

    /**
     * Updates object in Alfresco without creation new version.
     * 
     * @param object
     * @param force
     *            all properties
     */
    public boolean updateObject(AlfObject object, boolean force);

    /**
     * Updates object in Alfresco with creation new version.
     * 
     * @param object
     * @param force
     *            all properties
     * @param comment
     *            version comment
     */
    public boolean updateObject(AlfObject object, boolean force, String comment);

    /**
     * Updates object associations in Alfresco.
     * 
     * @param object
     */
    public boolean updateObjectAssociations(AlfObject object);

    /**
     * Deletes object from Alfresco.
     * 
     * @param object
     */
    public void deleteObject(AlfObject object);
}
