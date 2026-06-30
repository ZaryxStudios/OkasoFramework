package com.zaryxstudios.okaso.bukkit.event;

import com.zaryxstudios.okaso.common.event.EventBus;

import org.bukkit.event.Event;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.plugin.Plugin;

public class BukkitEventBusAdapter implements Listener {

    private final EventBus eventBus;
    private final Plugin plugin;

    public BukkitEventBusAdapter(EventBus eventBus, Plugin plugin) {
        this.eventBus = eventBus;
        this.plugin = plugin;
    }

    @EventHandler
    public void onBukkitEvent(Event event) {
        BukkitEvent okasoEvent = new BukkitEvent(event);
        eventBus.publish(okasoEvent);
    }

    public void register() {
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
    }
}
