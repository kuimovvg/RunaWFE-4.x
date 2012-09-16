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
package ru.runa.commons.cache;

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hibernate.type.Type;

/**
 * Base implementation of cache control objects.
 * 
 * @author Konstantinov Aleksey
 * @param <CacheImpl> Controlled cache implementation.
 */
public abstract class BaseCacheCtrl<CacheImpl extends CacheImplementation> implements CacheControl<CacheImpl>, ChangeListener {

    /**
     * Smart cache parameter name.
     */
    private static final String SMART_CACHE = "smart_cache";

    /**
     * Logging support. 
     */
    private final Log log;

    /**
     * Current cache implementation. May be null, if no cache implementation initialised.  
     */
    private final AtomicReference<CacheImpl> impl = new AtomicReference<CacheImpl>(null);

    /**
     * Flag, equals true, if cache currently initializing to set current cache implementation (impl).  
     */
    private final AtomicBoolean isInitiateInProcess = new AtomicBoolean(false);

    /**
     * Set of threads, which makes changes, affecting cache.
     * After thread transaction completes, it removes from this set. 
     */
    private final Set<Thread> dirtyThreads = new HashSet<Thread>();

    protected BaseCacheCtrl() {
        log = LogFactory.getLog(this.getClass());
    }

    public final CacheImpl getCache() {
        return impl.get();
    }

    public final void initCache(CacheImpl cache) {
        cache.commitCache();
        impl.set(cache);
        log.info("Cache is initialized");
    }

    public final void initiateComplete() {
        isInitiateInProcess.set(false);
    }

    public final void initiateInProcess() {
        isInitiateInProcess.set(true);
    }

    public final boolean isInInitiate() {
        return isInitiateInProcess.get();
    }

    public final boolean isLocked() {
        return !dirtyThreads.isEmpty();
    }

    @Override
    public final void onChange() {
        registerChange();
        doOnChange();
    }

    @Override
    public final void onChange(Object object, Object[] currentState, Object[] previousState, String[] propertyNames, Type[] types) {
        registerChange();
        doOnChange(object, currentState, previousState, propertyNames, types);
    }

    @Override
    public final void markTransactionComplete() {
        dirtyThreads.remove(Thread.currentThread());
        if (dirtyThreads.isEmpty()) {
            CachingLogic.class.notifyAll();
        }
        doMarkTransactionComplete();
    }

    @Override
    public void onTransactionComplete() {
    }

    /**
     * <b>Override this method if you need some additional actions on {@link #onChange()}. 
     * Default implementation is called {@linkplain #uninitialize()}.</b><p/>
     *
     * Called, then unrecognized object changed.
     */
    protected void doOnChange() {
        uninitialize(null);
    }

    /**
     * <b>Override this method if you need some additional actions on {@link #onChange(Object, Object[], Object[], String[], Type[])}. 
     * Default implementation is called {@linkplain #uninitialize()}.</b><p/>
     *
     * Called, then changed one of predefined object (e. q. specific sub interface exists).  
     * @param object Changed object.
     * @param currentState Current state of object properties.
     * @param previousState Previous state of object properties.
     * @param propertyNames Property names (same order as in currentState).
     * @param types Property types.
     */
    protected void doOnChange(Object object, Object[] currentState, Object[] previousState, String[] propertyNames, Type[] types) {
        uninitialize(object);
    }

    /**
     * <b>Override this method if you need some additional actions on {@link #markTransactionComplete()}. 
     * Default implementation is suitable for most case.</b><p/>
     * 
     * Called, then transaction in current thread is completed.
     * Cache controller must mark transaction as completed, but must not recreate cache.<p/>
     * Cache recreation may be done in {@link #onTransactionComplete()}, then all caches is marked transaction.<p/>
     * {@link CachingLogic} guarantees, what all caches receive {@link #markTransactionComplete()}, and only after 
     * what all caches receive {@link #onTransactionComplete()}. 
     */
    protected void doMarkTransactionComplete() {
    }

    /**
     * Drops current cache implementation. 
     * @param object Changed object, which leads to cache drop. 
     */
    protected void uninitialize(Object object) {
        if (impl.get() != null) {
            if (object == null) {
                log.info("Cache is uninitialized. Unknown reason.");
            } else {
                log.info("Cache is uninitialized. Changed object of type " + object.getClass().getName());
            }
        }
        impl.set(null);
    }

    /**
     * Register current Thread as changing.
     * After thread transaction completes it will be removed from changing threads.
     */
    private void registerChange() {
        dirtyThreads.add(Thread.currentThread());
    }

    /**
     * Check if current cache is 'smart'. 
     * If cache is not 'smart' it will completely drop cache implementation on all affecting changes;
     * 'smart' cache tries to remove only affected elements from cache.
     * @return true, if cache is 'smart'; false otherwise.
     */
    protected boolean isSmartCache() {
        try {
            String value = new CachingResources().readPropertyIfExist(SMART_CACHE);
            if (value == null || !"true".equalsIgnoreCase(value)) {
                return false;
            }
            value = new CachingResources().readPropertyIfExist(this.getClass().getName() + "." + SMART_CACHE);
            if (value == null || "true".equalsIgnoreCase(value)) {
                return true;
            }
            return false;
        } catch (Throwable e) {
            return false;
        }
    }
}
