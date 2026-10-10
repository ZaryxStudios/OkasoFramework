package com.zaryxstudios.okaso.velocity;

import com.google.inject.Inject;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.proxy.ProxyInitializeEvent;
import com.velocitypowered.api.event.proxy.ProxyShutdownEvent;
import com.velocitypowered.api.plugin.Plugin;
import com.velocitypowered.api.plugin.annotation.DataDirectory;
import com.velocitypowered.api.proxy.ProxyServer;

import com.zaryxstudios.okaso.common.OkasoAPI;
import com.zaryxstudios.okaso.common.config.OkasoConfigurationProvider;
import com.zaryxstudios.okaso.common.message.LogMessages;
import com.zaryxstudios.okaso.common.plugin.OkasoPlugin;
import com.zaryxstudios.okaso.common.service.ServiceRegistry;
import com.zaryxstudios.okaso.common.storage.StorageManager;
import com.zaryxstudios.okaso.config.DefaultConfigurationProvider;
import com.zaryxstudios.okaso.storage.OkasoStorageBootstrap;

import org.slf4j.Logger;

import java.nio.file.Path;

import lombok.Getter;

@Plugin(
    id = "okaso-velocity",
    name = "Okaso",
    version = OkasoVelocityPlugin.VERSION,
    description = "Okaso Framework — Velocity Adapter",
    authors = {"ZaryxStudios"}
)
public final class OkasoVelocityPlugin implements OkasoPlugin {

    public static final String VERSION = "1.8.0";

    @Getter
    private final ProxyServer server;
    private final Logger slf4jLogger;
    @Getter
    private final Path dataDirectory;

    private OkasoAPI api;
    private java.util.logging.Logger julLogger;
    private StorageManager storageManager;

    @Inject
    public OkasoVelocityPlugin(ProxyServer server, Logger logger, @DataDirectory Path dataDirectory) {
        this.server = server;
        this.slf4jLogger = logger;
        this.dataDirectory = dataDirectory;
    }

    @Subscribe
    public void onProxyInitialization(ProxyInitializeEvent event) {
        this.julLogger = java.util.logging.Logger.getLogger("Okaso");
        api = OkasoAPI.init(this);
        registerServices();
        onOkasoEnable();
        slf4jLogger.info(LogMessages.get(LogMessages.ADAPTER_ENABLED));
    }

    @Subscribe
    public void onProxyShutdown(ProxyShutdownEvent event) {
        onOkasoDisable();
        if (storageManager != null) {
            storageManager.close();
        }
        if (api != null) {
            api.getServiceRegistry().getAll().clear();
        }
        slf4jLogger.info(LogMessages.get(LogMessages.ADAPTER_DISABLED));
    }

    private void registerServices() {
        ServiceRegistry reg = api.getServiceRegistry();
        reg.register(OkasoConfigurationProvider.class, new DefaultConfigurationProvider());
        storageManager = OkasoStorageBootstrap.bootstrap(
            dataDirectory.toFile(), reg.get(OkasoConfigurationProvider.class), getOkasoLogger());
    }

    @Override
    public String getName() {
        return "Okaso";
    }

    @Override
    public String getVersion() {
        return VERSION;
    }

    @Override
    public void onOkasoEnable() {
    }

    @Override
    public void onOkasoDisable() {
    }

    @Override
    public java.util.logging.Logger getOkasoLogger() {
        return julLogger;
    }

}
