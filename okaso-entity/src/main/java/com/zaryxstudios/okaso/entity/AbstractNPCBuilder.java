package com.zaryxstudios.okaso.entity;

import com.zaryxstudios.okaso.common.entity.NPCBuilder;
import com.zaryxstudios.okaso.common.entity.NPCHandle;

import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

abstract class AbstractNPCBuilder<B extends NPCBuilder<B>> implements NPCBuilder<B> {

    protected float yaw;
    protected float pitch;
    protected String customName;
    protected boolean customNameVisible = true;
    protected boolean gravity;
    protected boolean invulnerable;
    protected boolean silent;
    protected boolean glowing;
    protected boolean ai;
    protected double moveSpeed = 0.2;
    protected boolean attackPlayers;
    protected boolean interactable = true;
    protected ItemStack helmet;
    protected ItemStack chest;
    protected ItemStack legs;
    protected ItemStack boots;
    protected final List<PotionEffect> effects = new ArrayList<>();
    protected NPCHandle followTarget;

    @Override
    @SuppressWarnings("unchecked")
    public B rotation(float yaw, float pitch) {
        this.yaw = yaw;
        this.pitch = pitch;
        return self();
    }

    @Override
    public B armor(ItemStack helmet, ItemStack chestplate, ItemStack leggings, ItemStack boots) {
        this.helmet = helmet;
        this.chest = chestplate;
        this.legs = leggings;
        this.boots = boots;
        return self();
    }

    @Override
    public B addPotionEffect(PotionEffect effect) {
        effects.add(effect);
        return self();
    }

    @Override
    public B removePotionEffect(PotionEffectType type) {
        effects.removeIf(e -> e.getType().equals(type));
        return self();
    }

    @Override
    public B customName(String name) {
        this.customName = name;
        return self();
    }

    @Override
    public B customNameVisible(boolean visible) {
        this.customNameVisible = visible;
        return self();
    }

    @Override
    public B gravity(boolean hasGravity) {
        this.gravity = hasGravity;
        return self();
    }

    @Override
    public B invulnerable(boolean invulnerable) {
        this.invulnerable = invulnerable;
        return self();
    }

    @Override
    public B silent(boolean silent) {
        this.silent = silent;
        return self();
    }

    @Override
    public B glow(boolean glowing) {
        this.glowing = glowing;
        return self();
    }

    @Override
    public B ai(boolean hasAI) {
        this.ai = hasAI;
        return self();
    }

    @Override
    public B moveSpeed(double speed) {
        this.moveSpeed = speed;
        return self();
    }

    @Override
    public B followTarget(NPCHandle target) {
        this.followTarget = target;
        return self();
    }

    @Override
    public B attackPlayers(boolean attack) {
        this.attackPlayers = attack;
        return self();
    }

    @Override
    public B interactable(boolean interactable) {
        this.interactable = interactable;
        return self();
    }


    @SuppressWarnings("unchecked")
    private B self() { return (B) this; }

    protected void applyCommon(Entity entity) {
        if (customName != null) {
            entity.setCustomName(customName);
            safeInvoke(entity, "setCustomNameVisible", customNameVisible);
        }
        safeInvoke(entity, "setGravity", gravity);
        safeInvoke(entity, "setInvulnerable", invulnerable);
        safeInvoke(entity, "setSilent", silent);
        safeInvoke(entity, "setGlowing", glowing);
        safeInvoke(entity, "setAI", ai);

        if (VersionUtil.hasPoseFlags()) {
            safeInvoke(entity, "setSneaking", sneaking());
            safeInvoke(entity, "setSprinting", sprinting());
            safeInvoke(entity, "setSwimming", swimming());
            safeInvoke(entity, "setGliding", gliding());
        }

        if (entity instanceof LivingEntity) {
            applyLivingEntityProperties((LivingEntity) entity);
        }

        if (yaw != 0f || pitch != 0f) {
            Location loc = entity.getLocation();
            loc.setYaw(yaw);
            loc.setPitch(pitch);
            entity.teleport(loc);
        }
    }

    protected boolean sneaking() { return false; }
    protected boolean sprinting() { return false; }
    protected boolean swimming() { return false; }
    protected boolean gliding() { return false; }

    private void applyLivingEntityProperties(LivingEntity le) {
        if (helmet != null) le.getEquipment().setHelmet(helmet);
        if (chest != null) le.getEquipment().setChestplate(chest);
        if (legs != null) le.getEquipment().setLeggings(legs);
        if (boots != null) le.getEquipment().setBoots(boots);
        for (PotionEffect pe : effects) le.addPotionEffect(pe, true);
    }

    static void safeInvoke(Object target, String methodName, Object... args) {
        try {
            Class<?>[] paramTypes = new Class<?>[args.length];
            for (int i = 0; i < args.length; i++) {
                paramTypes[i] = toPrimitiveOrBox(args[i] != null ? args[i].getClass() : Object.class);
            }
            Method m = findMethod(target.getClass(), methodName, paramTypes);
            if (m != null) {
                m.setAccessible(true);
                m.invoke(target, args);
            }
        } catch (Exception ignored) {
        }
    }

    private static Class<?> toPrimitiveOrBox(Class<?> type) {
        if (type == Boolean.class || type == boolean.class) return boolean.class;
        if (type == Integer.class || type == int.class) return int.class;
        if (type == Float.class || type == float.class) return float.class;
        if (type == Double.class || type == double.class) return double.class;
        if (type == Long.class || type == long.class) return long.class;
        if (type == Byte.class || type == byte.class) return byte.class;
        if (type == Short.class || type == short.class) return short.class;
        if (type == Character.class || type == char.class) return char.class;
        return type;
    }

    private static Method findMethod(Class<?> clazz, String name, Class<?>... paramTypes) {
        Class<?> current = clazz;
        while (current != null && current != Object.class) {
            try {
                return current.getDeclaredMethod(name, paramTypes);
            } catch (NoSuchMethodException e) {
                current = current.getSuperclass();
            }
        }
        return null;
    }
}