package com.zaryxstudios.okaso.entity;

import com.zaryxstudios.okaso.common.entity.*;
import com.zaryxstudios.okaso.common.event.EventBus;

import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.player.PlayerInteractAtEntityEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.plugin.Plugin;

import java.util.*;

public class BukkitNPCInteractionListener implements Listener {

    private final NPCManager npcManager;
    private final EventBus eventBus;
    private final Map<UUID, Set<UUID>> nearbyPlayers = new HashMap<>();

    public BukkitNPCInteractionListener(NPCManager npcManager, EventBus eventBus, Plugin plugin) {
        this.npcManager = npcManager;
        this.eventBus = eventBus;
        org.bukkit.Bukkit.getPluginManager().registerEvents(this, plugin);
    }

    @EventHandler
    public void onRightClick(PlayerInteractAtEntityEvent event) {
        Entity entity = event.getRightClicked();
        String playerName = event.getPlayer().getName();
        findNPCByEntity(entity).ifPresent(npc -> {
            eventBus.publish(new NPCInteractionEvent(npc, playerName, NPCInteractionType.RIGHT_CLICK));
        });
    }

    @EventHandler
    public void onLeftClick(EntityDamageByEntityEvent event) {
        if (!(event.getDamager() instanceof Player)) return;
        Entity entity = event.getEntity();
        String playerName = ((Player) event.getDamager()).getName();
        findNPCByEntity(entity).ifPresent(npc -> {
            eventBus.publish(new NPCInteractionEvent(npc, playerName, NPCInteractionType.LEFT_CLICK));
        });
    }

    @EventHandler
    public void onPlayerMove(PlayerMoveEvent event) {
        Player player = event.getPlayer();
        UUID playerUUID = player.getUniqueId();

        for (NPCHandle npc : npcManager.getSpawnedNPCs()) {
            if (!(npc instanceof PacketNPCHandle)) continue;
            Entity entity = ((PacketNPCHandle) npc).getEntity();
            if (entity == null || !entity.isValid()) continue;

            double distance = player.getLocation().distance(entity.getLocation());
            Set<UUID> nearby = nearbyPlayers.computeIfAbsent(npc.getUniqueId(), k -> new HashSet<>());

            if (distance <= 5.0 && nearby.add(playerUUID)) {
                eventBus.publish(new NPCInteractionEvent(npc, player.getName(), NPCInteractionType.PLAYER_ENTER));
            } else if (distance > 6.0 && nearby.remove(playerUUID)) {
                eventBus.publish(new NPCInteractionEvent(npc, player.getName(), NPCInteractionType.PLAYER_EXIT));
            }
        }
    }

    private Optional<NPCHandle> findNPCByEntity(Entity entity) {
        for (NPCHandle npc : npcManager.getAllNPCs()) {
            if (npc instanceof PacketNPCHandle) {
                Entity npcEntity = ((PacketNPCHandle) npc).getEntity();
                if (npcEntity != null && npcEntity.equals(entity)) {
                    return Optional.of(npc);
                }
            }
        }
        return Optional.empty();
    }
}