package com.zaryxstudios.okaso.common.compat;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public final class CompatibilityNames {

    // Legacy (pre-1.13) data values of the 16 dye colors, in order
    private static final String[] COLORS = {
        "WHITE", "ORANGE", "MAGENTA", "LIGHT_BLUE", "YELLOW", "LIME", "PINK", "GRAY",
        "LIGHT_GRAY", "CYAN", "PURPLE", "BLUE", "BROWN", "GREEN", "RED", "BLACK"
    };

    // Modern colored suffix -> legacy material that takes the color as data value
    private static final String[][] COLORED_FAMILIES = {
        {"WOOL", "WOOL"},
        {"STAINED_GLASS_PANE", "STAINED_GLASS_PANE"},
        {"STAINED_GLASS", "STAINED_GLASS"},
        {"TERRACOTTA", "STAINED_CLAY"},
        {"CARPET", "CARPET"},
        {"CONCRETE_POWDER", "CONCRETE_POWDER"},
        {"CONCRETE", "CONCRETE"}
    };

    private static final Map<String, List<String>> MATERIAL_ALIASES = aliases(withColoredFamilies(
        "CLOCK", "WATCH",
        "PLAYER_HEAD", "SKULL_ITEM",
        "PLAYER_WALL_HEAD", "SKULL",
        "GRASS_BLOCK", "GRASS",
        "OAK_PLANKS", "WOOD",
        "OAK_LOG", "LOG",
        "SPRUCE_LOG", "LOG",
        "BIRCH_LOG", "LOG",
        "JUNGLE_LOG", "LOG",
        "ACACIA_LOG", "LOG_2",
        "DARK_OAK_LOG", "LOG_2",
        "COBBLESTONE_WALL", "COBBLE_WALL",
        "STONE_BRICKS", "SMOOTH_BRICK",
        "NETHER_BRICKS", "NETHER_BRICK",
        "CRAFTING_TABLE", "WORKBENCH",
        "ENCHANTING_TABLE", "ENCHANTMENT_TABLE",
        "SPAWNER", "MOB_SPAWNER",
        "IRON_BARS", "IRON_FENCE",
        "GLASS_PANE", "THIN_GLASS",
        "COBWEB", "WEB",
        "FARMLAND", "SOIL",
        "MYCELIUM", "MYCEL",
        "LILY_PAD", "WATER_LILY",
        "POTTED_POPPY", "POTTED_PLANT",
        "OAK_SIGN", "SIGN_POST",
        "OAK_WALL_SIGN", "WALL_SIGN",
        "OAK_TRAPDOOR", "TRAP_DOOR",
        "REDSTONE_TORCH", "REDSTONE_TORCH_ON",
        "REDSTONE_WIRE", "REDSTONE"
    ));

    private static final Map<String, List<String>> ENTITY_ALIASES = aliases(
        "ZOMBIFIED_PIGLIN", "PIG_ZOMBIE",
        "SNOW_GOLEM", "SNOWMAN",
        "IRON_GOLEM", "VILLAGER_GOLEM",
        "OAK_BOAT", "BOAT",
        "OAK_CHEST_BOAT", "CHEST_BOAT",
        "MUSHROOM_COW", "MOOSHROOM",
        "WANDERING_TRADER", "TRADER_LLAMA"
    );

    private static final Map<String, List<String>> SOUND_ALIASES = aliases(
        "AMBIENT_CAVE", "AMBIENCE_CAVE",
        "BLOCK_ANVIL_BREAK", "ANVIL_BREAK",
        "BLOCK_ANVIL_LAND", "ANVIL_LAND",
        "BLOCK_CHEST_OPEN", "CHEST_OPEN",
        "BLOCK_CHEST_CLOSE", "CHEST_CLOSE",
        "ENTITY_PLAYER_LEVELUP", "LEVEL_UP",
        "ENTITY_PLAYER_HURT", "HURT_FLESH",
        "ENTITY_ZOMBIE_AMBIENT", "ZOMBIE_IDLE",
        "ENTITY_ZOMBIE_HURT", "ZOMBIE_HURT",
        "ENTITY_ZOMBIE_DEATH", "ZOMBIE_DEATH",
        "ENTITY_ITEM_PICKUP", "ITEM_PICKUP",
        "UI_BUTTON_CLICK", "CLICK"
    );

    private CompatibilityNames() {}

    public static List<String> materials(String name) {
        return candidates(name, MATERIAL_ALIASES);
    }

    public static List<String> entities(String name) {
        return candidates(name, ENTITY_ALIASES);
    }

    public static List<String> sounds(String name) {
        return candidates(name, SOUND_ALIASES);
    }

    /**
     * Data value of a modern material name on servers before 1.13, e.g. LIME_WOOL -> 5
     * (WOOL:5) and PLAYER_HEAD -> 3 (SKULL_ITEM:3). Returns 0 when no data value applies.
     */
    public static short legacyData(String name) {
        if (name == null) return 0;
        String normalized = normalize(name);
        if (normalized.equals("PLAYER_HEAD") || normalized.equals("PLAYER_WALL_HEAD")) return 3;
        for (String[] family : COLORED_FAMILIES) {
            for (short data = 0; data < COLORS.length; data++) {
                if (normalized.equals(COLORS[data] + "_" + family[0])) return data;
            }
        }
        return 0;
    }

    private static String[] withColoredFamilies(String... values) {
        List<String> result = new ArrayList<>(Arrays.asList(values));
        for (String[] family : COLORED_FAMILIES) {
            for (String color : COLORS) {
                result.add(color + "_" + family[0]);
                result.add(family[1]);
            }
        }
        return result.toArray(new String[0]);
    }

    private static List<String> candidates(String name, Map<String, List<String>> aliases) {
        if (name == null) return Collections.emptyList();
        String normalized = normalize(name);
        Set<String> candidates = new LinkedHashSet<>();
        candidates.add(normalized);
        List<String> mapped = aliases.get(normalized);
        if (mapped != null) candidates.addAll(mapped);
        return Arrays.asList(candidates.toArray(new String[0]));
    }

    private static Map<String, List<String>> aliases(String... values) {
        Map<String, List<String>> result = new LinkedHashMap<>();
        for (int i = 0; i + 1 < values.length; i += 2) {
            String first = normalize(values[i]);
            String second = normalize(values[i + 1]);
            result.computeIfAbsent(first, key -> new ArrayList<>()).add(second);
            result.computeIfAbsent(second, key -> new ArrayList<>()).add(first);
        }
        for (Map.Entry<String, List<String>> entry : result.entrySet()) {
            entry.setValue(Collections.unmodifiableList(entry.getValue()));
        }
        return Collections.unmodifiableMap(result);
    }

    private static String normalize(String name) {
        String normalized = name.trim().toUpperCase().replace('-', '_').replace(' ', '_');
        int separator = normalized.indexOf(':');
        return separator >= 0 ? normalized.substring(separator + 1) : normalized;
    }
}