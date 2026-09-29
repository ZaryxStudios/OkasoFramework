package com.zaryxstudios.okaso.storage;

import com.zaryxstudios.okaso.common.storage.PlayerDataMode;
import com.zaryxstudios.okaso.common.storage.PlayerStorageProvider;
import com.zaryxstudios.okaso.common.storage.StorageProvider;
import com.zaryxstudios.okaso.common.storage.StorageSettings;

import java.io.File;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

public class IsolatedPlayerStorageProvider implements PlayerStorageProvider {

    private static final Logger LOGGER = Logger.getLogger(IsolatedPlayerStorageProvider.class.getName());
    private static final String FILE_EXTENSION = ".json";

    private final File directory;
    private final StorageSettings settings;
    private final Map<UUID, StorageProvider> providers;

    public IsolatedPlayerStorageProvider(File directory, StorageSettings settings) {
        if (directory == null) throw new IllegalArgumentException("directory must not be null");
        this.directory = directory;
        this.settings = settings != null ? settings : StorageSettings.defaults();
        this.providers = new ConcurrentHashMap<>();
    }

    public File getDirectory() {
        return directory;
    }

    @Override
    public PlayerDataMode getMode() {
        return PlayerDataMode.PER_PLAYER;
    }

    @Override
    public String getScope() {
        return directory.getName();
    }

    @Override
    public boolean isPersistent() {
        return true;
    }

    @Override
    public void store(UUID playerId, String key, Object value) {
        provider(playerId).store(key, value);
    }

    @Override
    public <T> Optional<T> get(UUID playerId, String key, Class<T> type) {
        StorageProvider provider = resolve(requireId(playerId));
        if (provider == null) return Optional.empty();
        return provider.get(key, type);
    }

    @Override
    public boolean has(UUID playerId, String key) {
        StorageProvider provider = resolve(requireId(playerId));
        return provider != null && provider.has(key);
    }

    @Override
    public void delete(UUID playerId, String key) {
        StorageProvider provider = resolve(requireId(playerId));
        if (provider != null) {
            provider.delete(key);
        }
    }

    @Override
    public Map<String, Object> getAll(UUID playerId) {
        StorageProvider provider = resolve(requireId(playerId));
        return provider != null ? provider.getAll() : Collections.<String, Object>emptyMap();
    }

    @Override
    public void clear(UUID playerId) {
        StorageProvider provider = resolve(requireId(playerId));
        if (provider != null) {
            provider.clear();
        }
    }

    @Override
    public Set<String> keys(UUID playerId) {
        StorageProvider provider = resolve(requireId(playerId));
        return provider != null ? provider.keys() : Collections.<String>emptySet();
    }

    @Override
    public int size(UUID playerId) {
        StorageProvider provider = resolve(requireId(playerId));
        return provider != null ? provider.size() : 0;
    }

    @Override
    public boolean isEmpty(UUID playerId) {
        return size(playerId) == 0;
    }

    @Override
    public Set<UUID> getPlayers() {
        Set<UUID> players = new LinkedHashSet<>();
        for (File file : listPlayerFiles()) {
            UUID parsed = parseFile(file);
            if (parsed != null) {
                players.add(parsed);
            }
        }
        return Collections.unmodifiableSet(players);
    }

    @Override
    public void clearAll() {
        for (File file : listPlayerFiles()) {
            UUID parsed = parseFile(file);
            if (parsed != null) {
                clear(parsed);
            }
        }
    }

    @Override
    public int totalKeys() {
        int total = 0;
        for (UUID playerId : getPlayers()) {
            total += size(playerId);
        }
        return total;
    }

    @Override
    public void flush() {
        for (StorageProvider provider : providers.values()) {
            provider.flush();
        }
    }

    @Override
    public void close() {
        for (StorageProvider provider : providers.values()) {
            provider.close();
        }
        providers.clear();
    }

    public StorageProvider provider(UUID playerId) {
        UUID id = requireId(playerId);
        StorageProvider existing = providers.get(id);
        if (existing != null) return existing;
        return attach(id, new JsonFileStorageProvider(fileFor(id), settings));
    }

    public File fileFor(UUID playerId) {
        return new File(directory, requireId(playerId) + FILE_EXTENSION);
    }

    private StorageProvider resolve(UUID playerId) {
        StorageProvider existing = providers.get(playerId);
        if (existing != null) return existing;
        File file = fileFor(playerId);
        if (!file.exists()) return null;
        return attach(playerId, new JsonFileStorageProvider(file, settings));
    }

    private StorageProvider attach(UUID playerId, StorageProvider provider) {
        StorageProvider raced = providers.putIfAbsent(playerId, provider);
        if (raced != null) {
            provider.close();
            return raced;
        }
        return provider;
    }

    private Set<File> listPlayerFiles() {
        Set<File> files = new LinkedHashSet<>();
        File[] listed = directory.listFiles();
        if (listed == null) return files;
        for (File file : listed) {
            if (file.isFile() && file.getName().endsWith(FILE_EXTENSION)) {
                files.add(file);
            }
        }
        return files;
    }

    private static UUID parseFile(File file) {
        String name = file.getName();
        if (name.length() <= FILE_EXTENSION.length()) return null;
        String raw = name.substring(0, name.length() - FILE_EXTENSION.length());
        try {
            return UUID.fromString(raw);
        } catch (IllegalArgumentException e) {
            LOGGER.log(Level.FINE, "Ignoring storage file that is not named after a player: {0}", file.getName());
            return null;
        }
    }

    private static UUID requireId(UUID playerId) {
        if (playerId == null) throw new IllegalArgumentException("playerId must not be null");
        return playerId;
    }
}
