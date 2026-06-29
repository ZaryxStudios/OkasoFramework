package com.zaryxstudios.okaso.bukkit.event;

import com.zaryxstudios.okaso.common.event.OkasoEvent;

import org.bukkit.event.Event;

import lombok.Getter;

public class BukkitEvent extends OkasoEvent {

    @Getter
    private final Event bukkitEvent;

    public BukkitEvent(Event bukkitEvent) {
        super(bukkitEvent instanceof org.bukkit.event.Cancellable);
        this.bukkitEvent = bukkitEvent;
    }

    @Override
    public boolean isCancelled() {
        if (bukkitEvent instanceof org.bukkit.event.Cancellable) {
            return ((org.bukkit.event.Cancellable) bukkitEvent).isCancelled();
        }
        return super.isCancelled();
    }

    @Override
    public void setCancelled(boolean cancelled) {
        if (bukkitEvent instanceof org.bukkit.event.Cancellable) {
            ((org.bukkit.event.Cancellable) bukkitEvent).setCancelled(cancelled);
        }
        super.setCancelled(cancelled);
    }
}
