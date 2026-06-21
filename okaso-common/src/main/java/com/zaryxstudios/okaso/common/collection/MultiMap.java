package com.zaryxstudios.okaso.common.collection;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class MultiMap<K, V> {

    private final Map<K, Set<V>> backing = new HashMap<>();

    public void put(K key, V value) {
        backing.computeIfAbsent(key, k -> new HashSet<>()).add(value);
    }

    public Collection<V> get(K key) {
        return backing.getOrDefault(key, new HashSet<>());
    }

    public boolean containsKey(K key) {
        return backing.containsKey(key);
    }

    public boolean containsValue(K key, V value) {
        Set<V> values = backing.get(key);
        return values != null && values.contains(value);
    }

    public void remove(K key, V value) {
        Set<V> values = backing.get(key);
        if (values != null) {
            values.remove(value);
            if (values.isEmpty()) backing.remove(key);
        }
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
