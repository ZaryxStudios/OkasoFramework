package com.zaryxstudios.okaso.common.config;

import java.util.List;
import java.util.Set;

public interface ConfigurationSection {
    String getString(String path);
    String getString(String path, String def);
    int getInt(String path);
    int getInt(String path, int def);
    double getDouble(String path);
    double getDouble(String path, double def);
    long getLong(String path);
    long getLong(String path, long def);
    boolean getBoolean(String path);
    boolean getBoolean(String path, boolean def);
    List<String> getStringList(String path);
    List<Integer> getIntegerList(String path);
    ConfigurationSection getSection(String path);
    Set<String> getKeys(boolean deep);
    void set(String path, Object value);
    boolean contains(String path);
}
