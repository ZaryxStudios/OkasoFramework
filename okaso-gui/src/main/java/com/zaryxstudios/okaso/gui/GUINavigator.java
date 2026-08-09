package com.zaryxstudios.okaso.gui;

import com.zaryxstudios.okaso.common.gui.GUI;
import com.zaryxstudios.okaso.common.gui.GUIClickEvent;
import com.zaryxstudios.okaso.common.gui.GUIItem;

import org.bukkit.entity.Player;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.function.Consumer;

public class GUINavigator {

    private final Map<String, GUI> guiRegistry;
    private final Map<UUID, Deque<String>> playerHistory;
    private final Map<UUID, Deque<String>> playerForward;
    private final Map<UUID, String> playerCurrent;
    private Consumer<Player> onNavigate;

    public GUINavigator() {
        this.guiRegistry = new HashMap<>();
        this.playerHistory = new HashMap<>();
        this.playerForward = new HashMap<>();
        this.playerCurrent = new HashMap<>();
    }

    public void register(String id, GUI gui) {
        if (id != null && gui != null) {
            guiRegistry.put(id, gui);
        }
    }

    public void unregister(String id) {
        guiRegistry.remove(id);
    }

    public GUI getGUI(String id) {
        return guiRegistry.get(id);
    }

    public boolean hasGUI(String id) {
        return guiRegistry.containsKey(id);
    }

    public void navigate(Player player, String id) {
        if (player == null || id == null) return;
        GUI gui = guiRegistry.get(id);
        if (gui == null) return;
        UUID uuid = player.getUniqueId();
        String current = playerCurrent.get(uuid);
        if (current != null && !current.equals(id)) {
            Deque<String> history = playerHistory.computeIfAbsent(uuid, k -> new ArrayDeque<>());
            history.push(current);
        }
        playerForward.remove(uuid);
        playerCurrent.put(uuid, id);
        gui.open(player);
        if (onNavigate != null) {
            onNavigate.accept(player);
        }
    }

    public void replace(Player player, String id) {
        if (player == null || id == null) return;
        GUI gui = guiRegistry.get(id);
        if (gui == null) return;
        UUID uuid = player.getUniqueId();
        playerCurrent.put(uuid, id);
        gui.open(player);
        if (onNavigate != null) {
            onNavigate.accept(player);
        }
    }

    public boolean goBack(Player player) {
        if (player == null) return false;
        UUID uuid = player.getUniqueId();
        Deque<String> history = playerHistory.get(uuid);
        if (history == null || history.isEmpty()) return false;
        String current = playerCurrent.get(uuid);
        if (current != null) {
            Deque<String> forward = playerForward.computeIfAbsent(uuid, k -> new ArrayDeque<>());
            forward.push(current);
        }
        String previousId = history.pop();
        GUI gui = guiRegistry.get(previousId);
        if (gui == null) return false;
        playerCurrent.put(uuid, previousId);
        gui.open(player);
        if (onNavigate != null) {
            onNavigate.accept(player);
        }
        return true;
    }

    public boolean goForward(Player player) {
        if (player == null) return false;
        UUID uuid = player.getUniqueId();
        Deque<String> forward = playerForward.get(uuid);
        if (forward == null || forward.isEmpty()) return false;
        String current = playerCurrent.get(uuid);
        if (current != null) {
            Deque<String> history = playerHistory.computeIfAbsent(uuid, k -> new ArrayDeque<>());
            history.push(current);
        }
        String nextId = forward.pop();
        GUI gui = guiRegistry.get(nextId);
        if (gui == null) return false;
        playerCurrent.put(uuid, nextId);
        gui.open(player);
        if (onNavigate != null) {
            onNavigate.accept(player);
        }
        return true;
    }

    public String getCurrentId(Player player) {
        if (player == null) return null;
        return playerCurrent.get(player.getUniqueId());
    }

    public boolean canGoBack(Player player) {
        if (player == null) return false;
        Deque<String> history = playerHistory.get(player.getUniqueId());
        return history != null && !history.isEmpty();
    }

    public boolean canGoForward(Player player) {
        if (player == null) return false;
        Deque<String> forward = playerForward.get(player.getUniqueId());
        return forward != null && !forward.isEmpty();
    }

    public void clearHistory(Player player) {
        if (player == null) return;
        UUID uuid = player.getUniqueId();
        playerHistory.remove(uuid);
        playerForward.remove(uuid);
    }

    public void clearAll() {
        playerHistory.clear();
        playerForward.clear();
        playerCurrent.clear();
    }

    public void clearPlayer(Player player) {
        if (player == null) return;
        UUID uuid = player.getUniqueId();
        playerHistory.remove(uuid);
        playerForward.remove(uuid);
        playerCurrent.remove(uuid);
    }

    public void setOnNavigate(Consumer<Player> handler) {
        this.onNavigate = handler;
    }

    public int getHistorySize(Player player) {
        if (player == null) return 0;
        Deque<String> history = playerHistory.get(player.getUniqueId());
        return history == null ? 0 : history.size();
    }

    public GUIItem createBackButton(Player player) {
        if (!canGoBack(player)) {
            return OkasoBukkitGUIItem.builder(org.bukkit.Material.BARRIER)
                .name("&7Sin historial")
                .build();
        }
        return OkasoBukkitGUIItem.builder(org.bukkit.Material.ARROW)
            .name("&eAtrás")
            .clickHandler(event -> goBack(player))
            .build();
    }

    public GUIItem createForwardButton(Player player) {
        if (!canGoForward(player)) {
            return OkasoBukkitGUIItem.builder(org.bukkit.Material.BARRIER)
                .name("&7Sin adelante")
                .build();
        }
        return OkasoBukkitGUIItem.builder(org.bukkit.Material.ARROW)
            .name("&eAdelante")
            .clickHandler(event -> goForward(player))
            .build();
    }

    public GUIItem createNavButton(Player player, String targetId, String displayName, org.bukkit.Material material) {
        GUI target = guiRegistry.get(targetId);
        if (target == null) {
            return OkasoBukkitGUIItem.builder(org.bukkit.Material.BARRIER)
                .name("&cNo disponible")
                .build();
        }
        return OkasoBukkitGUIItem.builder(material)
            .name(displayName)
            .clickHandler(event -> navigate(player, targetId))
            .build();
    }
}
