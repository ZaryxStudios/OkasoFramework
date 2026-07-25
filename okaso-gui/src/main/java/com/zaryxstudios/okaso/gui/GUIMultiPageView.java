package com.zaryxstudios.okaso.gui;

import com.zaryxstudios.okaso.common.gui.GUI;
import com.zaryxstudios.okaso.common.gui.GUIClickEvent;
import com.zaryxstudios.okaso.common.gui.GUIItem;

import org.bukkit.Material;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Function;

public class GUIMultiPageView {

    private final BukkitGUI gui;
    private final List<GUIItem> allItems;
    private final int contentSlots;
    private final int[] navigationSlots;
    private int currentPage;
    private Function<Integer, String> titleFormatter;
    private BiConsumer<GUI, Integer> onPageChange;

    public GUIMultiPageView(BukkitGUI gui, List<GUIItem> items, int contentStartSlot, int contentEndSlot,
                            int... navigationSlots) {
        this.gui = gui;
        this.allItems = items != null ? new ArrayList<>(items) : new ArrayList<>();
        this.contentSlots = contentEndSlot - contentStartSlot + 1;
        this.navigationSlots = navigationSlots;
        this.currentPage = 0;
        this.titleFormatter = null;
        this.onPageChange = null;
    }

    public GUIMultiPageView(BukkitGUI gui, List<GUIItem> items, int contentSlots, int... navigationSlots) {
        this(gui, items, 0, contentSlots - 1, navigationSlots);
    }

    public void render() {
        gui.clear();
        int start = currentPage * contentSlots;
        int end = Math.min(start + contentSlots, allItems.size());
        int slot = 0;
        for (int i = start; i < end; i++) {
            gui.setItem(slot, allItems.get(i));
            slot++;
        }
        for (int navSlot : navigationSlots) {
            gui.setItem(navSlot, createNavPlaceholder());
        }
        updateNavButtons();
        if (titleFormatter != null) {
            updateTitle();
        }
    }

    public void updateNavButtons() {
        for (int navSlot : navigationSlots) {
            gui.setItem(navSlot, createNavPlaceholder());
        }
    }

    private GUIItem createNavPlaceholder() {
        return BukkitGUIItem.of(Material.AIR);
    }

    public void setPage(int page) {
        int totalPages = getTotalPages();
        if (totalPages == 0) {
            this.currentPage = 0;
        } else if (page < 0) {
            this.currentPage = 0;
        } else if (page >= totalPages) {
            this.currentPage = totalPages - 1;
        } else {
            this.currentPage = page;
        }
        render();
        if (onPageChange != null) {
            onPageChange.accept(gui, currentPage);
        }
    }

    public int getPage() {
        return currentPage;
    }

    public int getTotalPages() {
        if (allItems.isEmpty() || contentSlots <= 0) return 0;
        return (int) Math.ceil((double) allItems.size() / contentSlots);
    }

    public boolean hasNextPage() {
        return currentPage < getTotalPages() - 1;
    }

    public boolean hasPreviousPage() {
        return currentPage > 0;
    }

    public void nextPage() {
        if (hasNextPage()) {
            setPage(currentPage + 1);
        }
    }

    public void previousPage() {
        if (hasPreviousPage()) {
            setPage(currentPage - 1);
        }
    }

    public void firstPage() {
        setPage(0);
    }

    public void lastPage() {
        setPage(getTotalPages() - 1);
    }

    public GUIItem createNextButton() {
        if (!hasNextPage()) {
            return BukkitGUIItem.builder(Material.BARRIER)
                .name("&7Sin más páginas")
                .build();
        }
        return BukkitGUIItem.builder(Material.ARROW)
            .name("&aSiguiente →")
            .clickHandler(event -> nextPage())
            .build();
    }

    public GUIItem createPreviousButton() {
        if (!hasPreviousPage()) {
            return BukkitGUIItem.builder(Material.BARRIER)
                .name("&7Sin páginas previas")
                .build();
        }
        return BukkitGUIItem.builder(Material.ARROW)
            .name("&a← Anterior")
            .clickHandler(event -> previousPage())
            .build();
    }

    public GUIItem createPageIndicator() {
        int total = getTotalPages();
        if (total == 0) {
            return BukkitGUIItem.of(Material.PAPER);
        }
        String display = "&ePágina " + (currentPage + 1) + " / " + total;
        return BukkitGUIItem.builder(Material.PAPER)
            .name(display)
            .build();
    }

    public GUIItem createPageIndicator(String format) {
        int total = getTotalPages();
        if (total == 0) {
            return BukkitGUIItem.of(Material.PAPER);
        }
        String display = format
            .replace("{current}", String.valueOf(currentPage + 1))
            .replace("{total}", String.valueOf(total))
            .replace("{percent}", total == 0 ? "0" : String.valueOf((currentPage + 1) * 100 / total));
        return BukkitGUIItem.builder(Material.PAPER)
            .name(display)
            .build();
    }

    public GUIItem createFirstPageButton() {
            return BukkitGUIItem.builder(Material.CLOCK)
            .name("&ePrimera página")
            .clickHandler(event -> firstPage())
            .build();
    }

    public GUIItem createLastPageButton() {
        return BukkitGUIItem.builder(Material.CLOCK)
            .name("&eÚltima página")
            .clickHandler(event -> lastPage())
            .build();
    }

    public void addNavigation(int slot, Function<GUIMultiPageView, GUIItem> buttonFactory) {
        GUIItem button = buttonFactory.apply(this);
        if (button != null) {
            gui.setItem(slot, button);
        }
    }

    public void setContentSlots(int startSlot, int endSlot) {
    }

    public void addItem(GUIItem item) {
        if (item != null) {
            allItems.add(item);
        }
    }

    public void removeItem(int index) {
        if (index >= 0 && index < allItems.size()) {
            allItems.remove(index);
            int totalPages = getTotalPages();
            if (currentPage >= totalPages && currentPage > 0) {
                currentPage = totalPages - 1;
            }
        }
    }

    public void setItems(List<GUIItem> items) {
        allItems.clear();
        if (items != null) {
            allItems.addAll(items);
        }
        currentPage = 0;
    }

    public List<GUIItem> getItems() {
        return new ArrayList<>(allItems);
    }

    public int getItemCount() {
        return allItems.size();
    }

    public List<GUIItem> getCurrentPageItems() {
        List<GUIItem> result = new ArrayList<>();
        int start = currentPage * contentSlots;
        int end = Math.min(start + contentSlots, allItems.size());
        for (int i = start; i < end; i++) {
            result.add(allItems.get(i));
        }
        return result;
    }

    public BukkitGUI getGUI() {
        return gui;
    }

    public void setTitleFormatter(Function<Integer, String> formatter) {
        this.titleFormatter = formatter;
    }

    public void setOnPageChange(BiConsumer<GUI, Integer> handler) {
        this.onPageChange = handler;
    }

    private void updateTitle() {
    }

    public static GUIMultiPageView create(BukkitGUI gui, List<GUIItem> items,
                                          int contentSlots, int... navSlots) {
        return new GUIMultiPageView(gui, items, contentSlots, navSlots);
    }
}
