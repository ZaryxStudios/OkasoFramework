package com.zaryxstudios.okaso.common.command;

@FunctionalInterface
public interface CommandHandler {
    void execute(CommandContext context);
}
