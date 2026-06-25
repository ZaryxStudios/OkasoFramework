package com.zaryxstudios.okaso.common.message;

public final class Messages {

    public static final String COMMAND_NO_PERMISSION      = "command.no-permission";
    public static final String COMMAND_PLAYER_ONLY         = "command.player-only";
    public static final String COMMAND_CONSOLE_ONLY        = "command.console-only";
    public static final String COMMAND_COOLDOWN            = "command.cooldown";
    public static final String COMMAND_SUB_NO_PERMISSION   = "command.sub-no-permission";
    public static final String COMMAND_SUB_PLAYER_ONLY     = "command.sub-player-only";
    public static final String COMMAND_ERROR               = "command.error";
    public static final String COMMAND_HELP_HEADER         = "command.help.header";
    public static final String COMMAND_HELP_SUBTITLE       = "command.help.subtitle";
    public static final String COMMAND_HELP_ENTRY          = "command.help.entry";

    public static final String GUI_CONFIRM_TITLE           = "gui.confirm.title";

    public static final String DEFAULT_NO_PERMISSION       = "&cYou don't have permission to use this command.";
    public static final String DEFAULT_PLAYER_ONLY         = "&cOnly players can use this command.";
    public static final String DEFAULT_CONSOLE_ONLY        = "&cOnly the console can use this command.";
    public static final String DEFAULT_COOLDOWN            = "&cPlease wait &e{0} ticks &cbefore using this command again.";
    public static final String DEFAULT_SUB_NO_PERMISSION   = "&cYou don't have permission to use this sub-command.";
    public static final String DEFAULT_SUB_PLAYER_ONLY     = "&cOnly players can use this sub-command.";
    public static final String DEFAULT_COMMAND_ERROR       = "&cAn error occurred while executing this command.";
    public static final String DEFAULT_HELP_HEADER         = "&6&l{0} &7- &f{1}";
    public static final String DEFAULT_HELP_SUBTITLE       = "&6Sub-commands:";
    public static final String DEFAULT_HELP_ENTRY          = "  &e{0} &7- &f{1}";

    private Messages() {
        throw new UnsupportedOperationException("Constants class");
    }
}
