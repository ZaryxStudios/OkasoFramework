package com.zaryxstudios.okaso.world.structure;

import org.bukkit.Material;
import org.bukkit.block.Block;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

public final class MaterialResolver {

    private static final Logger LOG = Logger.getLogger(MaterialResolver.class.getName());

    private static final Map<String, String> LEGACY_TO_MODERN = new HashMap<>();

    private static final Map<String, String> MODERN_TO_LEGACY = new HashMap<>();

    private static final Method SET_TYPE_WITH_PHYSICS;
    private static final boolean HAS_SET_TYPE_WITH_PHYSICS;

    static {
        addMapping("WOOD", "OAK_PLANKS",      "SPRUCE_PLANKS",      "BIRCH_PLANKS",      "JUNGLE_PLANKS",      "ACACIA_PLANKS",      "DARK_OAK_PLANKS");
        addMapping("LOG", "OAK_LOG",          "SPRUCE_LOG",         "BIRCH_LOG",         "JUNGLE_LOG",         "ACACIA_LOG",         "DARK_OAK_LOG");
        addMapping("LOG_2", "ACACIA_LOG",       "DARK_OAK_LOG");
        addMapping("LEAVES", "OAK_LEAVES",       "SPRUCE_LEAVES",      "BIRCH_LEAVES",       "JUNGLE_LEAVES");
        addMapping("LEAVES_2", "ACACIA_LEAVES",    "DARK_OAK_LEAVES");
        addMapping("SAPLING", "OAK_SAPLING",      "SPRUCE_SAPLING",     "BIRCH_SAPLING",      "JUNGLE_SAPLING",     "ACACIA_SAPLING",     "DARK_OAK_SAPLING");
        addMapping("FENCE", "OAK_FENCE",        "SPRUCE_FENCE",       "BIRCH_FENCE",        "JUNGLE_FENCE",       "ACACIA_FENCE",       "DARK_OAK_FENCE", "NETHER_BRICK_FENCE");
        addMapping("FENCE_GATE", "OAK_FENCE_GATE",   "SPRUCE_FENCE_GATE",  "BIRCH_FENCE_GATE",   "JUNGLE_FENCE_GATE",  "ACACIA_FENCE_GATE",  "DARK_OAK_FENCE_GATE");
        addMapping("WOOD_STAIRS",  "OAK_STAIRS");
        addMapping("SPRUCE_WOOD_STAIRS", "SPRUCE_STAIRS");
        addMapping("BIRCH_WOOD_STAIRS", "BIRCH_STAIRS");
        addMapping("JUNGLE_WOOD_STAIRS", "JUNGLE_STAIRS");
        addMapping("WOODEN_DOOR", "OAK_DOOR");
        addMapping("WOODEN_TRAPDOOR","OAK_TRAPDOOR");
        addMapping("WOOD_BUTTON", "OAK_BUTTON");
        addMapping("WOOD_PLATE", "OAK_PRESSURE_PLATE");
        addMapping("STONE_PLATE", "STONE_PRESSURE_PLATE");
        addMapping("STEP", "OAK_SLAB",          "STONE_SLAB",         "SANDSTONE_SLAB",     "COBBLESTONE_SLAB",   "BRICK_SLAB",         "STONE_BRICK_SLAB",  "NETHER_BRICK_SLAB", "QUARTZ_SLAB");
        addMapping("DOUBLE_STEP", "OAK_DOUBLE_SLAB",   "STONE_DOUBLE_SLAB");
        addMapping("STONE_BRICK_STAIRS", "STONE_BRICK_STAIRS");
        addMapping("SMOOTH_BRICK", "STONE_BRICKS");
        addMapping("SMOOTH_STAIRS", "STONE_BRICK_STAIRS");
        addMapping("SMOOTH_DOUBLE_STEP", "STONE_BRICK_DOUBLE_SLAB");
        addMapping("STAINED_CLAY", "WHITE_TERRACOTTA",  "ORANGE_TERRACOTTA",  "MAGENTA_TERRACOTTA", "LIGHT_BLUE_TERRACOTTA", "YELLOW_TERRACOTTA", "LIME_TERRACOTTA",  "PINK_TERRACOTTA", "GRAY_TERRACOTTA", "LIGHT_GRAY_TERRACOTTA", "CYAN_TERRACOTTA", "PURPLE_TERRACOTTA", "BLUE_TERRACOTTA", "BROWN_TERRACOTTA", "GREEN_TERRACOTTA", "RED_TERRACOTTA", "BLACK_TERRACOTTA");
        addMapping("HARD_CLAY", "TERRACOTTA");
        addMapping("STAINED_GLASS", "WHITE_STAINED_GLASS", "ORANGE_STAINED_GLASS", "MAGENTA_STAINED_GLASS", "LIGHT_BLUE_STAINED_GLASS", "YELLOW_STAINED_GLASS", "LIME_STAINED_GLASS", "PINK_STAINED_GLASS", "GRAY_STAINED_GLASS", "LIGHT_GRAY_STAINED_GLASS", "CYAN_STAINED_GLASS", "PURPLE_STAINED_GLASS", "BLUE_STAINED_GLASS", "BROWN_STAINED_GLASS", "GREEN_STAINED_GLASS", "RED_STAINED_GLASS", "BLACK_STAINED_GLASS");
        addMapping("STAINED_GLASS_PANE", "WHITE_STAINED_GLASS_PANE", "ORANGE_STAINED_GLASS_PANE", "MAGENTA_STAINED_GLASS_PANE", "LIGHT_BLUE_STAINED_GLASS_PANE", "YELLOW_STAINED_GLASS_PANE", "LIME_STAINED_GLASS_PANE", "PINK_STAINED_GLASS_PANE", "GRAY_STAINED_GLASS_PANE", "LIGHT_GRAY_STAINED_GLASS_PANE", "CYAN_STAINED_GLASS_PANE", "PURPLE_STAINED_GLASS_PANE", "BLUE_STAINED_GLASS_PANE", "BROWN_STAINED_GLASS_PANE", "GREEN_STAINED_GLASS_PANE", "RED_STAINED_GLASS_PANE", "BLACK_STAINED_GLASS_PANE");
        addMapping("WOOL", "WHITE_WOOL",       "ORANGE_WOOL",        "MAGENTA_WOOL",       "LIGHT_BLUE_WOOL",    "YELLOW_WOOL",        "LIME_WOOL",         "PINK_WOOL", "GRAY_WOOL", "LIGHT_GRAY_WOOL", "CYAN_WOOL", "PURPLE_WOOL", "BLUE_WOOL", "BROWN_WOOL", "GREEN_WOOL", "RED_WOOL", "BLACK_WOOL");
        addMapping("BED", "RED_BED",          "WHITE_BED",          "ORANGE_BED",         "MAGENTA_BED",        "LIGHT_BLUE_BED",     "YELLOW_BED",        "LIME_BED", "PINK_BED", "GRAY_BED", "LIGHT_GRAY_BED", "CYAN_BED", "PURPLE_BED", "BLUE_BED", "BROWN_BED", "GREEN_BED", "BLACK_BED");
        addMapping("BANNER", "WHITE_BANNER",     "ORANGE_BANNER",      "MAGENTA_BANNER",     "LIGHT_BLUE_BANNER",  "YELLOW_BANNER",      "LIME_BANNER",       "PINK_BANNER", "GRAY_BANNER", "LIGHT_GRAY_BANNER", "CYAN_BANNER", "PURPLE_BANNER", "BLUE_BANNER", "BROWN_BANNER", "GREEN_BANNER", "RED_BANNER", "BLACK_BANNER");
        addMapping("WALL_BANNER", "WHITE_WALL_BANNER", "ORANGE_WALL_BANNER", "MAGENTA_WALL_BANNER", "LIGHT_BLUE_WALL_BANNER", "YELLOW_WALL_BANNER", "LIME_WALL_BANNER", "PINK_WALL_BANNER", "GRAY_WALL_BANNER", "LIGHT_GRAY_WALL_BANNER", "CYAN_WALL_BANNER", "PURPLE_WALL_BANNER", "BLUE_WALL_BANNER", "BROWN_WALL_BANNER", "GREEN_WALL_BANNER", "RED_WALL_BANNER", "BLACK_WALL_BANNER");
        addMapping("SKULL", "SKELETON_SKULL",   "WITHER_SKELETON_SKULL", "ZOMBIE_HEAD",      "PLAYER_HEAD",        "CREEPER_HEAD",       "DRAGON_HEAD");
        addMapping("WALL_SKULL", "SKELETON_WALL_SKULL", "WITHER_SKELETON_WALL_SKULL", "ZOMBIE_WALL_HEAD", "PLAYER_WALL_HEAD", "CREEPER_WALL_HEAD", "DRAGON_WALL_HEAD");
        addMapping("SIGN_POST", "OAK_SIGN");
        addMapping("WALL_SIGN", "OAK_WALL_SIGN");
        addMapping("STANDING_BANNER", "WHITE_BANNER");
        addMapping("CARPET", "WHITE_CARPET",     "ORANGE_CARPET",      "MAGENTA_CARPET",     "LIGHT_BLUE_CARPET",  "YELLOW_CARPET",      "LIME_CARPET",       "PINK_CARPET", "GRAY_CARPET", "LIGHT_GRAY_CARPET", "CYAN_CARPET", "PURPLE_CARPET", "BLUE_CARPET", "BROWN_CARPET", "GREEN_CARPET", "RED_CARPET", "BLACK_CARPET");
        addMapping("DOUBLE_PLANT", "SUNFLOWER",        "LILAC",              "TALL_GRASS",         "LARGE_FERN",         "ROSE_BUSH",          "PEONY");
        addMapping("LONG_GRASS", "GRASS",            "FERN",               "DEAD_BUSH");
        addMapping("RED_ROSE", "POPPY",            "BLUE_ORCHID",        "ALLIUM",             "AZURE_BLUET",        "RED_TULIP",          "ORANGE_TULIP", "WHITE_TULIP", "PINK_TULIP", "OXEYE_DAISY", "CORNFLOWER", "LILY_OF_THE_VALLEY", "WITHER_ROSE");
        addMapping("YELLOW_FLOWER", "DANDELION");
        addMapping("FLOWER_POT", "FLOWER_POT");
        addMapping("POTTED_FLOWER", "POTTED_POPPY",     "POTTED_BLUE_ORCHID", "POTTED_ALLIUM",      "POTTED_AZURE_BLUET", "POTTED_RED_TULIP",   "POTTED_ORANGE_TULIP", "POTTED_WHITE_TULIP", "POTTED_PINK_TULIP", "POTTED_OXEYE_DAISY", "POTTED_DANDELION", "POTTED_OAK_SAPLING", "POTTED_SPRUCE_SAPLING", "POTTED_BIRCH_SAPLING", "POTTED_JUNGLE_SAPLING", "POTTED_ACACIA_SAPLING", "POTTED_DARK_OAK_SAPLING", "POTTED_FERN", "POTTED_CACTUS", "POTTED_DEAD_BUSH", "POTTED_RED_MUSHROOM", "POTTED_BROWN_MUSHROOM");
        addMapping("WORKBENCH", "CRAFTING_TABLE");
        addMapping("ENCHANTMENT_TABLE", "ENCHANTING_TABLE");
        addMapping("MOB_SPAWNER", "SPAWNER");
        addMapping("MONSTER_EGGS", "INFESTED_STONE",   "INFESTED_COBBLESTONE", "INFESTED_STONE_BRICKS", "INFESTED_MOSSY_STONE_BRICKS", "INFESTED_CRACKED_STONE_BRICKS", "INFESTED_CHISELED_STONE_BRICKS");
        addMapping("BURNING_FURNACE", "FURNACE");
        addMapping("REDSTONE_COMPARATOR", "COMPARATOR");
        addMapping("REDSTONE_COMPARATOR_OFF", "COMPARATOR");
        addMapping("REDSTONE_COMPARATOR_ON", "COMPARATOR");
        addMapping("DIODE_BLOCK", "REPEATER");
        addMapping("DIODE_BLOCK_OFF","REPEATER");
        addMapping("DIODE_BLOCK_ON", "REPEATER");
        addMapping("IRON_FENCE", "IRON_BARS");
        addMapping("THIN_GLASS", "GLASS_PANE");
        addMapping("NETHER_FENCE", "NETHER_BRICK_FENCE");
        addMapping("NETHER_WARTS", "NETHER_WART");
        addMapping("SNOW", "SNOW");
        addMapping("SNOW_BLOCK",  "SNOW_BLOCK");
        addMapping("SUGAR_CANE_BLOCK","SUGAR_CANE");
        addMapping("CAKE_BLOCK", "CAKE");
        addMapping("BREWING_STAND",  "BREWING_STAND");
        addMapping("CAULDRON", "CAULDRON");
        addMapping("SKULL", "SKELETON_SKULL");
        addMapping("PISTON_BASE", "PISTON");
        addMapping("PISTON_STICKY_BASE", "STICKY_PISTON");
        addMapping("PISTON_MOVING_PIECE", "MOVING_PISTON");
        addMapping("HUGE_MUSHROOM_1","BROWN_MUSHROOM_BLOCK");
        addMapping("HUGE_MUSHROOM_2","RED_MUSHROOM_BLOCK");
        addMapping("SOIL", "FARMLAND");
        addMapping("GRASS", "GRASS_BLOCK");
        addMapping("MYCEL", "MYCELIUM");
        addMapping("WATER_LILY", "LILY_PAD");
        addMapping("STATIONARY_WATER","WATER");
        addMapping("STATIONARY_LAVA","LAVA");
        addMapping("BEDROCK",  "BEDROCK");
        addMapping("COBBLESTONE", "COBBLESTONE");
        addMapping("MOSSY_COBBLESTONE", "MOSSY_COBBLESTONE");
        addMapping("STONE", "STONE",            "GRANITE",             "DIORITE",            "ANDESITE",           "POLISHED_GRANITE",   "POLISHED_DIORITE",  "POLISHED_ANDESITE");
        addMapping("COBBLESTONE_WALL", "COBBLESTONE_WALL", "MOSSY_COBBLESTONE_WALL");
        addMapping("QUARTZ_BLOCK", "QUARTZ_BLOCK",     "CHISELED_QUARTZ_BLOCK", "QUARTZ_PILLAR");
        addMapping("SANDSTONE", "SANDSTONE",        "CHISELED_SANDSTONE",  "CUT_SANDSTONE",      "SMOOTH_SANDSTONE");
        addMapping("RED_SANDSTONE", "RED_SANDSTONE",    "CHISELED_RED_SANDSTONE", "CUT_RED_SANDSTONE", "SMOOTH_RED_SANDSTONE");
        addMapping("NETHER_BRICK",  "NETHER_BRICKS");
        addMapping("NETHER_BRICK_SLAB", "NETHER_BRICK_SLAB");
        addMapping("BRICK", "BRICKS");
        addMapping("HUGE_MUSHROOM", "BROWN_MUSHROOM_BLOCK");
        addMapping("PURPUR_DOUBLE_SLAB", "PURPUR_SLAB");
        addMapping("END_BRICKS", "END_STONE_BRICKS");
        addMapping("TRAP_DOOR", "OAK_TRAPDOOR");
        addMapping("IRON_TRAPDOOR", "IRON_TRAPDOOR");
        addMapping("WATER", "WATER");
        addMapping("LAVA", "LAVA");
        addMapping("WEB", "COBWEB");
        addMapping("TNT",   "TNT");
        addMapping("BOOKSHELF", "BOOKSHELF");
        addMapping("OBSIDIAN", "OBSIDIAN");
        addMapping("GLOWSTONE", "GLOWSTONE");
        addMapping("NETHERRACK", "NETHERRACK");
        addMapping("SOUL_SAND", "SOUL_SAND");
        addMapping("SOUL_SOIL", "SOUL_SOIL");
        addMapping("SLIME_BLOCK", "SLIME_BLOCK");
        addMapping("ENDER_STONE", "END_STONE");
        addMapping("SEA_LANTERN", "SEA_LANTERN");
        addMapping("PRISMARINE", "PRISMARINE",       "PRISMARINE_BRICKS",   "DARK_PRISMARINE");
        addMapping("SPONGE", "SPONGE",           "WET_SPONGE");
        addMapping("PACKED_ICE", "PACKED_ICE");
        addMapping("ICE", "ICE",              "BLUE_ICE",            "FROSTED_ICE");
        addMapping("MAGMA", "MAGMA_BLOCK");
        addMapping("RED_NETHER_BRICK", "RED_NETHER_BRICKS");
        addMapping("BONE_BLOCK", "BONE_BLOCK");
        addMapping("OBSERVER", "OBSERVER");
        addMapping("WHITE_SHULKER_BOX", "SHULKER_BOX",   "WHITE_SHULKER_BOX",  "ORANGE_SHULKER_BOX", "MAGENTA_SHULKER_BOX", "LIGHT_BLUE_SHULKER_BOX", "YELLOW_SHULKER_BOX", "LIME_SHULKER_BOX", "PINK_SHULKER_BOX", "GRAY_SHULKER_BOX", "LIGHT_GRAY_SHULKER_BOX", "CYAN_SHULKER_BOX", "PURPLE_SHULKER_BOX", "BLUE_SHULKER_BOX", "BROWN_SHULKER_BOX", "GREEN_SHULKER_BOX", "RED_SHULKER_BOX", "BLACK_SHULKER_BOX");

        Method setTypePhysics = null;
        boolean hasPhysics = false;
        try {
            setTypePhysics = Block.class.getMethod("setType", Material.class, boolean.class);
            hasPhysics = true;
        } catch (NoSuchMethodException e) {
            hasPhysics = false;
        }
        SET_TYPE_WITH_PHYSICS = setTypePhysics;
        HAS_SET_TYPE_WITH_PHYSICS = hasPhysics;
    }

