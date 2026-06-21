package com.zaryxstudios.okaso.common.cache;

import java.util.Optional;
import java.util.Set;

public interface Cache<K, V> {
    void put(K key, V value);
    Optional<V> get(K key);
    boolean contains(K key);
    void remove(K key);
    void clear();
    int size();
    Set<K> keys();
    V putIfAbsent(K key, V value);
    V getOrDefault(K key, V defaultValue);
    boolean isEmpty();
}
