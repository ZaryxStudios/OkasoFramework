package com.zaryxstudios.okaso.particle;

import com.zaryxstudios.okaso.common.OkasoAPI;
import com.zaryxstudios.okaso.common.particle.OkasoParticleEffect;
import com.zaryxstudios.okaso.common.particle.ParticleManager;

import org.bukkit.Bukkit;
import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.data.BlockData;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class ParticleTrail {

    private final JavaPlugin plugin;
    private final Map<UUID, TrailData> activeTrails;
    private BukkitRunnable task;

    public ParticleTrail(JavaPlugin plugin) {
        this.plugin = plugin;
        this.activeTrails = new ConcurrentHashMap<>();
    }

    public void start(Player player, String effectName, String particleType, long intervalTicks) {
        start(player, effectName, particleType, intervalTicks, null, null, 1.0f, null, null, null);
    }

    public void start(Player player, String effectName, String particleType, long intervalTicks,
                      Color color, Color transitionColor, float size, Material material,
                      BlockData blockData, ItemStack itemStack) {
        ParticleManager manager = OkasoAPI.service(ParticleManager.class);
        if (manager == null) return;
        manager.getOrCreateEffect(effectName, particleType);
        activeTrails.put(player.getUniqueId(), new TrailData(effectName, particleType, Math.max(intervalTicks, 1),
            color, transitionColor, size, material, blockData, itemStack));
        if (task == null) {
            task = new BukkitRunnable() {
                @Override
                public void run() {
                    tick();
                }
            };
            task.runTaskTimer(plugin, 1L, 1L);
        }
    }

    public void stop(Player player) {
        activeTrails.remove(player.getUniqueId());
        if (activeTrails.isEmpty() && task != null) {
            task.cancel();
            task = null;
        }
    }

    public void stopAll() {
        activeTrails.clear();
        if (task != null) {
            task.cancel();
            task = null;
        }
    }

    public boolean isActive(Player player) {
        return activeTrails.containsKey(player.getUniqueId());
    }

    public int getActiveCount() {
        return activeTrails.size();
    }

    private void tick() {
        if (activeTrails.isEmpty()) {
            if (task != null) {
                task.cancel();
                task = null;
            }
            return;
        }
        ParticleManager manager = OkasoAPI.service(ParticleManager.class);
        for (Map.Entry<UUID, TrailData> entry : activeTrails.entrySet()) {
            Player player = Bukkit.getPlayer(entry.getKey());
            if (player == null || !player.isOnline()) {
                activeTrails.remove(entry.getKey());
                continue;
            }
            TrailData data = entry.getValue();
            data.tickCounter += 1;
            if (data.tickCounter >= data.interval) {
                data.tickCounter -= data.interval;
                if (manager == null) continue;
                OkasoParticleEffect effect = manager.getOrCreateEffect(data.effectName, data.particleType);
                if (effect instanceof OkasoBukkitParticleEffect) {
                    OkasoBukkitParticleEffect bukkitEffect = (OkasoBukkitParticleEffect) effect;
                    if (data.color != null) bukkitEffect.color(data.color);
                    if (data.transitionColor != null) bukkitEffect.transitionColor(data.transitionColor);
                    if (data.size != 1.0f) bukkitEffect.size(data.size);
                    if (data.material != null) bukkitEffect.material(data.material);
                    if (data.blockData != null) bukkitEffect.blockData(data.blockData);
                    if (data.itemStack != null) bukkitEffect.itemStack(data.itemStack);
                }
                effect.play(player.getLocation(), 1, 0, 0, 0, 0);
            }
        }
    }

    private static class TrailData {
        final String effectName;
        final String particleType;
        final long interval;
        double tickCounter = 0;
        Color color;
        Color transitionColor;
        float size = 1.0f;
        Material material;
        BlockData blockData;
        ItemStack itemStack;

        TrailData(String effectName, String particleType, long interval,
                  Color color, Color transitionColor, float size,
                  Material material, BlockData blockData, ItemStack itemStack) {
            this.effectName = effectName;
            this.particleType = particleType;
            this.interval = interval;
            this.color = color;
            this.transitionColor = transitionColor;
            this.size = size;
            this.material = material;
            this.blockData = blockData;
            this.itemStack = itemStack;
        }
    }
}
