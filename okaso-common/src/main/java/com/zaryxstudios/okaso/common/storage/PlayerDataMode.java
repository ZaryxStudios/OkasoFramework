package com.zaryxstudios.okaso.common.storage;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public enum PlayerDataMode {

    PER_PLAYER("per-player", "perplayer", "split", "individual", "isolated", "separate", "by-player"),
    UNIFIED("unified", "single", "shared", "all", "merged", "combined", "global", "one-file");

    private static final Map<String, PlayerDataMode> LOOKUP = new LinkedHashMap<>();

    static {
        for (PlayerDataMode mode : values()) {
            LOOKUP.put(mode.configValue, mode);
            for (String alias : mode.aliases) {
                LOOKUP.put(alias, mode);
            }
        }
    }

    private final String configValue;
    private final List<String> aliases;

    PlayerDataMode(String configValue, String... aliases) {
        this.configValue = configValue;
        this.aliases = Collections.unmodifiableList(new ArrayList<>(Arrays.asList(aliases)));
    }

    public String getConfigValue() {
        return configValue;
    }

    public List<String> getAliases() {
        return aliases;
    }

    public boolean isPerPlayer() {
        return this == PER_PLAYER;
    }

    public boolean isUnified() {
        return this == UNIFIED;
    }

    public static List<String> configValues() {
        List<String> values = new ArrayList<>();
        for (PlayerDataMode mode : values()) {
            values.add(mode.configValue);
        }
        return Collections.unmodifiableList(values);
    }

    public static PlayerDataMode parse(String raw) {
        return parse(raw, null);
    }

    public static PlayerDataMode parse(String raw, PlayerDataMode fallback) {
        String normalized = StorageMode.normalize(raw);
        if (normalized == null) return fallback;
        PlayerDataMode resolved = LOOKUP.get(normalized);
        return resolved != null ? resolved : fallback;
    }
}
