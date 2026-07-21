package com.zaryxstudios.okaso.gui;

import com.zaryxstudios.okaso.common.gui.GUIClickEvent;
import com.zaryxstudios.okaso.common.gui.GUIItem;
import com.zaryxstudios.okaso.common.text.TextColorizer;

import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Base64;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

import lombok.Getter;
import lombok.Setter;

public class BukkitGUIItem implements GUIItem {

    private ItemStack itemStack;

    public void setItemStack(ItemStack itemStack) {
        this.itemStack = itemStack;
        this.enchantmentsBeforeGlow = null;
    }

    @Override
    public ItemStack getItemStack() {
        return itemStack;
    }

    @Getter @Setter
    private GUIClickHandler clickHandler;

    private Map<Enchantment, Integer> enchantmentsBeforeGlow;

    @FunctionalInterface
    public interface GUIClickHandler {
        void onClick(GUIClickEvent event);
    }

    public BukkitGUIItem(ItemStack itemStack, GUIClickHandler clickHandler) {
        this.itemStack = itemStack;
        this.clickHandler = clickHandler;
    }

    public BukkitGUIItem(ItemStack itemStack) {
        this(itemStack, null);
    }

    @Override
    public void onClick(GUIClickEvent event) {
        if (clickHandler != null) {
            clickHandler.onClick(event);
        }
    }

    public static BukkitGUIItem of(ItemStack itemStack, GUIClickHandler clickHandler) {
        return new BukkitGUIItem(itemStack, clickHandler);
    }

    public static BukkitGUIItem of(ItemStack itemStack) {
        return new BukkitGUIItem(itemStack);
    }

    public static BukkitGUIItem of(Material material) {
        return new BukkitGUIItem(new ItemStack(material));
    }

    public static BukkitGUIItem of(Material material, int amount) {
        return new BukkitGUIItem(new ItemStack(material, amount));
    }

    public static BukkitGUIItem of(Material material, String displayName) {
        return builder(material).name(displayName).build();
    }

    public static BukkitGUIItem empty() {
        return new BukkitGUIItem(new ItemStack(Material.AIR));
    }

    public static Builder builder() {
        return new Builder();
    }

    public static Builder builder(Material material) {
        return new Builder().material(material);
    }

    public static Builder builder(ItemStack itemStack) {
        return new Builder().itemStack(itemStack);
    }

    public static Builder builder(Material material, int amount) {
        return new Builder().material(material).amount(amount);
    }

    public static final class Builder {
        private Material material = Material.STONE;
        private int amount = 1;
        private String displayName;
        private List<String> lore;
        private Map<Enchantment, Integer> enchantments = new HashMap<>();
        private boolean glow;
        private boolean unbreakable;
        private int customModelData = -1;
        private short durability = -1;
        private Collection<ItemFlag> flags = new ArrayList<>();
        private String skullOwner;
        private String texture;
        private GUIClickHandler clickHandler;

        public Builder itemStack(ItemStack stack) {
            if (stack == null) return this;
            this.material = stack.getType();
            this.amount = stack.getAmount();
            if (stack.hasItemMeta()) {
                ItemMeta meta = stack.getItemMeta();
                if (meta.hasDisplayName()) this.displayName = meta.getDisplayName();
                if (meta.hasLore()) this.lore = meta.getLore();
                if (meta.hasEnchants()) this.enchantments.putAll(meta.getEnchants());
                this.flags.addAll(meta.getItemFlags());
                if (meta instanceof SkullMeta) {
                    SkullMeta skull = (SkullMeta) meta;
                    if (skull.hasOwner()) {
                        this.skullOwner = skull.getOwner();
                    }
                }
                try {
                    this.unbreakable = meta.isUnbreakable();
                } catch (NoSuchMethodError ignored) {}
                try {
                    this.customModelData = meta.getCustomModelData();
                } catch (NoSuchMethodError ignored) {}
            }
            return this;
        }

        private Builder() {}

        public Builder material(Material material) {
            this.material = material;
            return this;
        }

        public Builder amount(int amount) {
            this.amount = amount;
            return this;
        }

        public Builder name(String name) {
            this.displayName = name;
            return this;
        }

        public Builder lore(String... lines) {
            this.lore = new ArrayList<>(Arrays.asList(lines));
            return this;
        }

