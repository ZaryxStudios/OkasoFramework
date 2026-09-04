package com.zaryxstudios.okaso.common.hologram;

import org.bukkit.Location;

public interface HologramRenderer {
    Object spawnText(Location loc, String text);
    Object spawnItem(Location loc, String materialName, int amount);
    Object spawnMob(Location loc, String entityType);
    void despawnAll();
    boolean supportsLineType(HologramLineType type);
}