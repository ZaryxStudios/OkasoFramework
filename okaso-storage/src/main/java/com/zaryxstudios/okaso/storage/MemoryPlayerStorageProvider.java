package com.zaryxstudios.okaso.storage;

import com.zaryxstudios.okaso.common.storage.PlayerDataMode;
import com.zaryxstudios.okaso.common.storage.PlayerStorageProvider;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class MemoryPlayerStorageProvider implements PlayerStorageProvider {

    private final Map<UUID, Map<String, Object>> storage;

    public MemoryPlayerStorageProvider() {
        this.storage = new ConcurrentHashMap<>();
    }

    @Override
    public PlayerDataMode getMode() {
        return PlayerDataMode.PER_PLAYER;
    }

    @Override
    public String getScope() {
        return "memory-players";
    }

    @Override
    public boolean isPersistent() {
        return false;
    }

    @Override
    public void store(UUID playerId, String key, Object value) {
        if (key == null || key.isEmpty()) {
            throw new IllegalArgumentException("key must not be null or empty");
        }
        bucket(playerId).put(key, value);
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> Optional<T> get(UUID playerId, String key, Class<T> type) {
        if (type == null) return Optional.empty();
        Map<String, Object> bucket = storage.get(requireId(playerId));
        if (bucket == null) return Optional.empty();
        Object value = bucket.get(key);
        if (value == null) return Optional.empty();
        if (type.isInstance(value)) {
            return Optional.of((T) value);
        }
        return Optional.empty();
    }

    @Override
    public boolean has(UUID playerId, String key) {
        Map<String, Object> bucket = storage.get(requireId(playerId));
        return bucket != null && bucket.containsKey(key);
    }

    @Override
    public void delete(UUID playerId, String key) {
        Map<String, Object> bucket = storage.get(requireId(playerId));
        if (bucket != null) {
            bucket.remove(key);
        }
    }

    @Override
    public Map<String, Object> getAll(UUID playerId) {
        Map<String, Object> bucket = storage.get(requireId(playerId));
        if (bucket == null) return Collections.emptyMap();
        return Collections.unmodifiableMap(new LinkedHashMap<>(bucket));
    }

    @Override
    public void clear(UUID playerId) {
        Map<String, Object> bucket = storage.get(requireId(playerId));
        if (bucket != null) {
            bucket.clear();
        }
    }

    @Override
    public Set<String> keys(UUID playerId) {
        Map<String, Object> bucket = storage.get(requireId(playerId));
        if (bucket == null) return Collections.emptySet();
        return Collections.unmodifiableSet(new LinkedHashSet<>(bucket.keySet()));
    }

    @Override
    public int size(UUID playerId) {
        Map<String, Object> bucket = storage.get(requireId(playerId));
        return bucket != null ? bucket.size() : 0;
    }

    @Override
    public boolean isEmpty(UUID playerId) {
        return size(playerId) == 0;
    }

    @Override
    public Set<UUID> getPlayers() {
        return Collections.unmodifiableSet(new LinkedHashSet<>(storage.keySet()));
    }

    @Override
    public void clearAll() {
        for (Map<String, Object> bucket : storage.values()) {
            bucket.clear();
        }
    }

    @Override
    public int totalKeys() {
        int total = 0;
        for (Map<String, Object> bucket : storage.values()) {
            total += bucket.size();
        }
        return total;
    }

    private Map<String, Object> bucket(UUID playerId) {
        UUID id = requireId(playerId);
        Map<String, Object> bucket = storage.get(id);
        if (bucket != null) return bucket;
        Map<String, Object> created = new ConcurrentHashMap<>();
        Map<String, Object> raced = storage.putIfAbsent(id, created);
        return raced != null ? raced : created;
    }

    private static UUID requireId(UUID playerId) {
        if (playerId == null) throw new IllegalArgumentException("playerId must not be null");
        return playerId;
    }
}
