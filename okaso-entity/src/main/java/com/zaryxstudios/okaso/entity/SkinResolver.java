package com.zaryxstudios.okaso.entity;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.zaryxstudios.okaso.common.entity.SkinData;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public final class SkinResolver {

    private static final String MOJANG_PROFILE_URL = "https://api.mojang.com/users/profiles/minecraft/";
    private static final String SESSION_PROFILE_URL = "https://sessionserver.mojang.com/session/minecraft/profile/";
    private static final int CONNECT_TIMEOUT_MS = 8000;
    private static final int READ_TIMEOUT_MS = 8000;

    private static final ObjectMapper MAPPER = new ObjectMapper();
    private static final Map<String, SkinData> CACHE = new ConcurrentHashMap<String, SkinData>();

    private SkinResolver() {
    }

    public static SkinData resolve(String playerName) throws IOException {
        if (playerName == null) {
            throw new IllegalArgumentException("playerName must not be null");
        }
        String key = playerName.toLowerCase();
        SkinData cached = CACHE.get(key);
        if (cached != null) {
            return cached;
        }
        JsonNode idNode = get(MOJANG_PROFILE_URL + URLEncoder.encode(playerName, "UTF-8"));
        if (idNode == null || !idNode.hasNonNull("id")) {
            throw new IOException("Unknown Minecraft player: " + playerName);
        }
        String rawId = idNode.get("id").asText();
        JsonNode profile = get(SESSION_PROFILE_URL + rawId + "?unsigned=false");
        if (profile == null || !profile.has("properties")) {
            throw new IOException("No profile data for player: " + playerName);
        }
        for (JsonNode property : profile.get("properties")) {
            if (!property.hasNonNull("name") || !"textures".equals(property.get("name").asText())) {
                continue;
            }
            if (!property.hasNonNull("value")) {
                continue;
            }
            String value = property.get("value").asText();
            String signature = property.hasNonNull("signature") ? property.get("signature").asText() : null;
            SkinData skin = decode(value, signature, playerName);
            CACHE.put(key, skin);
            return skin;
        }
        throw new IOException("No textures property for player: " + playerName);
    }

    public static SkinData getCached(String playerName) {
        if (playerName == null) {
            return null;
        }
        return CACHE.get(playerName.toLowerCase());
    }

    public static void cache(String playerName, SkinData skin) {
        if (playerName == null || skin == null) {
            throw new IllegalArgumentException("playerName and skin must not be null");
        }
        CACHE.put(playerName.toLowerCase(), skin);
    }

    public static void invalidate(String playerName) {
        if (playerName != null) {
            CACHE.remove(playerName.toLowerCase());
        }
    }

    public static void clearCache() {
        CACHE.clear();
    }

    public static SkinData decode(String textureValue, String signature, String ownerName) throws IOException {
        SkinData base = signature == null ? SkinData.unsigned(textureValue) : SkinData.signed(textureValue, signature);
        try {
            byte[] decoded = Base64.getDecoder().decode(textureValue);
            JsonNode root = MAPPER.readTree(new String(decoded, StandardCharsets.UTF_8));
            JsonNode textures = root.path("textures");
            String skinUrl = urlOf(textures.path("SKIN"));
            String capeUrl = urlOf(textures.path("CAPE"));
            boolean slim = "slim".equalsIgnoreCase(textures.path("SKIN").path("metadata").path("model").asText(null));
            if (skinUrl == null && capeUrl == null) {
                return base;
            }
            return base.withResolvedUrls(skinUrl, capeUrl, slim);
        } catch (IllegalArgumentException ex) {
            throw new IOException("Invalid texture value", ex);
        }
    }

    private static String urlOf(JsonNode node) {
        if (node == null || node.isMissingNode() || !node.hasNonNull("url")) {
            return null;
        }
        return node.get("url").asText();
    }

    private static JsonNode get(String url) throws IOException {
        HttpURLConnection connection = null;
        try {
            connection = (HttpURLConnection) new URL(url).openConnection();
            connection.setConnectTimeout(CONNECT_TIMEOUT_MS);
            connection.setReadTimeout(READ_TIMEOUT_MS);
            connection.setRequestMethod("GET");
            connection.setRequestProperty("User-Agent", "OkasoFramework");
            connection.setRequestProperty("Accept", "application/json");
            int status = connection.getResponseCode();
            if (status == 204 || status == 404) {
                return null;
            }
            if (status < 200 || status >= 300) {
                throw new IOException("HTTP " + status + " from " + url);
            }
            InputStream input = connection.getInputStream();
            try {
                return MAPPER.readTree(input);
            } finally {
                try {
                    input.close();
                } catch (IOException ignored) {
                }
            }
        } finally {
            if (connection != null) {
                connection.disconnect();
            }
        }
    }
}
