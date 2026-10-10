package com.zaryxstudios.okaso.hologram;

import com.zaryxstudios.okaso.common.hologram.OkasoHologram;
import com.zaryxstudios.okaso.common.hologram.HologramLine;
import com.zaryxstudios.okaso.common.hologram.HologramManager;
import com.zaryxstudios.okaso.common.hologram.HologramRenderer;
import com.zaryxstudios.okaso.common.hologram.HologramStyle;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

public class OkasoBukkitHologramManager implements HologramManager {

    private final Map<String, OkasoBukkitHologram> holograms;
    private HologramStyle defaultStyle;

    public OkasoBukkitHologramManager() {
        this.holograms = new ConcurrentHashMap<>();
        this.defaultStyle = HologramStyle.AUTO;
    }

    public OkasoBukkitHologramManager(HologramStyle defaultStyle) {
        this.holograms = new ConcurrentHashMap<>();
        this.defaultStyle = defaultStyle == null ? HologramStyle.AUTO : defaultStyle;
    }

    @Override
    public void setDefaultStyle(HologramStyle style) {
        this.defaultStyle = style == null ? HologramStyle.AUTO : style;
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
        if (style == null) style = HologramStyle.AUTO;
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

    public List<OkasoBukkitHologram> getHologramsNear(Location location, double radius) {
        List<OkasoBukkitHologram> result = new ArrayList<>();
        if (location == null || radius <= 0) return result;
        double radiusSq = radius * radius;
        for (OkasoBukkitHologram h : holograms.values()) {
            Location hLoc = h.getLocation();
            if (hLoc != null && hLoc.getWorld().equals(location.getWorld())) {
                if (hLoc.distanceSquared(location) <= radiusSq) {
                    result.add(h);
                }
            }
        }
        return result;
    }

    public List<OkasoBukkitHologram> getHologramsNear(Player player, double radius) {
        if (player == null) return Collections.emptyList();
        return getHologramsNear(player.getLocation(), radius);
    }

    public List<OkasoBukkitHologram> getRunningHolograms() {
        return holograms.values().stream()
            .filter(OkasoBukkitHologram::isRunning)
            .collect(Collectors.toList());
    }

    public List<OkasoBukkitHologram> getStoppedHolograms() {
        return holograms.values().stream()
            .filter(h -> !h.isRunning())
            .collect(Collectors.toList());
    }

    public int startAll() {
        int count = 0;
        for (OkasoBukkitHologram h : holograms.values()) {
            if (!h.isRunning()) {
                h.start();
                count++;
            }
        }
        return count;
    }

    public int stopAllRunning() {
        int count = 0;
        for (OkasoBukkitHologram h : holograms.values()) {
            if (h.isRunning()) {
                h.stop();
                count++;
            }
        }
        return count;
    }

    public int removeAllInWorld(World world) {
        if (world == null) return 0;
        List<String> toRemove = new ArrayList<>();
        for (Map.Entry<String, OkasoBukkitHologram> entry : holograms.entrySet()) {
            Location loc = entry.getValue().getLocation();
            if (loc != null && world.equals(loc.getWorld())) {
                toRemove.add(entry.getKey());
            }
        }
        for (String id : toRemove) {
            removeHologram(id);
        }
        return toRemove.size();
    }

    public OkasoBukkitHologram getBukkitHologram(String id) {
        return holograms.get(id);
    }

    public boolean isRunning(String id) {
        OkasoBukkitHologram h = holograms.get(id);
        return h != null && h.isRunning();
    }

    public boolean teleportHologram(String id, Location location) {
        OkasoBukkitHologram h = holograms.get(id);
        if (h == null || location == null) return false;
        h.setLocation(location);
        return true;
    }

    public boolean renameHologram(String oldId, String newId) {
        if (oldId == null || newId == null || oldId.equals(newId)) return false;
        if (holograms.containsKey(newId)) return false;
        OkasoBukkitHologram h = holograms.remove(oldId);
        if (h == null) return false;
        OkasoBukkitHologram newHologram = new OkasoBukkitHologram(
            newId, h.getLocation(), new ArrayList<>(h.getLines()), h.getRenderer()
        );
        if (h.isRunning()) {
            newHologram.start();
        }
        holograms.put(newId, newHologram);
        return true;
    }

    public int getTotalLineCount() {
        return holograms.values().stream()
            .mapToInt(OkasoBukkitHologram::getLineCount)
            .sum();
    }

    public int getTotalEntityCount() {
        return holograms.values().stream()
            .mapToInt(OkasoBukkitHologram::getEntityCount)
            .sum();
    }

    public void clearRegistry() {
        holograms.clear();
    }
}