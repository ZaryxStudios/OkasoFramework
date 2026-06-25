# Okaso Framework

**Okaso** is a modular, multi-platform Java framework for Minecraft plugin development. It provides a unified API compatible with Bukkit, Spigot, Paper, BungeeCord, Velocity, and Waterfall, spanning Minecraft versions 1.7.10 through 26.1.2.

## Modules

Okaso is composed of 30+ independent modules covering entities, worlds, items, GUIs, holograms, scoreboards, NBT, particles, commands, events, tasks, storage, security, Redis, i18n, webhooks, metrics, placeholders, permissions, and more. Each module exposes a clean interface in `okaso-common` with platform-specific implementations in the corresponding adapter modules.

## Building

```bash
git clone https://github.com/ZaryxStudios/OkasoFramework.git
cd OkasoFramework/OkasoV2
mvn install -DskipTests
```

The build produces shaded JARs in each adapter's `target/` directory:

- `okaso-bukkit/target/okaso-bukkit-1.0.0.jar` — for Bukkit/Spigot/Paper servers
- `okaso-bungeecord/target/okaso-bungeecord-1.0.0.jar` — for BungeeCord proxies
- `okaso-velocity/target/okaso-velocity-1.0.0.jar` — for Velocity proxies
- `okaso-waterfall/target/okaso-waterfall-1.0.0.jar` — for Waterfall proxies

## Installation

Copy the appropriate adapter JAR into your server or proxy's `plugins/` folder and restart.

## Integrating Okaso in Your Plugin

### 1. Add the dependency

```xml
<dependency>
    <groupId>com.zaryxstudios.okaso</groupId>
    <artifactId>okaso-common</artifactId>
    <version>1.0.0</version>
    <scope>provided</scope>
</dependency>
```

Do not shade Okaso classes into your JAR. Use `provided` scope.

### 2. Declare the dependency in plugin.yml

```yaml
depend: [Okaso]
```

### 3. Access services

```java
import com.zaryxstudios.okaso.common.OkasoAPI;
import com.zaryxstudios.okaso.common.hologram.HologramManager;
import com.zaryxstudios.okaso.common.item.ItemBuilder;

HologramManager holograms = OkasoAPI.service(HologramManager.class);
ItemBuilder items = OkasoAPI.service(ItemBuilder.class);
```

## License

GNU Affero General Public License v3. See [LICENSE](LICENSE).
