package com.zaryxstudios.okaso.common.storage;

public final class StorageKeys {

    public static final String ENABLED = "storage.enabled";
    public static final String MODE = "storage.mode";
    public static final String PLAYER_DATA_MODE = "storage.player-data-mode";
    public static final String FILE_NAME = "storage.file-name";
    public static final String KEY_SEPARATOR = "storage.key-separator";
    public static final String PRETTY_PRINT = "storage.pretty-print";
    public static final String AUTO_SAVE = "storage.auto-save";
    public static final String SAVE_INTERVAL_SECONDS = "storage.save-interval-seconds";

    public static final String[] ALL_KEYS = {
        ENABLED,
        MODE,
        PLAYER_DATA_MODE,
        FILE_NAME,
        KEY_SEPARATOR,
        PRETTY_PRINT,
        AUTO_SAVE,
        SAVE_INTERVAL_SECONDS
    };

    private StorageKeys() {
        throw new UnsupportedOperationException("Constants class");
    }
}
