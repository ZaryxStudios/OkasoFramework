package com.zaryxstudios.okaso.common.gui;

import java.util.Collection;
import java.util.Map;
import java.util.function.Consumer;

public interface GUI {
    String getTitle();
    int getSize();
    int getRows();
    void open(Object player);
    void close(Object player);

    void setItem(int slot, GUIItem item);
    GUIItem getItem(int slot);
    void removeItem(int slot);
    int addItem(GUIItem item);
    void updateSlot(int slot);
    void updateAll();

    boolean isEmpty();
    int countItems();
    void clear();
    Map<Integer, GUIItem> getItems();

    Collection<Object> getViewers();
    void closeAll();
    boolean isViewing(Object player);

    void setOpenHandler(Consumer<Object> handler);
    void setCloseHandler(Consumer<Object> handler);

    void fillEmpty(GUIItem item);
    void fillBorder(GUIItem item);
    void fillRow(int row, GUIItem item);
    void fillColumn(int column, GUIItem item);

    int getFirstEmptySlot();
    boolean contains(int slot);
    boolean hasSlot(int slot);

    int getPage();
    void setPage(int page);
    int getTotalPages();
    boolean hasNextPage();
    boolean hasPreviousPage();
    void nextPage();
    void previousPage();
}
