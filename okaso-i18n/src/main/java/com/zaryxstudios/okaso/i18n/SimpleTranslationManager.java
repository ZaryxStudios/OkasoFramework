package com.zaryxstudios.okaso.i18n;

import com.zaryxstudios.okaso.common.i18n.TranslationManager;
import com.zaryxstudios.okaso.common.i18n.Translator;

import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import lombok.Getter;
import lombok.Setter;

public class SimpleTranslationManager implements TranslationManager {

    private final Map<Locale, Translator> translators;
    @Getter @Setter
    private Locale defaultLocale;

    public SimpleTranslationManager() {
        this.translators = new ConcurrentHashMap<Locale, Translator>();
        this.defaultLocale = Locale.US;
    }

    @Override
    public Translator getTranslator(Locale locale) {
        Translator t = translators.get(locale);
        if (t == null) {
            t = translators.get(defaultLocale);
        }
        return t != null ? t : new SimpleTranslator();
    }

    @Override
    public void registerTranslator(Locale locale, Translator translator) {
        translators.put(locale, translator);
    }

}
