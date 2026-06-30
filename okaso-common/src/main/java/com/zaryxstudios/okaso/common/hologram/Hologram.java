package com.zaryxstudios.okaso.common.hologram;

import com.zaryxstudios.okaso.common.Lifecycle;
import java.util.List;

public interface Hologram extends Lifecycle {

    String getId();

    List<HologramLine> getLines();
    void setLines(List<HologramLine> lines);
    void setLine(int index, HologramLine line);
    void addLine(HologramLine line);
    void insertLine(int index, HologramLine line);
    void removeLine(int index);
    int getLineCount();
    void clearLines();

    List<String> getTextLines();
    void setTextLines(List<String> lines);
    void setText(int index, String text);
    void addText(String text);
    void insertText(int index, String text);

    void addItem(String materialName, int amount);
    void addItem(String materialName);

    void addMob(String entityType);

    void teleport(double x, double y, double z, float yaw, float pitch);
}
