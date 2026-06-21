package com.zaryxstudios.okaso.common.plugin;

import com.zaryxstudios.okaso.common.OkasoAPI;

public interface OkasoPlugin {

    String getName();

    String getVersion();

    void onOkasoEnable();

    void onOkasoDisable();

    java.util.logging.Logger getOkasoLogger();

    default OkasoAPI getOkasoAPI() {
        return OkasoAPI.getInstance();
    }
}
