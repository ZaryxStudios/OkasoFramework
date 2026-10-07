package com.zaryxstudios.okaso.world;

import com.zaryxstudios.okaso.common.world.WorldManager;

import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.WorldCreator;
import org.bukkit.WorldType;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Optional;

public class OkasoBukkitWorldManager implements WorldManager {

    @Override
    public Optional<Object> getWorld(String name) {
        if (name == null || name.trim().isEmpty()) {
            return Optional.empty();
        }
        World world = Bukkit.getWorld(name);
        return Optional.ofNullable(world);
    }

    @Override
    public Collection<Object> getWorlds() {
        return new ArrayList<>(Bukkit.getWorlds());
    }

    @Override
    public Object createWorld(String name, Object worldType) {
        if (name == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException("World name cannot be empty");
        }
        WorldCreator creator = new WorldCreator(name);
        if (worldType instanceof World.Environment) {
            creator.environment((World.Environment) worldType);
        } else if (worldType instanceof WorldType) {
            creator.type((WorldType) worldType);
        }
        return creator.createWorld();
    }

    @Override
    public boolean unloadWorld(String name) {
        if (name == null || name.trim().isEmpty()) {
            return false;
        }
        World world = Bukkit.getWorld(name);
        if (world != null) {
            return Bukkit.unloadWorld(world, true);
        }
        return false;
    }

    @Override
    public boolean isWorldLoaded(String name) {
        return name != null && !name.trim().isEmpty() && Bukkit.getWorld(name) != null;
    }

    @Override
    public long getWorldTime(String name) {
        World world = Bukkit.getWorld(name);
        return world != null ? world.getTime() : 0L;
    }

    @Override
    public void setWorldTime(String name, long time) {
        World world = Bukkit.getWorld(name);
        if (world != null) {
            world.setTime(time);
        }
    }

    @Override
    public boolean hasStorm(String name) {
        World world = Bukkit.getWorld(name);
        return world != null && world.hasStorm();
    }

    @Override
    public void setStorm(String name, boolean storm) {
        World world = Bukkit.getWorld(name);
        if (world != null) {
            world.setStorm(storm);
        }
    }

    @Override
    public boolean isThundering(String name) {
        World world = Bukkit.getWorld(name);
        return world != null && world.isThundering();
    }

    @Override
    public void setThundering(String name, boolean thundering) {
        World world = Bukkit.getWorld(name);
        if (world != null) {
            world.setThundering(thundering);
        }
    }

    @Override
    public long getWorldSeed(String name) {
        World world = Bukkit.getWorld(name);
        return world != null ? world.getSeed() : 0L;
    }

    @Override
    public Collection<Object> getWorldPlayers(String name) {
        World world = Bukkit.getWorld(name);
        if (world != null) {
            return new ArrayList<>(world.getPlayers());
        }
        return new ArrayList<>();
    }

    @Override
    public boolean saveWorld(String name) {
        World world = Bukkit.getWorld(name);
        if (world != null) {
            try {
                world.save();
                return true;
            } catch (RuntimeException ignored) {
                return false;
            }
        }
        return false;
    }
}
