package com.zaryxstudios.okaso.placeholder;

import com.zaryxstudios.okaso.common.placeholder.PlaceholderParser;
import com.zaryxstudios.okaso.common.placeholder.PlaceholderRegistry;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.Player;

import java.lang.management.ManagementFactory;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class OkasoBukkitPlaceholderParser implements PlaceholderParser {

    private static final Pattern PLACEHOLDER_PATTERN = Pattern.compile("%([^%]+)%");
    private static final Pattern RELATIONAL_PATTERN = Pattern.compile("%([^%]+)%\\s*([<>]=?|==|!=)\\s*([^%]+)");
    private static final Pattern FORMAT_PATTERN = Pattern.compile("%([^%]+):([a-zA-Z]+)%");

    private final PlaceholderRegistry registry;

    public OkasoBukkitPlaceholderParser(PlaceholderRegistry registry) {
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
            value = resolvePlayerPlaceholder(placeholder, p);
            if (value != null) return Optional.of(value);
        } else if (player instanceof OfflinePlayer) {
            OfflinePlayer op = (OfflinePlayer) player;
            value = resolveOfflinePlayerPlaceholder(placeholder, op);
            if (value != null) return Optional.of(value);
        }

        value = resolveServerPlaceholder(placeholder);
        if (value != null) return Optional.of(value);

        value = resolveDateTimePlaceholder(placeholder);
        if (value != null) return Optional.of(value);

        value = resolveMathPlaceholder(placeholder);
        if (value != null) return Optional.of(value);

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
            return simple.resolve(placeholder, player);
        }
        return null;
    }

    private String resolvePlayerPlaceholder(String placeholder, Player player) {
        String lower = placeholder.toLowerCase();
        switch (lower) {
            case "player_name":
                return player.getName();
            case "player_displayname":
                return player.getDisplayName();
            case "player_health":
                return String.valueOf((int) player.getHealth());
            case "player_max_health":
                return String.valueOf((int) player.getAttribute(Attribute.GENERIC_MAX_HEALTH).getValue());
            case "player_food":
                return String.valueOf(player.getFoodLevel());
            case "player_saturation":
                return String.valueOf((int) player.getSaturation());
            case "player_level":
                return String.valueOf(player.getLevel());
            case "player_exp":
                return String.valueOf((int) (player.getExp() * 100));
            case "player_total_exp":
                return String.valueOf(player.getTotalExperience());
            case "player_world":
                return player.getWorld().getName();
            case "player_gamemode":
                return player.getGameMode().name();
            case "player_uuid":
                return player.getUniqueId().toString();
            case "player_ip":
                return player.getAddress() != null ? player.getAddress().getAddress().getHostAddress() : "unknown";
            case "player_ping":
                try {
                    Object handle = player.getClass().getMethod("getHandle").invoke(player);
                    return String.valueOf(handle.getClass().getField("ping").getInt(handle));
                } catch (Exception e) {
                    return "unknown";
                }
            case "player_online":
                return "true";
            case "player_op":
                return String.valueOf(player.isOp());
            case "player_whitelisted":
                return String.valueOf(player.isWhitelisted());
            case "player_banned":
                return String.valueOf(player.isBanned());
            case "player_flying":
                return String.valueOf(player.isFlying());
            case "player_sneaking":
                return String.valueOf(player.isSneaking());
            case "player_sprinting":
                return String.valueOf(player.isSprinting());
            case "player_sleeping":
                return String.valueOf(player.isSleeping());
            case "player_vehicle":
                return player.getVehicle() != null ? player.getVehicle().getType().name() : "none";
            case "player_location_x":
                return String.format("%.2f", player.getLocation().getX());
            case "player_location_y":
                return String.format("%.2f", player.getLocation().getY());
            case "player_location_z":
                return String.format("%.2f", player.getLocation().getZ());
            case "player_yaw":
                return String.format("%.2f", player.getLocation().getYaw());
            case "player_pitch":
                return String.format("%.2f", player.getLocation().getPitch());
            case "player_held_item":
                return player.getInventory().getItemInMainHand() != null ? player.getInventory().getItemInMainHand().getType().name() : "AIR";
            case "player_held_item_amount":
                return String.valueOf(player.getInventory().getItemInMainHand().getAmount());
            case "player_armor_helmet":
                return player.getInventory().getHelmet() != null ? player.getInventory().getHelmet().getType().name() : "AIR";
            case "player_armor_chestplate":
                return player.getInventory().getChestplate() != null ? player.getInventory().getChestplate().getType().name() : "AIR";
            case "player_armor_leggings":
                return player.getInventory().getLeggings() != null ? player.getInventory().getLeggings().getType().name() : "AIR";
            case "player_armor_boots":
                return player.getInventory().getBoots() != null ? player.getInventory().getBoots().getType().name() : "AIR";
            default:
                return null;
        }
    }

    private String resolveOfflinePlayerPlaceholder(String placeholder, OfflinePlayer player) {
        String lower = placeholder.toLowerCase();
        switch (lower) {
            case "player_name":
                return player.getName();
            case "player_uuid":
                return player.getUniqueId().toString();
            case "player_first_played":
                return player.getFirstPlayed() > 0 ? String.valueOf(player.getFirstPlayed()) : "never";
            case "player_last_played":
                return player.getLastPlayed() > 0 ? String.valueOf(player.getLastPlayed()) : "never";
            case "player_online":
                return String.valueOf(player.isOnline());
            case "player_op":
                return String.valueOf(player.isOp());
            case "player_banned":
                return String.valueOf(player.isBanned());
            case "player_whitelisted":
                return String.valueOf(player.isWhitelisted());
            default:
                return null;
        }
    }

    private String resolveServerPlaceholder(String placeholder) {
        String lower = placeholder.toLowerCase();
        switch (lower) {
            case "server_name":
                return Bukkit.getServer().getName();
            case "server_version":
                return Bukkit.getVersion();
            case "server_bukkit_version":
                return Bukkit.getBukkitVersion();
            case "server_online":
                return String.valueOf(Bukkit.getOnlinePlayers().size());
            case "server_max_players":
                return String.valueOf(Bukkit.getMaxPlayers());
            case "server_motd":
                return Bukkit.getMotd();
            case "server_ip":
                return Bukkit.getIp();
            case "server_port":
                return String.valueOf(Bukkit.getPort());
            case "server_tps":
                try {
                    Object tpsObj = Bukkit.getServer().getClass().getMethod("recentTps").invoke(Bukkit.getServer());
                    if (tpsObj instanceof double[]) {
                        double[] tps = (double[]) tpsObj;
                        return String.format("%.2f", tps[0]);
                    }
                    return "unknown";
                } catch (Exception e) {
                    return "unknown";
                }
            case "server_uptime":
                long uptime = ManagementFactory.getRuntimeMXBean().getUptime();
                long hours = uptime / 3600000;
                long minutes = (uptime % 3600000) / 60000;
                return String.format("%dh %dm", hours, minutes);
            case "server_memory_used":
                long used = (Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory()) / 1024 / 1024;
                return used + "MB";
            case "server_memory_max":
                long max = Runtime.getRuntime().maxMemory() / 1024 / 1024;
                return max + "MB";
            case "server_memory_free":
                long free = Runtime.getRuntime().freeMemory() / 1024 / 1024;
                return free + "MB";
            case "server_java_version":
                return System.getProperty("java.version");
            case "server_os_name":
                return System.getProperty("os.name");
            case "server_os_arch":
                return System.getProperty("os.arch");
            case "server_os_version":
                return System.getProperty("os.version");
            default:
                return null;
        }
    }

    private String resolveDateTimePlaceholder(String placeholder) {
        LocalDateTime now = LocalDateTime.now();
        String lower = placeholder.toLowerCase();
        switch (lower) {
            case "date":
                return now.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
            case "time":
                return now.format(DateTimeFormatter.ofPattern("HH:mm:ss"));
            case "datetime":
                return now.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
            case "date_short":
                return now.format(DateTimeFormatter.ofPattern("dd/MM/yyyy"));
            case "time_short":
                return now.format(DateTimeFormatter.ofPattern("HH:mm"));
            case "day":
                return String.valueOf(now.getDayOfMonth());
            case "month":
                return String.valueOf(now.getMonthValue());
            case "year":
                return String.valueOf(now.getYear());
            case "hour":
                return String.valueOf(now.getHour());
            case "minute":
                return String.valueOf(now.getMinute());
            case "second":
                return String.valueOf(now.getSecond());
            case "day_of_week":
                return now.getDayOfWeek().name();
            case "month_name":
                return now.getMonth().name();
            case "timestamp":
                return String.valueOf(System.currentTimeMillis());
            default:
                return null;
        }
    }

    private String resolveMathPlaceholder(String placeholder) {
        if (!placeholder.toLowerCase().startsWith("math_")) return null;
        
        String expr = placeholder.substring(5);
        try {
            return String.valueOf(evaluateMathExpression(expr));
        } catch (Exception e) {
            return "Error";
        }
    }

    private double evaluateMathExpression(String expression) {
        return new Object() {
            int pos = -1, ch;

            void nextChar() {
                ch = (++pos < expression.length()) ? expression.charAt(pos) : -1;
            }

            boolean eat(int charToEat) {
                while (ch == ' ') nextChar();
                if (ch == charToEat) {
                    nextChar();
                    return true;
                }
                return false;
            }

            double parse() {
                nextChar();
                double x = parseExpression();
                if (pos < expression.length()) throw new RuntimeException("Unexpected: " + (char)ch);
                return x;
            }

            double parseExpression() {
                double x = parseTerm();
                for (;;) {
                    if (eat('+')) x += parseTerm();
                    else if (eat('-')) x -= parseTerm();
                    else return x;
                }
            }

            double parseTerm() {
                double x = parseFactor();
                for (;;) {
                    if (eat('*')) x *= parseFactor();
                    else if (eat('/')) x /= parseFactor();
                    else return x;
                }
            }

            double parseFactor() {
                if (eat('+')) return parseFactor();
                if (eat('-')) return -parseFactor();
                double x;
                int startPos = this.pos;
                if (eat('(')) {
                    x = parseExpression();
                    eat(')');
                } else if ((ch >= '0' && ch <= '9') || ch == '.') {
                    while ((ch >= '0' && ch <= '9') || ch == '.') nextChar();
                    x = Double.parseDouble(expression.substring(startPos, this.pos));
                } else {
                    throw new RuntimeException("Unexpected: " + (char)ch);
                }
                if (eat('^')) x = Math.pow(x, parseFactor());
                return x;
            }
        }.parse();
    }

    public boolean evaluateCondition(String condition, Object player) {
        Matcher matcher = RELATIONAL_PATTERN.matcher(condition);
        if (!matcher.matches()) return false;

        String left = matcher.group(1);
        String operator = matcher.group(2);
        String right = matcher.group(3);

        Optional<String> leftValue = getPlaceholderValue(left, player);
        Optional<String> rightValue = getPlaceholderValue(right, player);

        if (!leftValue.isPresent() || !rightValue.isPresent()) return false;

        try {
            double leftNum = Double.parseDouble(leftValue.get());
            double rightNum = Double.parseDouble(rightValue.get());

            switch (operator) {
                case "<":
                    return leftNum < rightNum;
                case "<=":
                    return leftNum <= rightNum;
                case ">":
                    return leftNum > rightNum;
                case ">=":
                    return leftNum >= rightNum;
                case "==":
                    return leftNum == rightNum;
                case "!=":
                    return leftNum != rightNum;
                default:
                    return false;
            }
        } catch (NumberFormatException e) {
            String leftStr = leftValue.get();
            String rightStr = rightValue.get();
            switch (operator) {
                case "==":
                    return leftStr.equals(rightStr);
                case "!=":
                    return !leftStr.equals(rightStr);
                default:
                    return false;
            }
        }
    }

    public String parseWithConditionals(String text, Object player) {
        Pattern conditionalPattern = Pattern.compile("%if_([^_]+)_([^_]+)_([^%]+)%");
        Matcher matcher = conditionalPattern.matcher(text);
        
        StringBuffer result = new StringBuffer();
        while (matcher.find()) {
            String condition = matcher.group(1);
            String thenValue = matcher.group(2);
            String elseValue = matcher.group(3);
            
            boolean conditionResult = evaluateCondition(condition, player);
            String replacement = conditionResult ? thenValue : elseValue;
            matcher.appendReplacement(result, Matcher.quoteReplacement(replacement));
        }
        matcher.appendTail(result);
        
        return parse(result.toString(), player);
    }

    public String applyFormat(String text, Object player) {
        Matcher matcher = FORMAT_PATTERN.matcher(text);
        StringBuffer result = new StringBuffer();
        
        while (matcher.find()) {
            String placeholder = matcher.group(1);
            String format = matcher.group(2);
            
            Optional<String> value = getPlaceholderValue(placeholder, player);
            if (value.isPresent()) {
                String formatted;
                String lowerFormat = format.toLowerCase();
                if (lowerFormat.equals("uppercase") || lowerFormat.equals("upper")) {
                    formatted = value.get().toUpperCase();
                } else if (lowerFormat.equals("lowercase") || lowerFormat.equals("lower")) {
                    formatted = value.get().toLowerCase();
                } else if (lowerFormat.equals("capitalize")) {
                    String v = value.get();
                    formatted = v.substring(0, 1).toUpperCase() + v.substring(1).toLowerCase();
                } else if (lowerFormat.equals("title")) {
                    String[] words = value.get().split("\\s+");
                    StringBuilder sb = new StringBuilder();
                    for (String word : words) {
                        if (!word.isEmpty()) {
                            sb.append(word.substring(0, 1).toUpperCase())
                              .append(word.substring(1).toLowerCase())
                              .append(" ");
                        }
                    }
                    formatted = sb.toString().trim();
                } else {
                    formatted = value.get();
                }
                matcher.appendReplacement(result, Matcher.quoteReplacement(formatted));
            } else {
                matcher.appendReplacement(result, matcher.group(0));
            }
        }
        matcher.appendTail(result);
        
        return parse(result.toString(), player);
    }
}
