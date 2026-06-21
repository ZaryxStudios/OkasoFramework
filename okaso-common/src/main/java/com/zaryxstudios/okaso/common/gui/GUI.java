package com.zaryxstudios.okaso.common.gui;

public interface GUI {
    String getTitle();
    int getSize();
    void open(Object player);
    void close(Object player);
    void setItem(int slot, GUIItem item);
    GUIItem getItem(int slot);
    void removeItem(int slot);
    int addItem(GUIItem item);
    boolean isEmpty();
    int countItems();
    void clear();
}
