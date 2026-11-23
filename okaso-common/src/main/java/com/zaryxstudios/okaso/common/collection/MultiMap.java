package com.zaryxstudios.okaso.common.collection;

import java.util.Collection;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class MultiMap<K, V> {

    private final Map<K, Set<V>> backing = new ConcurrentHashMap<>();

    public void put(K key, V value) {
        backing.computeIfAbsent(key, k -> ConcurrentHashMap.newKeySet()).add(value);
    }

    public Collection<V> get(K key) {
        Set<V> values = backing.get(key);
        return values == null ? new HashSet<>() : new HashSet<>(values);
    }

    public boolean containsKey(K key) {
        return backing.containsKey(key);
    }

    public boolean containsValue(K key, V value) {
        Set<V> values = backing.get(key);
        return values != null && values.contains(value);
    }

    public void remove(K key, V value) {
        backing.computeIfPresent(key, (k, values) -> {
            values.remove(value);
            return values.isEmpty() ? null : values;
        });
    }

    public void removeAll(K key) {
        backing.remove(key);
    }

    public void clear() {
        backing.clear();
    }

    public Set<K> keySet() {
        return backing.keySet();
    }

    public int size() {
        return backing.size();
    }
}
