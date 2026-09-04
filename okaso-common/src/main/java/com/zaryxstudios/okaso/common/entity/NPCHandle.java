package com.zaryxstudios.okaso.common.entity;

import org.bukkit.Location;
import java.util.UUID;

public interface NPCHandle {
    void spawn();
    void despawn();
    void setLocation(Location location);
    Location getLocation();
    boolean isSpawned();
    UUID getUniqueId();
}