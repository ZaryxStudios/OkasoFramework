package com.zaryxstudios.okaso.module;

public interface ModuleLifecycleListener {

    default void onModuleEnabled(String moduleName) {
    }


    default void onModuleDisabled(String moduleName) {
    }

    default void onModuleEnableFailed(String moduleName, Exception cause) {
    }
}
