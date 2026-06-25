package com.zaryxstudios.okaso.common.message;

import com.zaryxstudios.okaso.common.text.TextColorizer;

import java.util.HashMap;
import java.util.Map;

public class DefaultMessageProvider implements MessageProvider {

    private final Map<String, String> messages = new HashMap<>();

    public DefaultMessageProvider() {
        loadDefaults();
    }

    private void loadDefaults() {
        messages.put(Messages.COMMAND_NO_PERMISSION,      TextColorizer.translate(Messages.DEFAULT_NO_PERMISSION));
        messages.put(Messages.COMMAND_PLAYER_ONLY,         TextColorizer.translate(Messages.DEFAULT_PLAYER_ONLY));
        messages.put(Messages.COMMAND_CONSOLE_ONLY,        TextColorizer.translate(Messages.DEFAULT_CONSOLE_ONLY));
        messages.put(Messages.COMMAND_COOLDOWN,            TextColorizer.translate(Messages.DEFAULT_COOLDOWN));
        messages.put(Messages.COMMAND_SUB_NO_PERMISSION,   TextColorizer.translate(Messages.DEFAULT_SUB_NO_PERMISSION));
        messages.put(Messages.COMMAND_SUB_PLAYER_ONLY,     TextColorizer.translate(Messages.DEFAULT_SUB_PLAYER_ONLY));
        messages.put(Messages.COMMAND_ERROR,               TextColorizer.translate(Messages.DEFAULT_COMMAND_ERROR));
        messages.put(Messages.COMMAND_HELP_HEADER,         TextColorizer.translate(Messages.DEFAULT_HELP_HEADER));
        messages.put(Messages.COMMAND_HELP_SUBTITLE,       TextColorizer.translate(Messages.DEFAULT_HELP_SUBTITLE));
        messages.put(Messages.COMMAND_HELP_ENTRY,          TextColorizer.translate(Messages.DEFAULT_HELP_ENTRY));
    }

    @Override
    public String get(String key) {
        return messages.getOrDefault(key, key);
    }

    @Override
    public String format(String key, Object... args) {
        String msg = messages.get(key);
        if (msg == null) return key;
        for (int i = 0; i < args.length; i++) {
            String value = args[i] != null ? args[i].toString() : "null";
            msg = msg.replace("{" + i + "}", value);
        }
        return msg;
    }

    @Override
    public void set(String key, String value) {
        if (value == null) {
            messages.remove(key);
        } else {
            messages.put(key, TextColorizer.translate(value));
        }
    }
}
