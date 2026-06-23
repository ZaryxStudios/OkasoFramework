package com.zaryxstudios.okaso.hologram;

import com.zaryxstudios.okaso.common.hologram.Hologram;
import com.zaryxstudios.okaso.common.hologram.HologramManager;

import org.bukkit.Location;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

public class BukkitHologramManager implements HologramManager {

    private final Map<String, BukkitHologram> holograms;

    public BukkitHologramManager() {
        this.holograms = new ConcurrentHashMap<>();
    }

    @Override
    public Hologram createHologram(String id) {
        return createHologram(id, new Location(null, 0, 0, 0), new ArrayList<String>());
    }

    public Hologram createHologram(String id, Location location, List<String> lines) {
        BukkitHologram hologram = new BukkitHologram(id, location.clone(), new ArrayList<>(lines));
        holograms.put(id, hologram);
        return hologram;
    }

    @Override
    public Optional<Hologram> getHologram(String id) {
        return Optional.ofNullable(holograms.get(id));
    }

    @Override
    public Collection<Hologram> getHolograms() {
        return Collections.unmodifiableCollection(new ArrayList<Hologram>(holograms.values()));
    }

    @Override
    public void removeHologram(String id) {
        BukkitHologram h = holograms.remove(id);
        if (h != null) {
            h.stop();
        }
    }

    @Override
    public boolean exists(String id) {
        return holograms.containsKey(id);
    }

    @Override
    public int count() {
        return holograms.size();
    }

    @Override
    public void removeAll() {
        for (BukkitHologram h : holograms.values()) {
            h.stop();
        }
        holograms.clear();
    }

    public void stopAll() {
        for (BukkitHologram h : holograms.values()) {
            if (h.isRunning()) {
                h.stop();
            }
        }
    }
}
