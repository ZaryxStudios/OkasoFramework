package com.zaryxstudios.okaso.common.entity;

import org.bukkit.DyeColor;
import org.bukkit.entity.*;
import org.bukkit.inventory.ItemStack;

public interface FakeEntityBuilder extends NPCBuilder<FakeEntityBuilder> {

    FakeEntityBuilder entityType(EntityType type);
    FakeEntityBuilder heldItem(ItemStack item);
    FakeEntityBuilder baby(boolean baby);
    FakeEntityBuilder variant(int variant);
    FakeEntityBuilder professional(Villager.Profession profession);
    FakeEntityBuilder catType(Cat.Type catType);
    FakeEntityBuilder foxType(Fox.Type foxType);
    FakeEntityBuilder llamaColor(Llama.Color color);
    FakeEntityBuilder parrotVariant(Parrot.Variant variant);
    FakeEntityBuilder rabbitType(Rabbit.Type type);
    FakeEntityBuilder sheepColor(DyeColor color);
    FakeEntityBuilder shulkerColor(DyeColor color);
    FakeEntityBuilder tropicalFishPattern(TropicalFish.Pattern pattern);
    FakeEntityBuilder tropicalFishBodyColor(DyeColor body);
    FakeEntityBuilder tropicalFishPatternColor(DyeColor pattern);
    FakeEntityBuilder frogVariant(String variant);
    FakeEntityBuilder axolotlVariant(String variant);
    FakeEntityBuilder camel(boolean saddle);
    FakeEntityBuilder sniffer(boolean sniffing);
    FakeEntityBuilder pandaGene(String mainGene, String hiddenGene);
}