        public Builder lore(List<String> lines) {
            this.lore = new ArrayList<>(lines);
            return this;
        }

        public Builder loreLine(String line) {
            if (this.lore == null) this.lore = new ArrayList<>();
            this.lore.add(line);
            return this;
        }

        public Builder enchant(Enchantment enchantment, int level) {
            this.enchantments.put(enchantment, level);
            return this;
        }

        public Builder enchantList(Map<Enchantment, Integer> enchantments) {
            if (enchantments != null) {
                this.enchantments.putAll(enchantments);
            }
            return this;
        }

        public Builder glow() {
            this.glow = true;
            return this;
        }

        public Builder unbreakable() {
            this.unbreakable = true;
            return this;
        }

        public Builder customModelData(int data) {
            this.customModelData = data;
            return this;
        }

        public Builder durability(short durability) {
            this.durability = durability;
            return this;
        }

        public Builder flag(ItemFlag flag) {
            this.flags.add(flag);
            return this;
        }

        public Builder hideAttributes() {
            this.flags.add(ItemFlag.HIDE_ATTRIBUTES);
            return this;
        }

        public Builder skullOwner(String owner) {
            this.skullOwner = owner;
            return this;
        }

        public Builder headTexture(String base64) {
            this.texture = base64;
            if (base64 != null && material != Material.PLAYER_HEAD && material != Material.PLAYER_WALL_HEAD) {
                this.material = Material.PLAYER_HEAD;
            }
            return this;
        }

        public Builder clickHandler(GUIClickHandler handler) {
            this.clickHandler = handler;
            return this;
        }

        private void applyGlow(ItemMeta meta) {
            meta.addEnchant(Enchantment.DURABILITY, 1, true);
        }

        public BukkitGUIItem build() {
            ItemStack stack = new ItemStack(material, amount);

            if (durability >= 0) {
                stack.setDurability(durability);
            }

            Map<Enchantment, Integer> preGlowEnchants = null;
            ItemMeta meta = stack.getItemMeta();
            if (meta != null) {
                if (displayName != null) {
                    meta.setDisplayName(TextColorizer.translate(displayName));
                }
                if (lore != null && !lore.isEmpty()) {
                    meta.setLore(lore.stream()
                        .map(TextColorizer::translate)
                        .collect(Collectors.toList()));
                }
                if (!enchantments.isEmpty()) {
                    for (Map.Entry<Enchantment, Integer> e : enchantments.entrySet()) {
                        meta.addEnchant(e.getKey(), e.getValue(), true);
                    }
                }
                if (glow) {
                    preGlowEnchants = new HashMap<>(meta.getEnchants());
                    if (!meta.hasEnchants()) {
                        applyGlow(meta);
                    }
                    meta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
                }
                if (unbreakable) {
                    try {
                        meta.setUnbreakable(true);
                    } catch (NoSuchMethodError ignored) {
                    }
                    meta.addItemFlags(ItemFlag.HIDE_UNBREAKABLE);
                }
                if (customModelData >= 0) {
                    try {
                        meta.setCustomModelData(customModelData);
                    } catch (NoSuchMethodError ignored) {
                    }
                }
                if (!flags.isEmpty()) {
                    meta.addItemFlags(flags.toArray(new ItemFlag[0]));
                }
                if (meta instanceof SkullMeta) {
                    SkullMeta skullMeta = (SkullMeta) meta;
                    if (texture != null && !texture.isEmpty()) {
                        try {
                            Method setProfile = skullMeta.getClass().getDeclaredMethod("setProfile", Class.forName("com.mojang.authlib.GameProfile"));
                            setProfile.setAccessible(true);
                            Object profile = createGameProfile(texture);
                            setProfile.invoke(skullMeta, profile);
                        } catch (Exception ignored) {
                            if (skullOwner != null) {
                                skullMeta.setOwner(skullOwner);
                            }
                        }
                    } else if (skullOwner != null) {
                        skullMeta.setOwner(skullOwner);
                    }
                }
                stack.setItemMeta(meta);
            }

            BukkitGUIItem result = new BukkitGUIItem(stack, clickHandler);
            if (preGlowEnchants != null) {
                result.enchantmentsBeforeGlow = preGlowEnchants;
            }
            return result;
        }
    }

