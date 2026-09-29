package com.zaryxstudios.okaso.common.storage;

import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

public interface PlayerStorageProvider {

    void store(UUID playerId, String key, Object value);

    <T> Optional<T> get(UUID playerId, String key, Class<T> type);

    default Optional<Object> get(UUID playerId, String key) {
        return get(playerId, key, Object.class);
    }

    default <T> T getOrDefault(UUID playerId, String key, Class<T> type, T fallback) {
        Optional<T> value = get(playerId, key, type);
        return value.isPresent() ? value.get() : fallback;
    }

    boolean has(UUID playerId, String key);

    void delete(UUID playerId, String key);

    Map<String, Object> getAll(UUID playerId);

    void clear(UUID playerId);

    Set<String> keys(UUID playerId);

    int size(UUID playerId);

    boolean isEmpty(UUID playerId);

    Set<UUID> getPlayers();

    void clearAll();

    int totalKeys();

    default PlayerDataMode getMode() {
        return PlayerDataMode.PER_PLAYER;
    }

    default String getScope() {
        return "players";
    }

    default boolean isPersistent() {
        return true;
    }

    default void flush() {
    }

    default void close() {
    }
}
