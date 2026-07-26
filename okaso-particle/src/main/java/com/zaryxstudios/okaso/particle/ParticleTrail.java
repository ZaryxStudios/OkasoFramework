package com.zaryxstudios.okaso.particle;

import com.zaryxstudios.okaso.common.OkasoAPI;
import com.zaryxstudios.okaso.common.particle.OkasoParticleEffect;
import com.zaryxstudios.okaso.common.particle.ParticleManager;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class ParticleTrail {

    private final JavaPlugin plugin;
    private final Map<UUID, TrailData> activeTrails;
    private int taskId = -1;

    public ParticleTrail(JavaPlugin plugin) {
        this.plugin = plugin;
        this.activeTrails = new ConcurrentHashMap<>();
    }

    public void start(Player player, String effectName, String particleType, double interval) {
        ParticleManager manager = OkasoAPI.service(ParticleManager.class);
        if (manager == null) return;
        manager.getOrCreateEffect(effectName, particleType);
        activeTrails.put(player.getUniqueId(), new TrailData(effectName, particleType, interval));
        if (taskId == -1) {
            taskId = Bukkit.getScheduler().runTaskTimer(plugin, this::tick, 1L, 1L).getTaskId();
        }
    }

    public void stop(Player player) {
        activeTrails.remove(player.getUniqueId());
        if (activeTrails.isEmpty() && taskId != -1) {
            Bukkit.getScheduler().cancelTask(taskId);
            taskId = -1;
        }
    }

    public boolean isActive(Player player) {
        return activeTrails.containsKey(player.getUniqueId());
    }

    private void tick() {
        if (activeTrails.isEmpty()) {
            Bukkit.getScheduler().cancelTask(taskId);
            taskId = -1;
            return;
        }
        for (Map.Entry<UUID, TrailData> entry : activeTrails.entrySet()) {
            Player player = Bukkit.getPlayer(entry.getKey());
            if (player == null || !player.isOnline()) {
                activeTrails.remove(entry.getKey());
                continue;
            }
            TrailData data = entry.getValue();
            data.tickCounter++;
            if (data.tickCounter >= (int) data.interval) {
                data.tickCounter = 0;
                ParticleManager manager = OkasoAPI.service(ParticleManager.class);
                if (manager == null) continue;
                manager.getEffect(data.effectName).ifPresent(effect ->
                    effect.play(player.getLocation(), 1, 0, 0, 0, 0)
                );
            }
        }
    }

    private static class TrailData {
        final String effectName;
        final String particleType;
        final double interval;
        int tickCounter = 0;

        TrailData(String effectName, String particleType, double interval) {
            this.effectName = effectName;
            this.particleType = particleType;
            this.interval = interval;
        }
    }
}
