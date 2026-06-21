package com.zaryxstudios.okaso.common.i18n;

public interface Translator {
    String translate(String key);
    String translate(String key, Object... args);
    boolean hasKey(String key);
}
