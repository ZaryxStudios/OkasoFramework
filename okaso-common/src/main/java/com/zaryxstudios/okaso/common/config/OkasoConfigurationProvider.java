package com.zaryxstudios.okaso.common.config;

import java.io.File;

public interface OkasoConfigurationProvider {
    OkasoConfigurationSection load(File file);
    void save(OkasoConfigurationSection section, File file);
}
