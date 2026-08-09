package com.zaryxstudios.okaso.bukkit.event;

import com.zaryxstudios.okaso.common.event.OkasoEvent;

import org.bukkit.event.Event;
import org.bukkit.event.Cancellable;

import lombok.Getter;

public class OkasoBukkitEvent extends OkasoEvent {

    @Getter
    private final Event OkasoBukkitEvent;

    public OkasoBukkitEvent(Event OkasoBukkitEvent) {
        super(OkasoBukkitEvent instanceof Cancellable);
        this.OkasoBukkitEvent = OkasoBukkitEvent;
    }

    @Override
    public boolean isCancelled() {
        if (OkasoBukkitEvent instanceof Cancellable) {
            return ((Cancellable) OkasoBukkitEvent).isCancelled();
        }
        return super.isCancelled();
    }

    @Override
    public void setCancelled(boolean cancelled) {
        if (OkasoBukkitEvent instanceof Cancellable) {
            ((Cancellable) OkasoBukkitEvent).setCancelled(cancelled);
        }
        super.setCancelled(cancelled);
    }
}
