package com.zaryxstudios.okaso.nbt;

import com.zaryxstudios.okaso.common.nbt.NBTCompound;
import com.zaryxstudios.okaso.common.nbt.NBTItem;

import org.bukkit.inventory.ItemStack;

import java.lang.reflect.Method;

public class ReflectionNBTItem implements NBTItem {

    private static final String CRAFT_BUKKIT = "org.bukkit.craftbukkit.";
    private static final String CRAFT_ITEM_STACK = "inventory.CraftItemStack";

    private final ItemStack bukkitStack;
    private final Object nmsCopy; 
    private ReflectionNBTCompound compound;

    public ReflectionNBTItem(ItemStack bukkitStack) {
        this.bukkitStack = bukkitStack;
        this.nmsCopy = asNMSCopy(bukkitStack);
        if (this.nmsCopy != null) {
            Object tag = getTag(this.nmsCopy);
            if (tag != null) {
                this.compound = new ReflectionNBTCompound(tag);
            }
        }
    }

    @Override
    public NBTCompound getCompound() {
        if (compound == null && nmsCopy != null) {
            try {
                Class<?> nbtTagClass = getNmsClass("NBTTagCompound");
                Object newTag = nbtTagClass.newInstance();
                setTag(nmsCopy, newTag);
                compound = new ReflectionNBTCompound(newTag);
            } catch (Exception e) {
            }
        }
        return compound;
    }

    @Override
    public Object getItemStack() {
        return bukkitStack;
    }

    @Override
    public void apply() {
        if (nmsCopy == null) return;
        try {
            Class<?> craftItemStackClass = getCraftClass(CRAFT_ITEM_STACK);
            Method asBukkitCopy = craftItemStackClass.getMethod("asBukkitCopy",
                getNmsClass("ItemStack"));
            Object result = asBukkitCopy.invoke(null, nmsCopy);
            if (result instanceof ItemStack) {
                bukkitStack.setItemMeta(((ItemStack) result).getItemMeta());
            }
        } catch (Exception e) {
            try {
                Class<?> craftItemStackClass = getCraftClass(CRAFT_ITEM_STACK);
                Method setItemMeta = craftItemStackClass.getMethod("setItemMeta",
                    getNmsClass("ItemStack"),
                    org.bukkit.inventory.meta.ItemMeta.class);
            } catch (Exception e2) {
                e2.printStackTrace();
            }
        }
    }

    private static Object asNMSCopy(ItemStack stack) {
        try {
            Class<?> craftItemStackClass = getCraftClass(CRAFT_ITEM_STACK);
            Method asNMSCopy = craftItemStackClass.getMethod("asNMSCopy", ItemStack.class);
            return asNMSCopy.invoke(null, stack);
        } catch (Exception e) {
            return null;
        }
    }

    private static Object getTag(Object nmsItemStack) {
        try {
            Method getTag = nmsItemStack.getClass().getMethod("getTag");
            return getTag.invoke(nmsItemStack);
        } catch (Exception e) {
            return null;
        }
    }

    private static void setTag(Object nmsItemStack, Object tag) {
        try {
            Method setTag = nmsItemStack.getClass().getMethod("setTag", getNmsClass("NBTTagCompound"));
            setTag.invoke(nmsItemStack, tag);
        } catch (Exception e) {
        }
    }

    private static Class<?> getCraftClass(String name) {
        try {
            String version = org.bukkit.Bukkit.getServer().getClass().getPackage().getName()
                .replace("org.bukkit.craftbukkit.", "");
            return Class.forName(CRAFT_BUKKIT + version + "." + name);
        } catch (Exception e) {
            return null;
        }
    }

    private static Class<?> getNmsClass(String simpleName) {
        try {
            return Class.forName("net.minecraft.server." + simpleName);
        } catch (Exception ignored) {
        }
        try {
            String pkg = org.bukkit.Bukkit.getServer().getClass().getPackage().getName();
            String version = pkg.substring(pkg.lastIndexOf('.') + 1);
            return Class.forName("net.minecraft.server." + version + "." + simpleName);
        } catch (Exception ignored) {
        }
        return null;
    }

    @Override
    public NBTItem copy() {
        ItemStack copy = bukkitStack.clone();
        ReflectionNBTItem copied = new ReflectionNBTItem(copy);
        NBTCompound src = getCompound();
        NBTCompound dst = copied.getCompound();
        if (src != null && dst != null) {
            for (String key : src.getKeys()) {
                if (src.hasKey(key)) {
                    dst.setString(key, src.getString(key));
                }
            }
        }
        return copied;
    }

    @Override
    public boolean hasTag(String key) {
        NBTCompound tag = getCompound();
        return tag != null && tag.hasKey(key);
    }

    @Override
    public void removeTag(String key) {
        NBTCompound tag = getCompound();
        if (tag != null) {
            tag.remove(key);
        }
    }
}
