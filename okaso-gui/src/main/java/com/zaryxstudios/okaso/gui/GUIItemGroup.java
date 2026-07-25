package com.zaryxstudios.okaso.gui;

import com.zaryxstudios.okaso.common.gui.GUI;
import com.zaryxstudios.okaso.common.gui.GUIItem;

import org.bukkit.Material;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class GUIItemGroup {

    private final Map<String, Set<Integer>> groups;
    private final Map<String, GUIItem> groupDefaults;
    private final Map<String, Boolean> groupVisibility;
    private final GUI gui;

    public GUIItemGroup(GUI gui) {
        this.gui = gui;
        this.groups = new HashMap<>();
        this.groupDefaults = new HashMap<>();
        this.groupVisibility = new HashMap<>();
    }

    public void createGroup(String groupId) {
        if (groupId != null) {
            groups.put(groupId, new HashSet<>());
            groupVisibility.put(groupId, true);
        }
    }

    public void addToGroup(String groupId, int slot) {
        Set<Integer> group = groups.get(groupId);
        if (group != null) {
            group.add(slot);
        }
    }

    public void addToGroup(String groupId, int... slots) {
        Set<Integer> group = groups.get(groupId);
        if (group != null) {
            for (int slot : slots) {
                group.add(slot);
            }
        }
    }

    public void addToGroup(String groupId, Collection<Integer> slots) {
        Set<Integer> group = groups.get(groupId);
        if (group != null && slots != null) {
            group.addAll(slots);
        }
    }

    public void removeFromGroup(String groupId, int slot) {
        Set<Integer> group = groups.get(groupId);
        if (group != null) {
            group.remove(slot);
        }
    }

    public void removeFromGroup(String groupId) {
        groups.remove(groupId);
        groupDefaults.remove(groupId);
        groupVisibility.remove(groupId);
    }

    public void setDefault(String groupId, GUIItem item) {
        groupDefaults.put(groupId, item);
    }

    public void show(String groupId) {
        if (!groups.containsKey(groupId)) return;
        groupVisibility.put(groupId, true);
        GUIItem defaultItem = groupDefaults.get(groupId);
        if (defaultItem != null) {
            for (int slot : groups.get(groupId)) {
                gui.setItem(slot, defaultItem);
            }
        } else {
            for (int slot : groups.get(groupId)) {
                gui.updateSlot(slot);
            }
        }
    }

    public void hide(String groupId) {
        if (!groups.containsKey(groupId)) return;
        groupVisibility.put(groupId, false);
        GUIItem filler = BukkitGUIItem.of(Material.AIR);
        for (int slot : groups.get(groupId)) {
            gui.setItem(slot, filler);
        }
    }

    public void hideWith(String groupId, GUIItem replacement) {
        if (!groups.containsKey(groupId) || replacement == null) return;
        groupVisibility.put(groupId, false);
        for (int slot : groups.get(groupId)) {
            gui.setItem(slot, replacement);
        }
    }

    public void toggle(String groupId) {
        if (isVisible(groupId)) {
            hide(groupId);
        } else {
            show(groupId);
        }
    }

    public boolean isVisible(String groupId) {
        return groupVisibility.getOrDefault(groupId, true);
    }

    public Set<Integer> getSlots(String groupId) {
        Set<Integer> group = groups.get(groupId);
        return group == null ? new HashSet<>() : new HashSet<>(group);
    }

    public boolean contains(String groupId, int slot) {
        Set<Integer> group = groups.get(groupId);
        return group != null && group.contains(slot);
    }

    public boolean hasGroup(String groupId) {
        return groups.containsKey(groupId);
    }

    public Set<String> getGroupIds() {
        return new HashSet<>(groups.keySet());
    }

    public int getGroupSize(String groupId) {
        Set<Integer> group = groups.get(groupId);
        return group == null ? 0 : group.size();
    }

    public void clearGroup(String groupId) {
        Set<Integer> group = groups.get(groupId);
        if (group != null) {
            group.clear();
        }
    }

    public void renameGroup(String oldId, String newId) {
        if (oldId == null || newId == null || !groups.containsKey(oldId)) return;
        groups.put(newId, groups.remove(oldId));
        GUIItem def = groupDefaults.remove(oldId);
        if (def != null) groupDefaults.put(newId, def);
        Boolean vis = groupVisibility.remove(oldId);
        if (vis != null) groupVisibility.put(newId, vis);
    }

    public void mergeGroups(String target, String source) {
        Set<Integer> targetGroup = groups.get(target);
        Set<Integer> sourceGroup = groups.get(source);
        if (targetGroup != null && sourceGroup != null) {
            targetGroup.addAll(sourceGroup);
        }
        groups.remove(source);
        groupDefaults.remove(source);
        groupVisibility.remove(source);
    }

    public List<String> getGroupsForSlot(int slot) {
        List<String> result = new ArrayList<>();
        for (Map.Entry<String, Set<Integer>> entry : groups.entrySet()) {
            if (entry.getValue().contains(slot)) {
                result.add(entry.getKey());
            }
        }
        return result;
    }

    public void hideAll() {
        for (String groupId : groups.keySet()) {
            hide(groupId);
        }
    }

    public void showAll() {
        for (String groupId : groups.keySet()) {
            show(groupId);
        }
    }
}
