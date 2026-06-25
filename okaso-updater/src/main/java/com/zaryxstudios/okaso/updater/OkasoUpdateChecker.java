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
            try {
                URL url = new URL(updateUrl);
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("GET");
                conn.setConnectTimeout(5000);
                conn.setReadTimeout(5000);
                conn.setRequestProperty("User-Agent", "OkasoUpdater/2.0");

                int code = conn.getResponseCode();
                if (code != 200) {
                    return Optional.empty();
                }

                BufferedReader reader = new BufferedReader(
                    new InputStreamReader(conn.getInputStream()));
                String latest = reader.readLine();
                reader.close();
                conn.disconnect();

                if (latest != null) {
                    latest = latest.trim();
                    latestVersion.set(latest);
                }

                return Optional.ofNullable(latest);
            } catch (Exception e) {
                return Optional.empty();
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
        return !currentVersion.equals(latest);
    }
}
