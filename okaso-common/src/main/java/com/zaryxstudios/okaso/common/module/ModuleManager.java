package com.zaryxstudios.okaso.common.module;

import java.util.Collection;
import java.util.Optional;

public interface ModuleManager {
    void registerModule(Module module);
    void unregisterModule(String name);
    Optional<Module> getModule(String name);
    Collection<Module> getModules();
    void enableAll();
    void disableAll();
}
