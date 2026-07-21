package com.zaryxstudios.okaso.common.command;

public interface OkasoCommandSender {

    String getName();

    void sendMessage(String message);

    void sendMessage(String... messages);

    boolean hasPermission(String permission);

    boolean isPlayer();

    default boolean isConsole() {
        return !isPlayer();
    }
}
