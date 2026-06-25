package com.zaryxstudios.okaso.common.text;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class TextColorizer {

    private static final Pattern HEX_PATTERN = Pattern.compile("&#([A-Fa-f0-9]{6})");
    private static final Pattern MINI_TAG_PATTERN = Pattern.compile("<[/]?([a-zA-Z#][a-zA-Z0-9]*)>");
    private static final Pattern MINI_HEX_PATTERN = Pattern.compile("<#([A-Fa-f0-9]{6})>");

    private static final Map<String, String> MINI_COLORS = new HashMap<>();
    private static final Map<String, String> MINI_FORMATS = new HashMap<>();

    static {
        MINI_COLORS.put("black",       "§0");
        MINI_COLORS.put("dark_blue",   "§1");
        MINI_COLORS.put("dark_green",  "§2");
        MINI_COLORS.put("dark_aqua",   "§3");
        MINI_COLORS.put("dark_red",    "§4");
        MINI_COLORS.put("dark_purple", "§5");
        MINI_COLORS.put("gold",        "§6");
        MINI_COLORS.put("gray",        "§7");
        MINI_COLORS.put("dark_gray",   "§8");
        MINI_COLORS.put("blue",        "§9");
        MINI_COLORS.put("green",       "§a");
        MINI_COLORS.put("aqua",        "§b");
        MINI_COLORS.put("red",         "§c");
        MINI_COLORS.put("light_purple","§d");
        MINI_COLORS.put("yellow",      "§e");
        MINI_COLORS.put("white",       "§f");
        MINI_COLORS.put("reset",       "§r");

        MINI_FORMATS.put("bold",       "§l");
        MINI_FORMATS.put("b",          "§l");
        MINI_FORMATS.put("italic",     "§o");
        MINI_FORMATS.put("i",          "§o");
        MINI_FORMATS.put("em",         "§o");
        MINI_FORMATS.put("underline",  "§n");
        MINI_FORMATS.put("u",          "§n");
        MINI_FORMATS.put("strikethrough","§m");
        MINI_FORMATS.put("st",         "§m");
        MINI_FORMATS.put("obfuscated", "§k");
        MINI_FORMATS.put("obf",        "§k");
        MINI_FORMATS.put("magic",      "§k");
    }

    private TextColorizer() {
        throw new UnsupportedOperationException("Utility class");
    }

    public static String translate(String text) {
        if (text == null || text.isEmpty()) return text;
        text = translateMiniMessage(text);
        text = translateHex(text);
        text = translateLegacy(text);
        return text;
    }

    public static String translateLegacy(String text) {
        if (text == null || text.isEmpty()) return text;
        char[] chars = text.toCharArray();
        StringBuilder sb = new StringBuilder(text.length());
        for (int i = 0; i < chars.length; i++) {
            if (chars[i] == '&' && i + 1 < chars.length) {
                char next = chars[i + 1];
                if ("0123456789AaBbCcDdEeFfKkLlMmNnOoRr".indexOf(next) != -1) {
                    sb.append('§').append(next);
                    i++;
                    continue;
                }
            }
            sb.append(chars[i]);
        }
        return sb.toString();
    }

    public static String translateHex(String text) {
        if (text == null || text.isEmpty() || !text.contains("&#")) return text;
        StringBuffer sb = new StringBuffer(text.length() + 48);
        Matcher m = HEX_PATTERN.matcher(text);
        while (m.find()) {
            String hex = m.group(1);
            StringBuilder replacement = new StringBuilder("§x");
            for (char c : hex.toCharArray()) {
                replacement.append('§').append(c);
            }
            m.appendReplacement(sb, Matcher.quoteReplacement(replacement.toString()));
        }
        m.appendTail(sb);
        return sb.toString();
    }

    public static String translateMiniMessage(String text) {
        if (text == null || text.isEmpty()) return text;

        {
            StringBuffer sb = new StringBuffer(text.length() + 48);
            Matcher m = MINI_HEX_PATTERN.matcher(text);
            while (m.find()) {
                String hex = m.group(1);
                StringBuilder repl = new StringBuilder("§x");
                for (char c : hex.toCharArray()) {
                    repl.append('§').append(c);
                }
                m.appendReplacement(sb, Matcher.quoteReplacement(repl.toString()));
            }
            m.appendTail(sb);
            text = sb.toString();
        }

        StringBuffer sb = new StringBuffer(text.length());
        Matcher m = MINI_TAG_PATTERN.matcher(text);
        while (m.find()) {
            String tag = m.group(1);
            boolean closing = text.charAt(m.start() + 1) == '/';
            String replacement = resolveMiniTag(tag, closing);
            m.appendReplacement(sb, Matcher.quoteReplacement(replacement));
        }
        m.appendTail(sb);
        return sb.toString();
    }

    private static String resolveMiniTag(String tag, boolean closing) {
        if (closing) return "§r";

        String lower = tag.toLowerCase();

        String color = MINI_COLORS.get(lower);
        if (color != null) return color;

        String format = MINI_FORMATS.get(lower);
        if (format != null) return format;

        return "<" + tag + ">";
    }

    public static String format(String template, Object... args) {
        String result = translate(template);
        if (args != null) {
            for (int i = 0; i < args.length; i++) {
                String value = args[i] != null ? args[i].toString() : "null";
                result = result.replace("{" + i + "}", value);
                result = result.replace("%s", value);
            }
        }
        return result;
    }
}
