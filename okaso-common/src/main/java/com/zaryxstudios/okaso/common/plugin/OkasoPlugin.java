package com.zaryxstudios.okaso.common.plugin;

import com.zaryxstudios.okaso.common.OkasoAPI;
import java.util.logging.Logger;

public interface OkasoPlugin {

    String getName();

    String getVersion();

    void onOkasoEnable();

    void onOkasoDisable();

    Logger getOkasoLogger();

    default OkasoAPI getOkasoAPI() {
        return OkasoAPI.getInstance();
    }
}
