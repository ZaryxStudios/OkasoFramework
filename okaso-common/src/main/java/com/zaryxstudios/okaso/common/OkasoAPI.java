package com.zaryxstudios.okaso.common;

import com.zaryxstudios.okaso.common.plugin.OkasoPlugin;
import com.zaryxstudios.okaso.common.service.ServiceRegistry;
import java.util.Objects;

import lombok.Getter;

public final class OkasoAPI {

    private static volatile OkasoAPI instance;

    @Getter
    private OkasoPlugin plugin;
    @Getter
    private ServiceRegistry serviceRegistry;
    @Getter
    private boolean initialized;

    private OkasoAPI() {
        this.serviceRegistry = new ServiceRegistry();
    }

    public static OkasoAPI getInstance() {
        if (instance == null) {
            throw new IllegalStateException("OkasoAPI is not initialized. Call OkasoAPI.init(plugin) first.");
        }
        return instance;
    }

    public static synchronized OkasoAPI init(OkasoPlugin plugin) {
        Objects.requireNonNull(plugin, "plugin");
        if (instance != null) {
            throw new IllegalStateException("OkasoAPI is already initialized.");
        }
        instance = new OkasoAPI();
        instance.plugin = plugin;
        instance.initialized = true;
        return instance;
    }

    public static <T> T service(Class<T> type) {
        return getInstance().serviceRegistry.get(type);
    }

    public static <T> void service(Class<T> type, T implementation) {
        getInstance().serviceRegistry.register(type, implementation);
    }
}
