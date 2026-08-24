package com.zaryxstudios.okaso.common.message;

import com.zaryxstudios.okaso.common.config.OkasoConfigurationProvider;
import com.zaryxstudios.okaso.common.config.OkasoConfigurationSection;

import java.io.File;
import java.util.Map;

public final class LogMessages {

    public static final String MODULE_REGISTERED = "log.module.registered";
    public static final String MODULE_UNREGISTERED = "log.module.unregistered";
    public static final String MODULE_ENABLE_CYCLE = "log.module.enable-cycle";
    public static final String MODULE_DISABLE_CYCLE = "log.module.disable-cycle";
    public static final String MODULE_ENABLED = "log.module.enabled";
    public static final String MODULE_ENABLE_FAILED = "log.module.enable-failed";
    public static final String MODULE_DISABLED = "log.module.disabled";
    public static final String MODULE_DISABLE_FAILED = "log.module.disable-failed";

    public static final String EVENTS_BRIDGED = "log.events.bridged";

    public static final String ADAPTER_ENABLED = "log.adapter.enabled";
    public static final String ADAPTER_DISABLED = "log.adapter.disabled";

    public static final String STRUCTURE_SAVE_INVALID_WORLD = "log.structure.save-invalid-world";
    public static final String STRUCTURE_NOT_FOUND = "log.structure.not-found";
    public static final String STRUCTURE_PLACE_INVALID_LOCATION = "log.structure.place-invalid-location";
    public static final String STRUCTURE_PLACE_FAILED = "log.structure.place-failed";
    public static final String STRUCTURE_EXPORTED = "log.structure.exported";
    public static final String STRUCTURE_EXPORT_FAILED = "log.structure.export-failed";
    public static final String STRUCTURE_IMPORT_NOT_FOUND = "log.structure.import-not-found";
    public static final String STRUCTURE_SAVED = "log.structure.saved";
    public static final String STRUCTURE_SAVE_FAILED = "log.structure.save-failed";
    public static final String STRUCTURE_INVALID_HEADER = "log.structure.invalid-header";
    public static final String STRUCTURE_TRUNCATED = "log.structure.truncated";
    public static final String STRUCTURE_UNKNOWN_MATERIAL = "log.structure.unknown-material";
    public static final String STRUCTURE_LOADED = "log.structure.loaded";
    public static final String STRUCTURE_LOAD_FAILED = "log.structure.load-failed";
    public static final String STRUCTURE_UNKNOWN_FALLBACK = "log.structure.unknown-fallback";

    public static final String DEFAULT_MODULE_REGISTERED = "Registered module: {0}";
    public static final String DEFAULT_MODULE_UNREGISTERED = "Unregistered module: {0}";
    public static final String DEFAULT_MODULE_ENABLE_CYCLE = "Cannot enable modules: {0}";
    public static final String DEFAULT_MODULE_DISABLE_CYCLE = "Cannot disable modules: {0}";
    public static final String DEFAULT_MODULE_ENABLED = "Enabled module: {0}";
    public static final String DEFAULT_MODULE_ENABLE_FAILED = "Failed to enable module {0}: {1}";
    public static final String DEFAULT_MODULE_DISABLED = "Disabled module: {0}";
    public static final String DEFAULT_MODULE_DISABLE_FAILED = "Error disabling module {0}: {1}";

    public static final String DEFAULT_EVENTS_BRIDGED = "[Okaso] Bridged {0} Bukkit events into the Okaso EventBus.";

    public static final String DEFAULT_ADAPTER_ENABLED = "Okaso adapter enabled.";
    public static final String DEFAULT_ADAPTER_DISABLED = "Okaso adapter disabled.";

    public static final String DEFAULT_STRUCTURE_SAVE_INVALID_WORLD = "saveStructure: world must be a Bukkit World";
    public static final String DEFAULT_STRUCTURE_NOT_FOUND = "Structure not found: {0}";
    public static final String DEFAULT_STRUCTURE_PLACE_INVALID_LOCATION = "placeStructure: location must be a Bukkit Location";
    public static final String DEFAULT_STRUCTURE_PLACE_FAILED = "Failed to place structure: {0}";
    public static final String DEFAULT_STRUCTURE_EXPORTED = "Exported structure: {0} -> {1}";
    public static final String DEFAULT_STRUCTURE_EXPORT_FAILED = "Failed to export structure: {0}";
    public static final String DEFAULT_STRUCTURE_IMPORT_NOT_FOUND = "Import file not found: {0}";
    public static final String DEFAULT_STRUCTURE_SAVED = "Saved structure: {0} ({1} blocks)";
    public static final String DEFAULT_STRUCTURE_SAVE_FAILED = "Failed to save structure: {0}";
    public static final String DEFAULT_STRUCTURE_INVALID_HEADER = "Invalid structure file header: {0}";
    public static final String DEFAULT_STRUCTURE_TRUNCATED = "Truncated structure file: {0}";
    public static final String DEFAULT_STRUCTURE_UNKNOWN_MATERIAL = "Unknown material in structure file: {0}";
    public static final String DEFAULT_STRUCTURE_LOADED = "Loaded structure: {0} ({1} blocks)";
    public static final String DEFAULT_STRUCTURE_LOAD_FAILED = "Failed to load structure: {0}";
    public static final String DEFAULT_STRUCTURE_UNKNOWN_FALLBACK = "Unknown material '{0}', falling back to STONE";

