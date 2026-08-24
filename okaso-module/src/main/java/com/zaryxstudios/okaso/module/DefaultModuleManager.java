package com.zaryxstudios.okaso.module;

import com.zaryxstudios.okaso.common.module.Module;
import com.zaryxstudios.okaso.common.module.ModuleManager;
import com.zaryxstudios.okaso.common.message.LogMessages;
import com.zaryxstudios.okaso.common.module.ModuleVersion;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Logger;

import lombok.Getter;

public class DefaultModuleManager implements ModuleManager {

    private static final Logger LOG = Logger.getLogger(DefaultModuleManager.class.getName());

    private final Map<String, Module> modules = new ConcurrentHashMap<>();
    private final Map<String, ModuleConfig> configs = new ConcurrentHashMap<>();
    private final ModuleDependencyResolver resolver = new ModuleDependencyResolver();
    @Getter
    private final ModuleEventBus eventBus = new ModuleEventBus();
    private final List<ModuleLifecycleListener> listeners = new ArrayList<>();

    @Override
    public void registerModule(Module module) {
        Objects.requireNonNull(module, "module");
        String name = module.getName();
        if (modules.containsKey(name)) {
            throw new IllegalStateException("Module already registered: " + name);
        }
        modules.put(name, module);
        configs.put(name, new ModuleConfig(name));
        resolver.register(name, module.getDependencies());
        LOG.info(LogMessages.get(LogMessages.MODULE_REGISTERED, name));
    }

    @Override
    public void unregisterModule(String name) {
        Module removed = modules.remove(name);
        if (removed != null) {
            configs.remove(name);
            resolver.unregister(name);
            LOG.info(LogMessages.get(LogMessages.MODULE_UNREGISTERED, name));
        }
    }

    @Override
    public Optional<Module> getModule(String name) {
        return Optional.ofNullable(modules.get(name));
    }

    @Override
    public Collection<Module> getModules() {
        return Collections.unmodifiableCollection(modules.values());
    }

    public ModuleConfig getConfig(String name) {
        return configs.getOrDefault(name, new ModuleConfig(name));
    }

    public void addListener(ModuleLifecycleListener listener) {
        listeners.add(listener);
    }

    public void removeListener(ModuleLifecycleListener listener) {
        listeners.remove(listener);
    }

    @Override
    public void enableAll() {
        List<String> order;
        try {
            order = resolver.getEnableOrder();
        } catch (ModuleDependencyResolver.CycleDependencyException e) {
            LOG.severe(LogMessages.get(LogMessages.MODULE_ENABLE_CYCLE, e.getMessage()));
            return;
        }

        for (String name : order) {
            Module module = modules.get(name);
            if (module != null && !module.isEnabled()) {
                enableModule(name, module);
            }
        }
    }

    @Override
    public void disableAll() {
        List<String> order;
        try {
            order = resolver.getDisableOrder();
        } catch (ModuleDependencyResolver.CycleDependencyException e) {
            LOG.severe(LogMessages.get(LogMessages.MODULE_DISABLE_CYCLE, e.getMessage()));
            return;
        }

        for (String name : order) {
            Module module = modules.get(name);
            if (module != null && module.isEnabled()) {
                disableModule(name, module);
            }
        }
    }

    public boolean enableModule(String name) {
        Module module = modules.get(name);
        if (module == null) return false;
        if (module.isEnabled()) return true;
        return enableModule(name, module);
    }

    public void disableModule(String name) {
        Module module = modules.get(name);
        if (module == null || !module.isEnabled()) return;
        disableModule(name, module);
    }

    private boolean enableModule(String name, Module module) {
        try {
            module.onEnable();
            LOG.info(LogMessages.get(LogMessages.MODULE_ENABLED, name));
            for (ModuleLifecycleListener l : listeners) {
                try { l.onModuleEnabled(name); } catch (Exception ignored) {}
            }
            return true;
        } catch (Exception e) {
            LOG.severe(LogMessages.get(LogMessages.MODULE_ENABLE_FAILED, name, e.getMessage()));
            for (ModuleLifecycleListener l : listeners) {
                try { l.onModuleEnableFailed(name, e); } catch (Exception ignored) {}
            }
            return false;
        }
    }

    private void disableModule(String name, Module module) {
        try {
            module.onDisable();
            LOG.info(LogMessages.get(LogMessages.MODULE_DISABLED, name));
            for (ModuleLifecycleListener l : listeners) {
                try { l.onModuleDisabled(name); } catch (Exception ignored) {}
            }
        } catch (Exception e) {
            LOG.warning(LogMessages.get(LogMessages.MODULE_DISABLE_FAILED, name, e.getMessage()));
        }
    }
}
