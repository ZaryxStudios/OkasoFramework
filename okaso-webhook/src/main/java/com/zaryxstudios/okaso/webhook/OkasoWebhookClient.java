package com.zaryxstudios.okaso.webhook;

import com.zaryxstudios.okaso.common.webhook.WebhookClient;
import com.zaryxstudios.okaso.common.webhook.WebhookEmbed;

import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;

public class OkasoWebhookClient implements WebhookClient {

    private final ObjectMapper mapper;
    private final AtomicLong lastRequestTime;
    private volatile boolean rateLimited;
    private static final long RATE_LIMIT_RESET_MS = 5000L;

    public OkasoWebhookClient() {
        this.mapper = new ObjectMapper();
        this.lastRequestTime = new AtomicLong(0);
        this.rateLimited = false;
    }

    @Override
    public void send(String url, String message) {
        if (url == null || url.isEmpty()) return;
        if (isRateLimited()) return;

        try {
            Map<String, Object> payload = new LinkedHashMap<String, Object>();
            payload.put("content", message);
            executePost(url, payload);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void sendEmbed(String url, WebhookEmbed embed) {
        if (url == null || url.isEmpty()) return;
        if (isRateLimited()) return;

        try {
            Map<String, Object> embedMap = new LinkedHashMap<String, Object>();
            embedMap.put("title", embed.getTitle());
            embedMap.put("description", embed.getDescription());
            embedMap.put("color", embed.getColor());

            Map<String, Object> payload = new LinkedHashMap<String, Object>();
            payload.put("embeds", new Object[]{ embedMap });
            executePost(url, payload);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public boolean isRateLimited() {
        if (!rateLimited) return false;
        long elapsed = System.currentTimeMillis() - lastRequestTime.get();
        if (elapsed > RATE_LIMIT_RESET_MS) {
            rateLimited = false;
        }
        return rateLimited;
    }

    private void executePost(String urlString, Map<String, Object> payload) throws Exception {
        URL url = new URL(urlString);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("POST");
        conn.setRequestProperty("Content-Type", "application/json");
        conn.setDoOutput(true);
        conn.setConnectTimeout(5000);
        conn.setReadTimeout(5000);

        byte[] json = mapper.writeValueAsBytes(payload);
        try (OutputStream os = conn.getOutputStream()) {
            os.write(json);
            os.flush();
        }

        int code = conn.getResponseCode();
        lastRequestTime.set(System.currentTimeMillis());

        if (code == 429) {
            rateLimited = true;
        }

        conn.disconnect();
    }
}
