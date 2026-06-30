package com.zaryxstudios.okaso.common.hologram;

import java.util.Objects;

public final class HologramLine {

    private final HologramLineType type;
    private final String content;

    private HologramLine(HologramLineType type, String content) {
        this.type = Objects.requireNonNull(type, "type");
        this.content = content != null ? content : "";
    }

    public static HologramLine text(String text) {
        return new HologramLine(HologramLineType.TEXT, text != null ? text : "");
    }

    public static HologramLine item(String materialName, int amount) {
        return new HologramLine(HologramLineType.ITEM, materialName + ":" + Math.max(1, amount));
    }

    public static HologramLine item(String materialName) {
        return item(materialName, 1);
    }

    public static HologramLine mob(String entityType) {
        return new HologramLine(HologramLineType.MOB, entityType != null ? entityType.toUpperCase() : "");
    }

    public HologramLineType getType() {
        return type;
    }

    public String getContent() {
        return content;
    }

    public String getText() {
        if (type == HologramLineType.TEXT) return content;
        return "";
    }

    public String getItemMaterial() {
        if (type != HologramLineType.ITEM) return "";
        int colon = content.indexOf(':');
        return colon > 0 ? content.substring(0, colon) : content;
    }

    public int getItemAmount() {
        if (type != HologramLineType.ITEM) return 0;
        int colon = content.indexOf(':');
        if (colon > 0 && colon + 1 < content.length()) {
            try {
                return Integer.parseInt(content.substring(colon + 1));
            } catch (NumberFormatException ignored) {
            }
        }
        return 1;
    }

    public String getEntityType() {
        if (type != HologramLineType.MOB) return "";
        return content;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof HologramLine)) return false;
        HologramLine that = (HologramLine) o;
        return type == that.type && content.equals(that.content);
    }

    @Override
    public int hashCode() {
        return Objects.hash(type, content);
    }

    @Override
    public String toString() {
        return type + ":" + content;
    }
}
