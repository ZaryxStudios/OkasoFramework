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

import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import static com.zaryxstudios.okaso.smoke.SmokeReport.require;

/**
 * Runs a fixed set of checks against Okaso once the server has started, writes
 * plugins/OkasoSmoke/result.json through {@link SmokeReport} and stops the server.
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

    private SmokeReport report;
    private final AtomicBoolean cancelledTaskRan = new AtomicBoolean(false);

    @Override
    public void onEnable() {
        report = new SmokeReport(getLogger()::info, getLogger()::severe);
        // Wait for worlds to finish loading before touching them
        getServer().getScheduler().runTaskLater(this, this::runChecks, 20L);
    }

    private void runChecks() {
        Plugin okaso = Bukkit.getPluginManager().getPlugin("Okaso");
        report.check("okaso-enabled", () -> require(okaso != null && okaso.isEnabled(), "Okaso is not enabled"));
        report.checkVersion(okaso == null ? null : okaso.getDescription().getVersion());
        for (Class<?> service : SERVICES) {
            report.check("service:" + service.getSimpleName(),
                () -> require(OkasoAPI.service(service) != null, "service not registered"));
        }
        report.check("version-util", () -> require(VersionUtil.getMajor() >= 1, "unexpected version " + VersionUtil.getMajor()));
        report.check("task-cancel", () -> {
            TaskHandle handle = OkasoAPI.service(TaskScheduler.class)
                .runLater(() -> cancelledTaskRan.set(true), 5, TimeUnit.SECONDS);
            require(!handle.isCancelled(), "new task reported as cancelled");
            handle.cancel();
            require(handle.isCancelled(), "cancelled task not reported as cancelled");
            handle.cancel();
        });
        report.check("item-builder", () -> {
            require(OkasoAPI.service(ItemBuilder.class).build() != null, "shared builder returned null");
            Object item = new OkasoBukkitItemBuilder(Material.STONE)
                .name("smoke").lore("line").durability((short) 1).unbreakable(true).glow().build();
            require(item instanceof ItemStack, "builder did not return an ItemStack");
        });
        report.check("compat-items", () -> {
            for (String name : new String[] {"LIME_WOOL", "RED_WOOL", "GRAY_STAINED_GLASS_PANE", "CLOCK", "PLAYER_HEAD"}) {
                require(BukkitCompat.item(name).getType() != Material.STONE, name + " resolved to STONE");
            }
        });
        report.check("gui-items", () -> {
            GUIPattern.fillerPane();
            GUIPattern.transparentPane();
            OkasoBukkitGUIItem.confirmButton(event -> {});
            OkasoBukkitGUIItem.cancelButton(event -> {});
            OkasoBukkitGUIItem item = OkasoBukkitGUIItem.builder(Material.STONE)
                .name("smoke").unbreakable().customModelData(1).headTexture("dGVzdA==").build();
            item.isUnbreakable();
            item.getCustomModelData();
        });
        report.check("gui-inventories", () -> {
            new OkasoBukkitGUI(this, "smoke", 27);
            new OkasoBukkitGUI(this, "smoke", InventoryType.HOPPER);
            GUIConfirmDialog.of(this);
            GUIConfirmDialog.delete(this, "smoke", player -> {});
        });
        report.check("hologram", () -> {
            HologramManager holograms = OkasoAPI.service(HologramManager.class);
            OkasoHologram hologram = holograms.createHologram("okaso-smoke",
                HologramLine.text("smoke"), HologramLine.item("STONE"));
            hologram.setLocation(spawn());
            holograms.removeHologram("okaso-smoke");
        });
        report.check("entity-flags", () -> {
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
        report.check("particle", () -> {
            ParticleManager particles = OkasoAPI.service(ParticleManager.class);
            OkasoParticleEffect effect = particles.createEffect("okaso-smoke", "FLAME");
            effect.play(spawn());
            particles.removeEffect("okaso-smoke");
        });
        report.check("world", () -> require(!OkasoAPI.service(WorldManager.class).getWorlds().isEmpty(), "no worlds"));
        report.check("nbt", () -> {
            ReflectionNBTItem nbt = new ReflectionNBTItem(new ItemStack(Material.STONE));
            require(nbt.getCompound() != null, "NBT compound unavailable");
            nbt.hasTag("okaso-smoke");
        });
        report.check("packet-resolver", () -> ReflectionPacketInterceptor.createPacket("PacketPlayOutChat"));

        // The task cancelled above was due after 5 seconds; finish once it would have run
        getServer().getScheduler().runTaskLater(this, this::finish, 120L);
    }

    private void finish() {
        report.check("task-cancel-effective", () -> require(!cancelledTaskRan.get(), "cancelled task still ran"));
        report.finish(Bukkit.getVersion(), getDataFolder());
        Bukkit.shutdown();
    }

    private Location spawn() {
        World world = Bukkit.getWorlds().get(0);
        return world.getSpawnLocation().add(0.5, 2, 0.5);
    }
}
