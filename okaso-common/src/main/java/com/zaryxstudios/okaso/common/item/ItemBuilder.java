package com.zaryxstudios.okaso.common.item;

import java.util.List;
import java.util.Map;

public interface ItemBuilder {
    ItemBuilder name(String name);
    ItemBuilder lore(List<String> lore);
    ItemBuilder lore(String... lines);
    ItemBuilder amount(int amount);
    ItemBuilder durability(short durability);
    ItemBuilder enchant(String enchantment, int level);
    ItemBuilder enchantList(Map<String, Integer> enchantments);
    ItemBuilder glow();
    ItemBuilder unbreakable(boolean unbreakable);
    ItemBuilder skullOwner(String owner);
    ItemBuilder flags(String... flags);
    Object build();
}
