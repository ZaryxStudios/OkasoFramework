# Contributing to Okaso

## Version compatibility

Okaso compiles against a recent Spigot API but must keep working on Minecraft 1.8.8. A call to an
API that 1.8.8 lacks compiles fine and only fails on the server, with `NoSuchMethodError`,
`NoSuchFieldError` or `NoClassDefFoundError`. Two checks catch this before it ships.

### 1. API signature check (every build)

Every module that depends on `spigot-api` is checked with
[Animal Sniffer](https://www.mojohaus.org/animal-sniffer/) against `okaso-signature`, a signature
of Java 8 plus Spigot 1.8.8. A reference to anything missing there fails the build:

```
OkasoBukkitTaskHandle.java:27: Undefined reference: boolean org.bukkit.scheduler.BukkitTask.isCancelled()
```

To fix a violation:

- Use `com.zaryxstudios.okaso.common.compat.BukkitCompat` when it already covers the call
  (entity flags, main/off hand, passengers, max health, unbreakable, custom model data, potion
  meta, ...). Add a method there for new cases rather than repeating try/catch blocks.
- For materials, use `BukkitCompat.item("LIME_WOOL")` or `BukkitCompat.material(...)` instead of
  `Material.LIME_WOOL`. On 1.8–1.12 they return the legacy material and data value (`WOOL:5`).
  New aliases go in `CompatibilityNames`.
- Only when the code is already guarded, annotate the smallest member possible with
  `@CompatGuarded(since = "1.13")`. Guarded means behind a version check, inside a
  `catch (LinkageError e)` with a fallback, or a field of a newer type that stays `null` on old
  servers. Do not annotate large methods: the check skips the whole member.

The signature needs Java 8 classes. Build with JDK 8, or point the build at a Java 8 runtime:

```bash
mvn install -Dokaso.compat.javaHome=/path/to/java8
```

`-Dokaso.compat.skip=true` skips the check locally; CI always runs it.

### 2. Smoke tests on real servers (CI)

`.github/workflows/compat.yml` builds the project and starts real servers with Okaso and the
`okaso-smoke` plugin, which runs a set of checks and shuts the server down. It runs on pull
requests, on `main` and weekly, so new Paper and Velocity releases are picked up automatically.

Run one locally after `mvn install` (needs bash, curl, python3 and a matching Java):

```bash
JAVA=/path/to/java8/bin/java scripts/compat/run-paper.sh 1.8.8
```

```bash
scripts/compat/run-proxy.sh velocity 3.5.1
```

Running a test server accepts the [Minecraft EULA](https://aka.ms/MinecraftEULA) for it.
Results are in `target/compat/<server>/` (`server.log` and `plugins/*/result.json`).

- To test another Minecraft version, add it to `VERSIONS` in `scripts/compat/matrix.py`. The Java
  version comes from the PaperMC API.
- To add a smoke check, add a `report.check(...)` call in `okaso-smoke`
  (`OkasoSmokePlugin` for Bukkit, `proxy/BungeeSmokePlugin` or `proxy/VelocitySmokePlugin`).
- When a check is known to fail on some versions, list it in `KNOWN_FAILURES` in `matrix.py`
  with the reason. CI reports it as a warning there, and tells you when it starts passing.
