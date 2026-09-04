package com.zaryxstudios.okaso.common.entity;

import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

public interface NPCBuilder<B extends NPCBuilder<B>> {

    B rotation(float yaw, float pitch);
    B armor(ItemStack helmet, ItemStack chestplate, ItemStack leggings, ItemStack boots);
    B addPotionEffect(PotionEffect effect);
    B removePotionEffect(PotionEffectType type);
    B customName(String name);
    B customNameVisible(boolean visible);
    B gravity(boolean hasGravity);
    B invulnerable(boolean invulnerable);
    B silent(boolean silent);
    B glow(boolean glowing);
    B ai(boolean hasAI);
    B moveSpeed(double speed);
    B followTarget(NPCHandle target);
    B attackPlayers(boolean attack);
    B interactable(boolean interactable);
}