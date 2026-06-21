package com.zaryxstudios.okaso.common.nbt;

import java.util.Set;

public interface NBTCompound {
    String getString(String key);
    int getInt(String key);
    double getDouble(String key);
    long getLong(String key);
    short getShort(String key);
    byte getByte(String key);
    boolean getBoolean(String key);
    float getFloat(String key);
    int[] getIntArray(String key);
    byte[] getByteArray(String key);
    NBTCompound getCompound(String key);
    Set<String> getKeys();
    void setString(String key, String value);
    void setInt(String key, int value);
    void setDouble(String key, double value);
    void setLong(String key, long value);
    void setShort(String key, short value);
    void setByte(String key, byte value);
    void setBoolean(String key, boolean value);
    void setFloat(String key, float value);
    void setIntArray(String key, int[] value);
    void setByteArray(String key, byte[] value);
    void setCompound(String key, NBTCompound compound);
    boolean hasKey(String key);
    void remove(String key);
    void clear();
    boolean isEmpty();
    int size();
}
