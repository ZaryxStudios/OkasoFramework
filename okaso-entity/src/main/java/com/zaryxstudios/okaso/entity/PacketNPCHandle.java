package com.zaryxstudios.okaso.entity;

import com.zaryxstudios.okaso.common.entity.NPCHandle;
import org.bukkit.Location;
import org.bukkit.entity.Entity;

import java.util.UUID;

public class PacketNPCHandle implements NPCHandle {
    private final Entity entity;
    private final boolean fakePlayer;
    private boolean spawned = false;

    public PacketNPCHandle(Entity entity, boolean fakePlayer) {
        this.entity = entity;
        this.fakePlayer = fakePlayer;
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
    public boolean isSpawned() {
        return spawned && entity.isValid();
    }

    @Override
    public UUID getUniqueId() {
        return entity.getUniqueId();
    }

    public Entity getEntity() {
        return entity;
    }
}