package com.zaryxstudios.okaso.waterfall;

import com.zaryxstudios.okaso.common.OkasoAPI;
import com.zaryxstudios.okaso.common.config.OkasoConfigurationProvider;
import com.zaryxstudios.okaso.common.plugin.OkasoPlugin;
import com.zaryxstudios.okaso.common.service.ServiceRegistry;
import com.zaryxstudios.okaso.common.storage.StorageManager;
import com.zaryxstudios.okaso.common.task.TaskScheduler;
import com.zaryxstudios.okaso.config.DefaultConfigurationProvider;
import com.zaryxstudios.okaso.storage.OkasoStorageBootstrap;
import com.zaryxstudios.okaso.waterfall.task.OkasoWaterfallTaskScheduler;

import net.md_5.bungee.api.plugin.Plugin;

import java.io.File;
import java.util.logging.Logger;

public final class OkasoWaterfallPlugin extends Plugin implements OkasoPlugin {

    private OkasoAPI api;
    private StorageManager storageManager;

    @Override
    public void onEnable() {
        api = OkasoAPI.init(this);
        registerServices();
        onOkasoEnable();
        getLogger().info("Okaso Waterfall adapter enabled.");
    }

    private void registerServices() {
        ServiceRegistry reg = api.getServiceRegistry();
        reg.register(TaskScheduler.class, new OkasoWaterfallTaskScheduler(this));
        reg.register(OkasoConfigurationProvider.class, new DefaultConfigurationProvider());
        storageManager = OkasoStorageBootstrap.bootstrap(
            getDataFolder(), reg.get(OkasoConfigurationProvider.class), getOkasoLogger());
    }

    @Override
    public void onDisable() {
        onOkasoDisable();
        if (storageManager != null) {
            storageManager.close();
        }
        if (api != null) {
            api.getServiceRegistry().getAll().clear();
        }
        getLogger().info("Okaso Waterfall adapter disabled.");
    }

    @Override
    public String getName() {
        return super.getDescription().getName();
    }

    @Override
    public String getVersion() {
        return super.getDescription().getVersion();
    }

    @Override
    public void onOkasoEnable() {
    }

    @Override
    public void onOkasoDisable() {
    }

    @Override
    public Logger getOkasoLogger() {
        return getLogger();
    }
}
