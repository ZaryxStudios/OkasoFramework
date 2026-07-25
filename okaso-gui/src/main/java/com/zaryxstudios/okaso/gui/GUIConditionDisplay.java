package com.zaryxstudios.okaso.gui;

import com.zaryxstudios.okaso.common.gui.GUI;
import com.zaryxstudios.okaso.common.gui.GUIItem;

import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;

public class GUIConditionDisplay {

    private final GUI gui;
    private final Map<Integer, List<ConditionalSlot>> conditions;

    public GUIConditionDisplay(GUI gui) {
        this.gui = gui;
        this.conditions = new HashMap<>();
    }

    public void addCondition(int slot, Predicate<Player> condition, GUIItem trueItem) {
        addCondition(slot, condition, trueItem, null);
    }

    public void addCondition(int slot, Predicate<Player> condition, GUIItem trueItem, GUIItem falseItem) {
        List<ConditionalSlot> list = conditions.computeIfAbsent(slot, k -> new ArrayList<>());
        list.add(new ConditionalSlot(condition, trueItem, falseItem));
    }

    public void addEqualityCondition(int slot, String playerValue, String expected, GUIItem trueItem) {
        addCondition(slot, p -> expected.equals(playerValue), trueItem);
    }

    public void addPermissionCondition(int slot, String permission, GUIItem granted, GUIItem denied) {
        addCondition(slot, p -> p.hasPermission(permission), granted, denied);
    }

    public void addHealthCondition(int slot, double minHealth, GUIItem aboveItem, GUIItem belowItem) {
        addCondition(slot, p -> p.getHealth() >= minHealth, aboveItem, belowItem);
    }

    public void addLevelCondition(int slot, int minLevel, GUIItem aboveItem, GUIItem belowItem) {
        addCondition(slot, p -> p.getLevel() >= minLevel, aboveItem, belowItem);
    }

    public void addGamemodeCondition(int slot, String gamemode, GUIItem matchItem, GUIItem noMatchItem) {
        addCondition(slot, p -> p.getGameMode().name().equalsIgnoreCase(gamemode), matchItem, noMatchItem);
    }

    public void addWorldCondition(int slot, String worldName, GUIItem matchItem, GUIItem noMatchItem) {
        addCondition(slot, p -> p.getWorld().getName().equals(worldName), matchItem, noMatchItem);
    }

    public void addOnlineCountCondition(int slot, int minOnline, GUIItem aboveItem, GUIItem belowItem) {
        addCondition(slot, p -> p.getServer().getOnlinePlayers().size() >= minOnline, aboveItem, belowItem);
    }

    public void update(Player player) {
        for (Map.Entry<Integer, List<ConditionalSlot>> entry : conditions.entrySet()) {
            int slot = entry.getKey();
            GUIItem resolved = resolve(slot, player);
            if (resolved != null) {
                gui.setItem(slot, resolved);
            }
        }
    }

    public GUIItem resolve(int slot, Player player) {
        List<ConditionalSlot> list = conditions.get(slot);
        if (list == null || list.isEmpty()) return null;
        for (ConditionalSlot cs : list) {
            if (cs.condition.test(player)) {
                return cs.trueItem;
            } else if (cs.falseItem != null) {
                return cs.falseItem;
            }
        }
        return null;
    }

    public void clear(int slot) {
        conditions.remove(slot);
    }

    public void clearAll() {
        conditions.clear();
    }

    public boolean hasConditions(int slot) {
        return conditions.containsKey(slot) && !conditions.get(slot).isEmpty();
    }

    public int getConditionCount() {
        return conditions.size();
    }

    public void removeLastCondition(int slot) {
        List<ConditionalSlot> list = conditions.get(slot);
        if (list != null && !list.isEmpty()) {
            list.remove(list.size() - 1);
        }
    }

    private static final class ConditionalSlot {
        final Predicate<Player> condition;
        final GUIItem trueItem;
        final GUIItem falseItem;

        ConditionalSlot(Predicate<Player> condition, GUIItem trueItem, GUIItem falseItem) {
            this.condition = condition;
            this.trueItem = trueItem;
            this.falseItem = falseItem;
        }
    }
}
