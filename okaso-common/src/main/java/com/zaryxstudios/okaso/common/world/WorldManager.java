package com.zaryxstudios.okaso.common.world;

import java.util.Collection;
import java.util.Optional;

public interface WorldManager {
    Optional<Object> getWorld(String name);
    Collection<Object> getWorlds();
    Object createWorld(String name, Object worldType);
    boolean unloadWorld(String name);
    boolean isWorldLoaded(String name);
    long getWorldTime(String name);
    void setWorldTime(String name, long time);
    boolean hasStorm(String name);
    void setStorm(String name, boolean storm);
    boolean isThundering(String name);
    void setThundering(String name, boolean thundering);
    long getWorldSeed(String name);
    Collection<Object> getWorldPlayers(String name);
    boolean saveWorld(String name);
}
