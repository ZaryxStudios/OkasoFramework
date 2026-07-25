package com.zaryxstudios.okaso.gui;

import com.zaryxstudios.okaso.common.gui.GUI;
import com.zaryxstudios.okaso.common.gui.GUIItem;

import org.bukkit.Material;

import java.util.ArrayList;
import java.util.List;

public final class GUIPattern {

    private GUIPattern() {
    }

    public static void checkerboard(GUI gui, GUIItem primary, GUIItem secondary) {
        if (gui == null || primary == null || secondary == null) return;
        for (int slot = 0; slot < gui.getSize(); slot++) {
            int col = slot % 9;
            int row = slot / 9;
            if ((row + col) % 2 == 0) {
                gui.setItem(slot, primary);
            } else {
                gui.setItem(slot, secondary);
            }
        }
    }

    public static void alternatingRows(GUI gui, GUIItem even, GUIItem odd) {
        if (gui == null || even == null || odd == null) return;
        for (int slot = 0; slot < gui.getSize(); slot++) {
            int row = slot / 9;
            if (row % 2 == 0) {
                gui.setItem(slot, even);
            } else {
                gui.setItem(slot, odd);
            }
        }
    }

    public static void alternatingColumns(GUI gui, GUIItem even, GUIItem odd) {
        if (gui == null || even == null || odd == null) return;
        for (int slot = 0; slot < gui.getSize(); slot++) {
            int col = slot % 9;
            if (col % 2 == 0) {
                gui.setItem(slot, even);
            } else {
                gui.setItem(slot, odd);
            }
        }
    }

    public static void gradientBorder(GUI gui, GUIItem topLeft, GUIItem topRight,
                                      GUIItem bottomLeft, GUIItem bottomRight) {
        if (gui == null) return;
        int rows = gui.getSize() / 9;
        for (int slot = 0; slot < gui.getSize(); slot++) {
            int col = slot % 9;
            int row = slot / 9;
            boolean isBorder = row == 0 || row == rows - 1 || col == 0 || col == 8;
            if (!isBorder) continue;
            double fx = (double) col / 8;
            double fy = rows == 0 ? 0 : (double) row / (rows - 1);
            double tl = (1.0 - fx) * (1.0 - fy);
            double tr = fx * (1.0 - fy);
            double bl = (1.0 - fx) * fy;
            double br = fx * fy;
            double maxVal = Math.max(Math.max(tl, tr), Math.max(bl, br));
            if (maxVal == tl) {
                gui.setItem(slot, topLeft);
            } else if (maxVal == tr) {
                gui.setItem(slot, topRight);
            } else if (maxVal == bl) {
                gui.setItem(slot, bottomLeft);
            } else {
                gui.setItem(slot, bottomRight);
            }
        }
    }

    public static void gradientBorder(GUI gui, GUIItem primary, GUIItem secondary) {
        if (gui == null || primary == null || secondary == null) return;
        int rows = gui.getSize() / 9;
        for (int slot = 0; slot < gui.getSize(); slot++) {
            int col = slot % 9;
            int row = slot / 9;
            boolean isBorder = row == 0 || row == rows - 1 || col == 0 || col == 8;
            if (!isBorder) continue;
            if ((row + col) % 2 == 0) {
                gui.setItem(slot, primary);
            } else {
                gui.setItem(slot, secondary);
            }
        }
    }

    public static void diagonalStripes(GUI gui, GUIItem primary, GUIItem secondary) {
        if (gui == null || primary == null || secondary == null) return;
        for (int slot = 0; slot < gui.getSize(); slot++) {
            int col = slot % 9;
            int row = slot / 9;
            if ((row + col) % 3 == 0) {
                gui.setItem(slot, primary);
            } else {
                gui.setItem(slot, secondary);
            }
        }
    }

    public static void concentricRect(GUI gui, GUIItem border, GUIItem interior) {
        if (gui == null || border == null || interior == null) return;
        int rows = gui.getSize() / 9;
        for (int slot = 0; slot < gui.getSize(); slot++) {
            int col = slot % 9;
            int row = slot / 9;
            int distFromTop = row;
            int distFromBottom = rows - 1 - row;
            int distFromLeft = col;
            int distFromRight = 8 - col;
            int minDist = Math.min(Math.min(distFromTop, distFromBottom), Math.min(distFromLeft, distFromRight));
            gui.setItem(slot, minDist == 0 ? border : interior);
        }
    }

