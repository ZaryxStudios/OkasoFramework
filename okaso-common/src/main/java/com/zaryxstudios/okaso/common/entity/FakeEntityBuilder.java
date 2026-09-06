package com.zaryxstudios.okaso.common.entity;

import org.bukkit.DyeColor;
import org.bukkit.entity.EntityType;
import org.bukkit.inventory.ItemStack;

public interface FakeEntityBuilder extends NPCBuilder<FakeEntityBuilder> {

    FakeEntityBuilder entityType(EntityType type);
    FakeEntityBuilder heldItem(ItemStack item);
    FakeEntityBuilder baby(boolean baby);
    FakeEntityBuilder variant(int variant);
    FakeEntityBuilder professional(String profession);
    FakeEntityBuilder catType(String catType);
    FakeEntityBuilder foxType(String foxType);
    FakeEntityBuilder llamaColor(String color);
    FakeEntityBuilder parrotVariant(String variant);
    FakeEntityBuilder rabbitType(String type);
    FakeEntityBuilder sheepColor(DyeColor color);
    FakeEntityBuilder shulkerColor(DyeColor color);
    FakeEntityBuilder tropicalFishPattern(String pattern);
    FakeEntityBuilder tropicalFishBodyColor(DyeColor body);
    FakeEntityBuilder tropicalFishPatternColor(DyeColor pattern);
    FakeEntityBuilder frogVariant(String variant);
    FakeEntityBuilder axolotlVariant(String variant);
    FakeEntityBuilder camel(boolean saddle);
    FakeEntityBuilder sniffer(boolean sniffing);
    FakeEntityBuilder pandaGene(String mainGene, String hiddenGene);
}