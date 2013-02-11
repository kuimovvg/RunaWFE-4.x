/*
 * This file is part of the RUNA WFE project.
 * 
 * This program is free software; you can redistribute it and/or 
 * modify it under the terms of the GNU Lesser General Public License 
 * as published by the Free Software Foundation; version 2.1 
 * of the License. 
 * 
 * This program is distributed in the hope that it will be useful, 
 * but WITHOUT ANY WARRANTY; without even the implied warranty of 
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the 
 * GNU Lesser General Public License for more details. 
 * 
 * You should have received a copy of the GNU Lesser General Public License 
 * along with this program; if not, write to the Free Software 
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA.
 */
package ru.runa.wfe.commons.cache;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.hibernate.type.Type;

import com.google.common.base.Throwables;

/**
 * Main class for RunaWFE caching. Register {@link ChangeListener} there to
 * receive events on objects change and transaction complete.
 */
public class CachingLogic {

    /**
     * Map from {@link Thread} to change listeners, which must be notified on
     * thread transaction complete. Then thread transaction change some objects,
     * affected listeners stored there.
     */
    private static Map<Thread, Set<ChangeListener>> dirtyThreads = new ConcurrentHashMap<Thread, Set<ChangeListener>>();

    /**
     * {@link ChangeListener}, which must be notified, when unrecognized object
     * change. All registered listeners contains in this collection.
     */
    static Set<ChangeListener> genericListeners = new HashSet<ChangeListener>();
    /**
     * {@link ChangeListener}, which must be notified, when executor related
     * object change.
     */
    static Set<ChangeListener> executorListeners = new HashSet<ChangeListener>();
    /**
     * {@link ChangeListener}, which must be notified, when substitution related
     * object change.
     */
    static Set<ChangeListener> substitutionListeners = new HashSet<ChangeListener>();
    /**
     * {@link ChangeListener}, which must be notified, when task instance
     * related object change.
     */
    static Set<ChangeListener> taskListeners = new HashSet<ChangeListener>();
    /**
     * {@link ChangeListener}, which must be notified, when process definition
     * related object change.
     */
    static Set<ChangeListener> processDefListeners = new HashSet<ChangeListener>();

    /**
     * Register listener. Listener will be notified on events, according to
     * implemented interfaces.
     * 
     * @param listener
     *            Listener, which must receive events.
     */
    public static synchronized void registerChangeListener(ChangeListener listener) {
        ChangeListenerGuard guarded = new ChangeListenerGuard(listener);
        genericListeners.add(guarded);
        if (listener instanceof ExecutorChangeListener) {
            executorListeners.add(guarded);
        }
        if (listener instanceof SubstitutionChangeListener) {
            substitutionListeners.add(guarded);
        }
        if (listener instanceof TaskChangeListener) {
            taskListeners.add(guarded);
        }
        if (listener instanceof ProcessDefChangeListener) {
            processDefListeners.add(guarded);
        }
    }

    /**
     * Notify registered listeners on unrecognized object change. All registered
     * listeners will be notified. TODO unused
     */
    public static synchronized void onGenericChange() {
        onWriteTransaction(genericListeners, null, null, null, null, null);
    }

    /**
     * Notify registered listeners on executor related object change. Only
     * registered listeners, implementing {@link ExecutorChangeListener} will be
     * notified.
     * 
     * @param object
     *            Changed object.
     * @param currentState
     *            Current state of object properties.
     * @param previousState
     *            Previous state of object properties.
     * @param propertyNames
     *            Property names (same order as in currentState).
     * @param types
     *            Property types.
     */
    public static synchronized void onExecutorChange(Object changed, Object[] currentState, Object[] previousState, String[] propertyNames,
            Type[] types) {
        onWriteTransaction(executorListeners, changed, currentState, previousState, propertyNames, types);
    }

    /**
     * Notify registered listeners on substitution related object change. Only
     * registered listeners, implementing {@link SubstitutionChangeListener}
     * will be notified.
     * 
     * @param object
     *            Changed object.
     * @param currentState
     *            Current state of object properties.
     * @param previousState
     *            Previous state of object properties.
     * @param propertyNames
     *            Property names (same order as in currentState).
     * @param types
     *            Property types.
     */
    public static synchronized void onSubstitutionChange(Object changed, Object[] currentState, Object[] previousState, String[] propertyNames,
            Type[] types) {
        onWriteTransaction(substitutionListeners, changed, currentState, previousState, propertyNames, types);
    }

    /**
     * Notify registered listeners on task related object change. Only
     * registered listeners, implementing {@link TaskChangeListener} will be
     * notified.
     * 
     * @param object
     *            Changed object.
     * @param currentState
     *            Current state of object properties.
     * @param previousState
     *            Previous state of object properties.
     * @param propertyNames
     *            Property names (same order as in currentState).
     * @param types
     *            Property types.
     */
    public static synchronized void onTaskChange(Object changed, Object[] currentState, Object[] previousState, String[] propertyNames, Type[] types) {
        onWriteTransaction(taskListeners, changed, currentState, previousState, propertyNames, types);
    }

    /**
     * Notify registered listeners on process definition related object change.
     * Only registered listeners, implementing {@link ProcessDefChangeListener}
     * will be notified.
     * 
     * @param object
     *            Changed object.
     * @param currentState
     *            Current state of object properties.
     * @param previousState
     *            Previous state of object properties.
     * @param propertyNames
     *            Property names (same order as in currentState).
     * @param types
     *            Property types.
     */
    public static synchronized void onProcessDefChange(Object changed, Object[] currentState, Object[] previousState, String[] propertyNames,
            Type[] types) {
        onWriteTransaction(processDefListeners, changed, currentState, previousState, propertyNames, types);
    }

