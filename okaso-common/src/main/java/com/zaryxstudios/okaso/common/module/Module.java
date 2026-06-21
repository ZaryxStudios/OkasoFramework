package com.zaryxstudios.okaso.common.module;

import java.util.Collections;
import java.util.List;

public interface Module {

    String getName();

    String getVersion();

    void onEnable();

    void onDisable();

    default boolean isEnabled() {
        return false;
    }

    default List<String> getDependencies() {
        return Collections.emptyList();
    }
}
