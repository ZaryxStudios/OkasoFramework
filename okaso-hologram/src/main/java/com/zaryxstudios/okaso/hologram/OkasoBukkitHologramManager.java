package com.zaryxstudios.okaso.hologram;

import com.zaryxstudios.okaso.common.hologram.OkasoHologram;
import com.zaryxstudios.okaso.common.hologram.HologramLine;
import com.zaryxstudios.okaso.common.hologram.HologramManager;
import com.zaryxstudios.okaso.common.hologram.HologramRenderer;
import com.zaryxstudios.okaso.common.hologram.HologramStyle;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;

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
    private HologramStyle defaultStyle;

    public OkasoBukkitHologramManager() {
        this.holograms = new ConcurrentHashMap<>();
        this.defaultStyle = HologramStyle.AUTO;
    }

    public OkasoBukkitHologramManager(HologramStyle defaultStyle) {
        this.holograms = new ConcurrentHashMap<>();
        this.defaultStyle = defaultStyle;
    }

    @Override
    public void setDefaultStyle(HologramStyle style) {
        this.defaultStyle = style;
    }

    @Override
    public HologramStyle getDefaultStyle() {
        return defaultStyle;
    }

    @Override
    public OkasoHologram createHologram(String id) {
        return createHologram(id, defaultStyle, new ArrayList<>());
    }

    @Override
    public OkasoHologram createHologram(String id, HologramLine... lines) {
        return createHologram(id, defaultStyle, Arrays.asList(lines));
    }

    @Override
    public OkasoHologram createHologram(String id, List<HologramLine> lines) {
        return createHologram(id, defaultStyle, lines);
    }

    @Override
    public OkasoHologram createHologram(String id, HologramStyle style) {
        return createHologram(id, style, new ArrayList<>());
    }

    @Override
    public OkasoHologram createHologram(String id, HologramStyle style, List<HologramLine> lines) {
        validateId(id);
        removeExisting(id);
        HologramRenderer renderer = resolveRenderer(style);
        Location loc = defaultLocation();
        OkasoBukkitHologram hologram = new OkasoBukkitHologram(id, loc, copyLines(lines), renderer);
        holograms.put(id, hologram);
        return hologram;
    }

    public OkasoHologram createHologram(String id, Location location, List<HologramLine> lines) {
        return createHologram(id, location, defaultStyle, lines);
    }

    public OkasoHologram createHologram(String id, Location location, HologramStyle style, List<HologramLine> lines) {
        validateId(id);
        removeExisting(id);
        Location loc = location != null ? location.clone() : defaultLocation();
        HologramRenderer renderer = resolveRenderer(style);
        OkasoBukkitHologram hologram = new OkasoBukkitHologram(id, loc, copyLines(lines), renderer);
        holograms.put(id, hologram);
        return hologram;
    }

    public OkasoHologram createHologram(String id, Location location, HologramLine... lines) {
        return createHologram(id, location, defaultStyle, Arrays.asList(lines));
    }

    @Override
    public Optional<OkasoHologram> getHologram(String id) {
        return Optional.ofNullable(holograms.get(id));
    }

    @Override
    public Collection<OkasoHologram> getHolograms() {
        return Collections.unmodifiableCollection(new ArrayList<>(holograms.values()));
    }

    public List<OkasoBukkitHologram> getHologramsInWorld(World world) {
        List<OkasoBukkitHologram> result = new ArrayList<>();
        if (world == null) return result;
        for (OkasoBukkitHologram h : holograms.values()) {
            Location loc = h.getLocation();
            if (loc != null && world.equals(loc.getWorld())) {
                result.add(h);
            }
        }
        return result;
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

    private HologramRenderer resolveRenderer(HologramStyle style) {
        switch (style) {
            case DISPLAY:
                if (DisplayRenderer.isAvailable()) return new DisplayRenderer();
                return new ArmorStandRenderer();
            case CLASSIC:
                return new ArmorStandRenderer();
            case AUTO:
            default:
                if (DisplayRenderer.isAvailable()) return new DisplayRenderer();
                return new ArmorStandRenderer();
        }
    }

    private Location defaultLocation() {
        List<World> worlds = Bukkit.getWorlds();
        if (!worlds.isEmpty()) {
            return worlds.get(0).getSpawnLocation();
        }
        return new Location(null, 0, 0, 0);
    }

    private List<HologramLine> copyLines(List<HologramLine> lines) {
        return lines != null ? new ArrayList<>(lines) : new ArrayList<>();
    }

    private void validateId(String id) {
        if (id == null || id.trim().isEmpty()) {
            throw new IllegalArgumentException("Hologram id cannot be null or empty");
        }
    }

    private void removeExisting(String id) {
        OkasoBukkitHologram existing = holograms.remove(id);
        if (existing != null) {
            existing.stop();
        } 
    }
}