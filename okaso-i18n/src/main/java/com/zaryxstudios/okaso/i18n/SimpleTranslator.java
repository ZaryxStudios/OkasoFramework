package com.zaryxstudios.okaso.i18n;

import com.zaryxstudios.okaso.common.i18n.Translator;

import java.text.MessageFormat;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class SimpleTranslator implements Translator {

    private final Map<String, String> messages;

    public SimpleTranslator() {
        this.messages = new ConcurrentHashMap<String, String>();
    }

    public SimpleTranslator(Map<String, String> messages) {
        this.messages = new ConcurrentHashMap<String, String>(messages);
    }

    public void set(String key, String message) {
        messages.put(key, message);
    }

    @Override
    public String translate(String key) {
        String message = messages.get(key);
        return message != null ? message : key;
    }

    @Override
    public String translate(String key, Object... args) {
        String message = messages.get(key);
        if (message == null) return key;
        return MessageFormat.format(message, args);
    }

    @Override
    public boolean hasKey(String key) {
        return messages.containsKey(key);
    }
}
