package com.zaryxstudios.okaso.common.updater;

import java.util.Optional;
import java.util.concurrent.CompletableFuture;

public interface UpdateChecker {
    CompletableFuture<Optional<String>> checkForUpdate();
    String getCurrentVersion();
    String getLatestVersion();
    boolean isUpdateAvailable();
}
