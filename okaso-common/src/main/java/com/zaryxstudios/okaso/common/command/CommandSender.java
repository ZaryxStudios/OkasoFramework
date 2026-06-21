package com.zaryxstudios.okaso.common.command;

public interface CommandSender {

    String getName();

    void sendMessage(String message);

    void sendMessage(String... messages);

    boolean hasPermission(String permission);

    boolean isPlayer();
}