    private static Object createGameProfile(String base64Texture) {
        try {
            Class<?> gameProfileClass = Class.forName("com.mojang.authlib.GameProfile");
            java.lang.reflect.Constructor<?> constructor = gameProfileClass.getConstructor(UUID.class, String.class);
            Object profile = constructor.newInstance(UUID.randomUUID(), "OkasoHead");
            Class<?> propertyMapClass = Class.forName("com.mojang.authlib.properties.PropertyMap");
            Method getProperties = gameProfileClass.getMethod("getProperties");
            Object propertyMap = getProperties.invoke(profile);
            Class<?> propertyClass = Class.forName("com.mojang.authlib.properties.Property");
            java.lang.reflect.Constructor<?> propConstructor = propertyClass.getConstructor(String.class, String.class, String.class);
            Object property = propConstructor.newInstance("textures", base64Texture, "");
            Method put = propertyMapClass.getMethod("put", Object.class, Object.class);
            put.invoke(propertyMap, "textures", property);
            return profile;
        } catch (Exception e) {
            return null;
        }
    }

    public boolean isAir() {
        return itemStack == null || itemStack.getType() == Material.AIR;
    }

    public boolean isSimilar(ItemStack other) {
        return itemStack != null && itemStack.isSimilar(other);
    }

    public int getAmount() {
        return itemStack == null ? 0 : itemStack.getAmount();
    }

    public void setAmount(int amount) {
        if (itemStack != null && amount > 0 && amount <= itemStack.getMaxStackSize()) {
            itemStack.setAmount(amount);
        }
    }

    public String getDisplayName() {
        if (itemStack == null) return null;
        ItemMeta meta = itemStack.getItemMeta();
        return meta == null ? null : meta.getDisplayName();
    }

    public void setDisplayName(String name) {
        if (itemStack == null) return;
        ItemMeta meta = itemStack.getItemMeta();
        if (meta != null) {
            if (name != null) {
                meta.setDisplayName(TextColorizer.translate(name));
            } else {
                meta.setDisplayName(null);
            }
            itemStack.setItemMeta(meta);
        }
    }

    public List<String> getLore() {
        if (itemStack == null) return null;
        ItemMeta meta = itemStack.getItemMeta();
        return meta == null ? null : meta.getLore();
    }

    public void setLore(List<String> lore) {
        if (itemStack == null) return;
        ItemMeta meta = itemStack.getItemMeta();
        if (meta != null) {
            if (lore != null) {
                meta.setLore(lore.stream()
                    .filter(java.util.Objects::nonNull)
                    .map(TextColorizer::translate)
                    .collect(Collectors.toList()));
            } else {
                meta.setLore(null);
            }
            itemStack.setItemMeta(meta);
        }
    }

    public void setLore(String... lore) {
        if (lore != null) {
            setLore(Arrays.asList(lore));
        } else {
            setLore((List<String>) null);
        }
    }

    public void addLore(String line) {
        if (itemStack == null || line == null) return;
        ItemMeta meta = itemStack.getItemMeta();
        if (meta != null) {
            List<String> lore = meta.getLore();
            if (lore == null) {
                lore = new ArrayList<>();
            } else {
                lore = new ArrayList<>(lore);
            }
            lore.add(TextColorizer.translate(line));
            meta.setLore(lore);
            itemStack.setItemMeta(meta);
        }
    }

    public void clearLore() {
        if (itemStack == null) return;
        ItemMeta meta = itemStack.getItemMeta();
        if (meta != null) {
            meta.setLore(null);
            itemStack.setItemMeta(meta);
        }
    }

    public boolean hasGlow() {
        if (itemStack == null) return false;
        if (enchantmentsBeforeGlow != null) return true;
        ItemMeta meta = itemStack.getItemMeta();
        if (meta == null || !meta.hasEnchants()) return false;
        return meta.getItemFlags().contains(ItemFlag.HIDE_ENCHANTS);
    }

