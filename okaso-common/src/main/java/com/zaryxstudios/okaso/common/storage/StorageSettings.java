package com.zaryxstudios.okaso.common.storage;

import com.zaryxstudios.okaso.common.config.OkasoConfigurationSection;

import java.util.Locale;

public final class StorageSettings {

    public static final String DEFAULT_FILE_NAME = "storage";
    public static final String DEFAULT_KEY_SEPARATOR = ".";
    public static final long DEFAULT_SAVE_INTERVAL_SECONDS = 30L;
    public static final long MIN_SAVE_INTERVAL_SECONDS = 0L;
    public static final long MAX_SAVE_INTERVAL_SECONDS = 86400L;

    private static final StorageSettings DEFAULTS = new StorageSettings(
        true,
        StorageMode.UNIFIED,
        PlayerDataMode.PER_PLAYER,
        DEFAULT_FILE_NAME,
        DEFAULT_KEY_SEPARATOR,
        true,
        true,
        DEFAULT_SAVE_INTERVAL_SECONDS
    );

    private final boolean enabled;
    private final StorageMode mode;
    private final PlayerDataMode playerDataMode;
    private final String fileName;
    private final String keySeparator;
    private final boolean prettyPrint;
    private final boolean autoSave;
    private final long saveIntervalSeconds;

    private StorageSettings(
        boolean enabled,
        StorageMode mode,
        PlayerDataMode playerDataMode,
        String fileName,
        String keySeparator,
        boolean prettyPrint,
        boolean autoSave,
        long saveIntervalSeconds
    ) {
        this.enabled = enabled;
        this.mode = mode;
        this.playerDataMode = playerDataMode;
        this.fileName = fileName;
        this.keySeparator = keySeparator;
        this.prettyPrint = prettyPrint;
        this.autoSave = autoSave;
        this.saveIntervalSeconds = saveIntervalSeconds;
    }

    public static StorageSettings defaults() {
        return DEFAULTS;
    }

    public static StorageSettings fromSection(OkasoConfigurationSection section) {
        return fromSection(section, DEFAULTS);
    }

    public static StorageSettings fromSection(OkasoConfigurationSection section, StorageSettings fallback) {
        StorageSettings base = fallback != null ? fallback : DEFAULTS;
        if (section == null) return base;
        return new StorageSettings(
            section.getBoolean(StorageKeys.ENABLED, base.enabled),
            StorageMode.parse(section.getString(StorageKeys.MODE), base.mode),
            PlayerDataMode.parse(section.getString(StorageKeys.PLAYER_DATA_MODE), base.playerDataMode),
            normalizeFileName(section.getString(StorageKeys.FILE_NAME, base.fileName)),
            normalizeSeparator(section.getString(StorageKeys.KEY_SEPARATOR, base.keySeparator)),
            section.getBoolean(StorageKeys.PRETTY_PRINT, base.prettyPrint),
            section.getBoolean(StorageKeys.AUTO_SAVE, base.autoSave),
            clampInterval(section.getLong(StorageKeys.SAVE_INTERVAL_SECONDS, base.saveIntervalSeconds))
        );
    }

    public void writeToSection(OkasoConfigurationSection section) {
        if (section == null) return;
        section.set(StorageKeys.ENABLED, enabled);
        section.set(StorageKeys.MODE, mode.getConfigValue());
        section.set(StorageKeys.PLAYER_DATA_MODE, playerDataMode.getConfigValue());
        section.set(StorageKeys.FILE_NAME, fileName);
        section.set(StorageKeys.KEY_SEPARATOR, keySeparator);
        section.set(StorageKeys.PRETTY_PRINT, prettyPrint);
        section.set(StorageKeys.AUTO_SAVE, autoSave);
        section.set(StorageKeys.SAVE_INTERVAL_SECONDS, saveIntervalSeconds);
    }

    public boolean isEnabled() {
        return enabled;
    }

    public StorageMode getMode() {
        return mode;
    }

    public PlayerDataMode getPlayerDataMode() {
        return playerDataMode;
    }

    public String getFileName() {
        return fileName;
    }

    public String getKeySeparator() {
        return keySeparator;
    }

    public boolean isPrettyPrint() {
        return prettyPrint;
    }

    public boolean isAutoSave() {
        return autoSave;
    }

    public long getSaveIntervalSeconds() {
        return saveIntervalSeconds;
    }

    public long getSaveIntervalMillis() {
        return saveIntervalSeconds * 1000L;
    }

    public boolean isPersistent() {
        return enabled;
    }

    public boolean isSharedFile() {
        return mode.isUnified();
    }

    public boolean isPlayerFileShared() {
        return playerDataMode.isUnified();
    }

    public StorageSettings withEnabled(boolean newEnabled) {
        return new StorageSettings(newEnabled, mode, playerDataMode, fileName, keySeparator,
            prettyPrint, autoSave, saveIntervalSeconds);
    }

    public StorageSettings withMode(StorageMode newMode) {
        return new StorageSettings(enabled, newMode != null ? newMode : mode, playerDataMode, fileName,
            keySeparator, prettyPrint, autoSave, saveIntervalSeconds);
    }

