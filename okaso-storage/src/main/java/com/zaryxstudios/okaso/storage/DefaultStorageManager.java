package com.zaryxstudios.okaso.storage;

import com.zaryxstudios.okaso.common.storage.PlayerDataMode;
import com.zaryxstudios.okaso.common.storage.PlayerStorageProvider;
import com.zaryxstudios.okaso.common.storage.StorageManager;
import com.zaryxstudios.okaso.common.storage.StorageMessages;
import com.zaryxstudios.okaso.common.storage.StorageMode;
import com.zaryxstudios.okaso.common.storage.StorageProvider;
import com.zaryxstudios.okaso.common.storage.StorageSettings;

import java.io.File;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.Level;
import java.util.logging.Logger;

public class DefaultStorageManager implements StorageManager {

    private static final Logger LOGGER = Logger.getLogger(DefaultStorageManager.class.getName());
    private static final String FRAMEWORK_SCOPE = "okaso";
    private static final String FILE_EXTENSION = ".json";
    private static final String PLAYERS_SUFFIX = "-players";

    private final File root;
    private final StorageSettings settings;
    private final Map<String, StorageProvider> scopes;
    private final Map<String, PlayerStorageProvider> playerScopes;
    private final Map<StorageProvider, Boolean> owned;
    private final AtomicBoolean closed;
    private final StorageProvider frameworkStorage;
    private final StorageProvider sharedStorage;
    private final StorageProvider sharedPlayerStorage;

    public DefaultStorageManager(File root, StorageSettings settings) {
        if (root == null) throw new IllegalArgumentException("root must not be null");
        this.root = root;
        this.settings = settings != null ? settings : StorageSettings.defaults();
        this.scopes = new ConcurrentHashMap<>();
        this.playerScopes = new ConcurrentHashMap<>();
        this.owned = new ConcurrentHashMap<>();
        this.closed = new AtomicBoolean(false);
        ensureDirectory(root);

        this.sharedStorage = settings.isEnabled() && settings.isSharedFile()
            ? createFileProvider(new File(root, settings.getFileName() + FILE_EXTENSION))
            : null;
        this.sharedPlayerStorage = settings.isEnabled() && settings.isSharedFile() && settings.isPlayerFileShared()
            ? createFileProvider(new File(root, settings.getFileName() + PLAYERS_SUFFIX + FILE_EXTENSION))
            : null;
        this.frameworkStorage = resolveFrameworkStorage();
    }

    public File getRoot() {
        return root;
    }

    public StorageMode getMode() {
        return settings.getMode();
    }

    public PlayerDataMode getPlayerDataMode() {
        return settings.getPlayerDataMode();
    }

    @Override
    public StorageSettings getSettings() {
        return settings;
    }

    @Override
    public StorageProvider getFrameworkStorage() {
        return frameworkStorage;
    }

    @Override
    public StorageProvider forPlugin(String pluginName) {
        String scope = requireScope(pluginName);
        StorageProvider existing = scopes.get(scope);
        if (existing != null) return existing;
        StorageProvider created = createScopeProvider(scope);
        StorageProvider raced = scopes.putIfAbsent(scope, created);
        if (raced != null) return raced;
        LOGGER.info(StorageMessages.get(StorageMessages.SCOPE_CREATED, scope, describe(created)));
        return created;
    }

    @Override
    public Optional<StorageProvider> findPlugin(String pluginName) {
        if (pluginName == null || pluginName.trim().isEmpty()) return Optional.empty();
        return Optional.ofNullable(scopes.get(sanitize(pluginName)));
    }

    @Override
    public PlayerStorageProvider forPlayerData(String pluginName) {
        String scope = requireScope(pluginName);
        PlayerStorageProvider existing = playerScopes.get(scope);
        if (existing != null) return existing;
        PlayerStorageProvider created = createPlayerScopeProvider(scope);
        PlayerStorageProvider raced = playerScopes.putIfAbsent(scope, created);
        if (raced != null) return raced;
        LOGGER.info(StorageMessages.get(StorageMessages.PLAYER_SCOPE_CREATED, scope, describe(created)));
        return created;
    }