    private static void addMapping(String legacyName, String... modernNames) {
        for (String modern : modernNames) {
            LEGACY_TO_MODERN.put(legacyName, modern); 
            MODERN_TO_LEGACY.put(modern, legacyName);
        }
    }

    private MaterialResolver() {}

    public static Material resolveMaterial(String name) {
        if (name == null || name.isEmpty()) {
            return Material.STONE;
        }

        if (name.startsWith("minecraft:")) {
            name = name.substring(10);
        }

        name = name.toUpperCase();

        if (name.equals("AIR") || name.equals("CAVE_AIR") || name.equals("VOID_AIR")) {
            return Material.AIR;
        }

        int bracketIdx = name.indexOf('[');
        if (bracketIdx > 0) {
            name = name.substring(0, bracketIdx);
        }

        try {
            return Material.valueOf(name);
        } catch (IllegalArgumentException ignored) {
        }

        String legacy = MODERN_TO_LEGACY.get(name);
        if (legacy != null) {
            try {
                return Material.valueOf(legacy);
            } catch (IllegalArgumentException ignored) {
            }
        }

        for (Map.Entry<String, String> entry : LEGACY_TO_MODERN.entrySet()) {
            if (entry.getValue().equals(name)) {
                try {
                    return Material.valueOf(entry.getKey());
                } catch (IllegalArgumentException ignored) {
                }
            }
        }

        String variant = resolveVariant(name);
        if (variant != null) {
            try {
                return Material.valueOf(variant);
            } catch (IllegalArgumentException ignored) {
            }
        }

        LOG.fine("Unknown material '" + name + "', falling back to STONE");
        return Material.STONE;
    }

