package com.zaryxstudios.okaso.updater;

import com.zaryxstudios.okaso.common.updater.UpdateChecker;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicReference;

import lombok.Getter;

public class OkasoUpdateChecker implements UpdateChecker {

    @Getter
    private final String currentVersion;
    private final String updateUrl;
    private final AtomicReference<String> latestVersion;

    public OkasoUpdateChecker(String currentVersion, String updateUrl) {
        this.currentVersion = currentVersion;
        this.updateUrl = updateUrl;
        this.latestVersion = new AtomicReference<String>(null);
    }

    @Override
    public CompletableFuture<Optional<String>> checkForUpdate() {
        return CompletableFuture.supplyAsync(() -> {
            HttpURLConnection conn = null;
            try {
                URL url = new URL(updateUrl);
                conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("GET");
                conn.setConnectTimeout(5000);
                conn.setReadTimeout(5000);
                conn.setRequestProperty("User-Agent", "OkasoUpdater/2.0");

                int code = conn.getResponseCode();
                if (code != 200) {
                    return Optional.empty();
                }

                try (BufferedReader reader = new BufferedReader(
                        new InputStreamReader(conn.getInputStream()))) {
                    String latest = reader.readLine();
                    if (latest != null) {
                        latest = latest.trim();
                    }
                    if (latest == null || latest.isEmpty()) {
                        return Optional.empty();
                    }
                    latestVersion.set(latest);
                    return Optional.of(latest);
                }
            } catch (Exception e) {
                return Optional.empty();
            } finally {
                if (conn != null) {
                    conn.disconnect();
                }
            }
        });
    }

    @Override
    public String getLatestVersion() {
        String latest = latestVersion.get();
        return latest != null ? latest : currentVersion;
    }

    @Override
    public boolean isUpdateAvailable() {
        String latest = latestVersion.get();
        if (latest == null) return false;
        return compareVersions(currentVersion, latest) < 0;
    }

    private int compareVersions(String v1, String v2) {
        String[] parts1 = v1.split("\\.");
        String[] parts2 = v2.split("\\.");
        int maxLen = Math.max(parts1.length, parts2.length);
        for (int i = 0; i < maxLen; i++) {
            int p1 = i < parts1.length ? parseInt(parts1[i]) : 0;
            int p2 = i < parts2.length ? parseInt(parts2[i]) : 0;
            if (p1 < p2) return -1;
            if (p1 > p2) return 1;
        }
        return 0;
    }

    private int parseInt(String s) {
        try {
            return Integer.parseInt(s);
        } catch (NumberFormatException e) {
            return 0;
        }
    }
}
