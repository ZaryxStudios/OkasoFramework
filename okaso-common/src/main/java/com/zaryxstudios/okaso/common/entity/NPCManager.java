package com.zaryxstudios.okaso.common.entity;

import java.util.Collection;
import java.util.Optional;
import java.util.UUID;

public interface NPCManager {
    void register(NPCHandle handle);
    Optional<NPCHandle> unregister(UUID uuid);
    Optional<NPCHandle> getNPC(UUID uuid);
    Collection<NPCHandle> getAllNPCs();
    Collection<NPCHandle> getSpawnedNPCs();
    int getCount();
    void despawnAll();
    boolean contains(UUID uuid);
}