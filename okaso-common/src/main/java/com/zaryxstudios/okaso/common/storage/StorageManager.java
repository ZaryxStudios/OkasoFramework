package com.zaryxstudios.okaso.common.storage;

import java.util.Optional;
import java.util.Set;

public interface StorageManager {

    StorageSettings getSettings();

    StorageProvider getFrameworkStorage();

    StorageProvider forPlugin(String pluginName);

    Optional<StorageProvider> findPlugin(String pluginName);

    PlayerStorageProvider forPlayerData(String pluginName);

    Set<String> getScopes();

    void flushAll();

    void close();
}
