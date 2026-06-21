package com.zaryxstudios.okaso.cache;

import com.zaryxstudios.okaso.common.cache.Cache;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

public class SimpleCache<K, V> implements Cache<K, V> {

    private final Map<K, V> cache;
    private final int maxSize;

    public SimpleCache() {
        this(0);
    }

    public SimpleCache(int maxSize) {
        this.maxSize = maxSize;
        this.cache = new LinkedHashMap<K, V>(16, 0.75f, true) {
            @Override
            protected boolean removeEldestEntry(Map.Entry<K, V> eldest) {
                return maxSize > 0 && size() > maxSize;
            }
        };
    }

    @Override
    public void put(K key, V value) {
        cache.put(key, value);
    }

    @Override
    public Optional<V> get(K key) {
        return Optional.ofNullable(cache.get(key));
    }

    @Override
    public boolean contains(K key) {
        return cache.containsKey(key);
    }

    @Override
    public void remove(K key) {
        cache.remove(key);
    }

    @Override
    public void clear() {
        cache.clear();
    }

    @Override
    public int size() {
        return cache.size();
    }

    @Override
    public Set<K> keys() {
        return Collections.unmodifiableSet(cache.keySet());
    }

    @Override
    public V putIfAbsent(K key, V value) {
        V old = cache.get(key);
        if (old == null) {
            cache.put(key, value);
        }
        return old;
    }

    @Override
    public V getOrDefault(K key, V defaultValue) {
        V value = cache.get(key);
        return value != null ? value : defaultValue;
    }

    @Override
    public boolean isEmpty() {
        return cache.isEmpty();
    }
}