    /**
     * Check current thread transaction type.
     * 
     * @return If transaction change some objects, return true; return false
     *         otherwise.
     */
    public static boolean isWriteTransaction() {
        return dirtyThreads.containsKey(Thread.currentThread());
    }

    /**
     * Notifies given listeners.
     * 
     * @param notifyThis
     *            Listeners to notify.
     * @param object
     *            Changed object.
     * @param currentState
     *            Current state of object properties.
     * @param previousState
     *            Previous state of object properties.
     * @param propertyNames
     *            Property names (same order as in currentState).
     * @param types
     *            Property types.
     */
    private static synchronized void onWriteTransaction(Set<ChangeListener> notifyThis, Object changed, Object[] currentState,
            Object[] previousState, String[] propertyNames, Type[] types) {
        Set<ChangeListener> toNotify = dirtyThreads.get(Thread.currentThread());
        if (toNotify == null) {
            toNotify = new HashSet<ChangeListener>();
            dirtyThreads.put(Thread.currentThread(), toNotify);
        }
        toNotify.addAll(notifyThis);
        for (ChangeListener listener : notifyThis) {
            if (changed == null) {
                listener.onChange();
            } else {
                listener.onChange(changed, currentState, previousState, propertyNames, types);
            }
        }
    }

    /**
     * Called, then thread transaction is completed. If thread transaction
     * change nothing, when do nothing. If thread transaction change some
     * objects, when all related listeners is notified on transaction complete.
     * All related listeners first receive markTransactionComplete event, after
     * what all related listeners receive onTransactionComplete event.
     */
    public static void onTransactionComplete() {
        Set<ChangeListener> toNotify = dirtyThreads.remove(Thread.currentThread());
        if (toNotify == null) {
            return;
        }
        synchronized (CachingLogic.class) {
            for (ChangeListener listener : toNotify) {
                listener.markTransactionComplete();
            }
            for (ChangeListener listener : toNotify) {
                listener.onTransactionComplete();
            }
        }
    }

    /**
     * Return cache instance from cache control instance. If control instance
     * already initialized with cache instance, then returning it. If control
     * instance not initialized with cache instance, then cache instance is
     * created and cache control initialized with created cache (if cache
     * instance is not locked).
     * <p>
     * This call can be blocked until change transactions will be complete.
     * </p>
     * 
     * @param <CacheImpl>
     *            Type of cache, controlled by cache control component.
     * @param cache
     *            Cache control component.
     * @return Cache instance. Must be always not null.
     */
    public static <CacheImpl extends CacheImplementation> CacheImpl getCacheImpl(CacheControl<CacheImpl> cache) {
        CacheImpl cacheImplTmp = tryGetCache(cache);
        if (cacheImplTmp != null) {
            return cacheImplTmp;
        }
        boolean isInitiateInProcess = !isWriteTransaction();
        try {
            CacheImpl result = cache.buildCache();
            if (!isWriteTransaction()) { // If this transaction is write to DB,
                                         // return temporary cache object.
                                         // Otherwise initiate cache with
                                         // current cache object.
                synchronized (CachingLogic.class) { // And notify all threads
                                                    // awaiting cache
                                                    // initialization
                    cache.initCache(result);
                }
            }
            return result;
        } finally {
            if (isInitiateInProcess) { // In all case release initialize lock
                                       // and notify others
                synchronized (CachingLogic.class) {
                    cache.initiateComplete();
                    CachingLogic.class.notifyAll();
                }
            }
        }
    }

    /**
     * Try to get cache implementation from cache control. If no cache
     * implementation in cache control or thread transaction is change some
     * objects, then return null. If cache control is already processing
     * initiation, then current thread blocking until initiation complete.
     * 
     * @param <CacheImpl>
     *            Type of cache, controlled by cache control component.
     * @param cache
     *            Cache control component.
     * @return Cache instance, or null, if cache instance need to be created.
     */
    private static <CacheImpl extends CacheImplementation> CacheImpl tryGetCache(CacheControl<CacheImpl> cache) {
        CacheImpl cacheImplTmp = cache.getCache();
        if (cacheImplTmp != null) {
            return cacheImplTmp;
        }
        synchronized (CachingLogic.class) {
            while (true) {
                try {
                    CacheImpl cacheImpl = cache.getCache();
                    if (cacheImpl != null) {
                        return cacheImpl;
                    }
                    if (!cache.isLocked() && !cache.isInInitiate() && !isWriteTransaction()) { // Cache
                                                                                               // is
                                                                                               // steel
                                                                                               // not
                                                                                               // initiated
                                                                                               // and
                                                                                               // no
                                                                                               // initiate
                                                                                               // in
                                                                                               // progress
                                                                                               // -
                                                                                               // mark
                                                                                               // it
                                                                                               // as
                                                                                               // initiateInProgress
                                                                                               // and
                                                                                               // initiate.
                        cache.initiateInProcess();
                        return null;
                    } else {
                        // Cache is steel not initiated but it locked or
                        // initiating.
                        if (isWriteTransaction()) { // Write transaction must be
                                                    // completed at all case.
                                                    // Moving to build cache
                                                    // stage
                            return null;
                        } else { // Wait until cache is unlocked or initiate
                                 // process finished
                            CachingLogic.class.wait();
                        }
                    }
                } catch (InterruptedException e) {
                    throw Throwables.propagate(e);
                }
            }
        }
    }

}
