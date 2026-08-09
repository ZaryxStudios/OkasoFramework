package com.zaryxstudios.okaso.bukkit.event;

import com.zaryxstudios.okaso.common.event.EventBus;

import org.bukkit.event.Event;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.plugin.Plugin;

public class OkasoBukkitEventBusAdapter implements Listener {

    private final EventBus eventBus;
    private final Plugin plugin;

    public OkasoBukkitEventBusAdapter(EventBus eventBus, Plugin plugin) {
        this.eventBus = eventBus;
        this.plugin = plugin;
    }

    @EventHandler
    public void onBukkitEvent(Event event) {
        OkasoBukkitEvent okasoEvent = new OkasoBukkitEvent(event);
        eventBus.publish(okasoEvent);
    }

    public void register() {
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
    }
}
