package com.zaryxstudios.okaso.updater;

import com.zaryxstudios.okaso.common.module.ModuleVersion;
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
        try {
            return ModuleVersion.parse(currentVersion).compareTo(ModuleVersion.parse(latest)) < 0;
        } catch (IllegalArgumentException e) {
            return false;
        }
    }
}
