package ru.runa.alfresco;

import java.util.Collection;
import java.util.List;

import ru.runa.alfresco.search.Search;

@SuppressWarnings("unchecked")
public class AlfSessionLazyProxy implements AlfConn {

    @Override
    public void initializeTypeDefinition(final AlfTypeDesc typeDesc) {
        new AlfSessionWrapper<Object>() {

            @Override
            protected Object code() throws Exception {
                session.initializeTypeDefinition(typeDesc);
                return null;
            }

        }.runInSession();
    }

    @Override
    public <T extends AlfObject> T loadObject(final String uuidRef) {
        return (T) new AlfSessionWrapper<AlfObject>() {

            @Override
            protected AlfObject code() throws Exception {
                return session.loadObject(uuidRef);
            }

        }.runInSession();
    }

    @Override
    public <T extends AlfObject> T loadObjectNotNull(final String uuidRef) {
        return (T) new AlfSessionWrapper<AlfObject>() {

            @Override
            protected AlfObject code() throws Exception {
                return session.loadObjectNotNull(uuidRef);
            }

        }.runInSession();
    }

    @Override
    public void loadAssociation(final String uuidRef, @SuppressWarnings("rawtypes") final Collection collection, final AlfSerializerDesc desc) {
        new AlfSessionWrapper<Object>() {

            @Override
            protected Object code() throws Exception {
                session.loadAssociation(uuidRef, collection, desc);
                return null;
            }

        }.runInSession();
    }

    @Override
    public <T extends AlfObject> T findObject(final Search search) {
        return (T) new AlfSessionWrapper<AlfObject>() {

            @Override
            protected AlfObject code() throws Exception {
                return session.findObject(search);
            }

        }.runInSession();
    }

    @Override
    public <T extends AlfObject> List<T> findObjects(final Search search) {
        return (List<T>) new AlfSessionWrapper<List<AlfObject>>() {

            @Override
            protected List<AlfObject> code() throws Exception {
                return session.findObjects(search);
            }

        }.runInSession();
    }

    @Override
    public boolean updateObject(final AlfObject object, final boolean force) {
        return new AlfSessionWrapper<Boolean>() {

            @Override
            protected Boolean code() throws Exception {
                return session.updateObject(object, force);
            }

        }.runInSession();
    }

    @Override
    public boolean updateObject(final AlfObject object, final boolean force, final String comment) {
        return new AlfSessionWrapper<Boolean>() {

            @Override
            protected Boolean code() throws Exception {
                return session.updateObject(object, force, comment);
            }

        }.runInSession();
    }

    @Override
    public boolean updateObjectAssociations(final AlfObject object) {
        return new AlfSessionWrapper<Boolean>() {

            @Override
            protected Boolean code() throws Exception {
                return session.updateObjectAssociations(object);
            }

        }.runInSession();
    }

    @Override
    public void deleteObject(final AlfObject object) {
        new AlfSessionWrapper<Object>() {

            @Override
            protected Object code() throws Exception {
                session.deleteObject(object);
                return null;
            }

        }.runInSession();
    }

}
