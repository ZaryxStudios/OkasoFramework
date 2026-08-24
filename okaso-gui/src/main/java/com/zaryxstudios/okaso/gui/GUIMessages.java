package com.zaryxstudios.okaso.gui;

import com.zaryxstudios.okaso.common.config.OkasoConfigurationProvider;
import com.zaryxstudios.okaso.common.config.OkasoConfigurationSection;
import com.zaryxstudios.okaso.common.message.DefaultMessageProvider;
import com.zaryxstudios.okaso.common.message.MessageProvider;

import java.io.File;
import java.util.Map;

public final class GUIMessages {

    public static final String PAGE_NEXT = "gui.page.next";
    public static final String PAGE_NEXT_DISABLED = "gui.page.next-disabled";
    public static final String PAGE_PREVIOUS = "gui.page.previous";
    public static final String PAGE_PREVIOUS_DISABLED = "gui.page.previous-disabled";
    public static final String PAGE_INDICATOR = "gui.page.indicator";
    public static final String PAGE_FIRST = "gui.page.first";
    public static final String PAGE_LAST = "gui.page.last";

    public static final String BUTTON_CLOSE = "gui.button.close";
    public static final String BUTTON_BACK = "gui.button.back";
    public static final String BUTTON_NEXT_PAGE = "gui.button.next-page";
    public static final String BUTTON_PREVIOUS_PAGE = "gui.button.previous-page";
    public static final String BUTTON_CONFIRM = "gui.button.confirm";
    public static final String BUTTON_CANCEL = "gui.button.cancel";

    public static final String NAV_NO_HISTORY = "gui.nav.no-history";
    public static final String NAV_BACK = "gui.nav.back";
    public static final String NAV_NO_FORWARD = "gui.nav.no-forward";
    public static final String NAV_FORWARD = "gui.nav.forward";
    public static final String NAV_UNAVAILABLE = "gui.nav.unavailable";

    public static final String DIALOG_TITLE = "gui.dialog.title";
    public static final String DIALOG_MESSAGE = "gui.dialog.message";
    public static final String DIALOG_DELETE_TITLE = "gui.dialog.delete-title";
    public static final String DIALOG_DELETE_MESSAGE = "gui.dialog.delete-message";
    public static final String DIALOG_DELETE_CONFIRM = "gui.dialog.delete-confirm";

    public static final String DEFAULT_PAGE_NEXT = "&aNext →";
    public static final String DEFAULT_PAGE_NEXT_DISABLED = "&7No more pages";
    public static final String DEFAULT_PAGE_PREVIOUS = "&a← Previous";
    public static final String DEFAULT_PAGE_PREVIOUS_DISABLED = "&7No previous pages";
    public static final String DEFAULT_PAGE_INDICATOR = "&ePage {0} / {1}";
    public static final String DEFAULT_PAGE_FIRST = "&eFirst page";
    public static final String DEFAULT_PAGE_LAST = "&eLast page";

    public static final String DEFAULT_BUTTON_CLOSE = "&cClose";
    public static final String DEFAULT_BUTTON_BACK = "&7Back";
    public static final String DEFAULT_BUTTON_NEXT_PAGE = "&aNext page";
    public static final String DEFAULT_BUTTON_PREVIOUS_PAGE = "&aPrevious page";
    public static final String DEFAULT_BUTTON_CONFIRM = "&a&lConfirm";
    public static final String DEFAULT_BUTTON_CANCEL = "&c&lCancel";

    public static final String DEFAULT_NAV_NO_HISTORY = "&7No history";
    public static final String DEFAULT_NAV_BACK = "&eBack";
    public static final String DEFAULT_NAV_NO_FORWARD = "&7Nothing to forward";
    public static final String DEFAULT_NAV_FORWARD = "&eForward";
    public static final String DEFAULT_NAV_UNAVAILABLE = "&cUnavailable";

    public static final String DEFAULT_DIALOG_TITLE = "&6Confirm";
    public static final String DEFAULT_DIALOG_MESSAGE = "Are you sure?";
    public static final String DEFAULT_DIALOG_DELETE_TITLE = "&cDelete {0}";
    public static final String DEFAULT_DIALOG_DELETE_MESSAGE = "&7Delete {0}?";
    public static final String DEFAULT_DIALOG_DELETE_CONFIRM = "&c&lDelete";

