package com.zaryxstudios.okaso.common.storage;

import com.zaryxstudios.okaso.common.config.OkasoConfigurationProvider;
import com.zaryxstudios.okaso.common.config.OkasoConfigurationSection;
import com.zaryxstudios.okaso.common.message.DefaultMessageProvider;
import com.zaryxstudios.okaso.common.message.MessageProvider;

import java.io.File;
import java.util.Map;

public final class StorageMessages {

    public static final String BOOTSTRAPPED = "log.storage.bootstrapped";
    public static final String ENABLED = "log.storage.enabled";
    public static final String DISABLED = "log.storage.disabled";
    public static final String CONFIG_CREATED = "log.storage.config-created";
    public static final String MODE_RESOLVED = "log.storage.mode-resolved";
    public static final String PLAYER_MODE_RESOLVED = "log.storage.player-mode-resolved";
    public static final String INVALID_MODE = "log.storage.invalid-mode";
    public static final String INVALID_PLAYER_MODE = "log.storage.invalid-player-mode";
    public static final String SCOPE_CREATED = "log.storage.scope-created";
    public static final String PLAYER_SCOPE_CREATED = "log.storage.player-scope-created";
    public static final String LOAD_FAILED = "log.storage.load-failed";
    public static final String SAVE_FAILED = "log.storage.save-failed";
    public static final String FLUSHED = "log.storage.flushed";
    public static final String CLOSED = "log.storage.closed";
    public static final String INVALID_PLUGIN_NAME = "log.storage.invalid-plugin-name";
    public static final String INVALID_KEY = "log.storage.invalid-key";

    public static final String DEFAULT_BOOTSTRAPPED = "Okaso storage initialised in {0} (mode: {1}, player data: {2}).";
    public static final String DEFAULT_ENABLED = "Storage is enabled. Changes are persisted to disk.";
    public static final String DEFAULT_DISABLED = "Storage is disabled. All data is kept in memory only.";
    public static final String DEFAULT_CONFIG_CREATED = "Created default storage configuration: {0}";
    public static final String DEFAULT_MODE_RESOLVED = "Storage mode: {0} ({1})";
    public static final String DEFAULT_PLAYER_MODE_RESOLVED = "Player data mode: {0} ({1})";
    public static final String DEFAULT_INVALID_MODE = "Unknown storage mode '{0}', falling back to '{1}'.";
    public static final String DEFAULT_INVALID_PLAYER_MODE = "Unknown player data mode '{0}', falling back to '{1}'.";
    public static final String DEFAULT_SCOPE_CREATED = "Storage scope ready: {0} -> {1}";
    public static final String DEFAULT_PLAYER_SCOPE_CREATED = "Player storage ready: {0} -> {1}";
    public static final String DEFAULT_LOAD_FAILED = "Failed to load storage file {0}: {1}";
    public static final String DEFAULT_SAVE_FAILED = "Failed to save storage file {0}: {1}";
    public static final String DEFAULT_FLUSHED = "Flushed {0} storage scope(s).";
    public static final String DEFAULT_CLOSED = "Storage closed.";
    public static final String DEFAULT_INVALID_PLUGIN_NAME = "Invalid storage scope name: {0}";
    public static final String DEFAULT_INVALID_KEY = "Storage key must not be null or empty.";

    private static final String[] ALL_KEYS = {
        BOOTSTRAPPED, ENABLED, DISABLED, CONFIG_CREATED, MODE_RESOLVED, PLAYER_MODE_RESOLVED,
        INVALID_MODE, INVALID_PLAYER_MODE, SCOPE_CREATED, PLAYER_SCOPE_CREATED, LOAD_FAILED,
        SAVE_FAILED, FLUSHED, CLOSED, INVALID_PLUGIN_NAME, INVALID_KEY
    };

    private static volatile MessageProvider provider;

    static {
        reset();
    }

    private StorageMessages() {
        throw new UnsupportedOperationException("Constants class");
    }

    public static void reset() {
        DefaultMessageProvider defaults = new DefaultMessageProvider();
        applyDefaults(defaults);
        provider = defaults;
    }

    public static MessageProvider getProvider() {
        return provider();
    }

    public static void setProvider(MessageProvider newProvider) {
        if (newProvider == null) {
            reset();
            return;
        }
        provider = newProvider;
    }

    public static String get(String key) {
        return provider().get(key);
    }

    public static String get(String key, Object... args) {
        return provider().format(key, args);
    }

    public static String get(String key, Map<String, ?> placeholders, Object... args) {
        return provider().format(key, placeholders, args);
    }

    public static void set(String key, String value) {
        if (key == null || key.isEmpty()) return;
        provider().set(key, value);
    }

    public static void loadFromSection(OkasoConfigurationSection section) {
        if (section == null) return;
        for (String key : ALL_KEYS) {
            if (section.contains(key)) {
                String value = section.getString(key);
                if (value != null && !value.isEmpty()) {
                    set(key, value);
                }
            }
        }
    }

    public static void loadFromFile(File file, OkasoConfigurationProvider configProvider) {
        if (file == null || !file.exists() || configProvider == null) return;
        loadFromSection(configProvider.load(file));
    }

    public static void saveToSection(OkasoConfigurationSection section) {
        if (section == null) return;
        for (String key : ALL_KEYS) {
            section.set(key, get(key));
        }
    }

    private static void applyDefaults(MessageProvider target) {
        target.set(BOOTSTRAPPED, DEFAULT_BOOTSTRAPPED);
        target.set(ENABLED, DEFAULT_ENABLED);
        target.set(DISABLED, DEFAULT_DISABLED);
        target.set(CONFIG_CREATED, DEFAULT_CONFIG_CREATED);
        target.set(MODE_RESOLVED, DEFAULT_MODE_RESOLVED);
        target.set(PLAYER_MODE_RESOLVED, DEFAULT_PLAYER_MODE_RESOLVED);
        target.set(INVALID_MODE, DEFAULT_INVALID_MODE);
        target.set(INVALID_PLAYER_MODE, DEFAULT_INVALID_PLAYER_MODE);
        target.set(SCOPE_CREATED, DEFAULT_SCOPE_CREATED);
        target.set(PLAYER_SCOPE_CREATED, DEFAULT_PLAYER_SCOPE_CREATED);
        target.set(LOAD_FAILED, DEFAULT_LOAD_FAILED);
        target.set(SAVE_FAILED, DEFAULT_SAVE_FAILED);
        target.set(FLUSHED, DEFAULT_FLUSHED);
        target.set(CLOSED, DEFAULT_CLOSED);
        target.set(INVALID_PLUGIN_NAME, DEFAULT_INVALID_PLUGIN_NAME);
        target.set(INVALID_KEY, DEFAULT_INVALID_KEY);
    }

    private static MessageProvider provider() {
        MessageProvider current = provider;
        if (current == null) {
            synchronized (StorageMessages.class) {
                current = provider;
                if (current == null) {
                    DefaultMessageProvider defaults = new DefaultMessageProvider();
                    applyDefaults(defaults);
                    provider = defaults;
                    current = defaults;
                }
            }
        }
        return current;
    }
}
