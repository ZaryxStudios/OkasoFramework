package com.zaryxstudios.okaso.smoke;

import com.zaryxstudios.okaso.common.OkasoAPI;
import com.zaryxstudios.okaso.common.compat.BukkitCompat;
import com.zaryxstudios.okaso.common.config.OkasoConfigurationProvider;
import com.zaryxstudios.okaso.common.entity.EntityService;
import com.zaryxstudios.okaso.common.entity.NPCManager;
import com.zaryxstudios.okaso.common.entity.NPCMovementController;
import com.zaryxstudios.okaso.common.entity.NPCSerializer;
import com.zaryxstudios.okaso.common.event.EventBus;
import com.zaryxstudios.okaso.common.hologram.HologramLine;
import com.zaryxstudios.okaso.common.hologram.HologramManager;
import com.zaryxstudios.okaso.common.hologram.OkasoHologram;
import com.zaryxstudios.okaso.common.i18n.TranslationManager;
import com.zaryxstudios.okaso.common.item.ItemBuilder;
import com.zaryxstudios.okaso.common.metrics.MetricsRegistry;
import com.zaryxstudios.okaso.common.module.ModuleManager;
import com.zaryxstudios.okaso.common.network.NetworkManager;
import com.zaryxstudios.okaso.common.packet.PacketInterceptor;
import com.zaryxstudios.okaso.common.particle.OkasoParticleEffect;
import com.zaryxstudios.okaso.common.particle.ParticleManager;
import com.zaryxstudios.okaso.common.permission.PermissionManager;
import com.zaryxstudios.okaso.common.placeholder.PlaceholderRegistry;
import com.zaryxstudios.okaso.common.scoreboard.OkasoScoreboardManager;
import com.zaryxstudios.okaso.common.security.OkasoSecurityManager;
import com.zaryxstudios.okaso.common.tablist.TabListManager;
import com.zaryxstudios.okaso.common.task.TaskHandle;
import com.zaryxstudios.okaso.common.task.TaskScheduler;
import com.zaryxstudios.okaso.common.updater.UpdateChecker;
import com.zaryxstudios.okaso.common.webhook.WebhookClient;
import com.zaryxstudios.okaso.common.world.OkasoStructureManager;
import com.zaryxstudios.okaso.common.world.WorldManager;
import com.zaryxstudios.okaso.entity.VersionUtil;
import com.zaryxstudios.okaso.gui.GUIConfirmDialog;
import com.zaryxstudios.okaso.gui.GUIPattern;
import com.zaryxstudios.okaso.gui.OkasoBukkitGUI;
import com.zaryxstudios.okaso.gui.OkasoBukkitGUIItem;
import com.zaryxstudios.okaso.item.OkasoBukkitItemBuilder;
import com.zaryxstudios.okaso.nbt.ReflectionNBTItem;
import com.zaryxstudios.okaso.packet.ReflectionPacketInterceptor;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.entity.ArmorStand;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;

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
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Runs a fixed set of checks against Okaso once the server has started, writes
 * plugins/OkasoSmoke/result.json, logs a single "OKASO-SMOKE: PASS|FAIL" line and stops
 * the server. Every check catches Throwable so a NoSuchMethodError or NoClassDefFoundError
 * is reported as a failure instead of aborting the run.
 */
public final class OkasoSmokePlugin extends JavaPlugin {

    private static final Class<?>[] SERVICES = {
        TaskScheduler.class, EventBus.class, OkasoConfigurationProvider.class, EntityService.class,
        WorldManager.class, OkasoStructureManager.class, ItemBuilder.class, PermissionManager.class,
        PlaceholderRegistry.class, OkasoScoreboardManager.class, TabListManager.class,
        HologramManager.class, ParticleManager.class, MetricsRegistry.class, TranslationManager.class,
        WebhookClient.class, NetworkManager.class, PacketInterceptor.class, OkasoSecurityManager.class,
        UpdateChecker.class, ModuleManager.class, NPCManager.class, NPCMovementController.class,
        NPCSerializer.class
    };

    private final List<Result> results = new ArrayList<>();
    private final AtomicBoolean cancelledTaskRan = new AtomicBoolean(false);

    @Override
    public void onEnable() {
        // Wait for worlds to finish loading before touching them
        getServer().getScheduler().runTaskLater(this, this::runChecks, 20L);
    }