    private static final String[] ALL_KEYS = {
        PAGE_NEXT, PAGE_NEXT_DISABLED, PAGE_PREVIOUS, PAGE_PREVIOUS_DISABLED,
        PAGE_INDICATOR, PAGE_FIRST, PAGE_LAST,
        BUTTON_CLOSE, BUTTON_BACK, BUTTON_NEXT_PAGE, BUTTON_PREVIOUS_PAGE,
        BUTTON_CONFIRM, BUTTON_CANCEL,
        NAV_NO_HISTORY, NAV_BACK, NAV_NO_FORWARD, NAV_FORWARD, NAV_UNAVAILABLE,
        DIALOG_TITLE, DIALOG_MESSAGE,
        DIALOG_DELETE_TITLE, DIALOG_DELETE_MESSAGE, DIALOG_DELETE_CONFIRM
    };

    private static volatile MessageProvider provider;

    static {
        reset();
    }

    private GUIMessages() {
        throw new UnsupportedOperationException("Constants class");
    }

    public static void reset() {
        DefaultMessageProvider defaults = new DefaultMessageProvider();
        applyDefaults(defaults);
        provider = defaults;
    }

    public static MessageProvider getProvider() {
        return provider();
    }

    public static void setProvider(MessageProvider newProvider) {
        if (newProvider == null) {
            reset();
            return;
        }
        provider = newProvider;
    }

    public static String get(String key) {
        return provider().get(key);
    }

    public static String get(String key, Object... args) {
        return provider().format(key, args);
    }

    public static String get(String key, Map<String, ?> placeholders, Object... args) {
        return provider().format(key, placeholders, args);
    }

    public static void set(String key, String value) {
        if (key == null || key.isEmpty()) return;
        provider().set(key, value);
    }

    public static void loadFromSection(OkasoConfigurationSection section) {
        if (section == null) return;
        for (String key : ALL_KEYS) {
            if (section.contains(key)) {
                String value = section.getString(key);
                if (value != null && !value.isEmpty()) {
                    set(key, value);
                }
            }
        }
    }

    public static void loadFromFile(File file, OkasoConfigurationProvider configProvider) {
        if (file == null || !file.exists() || configProvider == null) return;
        loadFromSection(configProvider.load(file));
    }

    public static void saveToSection(OkasoConfigurationSection section) {
        if (section == null) return;
        for (String key : ALL_KEYS) {
            section.set(key, get(key));
        }
    }

    private static void applyDefaults(MessageProvider target) {
        target.set(PAGE_NEXT, DEFAULT_PAGE_NEXT);
        target.set(PAGE_NEXT_DISABLED, DEFAULT_PAGE_NEXT_DISABLED);
        target.set(PAGE_PREVIOUS, DEFAULT_PAGE_PREVIOUS);
        target.set(PAGE_PREVIOUS_DISABLED, DEFAULT_PAGE_PREVIOUS_DISABLED);
        target.set(PAGE_INDICATOR, DEFAULT_PAGE_INDICATOR);
        target.set(PAGE_FIRST, DEFAULT_PAGE_FIRST);
        target.set(PAGE_LAST, DEFAULT_PAGE_LAST);
        target.set(BUTTON_CLOSE, DEFAULT_BUTTON_CLOSE);
        target.set(BUTTON_BACK, DEFAULT_BUTTON_BACK);
        target.set(BUTTON_NEXT_PAGE, DEFAULT_BUTTON_NEXT_PAGE);
        target.set(BUTTON_PREVIOUS_PAGE, DEFAULT_BUTTON_PREVIOUS_PAGE);
        target.set(BUTTON_CONFIRM, DEFAULT_BUTTON_CONFIRM);
        target.set(BUTTON_CANCEL, DEFAULT_BUTTON_CANCEL);
        target.set(NAV_NO_HISTORY, DEFAULT_NAV_NO_HISTORY);
        target.set(NAV_BACK, DEFAULT_NAV_BACK);
        target.set(NAV_NO_FORWARD, DEFAULT_NAV_NO_FORWARD);
        target.set(NAV_FORWARD, DEFAULT_NAV_FORWARD);
        target.set(NAV_UNAVAILABLE, DEFAULT_NAV_UNAVAILABLE);
        target.set(DIALOG_TITLE, DEFAULT_DIALOG_TITLE);
        target.set(DIALOG_MESSAGE, DEFAULT_DIALOG_MESSAGE);
        target.set(DIALOG_DELETE_TITLE, DEFAULT_DIALOG_DELETE_TITLE);
        target.set(DIALOG_DELETE_MESSAGE, DEFAULT_DIALOG_DELETE_MESSAGE);
        target.set(DIALOG_DELETE_CONFIRM, DEFAULT_DIALOG_DELETE_CONFIRM);
    }

    private static MessageProvider provider() {
        MessageProvider current = provider;
        if (current == null) {
            synchronized (GUIMessages.class) {
                current = provider;
                if (current == null) {
                    DefaultMessageProvider defaults = new DefaultMessageProvider();
                    applyDefaults(defaults);
                    provider = defaults;
                    current = defaults;
                }
            }
        }
        return current;
    }
}
