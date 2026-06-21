package com.zaryxstudios.okaso.common.nbt;

public interface NBTItem {
    NBTCompound getCompound();
    Object getItemStack();
    void apply();
    NBTItem copy();
    boolean hasTag(String key);
    void removeTag(String key);
}
