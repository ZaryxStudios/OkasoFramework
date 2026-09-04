package com.zaryxstudios.okaso.entity;

import com.zaryxstudios.okaso.common.entity.NPCHandle;

import org.bukkit.Location;
import org.bukkit.entity.Entity;

import java.util.UUID;

public class PacketNPCHandle implements NPCHandle {

    private final Entity entity;
    private final boolean fakePlayer;
    private boolean spawned;

    PacketNPCHandle(Entity entity, boolean fakePlayer) {
        this.entity = entity;
        this.fakePlayer = fakePlayer;
        this.spawned = true;
    }

    @Override
    public void spawn() {
        if (!spawned) {
            spawned = true;
        }
    }

    @Override
    public void despawn() {
        if (spawned && entity.isValid()) {
            entity.remove();
            spawned = false;
        }
    }

    @Override
    public void setLocation(Location location) {
        entity.teleport(location);
    }

    @Override
    public Location getLocation() {
        return entity.getLocation();
    }

    @Override
    public void setRotation(float yaw, float pitch) {
        Location loc = entity.getLocation();
        loc.setYaw(yaw);
        loc.setPitch(pitch);
        entity.teleport(loc);
    }

    @Override
    public boolean isSpawned() {
        return spawned && entity.isValid();
    }

    @Override
    public UUID getUniqueId() {
        return entity.getUniqueId();
    }

    @Override
    public boolean isFakePlayer() {
        return fakePlayer;
    }

    @Override
    public void setCustomName(String name) {
        entity.setCustomName(name);
    }
    
    public Entity getEntity() {
        return entity;
    }

    @Override
    public String toString() {
        return "PacketNPCHandle{uuid=" + getUniqueId()
            + ", fakePlayer=" + fakePlayer
            + ", spawned=" + isSpawned()
            + "}";
    }
}