    public void setGlow(boolean glow) {
        if (itemStack == null) return;
        ItemMeta meta = itemStack.getItemMeta();
        if (meta == null) return;
        if (glow) {
            if (!meta.hasEnchants()) {
                this.enchantmentsBeforeGlow = new HashMap<>();
                meta.addEnchant(Enchantment.DURABILITY, 1, true);
            }
            meta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
        } else {
            if (this.enchantmentsBeforeGlow != null) {
                Map<Enchantment, Integer> original = this.enchantmentsBeforeGlow;
                this.enchantmentsBeforeGlow = null;
                if (original.isEmpty()) {
                    meta.removeEnchant(Enchantment.DURABILITY);
                } else {
                    for (Enchantment e : new ArrayList<>(meta.getEnchants().keySet())) {
                        meta.removeEnchant(e);
                    }
                    for (Map.Entry<Enchantment, Integer> e : original.entrySet()) {
                        meta.addEnchant(e.getKey(), e.getValue(), true);
                    }
                }
            }
            meta.removeItemFlags(ItemFlag.HIDE_ENCHANTS);
        }
        itemStack.setItemMeta(meta);
    }

    public boolean isUnbreakable() {
        if (itemStack == null) return false;
        ItemMeta meta = itemStack.getItemMeta();
        if (meta == null) return false;
        try {
            return meta.isUnbreakable();
        } catch (NoSuchMethodError e) {
            return false;
        }
    }

    public void setUnbreakable(boolean unbreakable) {
        if (itemStack == null) return;
        ItemMeta meta = itemStack.getItemMeta();
        if (meta == null) return;
        try {
            meta.setUnbreakable(unbreakable);
        } catch (NoSuchMethodError ignored) {
        }
        if (unbreakable) {
            meta.addItemFlags(ItemFlag.HIDE_UNBREAKABLE);
        } else {
            meta.removeItemFlags(ItemFlag.HIDE_UNBREAKABLE);
        }
        itemStack.setItemMeta(meta);
    }

    public int getCustomModelData() {
        if (itemStack == null) return -1;
        ItemMeta meta = itemStack.getItemMeta();
        if (meta == null) return -1;
        try {
            return meta.getCustomModelData();
        } catch (NoSuchMethodError e) {
            return -1;
        }
    }

    public void setCustomModelData(int data) {
        if (itemStack == null) return;
        ItemMeta meta = itemStack.getItemMeta();
        if (meta == null) return;
        try {
            meta.setCustomModelData(data);
        } catch (NoSuchMethodError ignored) {
        }
        itemStack.setItemMeta(meta);
    }

    public Collection<ItemFlag> getFlags() {
        if (itemStack == null) return new ArrayList<>();
        ItemMeta meta = itemStack.getItemMeta();
        return meta == null ? new ArrayList<>() : meta.getItemFlags();
    }

    public void addFlags(ItemFlag... flags) {
        if (itemStack == null || flags == null) return;
        ItemMeta meta = itemStack.getItemMeta();
        if (meta != null) {
            meta.addItemFlags(flags);
            itemStack.setItemMeta(meta);
        }
    }

    public void removeFlags(ItemFlag... flags) {
        if (itemStack == null || flags == null) return;
        ItemMeta meta = itemStack.getItemMeta();
        if (meta != null) {
            meta.removeItemFlags(flags);
            itemStack.setItemMeta(meta);
        }
    }

    public BukkitGUIItem withName(String name) {
        BukkitGUIItem copy = copy();
        copy.setDisplayName(name);
        return copy;
    }

    public BukkitGUIItem withLore(List<String> lore) {
        BukkitGUIItem copy = copy();
        copy.setLore(lore);
        return copy;
    }

    public BukkitGUIItem withLore(String... lore) {
        return withLore(Arrays.asList(lore));
    }

    public BukkitGUIItem withAmount(int amount) {
        BukkitGUIItem copy = copy();
        copy.setAmount(amount);
        return copy;
    }

    public BukkitGUIItem withGlow() {
        BukkitGUIItem copy = copy();
        copy.setGlow(true);
        return copy;
    }

    public BukkitGUIItem withUnbreakable() {
        BukkitGUIItem copy = copy();
        copy.setUnbreakable(true);
        return copy;
    }

    public BukkitGUIItem withCustomModelData(int data) {
        BukkitGUIItem copy = copy();
        copy.setCustomModelData(data);
        return copy;
    }

    public BukkitGUIItem clickHandler(GUIClickHandler handler) {
        this.clickHandler = handler;
        return this;
    }

    public BukkitGUIItem copy() {
        BukkitGUIItem copy = new BukkitGUIItem(itemStack != null ? itemStack.clone() : null, clickHandler);
        if (this.enchantmentsBeforeGlow != null) {
            copy.enchantmentsBeforeGlow = new HashMap<>(this.enchantmentsBeforeGlow);
        }
        return copy;
    }
}
