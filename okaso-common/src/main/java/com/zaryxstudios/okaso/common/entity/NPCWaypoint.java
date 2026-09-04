package com.zaryxstudios.okaso.common.entity;

import org.bukkit.Location;

public class NPCWaypoint {
    private final Location location;
    private final float yaw;
    private final float pitch;
    private final double speed;
    private final int delayTicks;

    public NPCWaypoint(Location location, float yaw, float pitch, double speed, int delayTicks) {
        this.location = location;
        this.yaw = yaw;
        this.pitch = pitch;
        this.speed = speed;
        this.delayTicks = delayTicks;
    }

    public NPCWaypoint(Location location, double speed) {
        this(location, location.getYaw(), location.getPitch(), speed, 0);
    }

    public NPCWaypoint(Location location) {
        this(location, 0.2);
    }

    public Location getLocation() {
        return location;
    }
    
    public float getYaw() {
        return yaw;
    }

    public float getPitch() {
        return pitch;
    }

    public double getSpeed() {
        return speed;
    }

    public int getDelayTicks() {
        return delayTicks;
    }

    @Override
    public String toString() {
        return "NPCWaypoint{x=" + location.getBlockX() + ", y=" + location.getBlockY() + ", z=" + location.getBlockZ() + ", speed=" + speed + "}";
    }
}