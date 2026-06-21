package com.zaryxstudios.okaso.bungeecord;

import com.zaryxstudios.okaso.common.OkasoAPI;
import com.zaryxstudios.okaso.common.plugin.OkasoPlugin;
import com.zaryxstudios.okaso.common.service.ServiceRegistry;

import net.md_5.bungee.api.plugin.Plugin;

import java.util.logging.Logger;

public class BungeeOkasoPlugin extends Plugin implements OkasoPlugin {

    private OkasoAPI api;

    @Override
    public void onEnable() {
        api = OkasoAPI.init(this);
        onOkasoEnable();
        getLogger().info("Okaso BungeeCord adapter enabled.");
    }

    @Override
    public void onDisable() {
        onOkasoDisable();
        if (api != null) {
            api.getServiceRegistry().getAll().clear();
        }
        getLogger().info("Okaso BungeeCord adapter disabled.");
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
