package ru.runa.commons.hibernate;

import java.io.Serializable;

import org.hibernate.CallbackException;
import org.hibernate.EmptyInterceptor;
import org.hibernate.Transaction;
import org.hibernate.type.Type;

import ru.runa.commons.cache.CachingLogic;

public class RunaHibernateIntercepter extends EmptyInterceptor {
    static final long serialVersionUID = 1L;

    public void afterTransactionCompletion(Transaction transaction) {
    }

    public void beforeTransactionCompletion(Transaction transaction) {
        if (Thread.currentThread().getName().contains("JbpmJobExecutor")) {
            CachingLogic.onTransactionComplete();
        }
    }

    public boolean onSave(Object entity, Serializable id, Object[] state, String[] propertyNames, Type[] types) throws CallbackException {
        CachingLogic.onGenericChange();
        return false;
    }

    public void onDelete(Object entity, Serializable id, Object[] state, String[] propertyNames, Type[] types) throws CallbackException {
        CachingLogic.onGenericChange();
    }

    public boolean onFlushDirty(Object entity, Serializable id, Object[] currentState, Object[] previousState, String[] propertyNames, Type[] types) {
        CachingLogic.onGenericChange();
        return false;
    }
}
