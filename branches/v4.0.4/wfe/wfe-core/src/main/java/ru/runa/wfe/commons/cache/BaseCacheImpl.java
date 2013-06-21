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

import java.io.Serializable;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.google.common.collect.Queues;

/**
 * Base cache implementation. Contains support for cache versions.
 * 
 * @author Konstantinov Aleksey
 */
public abstract class BaseCacheImpl implements CacheImplementation, VersionedCache {
    protected final Log log = LogFactory.getLog(this.getClass());

    /**
     * Static counter for cache version. Calls like GetExecutors/SetExecutors
     * must be perform in consistent way: Cache must not caching executors list
     * if cache version changed during executors list loading.
     */
    private static AtomicInteger cacheVersion = new AtomicInteger(1);

    /**
     * Caches, used to store cached values.
     */
    private final ConcurrentLinkedQueue<Cache<? extends Serializable, ? extends Serializable>> caches = Queues.newConcurrentLinkedQueue();

    /**
     * Current cache version. Calls like GetExecutors/SetExecutors must be
     * perform in consistent way: Cache must not caching executors list if cache
     * version changed during executors list loading.
     */
    protected volatile int currentCacheVersion;

    /**
     * Creates base cache implementation.
     */
    public BaseCacheImpl() {
        currentCacheVersion = cacheVersion.get();
    }

    @Override
    public final void commitCache() {
        for (Cache<? extends Serializable, ? extends Serializable> cache : caches) {
            cache.commitCache();
        }
        currentCacheVersion = cacheVersion.incrementAndGet();
    }

    @Override
    public int getCacheVersion() {
        return currentCacheVersion;
    }

    /**
     * Create cache to store cached values.
     * 
     * @param <K>
     *            Key type.
     * @param <V>
     *            Value type.
     * @param cacheName
     *            Cache name.
     * @return Cache to store cached values.
     */
    protected <K extends Serializable, V extends Serializable> Cache<K, V> createCache(String cacheName) {
        Cache<K, V> result = new CacheStatisticProxy<K, V>(new EhCacheSupport<K, V>(cacheName), cacheName);
        caches.add(result);
        return result;
    }

    /**
     * Create cache to store cached values.
     * 
     * @param <K>
     *            Key type.
     * @param <V>
     *            Value type.
     * @param cacheName
     *            Cache name.
     * @param infiniteLifeTime
     *            Flag equals true, if element lifetime must be infinite; false
     *            to use ehcache settings.
     * @return Cache to store cached values.
     */
    protected <K extends Serializable, V extends Serializable> Cache<K, V> createCache(String cacheName, boolean infiniteLifeTime) {
        Cache<K, V> result = new CacheStatisticProxy<K, V>(new EhCacheSupport<K, V>(cacheName, infiniteLifeTime), cacheName);
        caches.add(result);
        return result;
    }
}