    public StorageSettings withPlayerDataMode(PlayerDataMode newPlayerDataMode) {
        return new StorageSettings(enabled, mode, newPlayerDataMode != null ? newPlayerDataMode : playerDataMode,
            fileName, keySeparator, prettyPrint, autoSave, saveIntervalSeconds);
    }

    public StorageSettings withFileName(String newFileName) {
        return new StorageSettings(enabled, mode, playerDataMode, normalizeFileName(newFileName), keySeparator,
            prettyPrint, autoSave, saveIntervalSeconds);
    }

    public StorageSettings withKeySeparator(String newKeySeparator) {
        return new StorageSettings(enabled, mode, playerDataMode, fileName, normalizeSeparator(newKeySeparator),
            prettyPrint, autoSave, saveIntervalSeconds);
    }

    public StorageSettings withPrettyPrint(boolean newPrettyPrint) {
        return new StorageSettings(enabled, mode, playerDataMode, fileName, keySeparator, newPrettyPrint,
            autoSave, saveIntervalSeconds);
    }

    public StorageSettings withAutoSave(boolean newAutoSave) {
        return new StorageSettings(enabled, mode, playerDataMode, fileName, keySeparator, prettyPrint,
            newAutoSave, saveIntervalSeconds);
    }

    public StorageSettings withSaveIntervalSeconds(long newSaveIntervalSeconds) {
        return new StorageSettings(enabled, mode, playerDataMode, fileName, keySeparator, prettyPrint,
            autoSave, clampInterval(newSaveIntervalSeconds));
    }

    public static String normalizeFileName(String raw) {
        if (raw == null) return DEFAULT_FILE_NAME;
        String trimmed = raw.trim();
        if (trimmed.isEmpty()) return DEFAULT_FILE_NAME;
        String withoutExtension = trimmed.toLowerCase(Locale.ROOT).endsWith(".json")
            ? trimmed.substring(0, trimmed.length() - 5)
            : trimmed;
        String sanitized = sanitize(withoutExtension);
        return sanitized.isEmpty() ? DEFAULT_FILE_NAME : sanitized;
    }

    static String normalizeSeparator(String raw) {
        if (raw == null) return DEFAULT_KEY_SEPARATOR;
        char candidate = raw.trim().isEmpty() ? '\0' : raw.trim().charAt(0);
        switch (candidate) {
            case '.':
            case '-':
            case '_':
            case '/':
            case ':':
                return String.valueOf(candidate);
            default:
                return DEFAULT_KEY_SEPARATOR;
        }
    }

    public static String sanitize(String raw) {
        if (raw == null) return "";
        StringBuilder builder = new StringBuilder(raw.length());
        for (int i = 0; i < raw.length(); i++) {
            char current = raw.charAt(i);
            boolean allowed = (current >= 'a' && current <= 'z')
                || (current >= 'A' && current <= 'Z')
                || (current >= '0' && current <= '9')
                || current == '-' || current == '_' || current == '.';
            builder.append(allowed ? current : '_');
        }
        return builder.toString();
    }

    private static long clampInterval(long seconds) {
        if (seconds < MIN_SAVE_INTERVAL_SECONDS) return MIN_SAVE_INTERVAL_SECONDS;
        if (seconds > MAX_SAVE_INTERVAL_SECONDS) return MAX_SAVE_INTERVAL_SECONDS;
        return seconds;
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) return true;
        if (!(other instanceof StorageSettings)) return false;
        StorageSettings that = (StorageSettings) other;
        return enabled == that.enabled
            && prettyPrint == that.prettyPrint
            && autoSave == that.autoSave
            && saveIntervalSeconds == that.saveIntervalSeconds
            && mode == that.mode
            && playerDataMode == that.playerDataMode
            && fileName.equals(that.fileName)
            && keySeparator.equals(that.keySeparator);
    }

    @Override
    public int hashCode() {
        int result = Boolean.valueOf(enabled).hashCode();
        result = 31 * result + mode.hashCode();
        result = 31 * result + playerDataMode.hashCode();
        result = 31 * result + fileName.hashCode();
        result = 31 * result + keySeparator.hashCode();
        result = 31 * result + Boolean.valueOf(prettyPrint).hashCode();
        result = 31 * result + Boolean.valueOf(autoSave).hashCode();
        result = 31 * result + (int) (saveIntervalSeconds ^ (saveIntervalSeconds >>> 32));
        return result;
    }

    @Override
    public String toString() {
        return "StorageSettings{enabled=" + enabled
            + ", mode=" + mode.getConfigValue()
            + ", playerDataMode=" + playerDataMode.getConfigValue()
            + ", fileName='" + fileName + '\''
            + ", keySeparator='" + keySeparator + '\''
            + ", prettyPrint=" + prettyPrint
            + ", autoSave=" + autoSave
            + ", saveIntervalSeconds=" + saveIntervalSeconds
            + '}';
    }
}
