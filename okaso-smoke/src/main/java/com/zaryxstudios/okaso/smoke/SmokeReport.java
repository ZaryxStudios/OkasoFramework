package com.zaryxstudios.okaso.smoke;

import java.io.File;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

/**
 * Platform-independent check runner shared by the Bukkit, BungeeCord and Velocity smoke plugins.
 * Every check catches Throwable so a NoSuchMethodError or NoClassDefFoundError is reported as a
 * failure instead of aborting the run. {@link #finish} writes result.json and logs a single
 * "OKASO-SMOKE: PASS|FAIL" line that scripts/compat/run-*.sh look for.
 */
public final class SmokeReport {

    private final List<Result> results = new ArrayList<>();
    private final Consumer<String> info;
    private final Consumer<String> error;

    public SmokeReport(Consumer<String> info, Consumer<String> error) {
        this.info = info;
        this.error = error;
    }

    public void check(String name, ThrowingRunnable body) {
        try {
            body.run();
            results.add(new Result(name, null));
        } catch (Throwable t) {
            StringWriter trace = new StringWriter();
            t.printStackTrace(new PrintWriter(trace));
            error.accept("OKASO-SMOKE: check " + name + " threw\n" + trace);
            results.add(new Result(name, t.getClass().getName() + ": " + t.getMessage()));
        }
    }

    /** Checks the plugin descriptor version was filled in by Maven resource filtering. */
    public void checkVersion(String version) {
        check("okaso-version", () -> require(version != null && !version.contains("${"),
            "Okaso reports version " + version));
    }

    /** Writes result.json into dataFolder, logs the summary and returns whether every check passed. */
    public boolean finish(String server, File dataFolder) {
        int passed = 0;
        for (Result result : results) {
            if (result.error == null) {
                passed++;
            } else {
                error.accept("OKASO-SMOKE: FAIL " + result.name + ": " + result.error);
            }
        }
        boolean ok = passed == results.size();
        write(new File(dataFolder, "result.json"), ok, passed, server);
        info.accept("OKASO-SMOKE: " + (ok ? "PASS" : "FAIL") + " " + passed + "/" + results.size() + " on " + server);
        return ok;
    }

    public static void require(boolean condition, String message) {
        if (!condition) throw new AssertionError(message);
    }

    private void write(File file, boolean ok, int passed, String server) {
        StringBuilder json = new StringBuilder();
        json.append("{\n  \"status\": \"").append(ok ? "PASS" : "FAIL").append("\",\n");
        json.append("  \"server\": \"").append(escape(server)).append("\",\n");
        json.append("  \"passed\": ").append(passed).append(",\n");
        json.append("  \"total\": ").append(results.size()).append(",\n");
        json.append("  \"checks\": [\n");
        for (int i = 0; i < results.size(); i++) {
            Result result = results.get(i);
            json.append("    {\"name\": \"").append(escape(result.name)).append("\", \"error\": ");
            json.append(result.error == null ? "null" : "\"" + escape(result.error) + "\"");
            json.append(i + 1 < results.size() ? "},\n" : "}\n");
        }
        json.append("  ]\n}\n");
        try {
            Files.createDirectories(file.getParentFile().toPath());
            try (Writer writer = new OutputStreamWriter(Files.newOutputStream(file.toPath()), StandardCharsets.UTF_8)) {
                writer.write(json.toString());
            }
        } catch (IOException e) {
            error.accept("OKASO-SMOKE: could not write " + file + ": " + e);
        }
    }

    private static String escape(String value) {
        StringBuilder out = new StringBuilder();
        for (char c : String.valueOf(value).toCharArray()) {
            if (c == '"' || c == '\\') out.append('\\').append(c);
            else if (c == '\n') out.append("\\n");
            else if (c < 0x20) out.append(' ');
            else out.append(c);
        }
        return out.toString();
    }

    @FunctionalInterface
    public interface ThrowingRunnable {
        void run() throws Exception;
    }

    private static final class Result {
        private final String name;
        private final String error;

        private Result(String name, String error) {
            this.name = name;
            this.error = error;
        }
    }
}
