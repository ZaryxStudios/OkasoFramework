package com.zaryxstudios.okaso.bukkit;

import com.zaryxstudios.okaso.command.CommandRegistryImpl;
import com.zaryxstudios.okaso.common.OkasoAPI;
import com.zaryxstudios.okaso.common.config.ConfigurationProvider;
import com.zaryxstudios.okaso.common.entity.EntityService;
import com.zaryxstudios.okaso.common.event.EventBus;
import com.zaryxstudios.okaso.common.hologram.HologramManager;
import com.zaryxstudios.okaso.common.i18n.TranslationManager;
import com.zaryxstudios.okaso.common.item.ItemBuilder;
import com.zaryxstudios.okaso.common.metrics.MetricsRegistry;
import com.zaryxstudios.okaso.common.network.NetworkManager;
import com.zaryxstudios.okaso.common.packet.PacketInterceptor;
import com.zaryxstudios.okaso.common.particle.ParticleManager;
import com.zaryxstudios.okaso.common.permission.PermissionManager;
import org.bukkit.Material;
import com.zaryxstudios.okaso.common.placeholder.PlaceholderRegistry;
import com.zaryxstudios.okaso.common.plugin.OkasoPlugin;
import com.zaryxstudios.okaso.common.scoreboard.ScoreboardManager;
import com.zaryxstudios.okaso.common.security.SecurityManager;
import com.zaryxstudios.okaso.common.service.ServiceRegistry;
import com.zaryxstudios.okaso.common.storage.StorageProvider;
import com.zaryxstudios.okaso.common.tablist.TabListManager;
import com.zaryxstudios.okaso.common.task.TaskScheduler;
import com.zaryxstudios.okaso.common.updater.UpdateChecker;
import com.zaryxstudios.okaso.common.webhook.WebhookClient;
import com.zaryxstudios.okaso.common.world.WorldManager;
import com.zaryxstudios.okaso.common.world.StructureManager;
import com.zaryxstudios.okaso.common.module.ModuleManager;
import com.zaryxstudios.okaso.config.OkasoConfigurationProvider;
import com.zaryxstudios.okaso.entity.BukkitEntityService;
import com.zaryxstudios.okaso.event.AnnotationEventRegistry;
import com.zaryxstudios.okaso.hologram.BukkitHologramManager;
import com.zaryxstudios.okaso.i18n.SimpleTranslationManager;
import com.zaryxstudios.okaso.item.BukkitItemBuilder;
import com.zaryxstudios.okaso.metrics.SimpleMetricsRegistry;
import com.zaryxstudios.okaso.module.DefaultModuleManager;
import com.zaryxstudios.okaso.network.BukkitNetworkManager;
import com.zaryxstudios.okaso.packet.ReflectionPacketInterceptor;
import com.zaryxstudios.okaso.particle.BukkitParticleManager;
import com.zaryxstudios.okaso.permission.BukkitPermissionManager;
import com.zaryxstudios.okaso.placeholder.SimplePlaceholderRegistry;
import com.zaryxstudios.okaso.scoreboard.BukkitScoreboardManager;
import com.zaryxstudios.okaso.security.OkasoSecurityManager;
import com.zaryxstudios.okaso.storage.JsonFileStorageProvider;
import com.zaryxstudios.okaso.storage.MemoryStorageProvider;
import com.zaryxstudios.okaso.tablist.BukkitTabListManager;
import com.zaryxstudios.okaso.task.BukkitTaskScheduler;
import com.zaryxstudios.okaso.updater.OkasoUpdateChecker;
import com.zaryxstudios.okaso.webhook.OkasoWebhookClient;
import com.zaryxstudios.okaso.world.BukkitWorldManager;
import com.zaryxstudios.okaso.world.structure.BukkitStructureManager;
import com.zaryxstudios.okaso.bukkit.event.BukkitEventBusAdapter;

import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.util.logging.Logger;

public class BukkitOkasoPlugin extends JavaPlugin implements OkasoPlugin {

    private OkasoAPI api;
    private EventBus eventBus;
    private CommandRegistryImpl commandRegistry;
    private AnnotationEventRegistry annotationEventRegistry;

    @Override
    public void onEnable() {
        api = OkasoAPI.init(this);

        registerServices();

        registerEventListeners();

        onOkasoEnable();

        getLogger().info("Okaso Bukkit adapter enabled.");
    }

    @Override
    public void onDisable() {
        onOkasoDisable();

        if (api != null) {
            api.getServiceRegistry().getAll().clear();
        }

        getLogger().info("Okaso Bukkit adapter disabled.");
    }

    private void registerServices() {
        ServiceRegistry reg = api.getServiceRegistry();

        reg.register(TaskScheduler.class, new BukkitTaskScheduler(this));

        eventBus = new EventBus();
        reg.register(EventBus.class, eventBus);

        reg.register(ConfigurationProvider.class, new OkasoConfigurationProvider());

        reg.register(EntityService.class, new BukkitEntityService());

        reg.register(WorldManager.class, new BukkitWorldManager());

        reg.register(StructureManager.class, new BukkitStructureManager(this));

        reg.register(ItemBuilder.class, new BukkitItemBuilder(Material.STONE, 1));

        reg.register(PermissionManager.class, new BukkitPermissionManager(this));

        reg.register(PlaceholderRegistry.class, new SimplePlaceholderRegistry());

        reg.register(ScoreboardManager.class, new BukkitScoreboardManager());

        reg.register(TabListManager.class, new BukkitTabListManager());

        reg.register(HologramManager.class, new BukkitHologramManager());

        reg.register(ParticleManager.class, new BukkitParticleManager());

        reg.register(MetricsRegistry.class, new SimpleMetricsRegistry());

        reg.register(TranslationManager.class, new SimpleTranslationManager());

        reg.register(WebhookClient.class, new OkasoWebhookClient());

        reg.register(NetworkManager.class, new BukkitNetworkManager(this));

        reg.register(PacketInterceptor.class, new ReflectionPacketInterceptor());

        reg.register(SecurityManager.class, new OkasoSecurityManager(30_000L, 20));

        String currentVersion = getDescription().getVersion();
        String updateUrl = "https://raw.githubusercontent.com/ZaryxStudios/OkasoFramework/refs/heads/main/version.txt";
        reg.register(UpdateChecker.class, new OkasoUpdateChecker(currentVersion, updateUrl));

        File dataFolder = getDataFolder();
        if (!dataFolder.exists()) dataFolder.mkdirs();
        JsonFileStorageProvider jsonStorage = new JsonFileStorageProvider(
            new File(dataFolder, "storage.json"));
        reg.register(StorageProvider.class, jsonStorage);

        commandRegistry = new CommandRegistryImpl();

        annotationEventRegistry = new AnnotationEventRegistry(eventBus);

        reg.register(ModuleManager.class, new DefaultModuleManager());
    }

    private void registerEventListeners() {
        BukkitEventBusAdapter adapter = new BukkitEventBusAdapter(eventBus, this);
        adapter.register();
    }

    @Override
    public String getVersion() {
        return super.getDescription().getVersion();
    }

    @Override
    public void onOkasoEnable() {
    }

    @Override
    public void onOkasoDisable() {
    }

    @Override
    public Logger getOkasoLogger() {
        return getLogger();
    }
}