    private void runChecks() {
        check("okaso-enabled", () -> {
            Plugin okaso = Bukkit.getPluginManager().getPlugin("Okaso");
            require(okaso != null && okaso.isEnabled(), "Okaso is not enabled");
        });
        for (Class<?> service : SERVICES) {
            check("service:" + service.getSimpleName(),
                () -> require(OkasoAPI.service(service) != null, "service not registered"));
        }
        check("version-util", () -> require(VersionUtil.getMajor() >= 1, "unexpected version " + VersionUtil.getMajor()));
        check("task-cancel", () -> {
            TaskHandle handle = OkasoAPI.service(TaskScheduler.class)
                .runLater(() -> cancelledTaskRan.set(true), 5, TimeUnit.SECONDS);
            require(!handle.isCancelled(), "new task reported as cancelled");
            handle.cancel();
            require(handle.isCancelled(), "cancelled task not reported as cancelled");
            handle.cancel();
        });
        check("item-builder", () -> {
            require(OkasoAPI.service(ItemBuilder.class).build() != null, "shared builder returned null");
            Object item = new OkasoBukkitItemBuilder(Material.STONE)
                .name("smoke").lore("line").durability((short) 1).unbreakable(true).glow().build();
            require(item instanceof ItemStack, "builder did not return an ItemStack");
        });
        check("compat-items", () -> {
            for (String name : new String[] {"LIME_WOOL", "RED_WOOL", "GRAY_STAINED_GLASS_PANE", "CLOCK", "PLAYER_HEAD"}) {
                require(BukkitCompat.item(name).getType() != Material.STONE, name + " resolved to STONE");
            }
        });
        check("gui-items", () -> {
            GUIPattern.fillerPane();
            GUIPattern.transparentPane();
            OkasoBukkitGUIItem.confirmButton(event -> {});
            OkasoBukkitGUIItem.cancelButton(event -> {});
            OkasoBukkitGUIItem item = OkasoBukkitGUIItem.builder(Material.STONE)
                .name("smoke").unbreakable().customModelData(1).headTexture("dGVzdA==").build();
            item.isUnbreakable();
            item.getCustomModelData();
        });
        check("gui-inventories", () -> {
            new OkasoBukkitGUI(this, "smoke", 27);
            new OkasoBukkitGUI(this, "smoke", InventoryType.HOPPER);
            GUIConfirmDialog.of(this);
            GUIConfirmDialog.delete(this, "smoke", player -> {});
        });
        check("hologram", () -> {
            HologramManager holograms = OkasoAPI.service(HologramManager.class);
            OkasoHologram hologram = holograms.createHologram("okaso-smoke",
                HologramLine.text("smoke"), HologramLine.item("STONE"));
            hologram.setLocation(spawn());
            holograms.removeHologram("okaso-smoke");
        });
        check("entity-flags", () -> {
            ArmorStand stand = spawn().getWorld().spawn(spawn(), ArmorStand.class);
            try {
                BukkitCompat.setGlowing(stand, true);
                BukkitCompat.setInvulnerable(stand, true);
                BukkitCompat.setSilent(stand, true);
                BukkitCompat.setGravity(stand, false);
                BukkitCompat.setCollidable(stand, false);
                BukkitCompat.setAI(stand, false);
                OkasoAPI.service(EntityService.class).getPassengers(stand);
                BukkitCompat.setItemInMainHand(stand.getEquipment(), new ItemStack(Material.STONE));
            } finally {
                stand.remove();
            }
        });
        check("particle", () -> {
            ParticleManager particles = OkasoAPI.service(ParticleManager.class);
            OkasoParticleEffect effect = particles.createEffect("okaso-smoke", "FLAME");
            effect.play(spawn());
            particles.removeEffect("okaso-smoke");
        });
        check("world", () -> require(!OkasoAPI.service(WorldManager.class).getWorlds().isEmpty(), "no worlds"));
        check("nbt", () -> {
            ReflectionNBTItem nbt = new ReflectionNBTItem(new ItemStack(Material.STONE));
            require(nbt.getCompound() != null, "NBT compound unavailable");
            nbt.hasTag("okaso-smoke");
        });
        check("packet-resolver", () -> ReflectionPacketInterceptor.createPacket("PacketPlayOutChat"));

        // The task cancelled above was due after 5 seconds; finish once it would have run
        getServer().getScheduler().runTaskLater(this, this::finish, 120L);
    }

    private void finish() {
        check("task-cancel-effective", () -> require(!cancelledTaskRan.get(), "cancelled task still ran"));
        int passed = 0;
        for (Result result : results) {
            if (result.error == null) {
                passed++;
            } else {
                getLogger().severe("OKASO-SMOKE: FAIL " + result.name + ": " + result.error);
            }
        }
        boolean ok = passed == results.size();
        writeResult(ok, passed);
        getLogger().info("OKASO-SMOKE: " + (ok ? "PASS" : "FAIL") + " " + passed + "/" + results.size()
            + " on " + Bukkit.getVersion());
        Bukkit.shutdown();
    }

    private void check(String name, ThrowingRunnable body) {
        try {
            body.run();
            results.add(new Result(name, null));
        } catch (Throwable t) {
            StringWriter trace = new StringWriter();
            t.printStackTrace(new PrintWriter(trace));
            getLogger().severe("OKASO-SMOKE: check " + name + " threw\n" + trace);
            results.add(new Result(name, t.getClass().getName() + ": " + t.getMessage()));
        }
    }

    private Location spawn() {
        World world = Bukkit.getWorlds().get(0);
        return world.getSpawnLocation().add(0.5, 2, 0.5);
    }

    private void writeResult(boolean ok, int passed) {
        StringBuilder json = new StringBuilder();
        json.append("{\n  \"status\": \"").append(ok ? "PASS" : "FAIL").append("\",\n");
        json.append("  \"server\": \"").append(escape(Bukkit.getVersion())).append("\",\n");
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
        File file = new File(getDataFolder(), "result.json");
        try {
            Files.createDirectories(getDataFolder().toPath());
            try (Writer writer = new OutputStreamWriter(Files.newOutputStream(file.toPath()), StandardCharsets.UTF_8)) {
                writer.write(json.toString());
            }
        } catch (IOException e) {
            getLogger().severe("OKASO-SMOKE: could not write " + file + ": " + e);
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

    private static void require(boolean condition, String message) {
        if (!condition) throw new AssertionError(message);
    }

    @FunctionalInterface
    private interface ThrowingRunnable {
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
