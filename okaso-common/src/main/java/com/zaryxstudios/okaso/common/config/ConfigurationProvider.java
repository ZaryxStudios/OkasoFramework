package com.zaryxstudios.okaso.common.config;

import java.io.File;

public interface ConfigurationProvider {
    ConfigurationSection load(File file);
    void save(ConfigurationSection section, File file);
}