    public static void diamond(GUI gui, GUIItem outline, GUIItem fill) {
        if (gui == null || outline == null || fill == null) return;
        int rows = gui.getSize() / 9;
        int midRow = rows / 2;
        int midCol = 4;
        for (int slot = 0; slot < gui.getSize(); slot++) {
            int col = slot % 9;
            int row = slot / 9;
            int dist = Math.abs(row - midRow) + Math.abs(col - midCol);
            if (dist <= Math.min(midRow, 4)) {
                gui.setItem(slot, dist == Math.min(midRow, 4) ? outline : fill);
            }
        }
    }

    public static void cross(GUI gui, GUIItem arm, GUIItem background) {
        if (gui == null || arm == null || background == null) return;
        int rows = gui.getSize() / 9;
        int midRow = rows / 2;
        for (int slot = 0; slot < gui.getSize(); slot++) {
            int col = slot % 9;
            int row = slot / 9;
            if (row == midRow || col == 4) {
                gui.setItem(slot, arm);
            } else {
                gui.setItem(slot, background);
            }
        }
    }

    public static void frame(GUI gui, GUIItem borderItem, int thickness) {
        if (gui == null || borderItem == null || thickness <= 0) return;
        int rows = gui.getSize() / 9;
        for (int slot = 0; slot < gui.getSize(); slot++) {
            int col = slot % 9;
            int row = slot / 9;
            boolean isBorder = row < thickness || row >= rows - thickness
                || col < thickness || col >= 8 - thickness + 1;
            if (isBorder) {
                gui.setItem(slot, borderItem);
            }
        }
    }

    public static void stripesHorizontal(GUI gui, GUIItem stripe, int stripeHeight, int gapHeight) {
        if (gui == null || stripe == null || stripeHeight <= 0 || gapHeight < 0) return;
        int interval = stripeHeight + gapHeight;
        for (int slot = 0; slot < gui.getSize(); slot++) {
            int row = slot / 9;
            if (row % interval < stripeHeight) {
                gui.setItem(slot, stripe);
            }
        }
    }

    public static void stripesVertical(GUI gui, GUIItem stripe, int stripeWidth, int gapWidth) {
        if (gui == null || stripe == null || stripeWidth <= 0 || gapWidth < 0) return;
        int interval = stripeWidth + gapWidth;
        for (int slot = 0; slot < gui.getSize(); slot++) {
            int col = slot % 9;
            if (col % interval < stripeWidth) {
                gui.setItem(slot, stripe);
            }
        }
    }

    public static List<GUIItem> getRow(GUI gui, int row) {
        List<GUIItem> result = new ArrayList<>();
        if (gui == null || row < 0) return result;
        int start = row * 9;
        int end = Math.min(start + 9, gui.getSize());
        for (int slot = start; slot < end; slot++) {
            result.add(gui.getItem(slot));
        }
        return result;
    }

    public static List<GUIItem> getColumn(GUI gui, int column) {
        List<GUIItem> result = new ArrayList<>();
        if (gui == null || column < 0 || column > 8) return result;
        int rows = gui.getSize() / 9;
        for (int row = 0; row < rows; row++) {
            result.add(gui.getItem(row * 9 + column));
        }
        return result;
    }

    public static GUIItem createGlassPane(Material material) {
        return BukkitGUIItem.of(material);
    }

    public static GUIItem createGlassPane(Material material, String name) {
        return BukkitGUIItem.builder(material).name(name).build();
    }

    public static GUIItem fillerPane() {
        return BukkitGUIItem.of(Material.BLACK_STAINED_GLASS_PANE);
    }

    public static GUIItem transparentPane() {
        return BukkitGUIItem.of(Material.GRAY_STAINED_GLASS_PANE);
    }

    public static GUIItem glassPane(Material material, String displayName, String... lore) {
        return BukkitGUIItem.builder(material).name(displayName).lore(lore).build();
    }

}