    private static final String[] ALL_KEYS = {
        MODULE_REGISTERED, MODULE_UNREGISTERED, MODULE_ENABLE_CYCLE, MODULE_DISABLE_CYCLE,
        MODULE_ENABLED, MODULE_ENABLE_FAILED, MODULE_DISABLED, MODULE_DISABLE_FAILED,
        EVENTS_BRIDGED, ADAPTER_ENABLED, ADAPTER_DISABLED,
        STRUCTURE_SAVE_INVALID_WORLD, STRUCTURE_NOT_FOUND, STRUCTURE_PLACE_INVALID_LOCATION,
        STRUCTURE_PLACE_FAILED, STRUCTURE_EXPORTED, STRUCTURE_EXPORT_FAILED,
        STRUCTURE_IMPORT_NOT_FOUND, STRUCTURE_SAVED, STRUCTURE_SAVE_FAILED,
        STRUCTURE_INVALID_HEADER, STRUCTURE_TRUNCATED, STRUCTURE_UNKNOWN_MATERIAL,
        STRUCTURE_LOADED, STRUCTURE_LOAD_FAILED, STRUCTURE_UNKNOWN_FALLBACK
    };

    private static volatile MessageProvider provider;

    static {
        reset();
    }

    private LogMessages() {
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
        target.set(MODULE_REGISTERED, DEFAULT_MODULE_REGISTERED);
        target.set(MODULE_UNREGISTERED, DEFAULT_MODULE_UNREGISTERED);
        target.set(MODULE_ENABLE_CYCLE, DEFAULT_MODULE_ENABLE_CYCLE);
        target.set(MODULE_DISABLE_CYCLE, DEFAULT_MODULE_DISABLE_CYCLE);
        target.set(MODULE_ENABLED, DEFAULT_MODULE_ENABLED);
        target.set(MODULE_ENABLE_FAILED, DEFAULT_MODULE_ENABLE_FAILED);
        target.set(MODULE_DISABLED, DEFAULT_MODULE_DISABLED);
        target.set(MODULE_DISABLE_FAILED, DEFAULT_MODULE_DISABLE_FAILED);
        target.set(EVENTS_BRIDGED, DEFAULT_EVENTS_BRIDGED);
        target.set(ADAPTER_ENABLED, DEFAULT_ADAPTER_ENABLED);
        target.set(ADAPTER_DISABLED, DEFAULT_ADAPTER_DISABLED);
        target.set(STRUCTURE_SAVE_INVALID_WORLD, DEFAULT_STRUCTURE_SAVE_INVALID_WORLD);
        target.set(STRUCTURE_NOT_FOUND, DEFAULT_STRUCTURE_NOT_FOUND);
        target.set(STRUCTURE_PLACE_INVALID_LOCATION, DEFAULT_STRUCTURE_PLACE_INVALID_LOCATION);
        target.set(STRUCTURE_PLACE_FAILED, DEFAULT_STRUCTURE_PLACE_FAILED);
        target.set(STRUCTURE_EXPORTED, DEFAULT_STRUCTURE_EXPORTED);
        target.set(STRUCTURE_EXPORT_FAILED, DEFAULT_STRUCTURE_EXPORT_FAILED);
        target.set(STRUCTURE_IMPORT_NOT_FOUND, DEFAULT_STRUCTURE_IMPORT_NOT_FOUND);
        target.set(STRUCTURE_SAVED, DEFAULT_STRUCTURE_SAVED);
        target.set(STRUCTURE_SAVE_FAILED, DEFAULT_STRUCTURE_SAVE_FAILED);
        target.set(STRUCTURE_INVALID_HEADER, DEFAULT_STRUCTURE_INVALID_HEADER);
        target.set(STRUCTURE_TRUNCATED, DEFAULT_STRUCTURE_TRUNCATED);
        target.set(STRUCTURE_UNKNOWN_MATERIAL, DEFAULT_STRUCTURE_UNKNOWN_MATERIAL);
        target.set(STRUCTURE_LOADED, DEFAULT_STRUCTURE_LOADED);
        target.set(STRUCTURE_LOAD_FAILED, DEFAULT_STRUCTURE_LOAD_FAILED);
        target.set(STRUCTURE_UNKNOWN_FALLBACK, DEFAULT_STRUCTURE_UNKNOWN_FALLBACK);
    }

    private static MessageProvider provider() {
        MessageProvider current = provider;
        if (current == null) {
            synchronized (LogMessages.class) {
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
