package com.zaryxstudios.okaso.cache;

import com.zaryxstudios.okaso.common.cache.Cache;

import java.util.Collections;
import java.util.HashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

public class ExpiringCache<K, V> implements Cache<K, V> {

    private final Map<K, ExpiringEntry<V>> cache;
    private final long ttlMillis;

    public ExpiringCache(long ttl, TimeUnit unit) {
        this.ttlMillis = unit.toMillis(ttl);
        this.cache = new ConcurrentHashMap<K, ExpiringEntry<V>>();
    }

    @Override
    public void put(K key, V value) {
        cache.put(key, new ExpiringEntry<V>(value, System.currentTimeMillis() + ttlMillis));
    }

    @Override
    public Optional<V> get(K key) {
        ExpiringEntry<V> entry = cache.get(key);
        if (entry == null) return Optional.empty();
        if (System.currentTimeMillis() > entry.expiry) {
            cache.remove(key);
            return Optional.empty();
        }
        return Optional.of(entry.value);
    }

    @Override
    public boolean contains(K key) {
        return get(key).isPresent();
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
        evictExpired();
        return cache.size();
    }

    @Override
    public Set<K> keys() {
        evictExpired();
        return Collections.unmodifiableSet(cache.keySet());
    }

    private void evictExpired() {
        long now = System.currentTimeMillis();
        Set<K> expired = new HashSet<K>();
        for (Map.Entry<K, ExpiringEntry<V>> e : cache.entrySet()) {
            if (now > e.getValue().expiry) {
                expired.add(e.getKey());
            }
        }
        for (K key : expired) {
            cache.remove(key);
        }
    }

    @Override
    public V putIfAbsent(K key, V value) {
        ExpiringEntry<V> existing = cache.get(key);
        if (existing == null || System.currentTimeMillis() > existing.expiry) {
            cache.putIfAbsent(key, new ExpiringEntry<V>(value, System.currentTimeMillis() + ttlMillis));
            return null;
        }
        return existing.value;
    }

    @Override
    public V getOrDefault(K key, V defaultValue) {
        ExpiringEntry<V> entry = cache.get(key);
        if (entry == null || System.currentTimeMillis() > entry.expiry) {
            if (entry != null) cache.remove(key);
            return defaultValue;
        }
        return entry.value;
    }

    @Override
    public boolean isEmpty() {
        evictExpired();
        return cache.isEmpty();
    }

    private static class ExpiringEntry<V> {
        final V value;
        final long expiry;

        ExpiringEntry(V value, long expiry) {
            this.value = value;
            this.expiry = expiry;
        }
    }
}
