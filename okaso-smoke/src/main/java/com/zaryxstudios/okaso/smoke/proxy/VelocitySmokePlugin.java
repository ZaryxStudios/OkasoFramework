package com.zaryxstudios.okaso.smoke.proxy;

import com.google.inject.Inject;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.proxy.ProxyInitializeEvent;
import com.velocitypowered.api.plugin.PluginContainer;
import com.velocitypowered.api.plugin.annotation.DataDirectory;
import com.velocitypowered.api.proxy.ProxyServer;
import com.zaryxstudios.okaso.common.OkasoAPI;
import com.zaryxstudios.okaso.common.config.OkasoConfigurationProvider;
import com.zaryxstudios.okaso.smoke.SmokeReport;

import org.slf4j.Logger;

import java.nio.file.Path;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

import static com.zaryxstudios.okaso.smoke.SmokeReport.require;

/**
 * Smoke checks for the Velocity adapter; shuts the proxy down when done. Declared in
 * velocity-plugin.json instead of with @Plugin, since the build only runs Lombok's processor.
 */
public final class VelocitySmokePlugin {

    private final ProxyServer server;
    private final Path dataDirectory;
    private final SmokeReport report;

    @Inject
    public VelocitySmokePlugin(ProxyServer server, Logger logger, @DataDirectory Path dataDirectory) {
        this.server = server;
        this.dataDirectory = dataDirectory;
        this.report = new SmokeReport(logger::info, logger::error);
    }

    @Subscribe
    public void onProxyInitialization(ProxyInitializeEvent event) {
        // Let Okaso finish its own ProxyInitializeEvent handler first
        server.getScheduler().buildTask(this, this::runChecks).delay(2, TimeUnit.SECONDS).schedule();
    }

    private void runChecks() {
        Optional<PluginContainer> okaso = server.getPluginManager().getPlugin("okaso-velocity");
        report.check("okaso-enabled", () -> require(okaso.isPresent(), "Okaso is not loaded"));
        report.checkVersion(okaso.flatMap(container -> container.getDescription().getVersion()).orElse(null));
        report.check("service:OkasoConfigurationProvider",
            () -> require(OkasoAPI.service(OkasoConfigurationProvider.class) != null, "service not registered"));
        report.finish(server.getVersion().getName() + " " + server.getVersion().getVersion(), dataDirectory.toFile());
        server.shutdown();
    }
}
