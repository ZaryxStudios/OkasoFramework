package com.zaryxstudios.okaso.common.item;

import java.util.List;

public interface ItemBuilder {
    ItemBuilder name(String name);
    ItemBuilder lore(List<String> lore);
    ItemBuilder lore(String... lines);
    ItemBuilder amount(int amount);
    ItemBuilder durability(short durability);
    ItemBuilder enchant(String enchantment, int level);
    ItemBuilder glow();
    ItemBuilder unbreakable(boolean unbreakable);
    Object build();
}
