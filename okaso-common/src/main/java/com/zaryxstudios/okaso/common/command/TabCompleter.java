package com.zaryxstudios.okaso.common.command;

import java.util.Collections;
import java.util.List;

@FunctionalInterface
public interface OkasoTabCompleter {
    List<String> onTabComplete(CommandContext context);
}