    @Override
    public Set<String> getScopes() {
        return Collections.unmodifiableSet(new LinkedHashSet<>(scopes.keySet()));
    }

    @Override
    public void flushAll() {
        for (PlayerStorageProvider provider : playerScopes.values()) {
            provider.flush();
        }
        for (StorageProvider provider : owned.keySet()) {
            provider.flush();
        }
        LOGGER.fine(StorageMessages.get(StorageMessages.FLUSHED, owned.size()));
    }

    @Override
    public void close() {
        if (!closed.compareAndSet(false, true)) return;
        for (PlayerStorageProvider provider : playerScopes.values()) {
            provider.close();
        }
        playerScopes.clear();
        for (StorageProvider provider : owned.keySet()) {
            provider.close();
        }
        owned.clear();
        scopes.clear();
        LOGGER.info(StorageMessages.get(StorageMessages.CLOSED));
    }

    private StorageProvider resolveFrameworkStorage() {
        if (!settings.isEnabled()) return namespaced(createMemoryProvider(), FRAMEWORK_SCOPE);
        if (sharedStorage != null) return namespaced(sharedStorage, FRAMEWORK_SCOPE);
        return createFileProvider(new File(scopeDirectory(), FRAMEWORK_SCOPE + FILE_EXTENSION));
    }

    private StorageProvider createScopeProvider(String scope) {
        if (!settings.isEnabled()) {
            return namespaced(createMemoryProvider(), scope);
        }
        if (sharedStorage != null) {
            return namespaced(sharedStorage, scope);
        }
        return createFileProvider(new File(scopeDirectory(), scope + FILE_EXTENSION));
    }

    private PlayerStorageProvider createPlayerScopeProvider(String scope) {
        if (!settings.isEnabled()) {
            return settings.isPlayerFileShared()
                ? new UnifiedPlayerStorageProvider(namespaced(createMemoryProvider(), scope), settings.getKeySeparator())
                : new MemoryPlayerStorageProvider();
        }
        if (settings.isPlayerFileShared()) {
            StorageProvider container = sharedPlayerStorage != null
                ? namespaced(sharedPlayerStorage, scope)
                : createFileProvider(new File(playersDirectory(), scope + FILE_EXTENSION));
            return new UnifiedPlayerStorageProvider(container, settings.getKeySeparator());
        }
        return new IsolatedPlayerStorageProvider(new File(playersDirectory(), scope), settings);
    }

    private StorageProvider namespaced(StorageProvider provider, String scope) {
        return new NamespacedStorageProvider(provider, scope, settings.getKeySeparator());
    }

    private StorageProvider createMemoryProvider() {
        MemoryStorageProvider provider = new MemoryStorageProvider();
        owned.put(provider, Boolean.TRUE);
        return provider;
    }

    private StorageProvider createFileProvider(File file) {
        JsonFileStorageProvider provider = new JsonFileStorageProvider(file, settings);
        owned.put(provider, Boolean.TRUE);
        return provider;
    }

    private File scopeDirectory() {
        return new File(root, settings.getFileName());
    }

    private File playersDirectory() {
        return new File(root, settings.getFileName() + PLAYERS_SUFFIX);
    }

    private String requireScope(String pluginName) {
        String scope = sanitize(pluginName);
        if (scope.isEmpty()) {
            LOGGER.warning(StorageMessages.get(StorageMessages.INVALID_PLUGIN_NAME, String.valueOf(pluginName)));
            throw new IllegalArgumentException("pluginName must not be null or empty");
        }
        return scope;
    }

    private static String sanitize(String raw) {
        if (raw == null) return "";
        return StorageSettings.sanitize(raw.trim().toLowerCase(Locale.ROOT));
    }

    private static void ensureDirectory(File directory) {
        if (!directory.exists() && !directory.mkdirs() && !directory.exists()) {
            LOGGER.log(Level.SEVERE, "Failed to create storage directory: {0}", directory.getAbsolutePath());
        }
    }

    private static String describe(StorageProvider provider) {
        return provider.getScope();
    }

    private static String describe(PlayerStorageProvider provider) {
        return provider.getMode().getConfigValue();
    }
}