    public static void setBlockSilently(Block block, Material material) {
        if (HAS_SET_TYPE_WITH_PHYSICS) {
            try {
                SET_TYPE_WITH_PHYSICS.invoke(block, material, false);
                return;
            } catch (Exception ignored) {
            }
        }
        block.setType(material);
    }

    public static void setLegacyData(Block block, byte data) {
        if (data == 0) return;
        try {
            block.getState().setRawData(data);
            block.getState().update();
        } catch (Exception ignored) {
        }
    }

    public static byte getLegacyData(Block block) {
        try {
            return block.getState().getRawData();
        } catch (Exception ignored) {
            return 0;
        }
    }

    public static void ensureChunkLoaded(Block block) {
        if (!block.getChunk().isLoaded()) {
            block.getChunk().load();
        }
    }

    private static String resolveVariant(String name) {
        if (name.startsWith("WALL_")) {
            String base = name.substring(5);
            try {
                Material.valueOf(base);
                return base;
            } catch (IllegalArgumentException ignored) {}
        }

        if (!name.endsWith("_BLOCK")) {
            String withBlock = name + "_BLOCK";
            try {
                Material.valueOf(withBlock);
                return withBlock;
            } catch (IllegalArgumentException ignored) {}
        }

        if (name.endsWith("_BLOCK")) {
            String without = name.substring(0, name.length() - 6);
            try {
                Material.valueOf(without);
                return without;
            } catch (IllegalArgumentException ignored) {}
        }

        if (name.startsWith("POTTED_")) {
            return "FLOWER_POT";
        }

        if (name.contains("STEM")) {
            return "MELON";
        }

        if (name.contains("WALL_TORCH")) {
            return "TORCH";
        }

        if (name.contains("WALL_SKULL") || name.contains("SKULL")) {
            return "SKELETON_SKULL";
        }

        return null;
    }
}
