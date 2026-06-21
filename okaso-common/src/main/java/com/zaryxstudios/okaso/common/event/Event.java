package com.zaryxstudios.okaso.common.event;

public abstract class Event {

    private boolean cancelled;
    private boolean cancellable;

    protected Event() {
        this(false);
    }

    protected Event(boolean cancellable) {
        this.cancellable = cancellable;
    }

    public boolean isCancelled() {
        return cancelled;
    }

    public void setCancelled(boolean cancelled) {
        if (!cancellable && cancelled) {
            throw new IllegalStateException("Event is not cancellable: " + getClass().getName());
        }
        this.cancelled = cancelled;
    }

    public boolean isCancellable() {
        return cancellable;
    }
}
