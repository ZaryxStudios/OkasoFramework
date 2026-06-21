package com.zaryxstudios.okaso.common.i18n;

import java.util.Locale;

public interface TranslationManager {
    Translator getTranslator(Locale locale);
    void registerTranslator(Locale locale, Translator translator);
    Locale getDefaultLocale();
    void setDefaultLocale(Locale locale);
}
