package com.zaryxstudios.okaso.common.storage;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public enum StorageMode {

    UNIFIED("unified", "global", "shared", "single", "all", "merged", "combined"),
    PER_PLUGIN("per-plugin", "perplugin", "plugin", "isolated", "separate", "individual", "split");

    private static final Map<String, StorageMode> LOOKUP = new LinkedHashMap<>();

    static {
        for (StorageMode mode : values()) {
            LOOKUP.put(mode.configValue, mode);
            for (String alias : mode.aliases) {
                LOOKUP.put(alias, mode);
            }
        }
    }

    private final String configValue;
    private final List<String> aliases;

    StorageMode(String configValue, String... aliases) {
        this.configValue = configValue;
        this.aliases = Collections.unmodifiableList(new ArrayList<>(Arrays.asList(aliases)));
    }

    public String getConfigValue() {
        return configValue;
    }

    public List<String> getAliases() {
        return aliases;
    }

    public boolean isUnified() {
        return this == UNIFIED;
    }

    public boolean isPerPlugin() {
        return this == PER_PLUGIN;
    }

    public static List<String> configValues() {
        List<String> values = new ArrayList<>();
        for (StorageMode mode : values()) {
            values.add(mode.configValue);
        }
        return Collections.unmodifiableList(values);
    }

    public static StorageMode parse(String raw) {
        return parse(raw, null);
    }

    public static StorageMode parse(String raw, StorageMode fallback) {
        String normalized = normalize(raw);
        if (normalized == null) return fallback;
        StorageMode resolved = LOOKUP.get(normalized);
        return resolved != null ? resolved : fallback;
    }

    static String normalize(String raw) {
        if (raw == null) return null;
        String normalized = raw.trim().toLowerCase(Locale.ROOT).replace('_', '-').replace(' ', '-');
        return normalized.isEmpty() ? null : normalized;
    }
}
