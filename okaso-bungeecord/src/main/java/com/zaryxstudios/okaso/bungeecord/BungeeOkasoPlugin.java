package com.zaryxstudios.okaso.bungeecord;

import com.zaryxstudios.okaso.common.OkasoAPI;
import com.zaryxstudios.okaso.common.plugin.OkasoPlugin;
import com.zaryxstudios.okaso.common.service.ServiceRegistry;
import com.zaryxstudios.okaso.common.task.TaskScheduler;
import com.zaryxstudios.okaso.bungeecord.task.BungeeTaskScheduler;

import net.md_5.bungee.api.plugin.Plugin;

import java.util.logging.Logger;

public class BungeeOkasoPlugin extends Plugin implements OkasoPlugin {

    private OkasoAPI api;

    @Override
    public void onEnable() {
        api = OkasoAPI.init(this);
        registerServices();
        onOkasoEnable();
        getLogger().info("Okaso BungeeCord adapter enabled.");
    }

    private void registerServices() {
        ServiceRegistry reg = api.getServiceRegistry();
        reg.register(TaskScheduler.class, new BungeeTaskScheduler(this));
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
