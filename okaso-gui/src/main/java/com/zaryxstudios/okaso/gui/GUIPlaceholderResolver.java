package com.zaryxstudios.okaso.gui;

import com.zaryxstudios.okaso.common.gui.GUI;
import com.zaryxstudios.okaso.common.gui.GUIItem;

import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;

public class GUIPlaceholderResolver {

    private final Map<String, Function<Player, String>> resolvers;
    private final Map<UUID, Map<String, String>> playerCache;

    public GUIPlaceholderResolver() {
        this.resolvers = new HashMap<>();
        this.playerCache = new HashMap<>();
    }

    public void register(String key, Function<Player, String> resolver) {
        if (key != null && resolver != null) {
            resolvers.put(key, resolver);
        }
    }

    public void unregister(String key) {
        resolvers.remove(key);
    }

    public boolean isRegistered(String key) {
        return resolvers.containsKey(key);
    }

    public String resolve(String text, Player player) {
        if (text == null || player == null) return text;
        String result = text;
        for (Map.Entry<String, Function<Player, String>> entry : resolvers.entrySet()) {
            String placeholder = "%" + entry.getKey() + "%";
            if (result.contains(placeholder)) {
                try {
                    String value = entry.getValue().apply(player);
                    if (value == null) value = "";
                    result = result.replace(placeholder, value);
                } catch (Exception ignored) {
                }
            }
        }
        return result;
    }

    public void resolveGUI(GUI gui, Player player) {
        if (gui == null || player == null) return;
        for (int slot = 0; slot < gui.getSize(); slot++) {
            GUIItem item = gui.getItem(slot);
            if (item == null) continue;
            GUIItem resolved = resolveItem(item, player);
            if (resolved != item) {
                gui.setItem(slot, resolved);
            }
        }
    }

    public GUIItem resolveItem(GUIItem item, Player player) {
        if (item == null || player == null) return item;
        Object bukkitItem = item.getItemStack();
        if (!(bukkitItem instanceof ItemStack)) return item;
        ItemStack stack = ((ItemStack) bukkitItem).clone();
        ItemMeta meta = stack.getItemMeta();
        if (meta == null) return item;
        boolean changed = false;
        if (meta.hasDisplayName()) {
            String resolved = resolve(meta.getDisplayName(), player);
            if (!resolved.equals(meta.getDisplayName())) {
                meta.setDisplayName(resolved);
                changed = true;
            }
        }
        if (meta.hasLore()) {
            List<String> lore = meta.getLore();
            List<String> newLore = new ArrayList<>(lore);
            boolean loreChanged = false;
            for (int i = 0; i < newLore.size(); i++) {
                String resolved = resolve(newLore.get(i), player);
                if (!resolved.equals(newLore.get(i))) {
                    newLore.set(i, resolved);
                    loreChanged = true;
                }
            }
            if (loreChanged) {
                meta.setLore(newLore);
                changed = true;
            }
        }
        if (changed) {
            stack.setItemMeta(meta);
            if (item instanceof BukkitGUIItem) {
                BukkitGUIItem copy = ((BukkitGUIItem) item).copy();
                copy.setItemStack(stack);
                return copy;
            }
        }
        return item;
    }

    public String resolveCached(String key, Player player) {
        if (player == null || key == null) return null;
        Map<String, String> cache = playerCache.get(player.getUniqueId());
        if (cache != null && cache.containsKey(key)) {
            return cache.get(key);
        }
        Function<Player, String> resolver = resolvers.get(key);
        if (resolver == null) return null;
        try {
            String value = resolver.apply(player);
            if (value == null) value = "";
            playerCache.computeIfAbsent(player.getUniqueId(), k -> new HashMap<>()).put(key, value);
            return value;
        } catch (Exception e) {
            return "";
        }
    }

    public void clearCache(Player player) {
        if (player != null) {
            playerCache.remove(player.getUniqueId());
        }
    }

    public void clearAllCache() {
        playerCache.clear();
    }

    public void clearCache(String key) {
        for (Map<String, String> cache : playerCache.values()) {
            cache.remove(key);
        }
    }

    public static GUIPlaceholderResolver createDefault() {
        GUIPlaceholderResolver resolver = new GUIPlaceholderResolver();
        resolver.register("player_name", Player::getName);
        resolver.register("player_uuid", p -> p.getUniqueId().toString());
        resolver.register("player_health", p -> String.valueOf((int) p.getHealth()));
        resolver.register("player_max_health", p -> String.valueOf((int) p.getAttribute(org.bukkit.attribute.Attribute.GENERIC_MAX_HEALTH).getValue()));
        resolver.register("player_food", p -> String.valueOf(p.getFoodLevel()));
        resolver.register("player_level", p -> String.valueOf(p.getLevel()));
        resolver.register("player_exp", p -> String.valueOf(Math.round(p.getExp() * 100)));
        resolver.register("player_world", p -> p.getWorld().getName());
        resolver.register("player_gamemode", p -> p.getGameMode().name());
        resolver.register("player_ping", p -> {
            try {
                Object handle = p.getClass().getMethod("getHandle").invoke(p);
                return String.valueOf(handle.getClass().getField("ping").getInt(handle));
            } catch (Exception e) {
                return "N/A";
            }
        });
        resolver.register("online_count", p -> String.valueOf(p.getServer().getOnlinePlayers().size()));
        resolver.register("max_players", p -> String.valueOf(p.getServer().getMaxPlayers()));
        resolver.register("server_name", p -> p.getServer().getClass().getSimpleName());
        resolver.register("server_version", p -> p.getServer().getBukkitVersion());
        return resolver;
    }
}
