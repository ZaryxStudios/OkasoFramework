package com.zaryxstudios.okaso.storage;

import com.zaryxstudios.okaso.common.storage.PlayerDataMode;
import com.zaryxstudios.okaso.common.storage.PlayerStorageProvider;
import com.zaryxstudios.okaso.common.storage.StorageProvider;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

public class UnifiedPlayerStorageProvider implements PlayerStorageProvider {

    private final StorageProvider delegate;
    private final String separator;

    public UnifiedPlayerStorageProvider(StorageProvider delegate, String separator) {
        if (delegate == null) throw new IllegalArgumentException("delegate must not be null");
        this.delegate = delegate;
        this.separator = separator == null || separator.isEmpty() ? "." : separator;
    }

    public StorageProvider getDelegate() {
        return delegate;
    }

    @Override
    public PlayerDataMode getMode() {
        return PlayerDataMode.UNIFIED;
    }

    @Override
    public String getScope() {
        return delegate.getScope() + separator + "players";
    }

    @Override
    public boolean isPersistent() {
        return delegate.isPersistent();
    }

    @Override
    public void store(UUID playerId, String key, Object value) {
        delegate.store(resolve(playerId, key), value);
    }

    @Override
    public <T> Optional<T> get(UUID playerId, String key, Class<T> type) {
        return delegate.get(resolve(playerId, key), type);
    }

    @Override
    public boolean has(UUID playerId, String key) {
        return delegate.has(resolve(playerId, key));
    }

    @Override
    public void delete(UUID playerId, String key) {
        delegate.delete(resolve(playerId, key));
    }

    @Override
    public Map<String, Object> getAll(UUID playerId) {
        String prefix = prefix(playerId);
        Map<String, Object> scoped = new LinkedHashMap<>();
        for (String key : delegate.keys()) {
            if (!key.startsWith(prefix)) continue;
            String stripped = key.substring(prefix.length());
            if (stripped.isEmpty()) continue;
            scoped.put(stripped, delegate.get(key, Object.class).orElse(null));
        }
        return Collections.unmodifiableMap(scoped);
    }

    @Override
    public void clear(UUID playerId) {
        String prefix = prefix(playerId);
        for (String key : delegate.keys()) {
            if (key.startsWith(prefix)) {
                delegate.delete(key);
            }
        }
    }

    @Override
    public Set<String> keys(UUID playerId) {
        String prefix = prefix(playerId);
        Set<String> scoped = new LinkedHashSet<>();
        for (String key : delegate.keys()) {
            if (!key.startsWith(prefix)) continue;
            String stripped = key.substring(prefix.length());
            if (!stripped.isEmpty()) {
                scoped.add(stripped);
            }
        }
        return Collections.unmodifiableSet(scoped);
    }

    @Override
    public int size(UUID playerId) {
        return keys(playerId).size();
    }

    @Override
    public boolean isEmpty(UUID playerId) {
        return size(playerId) == 0;
    }

    @Override
    public Set<UUID> getPlayers() {
        Set<UUID> players = new LinkedHashSet<>();
        for (String key : delegate.keys()) {
            int index = key.indexOf(separator);
            if (index <= 0) continue;
            UUID parsed = parse(key.substring(0, index));
            if (parsed != null) {
                players.add(parsed);
            }
        }
        return Collections.unmodifiableSet(players);
    }

    @Override
    public void clearAll() {
        delegate.clear();
    }

    @Override
    public int totalKeys() {
        return delegate.size();
    }

    @Override
    public void flush() {
        delegate.flush();
    }

    private static UUID parse(String raw) {
        if (raw == null || raw.length() != 36) return null;
        try {
            return UUID.fromString(raw);
        } catch (IllegalArgumentException e) {
            return null;
        }
    }

    private String prefix(UUID playerId) {
        return requireId(playerId) + separator;
    }

    private String resolve(UUID playerId, String key) {
        if (key == null || key.isEmpty()) {
            throw new IllegalArgumentException("key must not be null or empty");
        }
        return prefix(playerId) + key;
    }

    private static String requireId(UUID playerId) {
        if (playerId == null) throw new IllegalArgumentException("playerId must not be null");
        return playerId.toString();
    }
}
