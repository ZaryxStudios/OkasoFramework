package com.zaryxstudios.okaso.placeholder;

import com.zaryxstudios.okaso.common.placeholder.PlaceholderParser;
import com.zaryxstudios.okaso.common.placeholder.PlaceholderRegistry;

import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;

import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class BukkitPlaceholderParser implements PlaceholderParser {

    private static final Pattern PLACEHOLDER_PATTERN = Pattern.compile("%([^%]+)%");

    private final PlaceholderRegistry registry;

    public BukkitPlaceholderParser(PlaceholderRegistry registry) {
        this.registry = registry;
    }

    @Override
    public String parse(String text, Object player) {
        if (text == null || text.isEmpty()) return text;

        StringBuilder result = new StringBuilder();
        Matcher matcher = PLACEHOLDER_PATTERN.matcher(text);
        int lastEnd = 0;

        while (matcher.find()) {
            result.append(text, lastEnd, matcher.start());

            String identifier = matcher.group(1);
            Optional<String> value = getPlaceholderValue(identifier, player);
            result.append(value.orElse(matcher.group(0)));

            lastEnd = matcher.end();
        }

        result.append(text.substring(lastEnd));
        return result.toString();
    }

    @Override
    public Optional<String> getPlaceholderValue(String placeholder, Object player) {
        if (placeholder == null || placeholder.isEmpty()) return Optional.empty();

        String value = resolveFromRegistry(placeholder, player);
        if (value != null) return Optional.of(value);

        if (player instanceof Player) {
            Player p = (Player) player;
            if ("player_name".equals(placeholder)) return Optional.of(p.getName());
            if ("player_displayname".equals(placeholder)) return Optional.of(p.getDisplayName());
            if ("player_health".equals(placeholder)) return Optional.of(String.valueOf((int) p.getHealth()));
            if ("player_food".equals(placeholder)) return Optional.of(String.valueOf(p.getFoodLevel()));
            if ("player_level".equals(placeholder)) return Optional.of(String.valueOf(p.getLevel()));
            if ("player_world".equals(placeholder)) return Optional.of(p.getWorld().getName());
            if ("player_gamemode".equals(placeholder)) return Optional.of(p.getGameMode().name());
        } else if (player instanceof OfflinePlayer) {
            OfflinePlayer op = (OfflinePlayer) player;
            if ("player_name".equals(placeholder)) return Optional.of(op.getName());
        }

        return Optional.empty();
    }

    @Override
    public boolean hasPlaceholders(String text) {
        if (text == null || text.isEmpty()) return false;
        return PLACEHOLDER_PATTERN.matcher(text).find();
    }

    @Override
    public String removePlaceholders(String text) {
        if (text == null || text.isEmpty()) return text;
        return PLACEHOLDER_PATTERN.matcher(text).replaceAll("");
    }

    private String resolveFromRegistry(String placeholder, Object player) {
        if (registry instanceof SimplePlaceholderRegistry) {
            SimplePlaceholderRegistry simple = (SimplePlaceholderRegistry) registry;
            for (String identifier : simple.getIdentifiers()) {
                if (placeholder.startsWith(identifier + "_") || placeholder.equals(identifier)) {
                    return simple.resolve(identifier, player);
                }
            }
        }
        return null;
    }
}
