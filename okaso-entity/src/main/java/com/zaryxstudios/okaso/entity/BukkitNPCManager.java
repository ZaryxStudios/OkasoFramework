package com.zaryxstudios.okaso.entity;

import com.zaryxstudios.okaso.common.entity.NPCHandle;
import com.zaryxstudios.okaso.common.entity.NPCManager;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

public class BukkitNPCManager implements NPCManager {

    private final Map<UUID, NPCHandle> npcs = new ConcurrentHashMap<>();

    @Override
    public void register(NPCHandle handle) {
        npcs.put(handle.getUniqueId(), handle);
    }

    @Override
    public Optional<NPCHandle> unregister(UUID uuid) {
        return Optional.ofNullable(npcs.remove(uuid));
    }

    @Override
    public Optional<NPCHandle> getNPC(UUID uuid) {
        return Optional.ofNullable(npcs.get(uuid));
    }

    @Override
    public Collection<NPCHandle> getAllNPCs() {
        return Collections.unmodifiableCollection(new ArrayList<>(npcs.values()));
    }

    @Override
    public Collection<NPCHandle> getSpawnedNPCs() {
        return npcs.values().stream()
            .filter(NPCHandle::isSpawned)
            .collect(Collectors.toList());
    }

    @Override
    public int getCount() {
        return npcs.size();
    }

    @Override
    public void despawnAll() {
        for (NPCHandle handle : npcs.values()) {
            handle.despawn();
        }
        npcs.clear();
    }

    @Override
    public boolean contains(UUID uuid) {
        return npcs.containsKey(uuid);
    }
}