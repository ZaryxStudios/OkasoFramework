package com.zaryxstudios.okaso.bukkit.event;

import com.zaryxstudios.okaso.common.event.OkasoEvent;

import org.bukkit.event.Event;
import org.bukkit.event.Cancellable;

import lombok.Getter;

public class BukkitEvent extends OkasoEvent {

    @Getter
    private final Event bukkitEvent;

    public BukkitEvent(Event bukkitEvent) {
        super(bukkitEvent instanceof Cancellable);
        this.bukkitEvent = bukkitEvent;
    }

    @Override
    public boolean isCancelled() {
        if (bukkitEvent instanceof Cancellable) {
            return ((Cancellable) bukkitEvent).isCancelled();
        }
        return super.isCancelled();
    }

    @Override
    public void setCancelled(boolean cancelled) {
        if (bukkitEvent instanceof Cancellable) {
            ((Cancellable) bukkitEvent).setCancelled(cancelled);
        }
        super.setCancelled(cancelled);
    }
}
