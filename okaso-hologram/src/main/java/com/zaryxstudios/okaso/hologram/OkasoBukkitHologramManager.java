package com.zaryxstudios.okaso.hologram;

import com.zaryxstudios.okaso.common.hologram.OkasoHologram;
import com.zaryxstudios.okaso.common.hologram.HologramLine;
import com.zaryxstudios.okaso.common.hologram.HologramManager;

import org.bukkit.Location;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

public class OkasoBukkitHologramManager implements HologramManager {

    private final Map<String, OkasoBukkitHologram> holograms;

    public OkasoBukkitHologramManager() {
        this.holograms = new ConcurrentHashMap<>();
    }

    @Override
    public OkasoHologram createHologram(String id) {
        return createHologram(id, new ArrayList<>());
    }

    @Override
    public OkasoHologram createHologram(String id, HologramLine... lines) {
        return createHologram(id, Arrays.asList(lines));
    }

    @Override
    public OkasoHologram createHologram(String id, List<HologramLine> lines) {
        Location fallback = new Location(null, 0, 0, 0);
        OkasoBukkitHologram hologram = new OkasoBukkitHologram(id, fallback, new ArrayList<>(lines));
        holograms.put(id, hologram);
        return hologram;
    }

    public OkasoHologram createHologram(String id, Location location, List<HologramLine> lines) {
        OkasoBukkitHologram hologram = new OkasoBukkitHologram(id, location.clone(), new ArrayList<>(lines));
        holograms.put(id, hologram);
        return hologram;
    }

    public OkasoHologram createHologram(String id, Location location, HologramLine... lines) {
        return createHologram(id, location, Arrays.asList(lines));
    }

    @Override
    public Optional<OkasoHologram> getHologram(String id) {
        return Optional.ofNullable(holograms.get(id));
    }

    @Override
    public Collection<OkasoHologram> getHolograms() {
        return Collections.unmodifiableCollection(new ArrayList<>(holograms.values()));
    }

    @Override
    public void removeHologram(String id) {
        OkasoBukkitHologram h = holograms.remove(id);
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
        for (OkasoBukkitHologram h : holograms.values()) {
            h.stop();
        }
        holograms.clear();
    }

    public void stopAll() {
        for (OkasoBukkitHologram h : holograms.values()) {
            if (h.isRunning()) {
                h.stop();
            }
        }
    }
}
