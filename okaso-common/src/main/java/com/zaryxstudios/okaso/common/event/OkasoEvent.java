package com.zaryxstudios.okaso.common.event;

public abstract class OkasoEvent {

    private boolean cancelled;
    private boolean cancellable;
    private String eventName;

    protected OkasoEvent() {
        this(false);
    }

    protected OkasoEvent(boolean cancellable) {
        this.cancellable = cancellable;
        this.eventName = getClass().getSimpleName();
    }

    public String getEventName() {
        return eventName;
    }

    public boolean isCancelled() {
        return cancelled;
    }

    public void setCancelled(boolean cancelled) {
        if (!cancellable && cancelled) {
            throw new IllegalStateException("Event is not cancellable: " + eventName);
        }
        this.cancelled = cancelled;
    }

    public boolean isCancellable() {
        return cancellable;
    }
}
