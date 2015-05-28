package ru.runa.wfe.util;

import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

import org.json.simple.JSONObject;

public class OrderedJSONObject extends JSONObject {

    /**
     * 
     */
    private static final long serialVersionUID = 1L;

    private final AtomicInteger order = new AtomicInteger(0);

    @SuppressWarnings("rawtypes")
    @Override
    public final Object get(Object key) {
        Iterator iter = super.entrySet().iterator();
        while (iter.hasNext()) {
            Map.Entry entry = (Map.Entry) iter.next();
            Entry e = (Entry) entry.getValue();
            if (!e.getKey().equals(key)) {
                continue;
            }
            return e.getValue();
        }
        return null;
    }

    @SuppressWarnings({ "unchecked", "rawtypes" })
    @Override
    public final Object put(Object key, Object value) {
        super.put(order.getAndIncrement(), new Entry(key, value));
        return value;
    }

    @SuppressWarnings("rawtypes")
    @Override
    public final Object remove(Object key) {
        Iterator iter = super.entrySet().iterator();
        while (iter.hasNext()) {
            Map.Entry entry = (Map.Entry) iter.next();
            Entry e = (Entry) entry.getValue();
            if (!e.getKey().equals(key)) {
                continue;
            }
            Object result = e.getValue();
            super.remove(entry.getKey());
            return result;
        }
        return null;
    }

    @SuppressWarnings("rawtypes")
    @Override
    public final void putAll(Map m) {
        Iterator iter = m.entrySet().iterator();
        while (iter.hasNext()) {
            Map.Entry entry = (Map.Entry) iter.next();
            put(entry.getKey(), entry.getValue());
        }
    }

    @SuppressWarnings("rawtypes")
    @Override
    public final boolean containsKey(Object key) {
        Iterator iter = super.entrySet().iterator();
        while (iter.hasNext()) {
            Map.Entry entry = (Map.Entry) iter.next();
            Entry e = (Entry) entry.getValue();
            if (!e.getKey().equals(key)) {
                continue;
            }
            return true;
        }
        return false;
    }

    @Override
    @SuppressWarnings("rawtypes")
    public final boolean containsValue(Object value) {
        Iterator iter = super.entrySet().iterator();
        while (iter.hasNext()) {
            Map.Entry entry = (Map.Entry) iter.next();
            Entry e = (Entry) entry.getValue();
            if (!e.getValue().equals(value)) {
                continue;
            }
            return true;
        }
        return false;
    }

    @Override
    @SuppressWarnings({ "rawtypes", "unchecked" })
    public final Set keySet() {
        Set result = new ArraySet();
        Iterator iter = super.entrySet().iterator();
        while (iter.hasNext()) {
            Map.Entry entry = (Map.Entry) iter.next();
            Entry e = (Entry) entry.getValue();
            result.add(e.getKey());
        }
        return result;
    }

    @Override
    @SuppressWarnings({ "rawtypes", "unchecked" })
    public final Set entrySet() {
        Set result = new ArraySet();
        Iterator iter = super.entrySet().iterator();
        while (iter.hasNext()) {
            Map.Entry entry = (Map.Entry) iter.next();
            Entry e = (Entry) entry.getValue();
            result.add(e);
        }
        return result;
    }

    @Override
    @SuppressWarnings({ "rawtypes", "unchecked" })
    public Collection values() {
        Collection result = Collections.emptyList();
        Iterator iter = super.entrySet().iterator();
        while (iter.hasNext()) {
            Map.Entry entry = (Map.Entry) iter.next();
            Entry e = (Entry) entry.getValue();
            result.add(e.getValue());
        }
        return result;
    }

    static class Entry<K, V> implements Map.Entry<K, V> {

        K key;
        V value;

        Entry(K k, V v) {
            key = k;
            setValue(v);
        }

        @Override
        public K getKey() {
            return key;
        }

        @Override
        public V getValue() {
            return value;
        }

        @Override
        public V setValue(V value) {
            this.value = value;
            return value;
        }

    }
}
