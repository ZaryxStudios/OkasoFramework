package com.zaryxstudios.okaso.storage;

import com.zaryxstudios.okaso.common.OkasoAPI;
import com.zaryxstudios.okaso.common.config.OkasoConfigurationProvider;
import com.zaryxstudios.okaso.common.config.OkasoConfigurationSection;
import com.zaryxstudios.okaso.common.service.ServiceRegistry;
import com.zaryxstudios.okaso.common.storage.PlayerDataMode;
import com.zaryxstudios.okaso.common.storage.StorageKeys;
import com.zaryxstudios.okaso.common.storage.StorageManager;
import com.zaryxstudios.okaso.common.storage.StorageMessages;
import com.zaryxstudios.okaso.common.storage.StorageMode;
import com.zaryxstudios.okaso.common.storage.StorageProvider;
import com.zaryxstudios.okaso.common.storage.StorageSettings;

import java.io.File;
import java.util.logging.Logger;

public final class OkasoStorageBootstrap {

    public static final String CONFIG_FILE_NAME = "config.yml";

    private OkasoStorageBootstrap() {
        throw new UnsupportedOperationException("Constants class");
    }

    public static StorageManager bootstrap(File dataFolder, OkasoConfigurationProvider configProvider, Logger logger) {
        StorageManager manager = create(dataFolder, configProvider, logger);
        register(manager);
        return manager;
    }

    public static StorageManager create(File dataFolder, OkasoConfigurationProvider configProvider, Logger logger) {
        if (dataFolder == null) throw new IllegalArgumentException("dataFolder must not be null");
        if (logger == null) logger = Logger.getLogger(OkasoStorageBootstrap.class.getName());
        ensureDirectory(dataFolder, logger);

        OkasoConfigurationSection section = configProvider != null
            ? configProvider.load(new File(dataFolder, CONFIG_FILE_NAME))
            : null;

        if (configProvider != null && !new File(dataFolder, CONFIG_FILE_NAME).exists()) {
            StorageSettings.defaults().writeToSection(section);
            StorageMessages.saveToSection(section);
            configProvider.save(section, new File(dataFolder, CONFIG_FILE_NAME));
            logger.info(StorageMessages.get(StorageMessages.CONFIG_CREATED,
                new File(dataFolder, CONFIG_FILE_NAME).getAbsolutePath()));
        } else {
            StorageMessages.loadFromSection(section);
        }

        StorageSettings settings = StorageSettings.fromSection(section);
        warnIfUnresolved(section, StorageKeys.MODE, StorageMessages.INVALID_MODE,
            settings.getMode().getConfigValue(), logger);
        warnIfUnresolved(section, StorageKeys.PLAYER_DATA_MODE, StorageMessages.INVALID_PLAYER_MODE,
            settings.getPlayerDataMode().getConfigValue(), logger);

        logger.info(StorageMessages.get(StorageMessages.MODE_RESOLVED,
            settings.getMode().getConfigValue(), describe(settings.getMode())));
        logger.info(StorageMessages.get(StorageMessages.PLAYER_MODE_RESOLVED,
            settings.getPlayerDataMode().getConfigValue(), describe(settings.getPlayerDataMode())));
        logger.info(settings.isEnabled()
            ? StorageMessages.get(StorageMessages.ENABLED)
            : StorageMessages.get(StorageMessages.DISABLED));
        logger.info(StorageMessages.get(StorageMessages.BOOTSTRAPPED,
            dataFolder.getAbsolutePath(),
            settings.getMode().getConfigValue(),
            settings.getPlayerDataMode().getConfigValue()));

        return new DefaultStorageManager(dataFolder, settings);
    }

    public static void register(StorageManager manager) {
        if (manager == null) return;
        ServiceRegistry registry = OkasoAPI.getInstance().getServiceRegistry();
        registry.registerOrReplace(StorageManager.class, manager);
        registry.registerOrReplace(StorageProvider.class, manager.getFrameworkStorage());
    }

    public static StorageSettings loadSettings(File dataFolder, OkasoConfigurationProvider configProvider) {
        if (dataFolder == null || configProvider == null) return StorageSettings.defaults();
        return StorageSettings.fromSection(configProvider.load(new File(dataFolder, CONFIG_FILE_NAME)));
    }

    private static void warnIfUnresolved(OkasoConfigurationSection section, String key, String messageKey,
                                          String resolved, Logger logger) {
        if (section == null) return;
        String raw = section.getString(key);
        if (raw == null || raw.trim().isEmpty()) return;
        if (raw.trim().equalsIgnoreCase(resolved)) return;
        logger.warning(StorageMessages.get(messageKey, raw, resolved));
    }

    private static void ensureDirectory(File directory, Logger logger) {
        if (!directory.exists() && !directory.mkdirs() && !directory.exists()) {
            logger.warning(StorageMessages.get(StorageMessages.SAVE_FAILED,
                directory.getAbsolutePath(), "directory could not be created"));
        }
    }

    private static String describe(StorageMode mode) {
        return mode.isUnified() ? "one file shared by every plugin" : "one file per plugin";
    }

    private static String describe(PlayerDataMode mode) {
        return mode.isUnified() ? "every player in a single file" : "one file per player";
    }
}
