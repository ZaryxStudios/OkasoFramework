package com.zaryxstudios.okaso.webhook;

import com.zaryxstudios.okaso.common.webhook.WebhookClient;
import com.zaryxstudios.okaso.common.webhook.WebhookEmbed;

import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.OutputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.nio.charset.StandardCharsets;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

public class OkasoWebhookClient implements WebhookClient {

    private static final Logger LOGGER = Logger.getLogger(OkasoWebhookClient.class.getName());

    private final ObjectMapper mapper;
    private volatile long rateLimitUntil;
    private static final long RATE_LIMIT_RESET_MS = 5000L;

    public OkasoWebhookClient() {
        this.mapper = new ObjectMapper();
        this.rateLimitUntil = 0L;
    }

    @Override
    public void send(String url, String message) {
        if (!isValidUrl(url)) return;
        if (isRateLimited()) return;

        try {
            Map<String, Object> payload = new LinkedHashMap<>();
            payload.put("content", message == null ? "" : message);
            executePost(url, payload);
        } catch (Exception e) {
            LOGGER.log(Level.WARNING, "Failed to send webhook message", e);
        }
    }

    @Override
    public void sendEmbed(String url, WebhookEmbed embed) {
        if (!isValidUrl(url) || embed == null) return;
        if (isRateLimited()) return;

        try {
            Map<String, Object> embedMap = new LinkedHashMap<>();
            embedMap.put("title", embed.getTitle());
            embedMap.put("description", embed.getDescription());
            embedMap.put("color", embed.getColor());
            if (embed.getFields() != null && !embed.getFields().isEmpty()) {
                embedMap.put("fields", embed.getFields());
            }

            Map<String, Object> payload = new LinkedHashMap<>();
            payload.put("embeds", new Object[]{ embedMap });
            executePost(url, payload);
        } catch (Exception e) {
            LOGGER.log(Level.WARNING, "Failed to send webhook embed", e);
        }
    }

    @Override
    public boolean isRateLimited() {
        long until = rateLimitUntil;
        if (until == 0L) return false;
        if (System.currentTimeMillis() >= until) {
            rateLimitUntil = 0L;
            return false;
        }
        return true;
    }

    private void executePost(String urlString, Map<String, Object> payload) throws Exception {
        URL url = new URL(urlString);
        URLConnection connection = url.openConnection();
        if (!(connection instanceof HttpURLConnection)) {
            throw new IllegalArgumentException("Webhook URL must use HTTP or HTTPS");
        }
        HttpURLConnection conn = (HttpURLConnection) connection;
        try {
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
            conn.setRequestProperty("Accept", "application/json");
            conn.setDoOutput(true);
            conn.setUseCaches(false);
            conn.setConnectTimeout(5000);
            conn.setReadTimeout(5000);

            byte[] json = mapper.writeValueAsBytes(payload);
            try (OutputStream os = conn.getOutputStream()) {
                os.write(json);
            }

            int code = conn.getResponseCode();
            if (code == 429) {
                rateLimitUntil = System.currentTimeMillis() + getRetryDelay(conn);
            } else if (code < 200 || code >= 300) {
                LOGGER.warning("Webhook returned HTTP status " + code);
            }
            consumeResponse(conn, code);
        } finally {
            conn.disconnect();
        }
    }

    private long getRetryDelay(HttpURLConnection conn) {
        String retryAfter = conn.getHeaderField("Retry-After");
        if (retryAfter != null) {
            try {
                double seconds = Double.parseDouble(retryAfter);
                return Math.max(1L, (long) (seconds * 1000L));
            } catch (NumberFormatException ignored) {
            }
        }
        return RATE_LIMIT_RESET_MS;
    }

    private void consumeResponse(HttpURLConnection conn, int code) throws Exception {
        InputStream stream = code >= 400 ? conn.getErrorStream() : conn.getInputStream();
        if (stream == null) return;
        try (InputStream response = stream) {
            byte[] buffer = new byte[512];
            while (response.read(buffer) != -1) {
            }
        }
    }

    private boolean isValidUrl(String value) {
        if (value == null || value.trim().isEmpty()) return false;
        try {
            URL url = new URL(value);
            return "http".equalsIgnoreCase(url.getProtocol())
                || "https".equalsIgnoreCase(url.getProtocol());
        } catch (Exception ignored) {
            return false;
        }
    }
}
