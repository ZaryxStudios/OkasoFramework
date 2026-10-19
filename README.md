# Okaso Framework

**Okaso** is a modular, multi-platform Java framework for Minecraft plugin development. It provides a unified API compatible with Bukkit, Spigot, Paper, BungeeCord, Velocity, and Waterfall, supporting Minecraft 1.8.8 and newer.

## Modules

Okaso is composed of 30+ independent modules covering entities, worlds, items, GUIs, holograms, scoreboards, NBT, particles, commands, events, tasks, storage, security, Redis, i18n, webhooks, metrics, placeholders, permissions, and more. Each module exposes a clean interface in `okaso-common` with platform-specific implementations in the corresponding adapter modules.

## Building

Requires Maven and JDK 8. The build checks every Bukkit module against the Spigot 1.8.8 API, which needs Java 8 classes; with a newer JDK, point it at a Java 8 runtime or skip the check:

```bash
git clone https://github.com/ZaryxStudios/OkasoFramework.git
cd OkasoFramework
mvn install -DskipTests
```

```bash
mvn install -DskipTests -Dokaso.compat.javaHome=/path/to/java8
```

The build produces shaded JARs in each adapter's `target/` directory:

- `okaso-bukkit/target/okaso-bukkit-1.8.2.jar` — for Bukkit/Spigot/Paper servers
- `okaso-bungeecord/target/okaso-bungeecord-1.8.2.jar` — for BungeeCord proxies
- `okaso-velocity/target/okaso-velocity-1.8.2.jar` — for Velocity proxies
- `okaso-waterfall/target/okaso-waterfall-1.8.2.jar` — for Waterfall proxies

## Compatibility

Every pull request is tested on real servers: Paper 1.8.8, 1.12.2, 1.16.5, 1.17.1, 1.18.2, 1.19.4, 1.20.4, 1.20.6, 1.21.4, 1.21.11 and the newest Paper release, plus BungeeCord, Waterfall 1.21, Velocity 3.5.1 and the newest Velocity release. See [CONTRIBUTING.md](CONTRIBUTING.md).

Known limitation: the NBT module (`okaso-nbt`) only works up to Minecraft 1.16.5.

## Installation

Copy the appropriate adapter JAR into your server or proxy's `plugins/` folder and restart.

## Integrating Okaso in Your Plugin

### 1. Add the dependency

```xml
<dependency>
    <groupId>com.zaryxstudios.okaso</groupId>
    <artifactId>okaso-bukkit</artifactId>
    <version>1.8.2</version>
    <scope>provided</scope>
</dependency>
```

Use the adapter for your platform (`okaso-bukkit`, `okaso-bungeecord`, `okaso-velocity`, or `okaso-waterfall`) with `provided` scope. Do not shade Okaso classes into your JAR.

Okaso is not published to a public Maven repository. Run `mvn install` (see [Building](#building)) so the artifacts are available in your local Maven repository.

### 2. Declare the dependency

Bukkit / Spigot / Paper (`plugin.yml`):

```yaml
depend: [Okaso]
```

BungeeCord / Waterfall (`bungee.yml`):

```yaml
depends: [Okaso]
```

Velocity (the plugin id is `okaso-velocity`):

```java
@Plugin(id = "myplugin", dependencies = @Dependency(id = "okaso-velocity"))
```

### 3. Access services

```java
import com.zaryxstudios.okaso.common.OkasoAPI;
import com.zaryxstudios.okaso.common.hologram.HologramManager;
import com.zaryxstudios.okaso.common.task.TaskScheduler;

HologramManager holograms = OkasoAPI.service(HologramManager.class);
TaskScheduler tasks = OkasoAPI.service(TaskScheduler.class);
```

`OkasoAPI.service` returns `null` when the service is not registered on the current platform:

| Platform | Registered services |
|----------|---------------------|
| Bukkit / Spigot / Paper | `TaskScheduler`, `EventBus`, `OkasoConfigurationProvider`, `EntityService`, `WorldManager`, `OkasoStructureManager`, `ItemBuilder`, `PermissionManager`, `PlaceholderRegistry`, `OkasoScoreboardManager`, `TabListManager`, `HologramManager`, `ParticleManager`, `MetricsRegistry`, `TranslationManager`, `WebhookClient`, `NetworkManager`, `PacketInterceptor`, `OkasoSecurityManager`, `UpdateChecker`, `ModuleManager`, `NPCManager`, `NPCMovementController`, `NPCSerializer` |
| BungeeCord / Waterfall | `TaskScheduler`, `OkasoConfigurationProvider` |
| Velocity | `OkasoConfigurationProvider` |

GUI, command, storage, database, cache and Redis modules are not registered as services; use their classes directly.

The `ItemBuilder` service on Bukkit is a single shared instance. To build items, create your own builder with `new OkasoBukkitItemBuilder(Material.DIAMOND)` (from `okaso-item`) instead of modifying the shared one.

## License

MIT License. See [LICENSE](LICENSE).
