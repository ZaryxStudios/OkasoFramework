package com.zaryxstudios.okaso.smoke.proxy;

import com.zaryxstudios.okaso.common.OkasoAPI;
import com.zaryxstudios.okaso.common.config.OkasoConfigurationProvider;
import com.zaryxstudios.okaso.common.task.TaskHandle;
import com.zaryxstudios.okaso.common.task.TaskScheduler;
import com.zaryxstudios.okaso.smoke.SmokeReport;

import net.md_5.bungee.api.plugin.Plugin;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import static com.zaryxstudios.okaso.smoke.SmokeReport.require;

/** Smoke checks for the BungeeCord and Waterfall adapters; stops the proxy when done. */
public final class BungeeSmokePlugin extends Plugin {

    private final AtomicBoolean cancelledTaskRan = new AtomicBoolean(false);
    private SmokeReport report;

    @Override
    public void onEnable() {
        report = new SmokeReport(getLogger()::info, getLogger()::severe);
        getProxy().getScheduler().schedule(this, this::runChecks, 2, TimeUnit.SECONDS);
    }

    private void runChecks() {
        Plugin okaso = getProxy().getPluginManager().getPlugin("Okaso");
        report.check("okaso-enabled", () -> require(okaso != null, "Okaso is not loaded"));
        report.checkVersion(okaso == null ? null : okaso.getDescription().getVersion());
        report.check("service:OkasoConfigurationProvider",
            () -> require(OkasoAPI.service(OkasoConfigurationProvider.class) != null, "service not registered"));
        report.check("service:TaskScheduler",
            () -> require(OkasoAPI.service(TaskScheduler.class) != null, "service not registered"));
        report.check("task-cancel", () -> {
            TaskHandle handle = OkasoAPI.service(TaskScheduler.class)
                .runLater(() -> cancelledTaskRan.set(true), 3, TimeUnit.SECONDS);
            require(!handle.isCancelled(), "new task reported as cancelled");
            handle.cancel();
            require(handle.isCancelled(), "cancelled task not reported as cancelled");
        });
        // The cancelled task was due after 3 seconds; finish once it would have run
        getProxy().getScheduler().schedule(this, this::finish, 5, TimeUnit.SECONDS);
    }

    private void finish() {
        report.check("task-cancel-effective", () -> require(!cancelledTaskRan.get(), "cancelled task still ran"));
        report.finish(getProxy().getName() + " " + getProxy().getVersion(), getDataFolder());
        getProxy().stop();
    }
}
