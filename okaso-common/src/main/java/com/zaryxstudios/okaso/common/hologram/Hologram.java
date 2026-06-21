package com.zaryxstudios.okaso.common.hologram;

import com.zaryxstudios.okaso.common.Lifecycle;
import java.util.List;

public interface Hologram extends Lifecycle {
    String getId();
    List<String> getLines();
    void setLines(List<String> lines);
    void setLine(int index, String text);
    void addLine(String text);
    void removeLine(int index);
    void insertLine(int index, String text);
    int getLineCount();
    void clearLines();
    void teleport(double x, double y, double z, float yaw, float pitch);
}
