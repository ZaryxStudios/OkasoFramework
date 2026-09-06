package com.zaryxstudios.okaso.common.entity;

import org.bukkit.inventory.ItemStack;

public interface FakePlayerBuilder extends NPCBuilder<FakePlayerBuilder> {

    FakePlayerBuilder skin(String textureUrl);
    FakePlayerBuilder skin(byte[] skinData, byte[] capeData);
    FakePlayerBuilder skin(SkinData skin);
    FakePlayerBuilder skin(String textureValue, String signature);
    FakePlayerBuilder skinOwner(String playerName);
    FakePlayerBuilder skinUrls(String skinUrl, String capeUrl);
    FakePlayerBuilder slimModel(boolean slim);
    FakePlayerBuilder heldItemMainHand(ItemStack item);
    FakePlayerBuilder heldItemOffHand(ItemStack item);
    FakePlayerBuilder displayName(String name);
    FakePlayerBuilder sneaking(boolean sneaking);
    FakePlayerBuilder sprinting(boolean sprinting);
    FakePlayerBuilder swimming(boolean swimming);
    FakePlayerBuilder gliding(boolean gliding);
}
