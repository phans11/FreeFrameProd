package de.isolveproblems.freeframe;

import de.isolveproblems.freeframe.economy.VaultEconomyService;
import de.isolveproblems.freeframe.utils.AmountValidator;
import de.isolveproblems.freeframe.utils.ConfigurationMessages;
import de.isolveproblems.freeframe.utils.FrameRegistry;
import de.isolveproblems.freeframe.utils.FreeFrameData;
import de.isolveproblems.freeframe.utils.InteractionLimiter;
import de.isolveproblems.freeframe.utils.MetricsTracker;
import de.isolveproblems.freeframe.utils.RegisterClasses;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.plugin.java.JavaPlugin;

import java.lang.reflect.Constructor;

public class FreeFrame extends JavaPlugin {
    private static final String DEFAULT_PREFIX = "&eFreeFrame &8>>";
    private static final String DEFAULT_PERMISSION_ERROR = "%prefix% &cYou don't have enough permissions to perform this command.";

    private RegisterClasses registrar;
    private ConfigurationMessages configHandler;
    private FrameRegistry frameRegistry;
    private MetricsTracker metricsTracker;
    private InteractionLimiter interactionLimiter;
    private VaultEconomyService economyService;

    @Override
    public void onEnable() {
        this.configHandler = new ConfigurationMessages(this);
        this.registrar = new RegisterClasses(this);

        this.load();
        this.logLifecycleState(true);
    }

    @Override
    public void onDisable() {
        if (this.frameRegistry != null) {
            this.frameRegistry.saveToConfig();
        }
        this.logLifecycleState(false);
    }

    private void load() {
        this.configHandler.load();

        this.metricsTracker = new MetricsTracker();
        this.interactionLimiter = new InteractionLimiter();
        this.economyService = new VaultEconomyService(this);
        this.economyService.initialize();

        this.frameRegistry = new FrameRegistry(this);
        this.frameRegistry.loadFromConfig();

        int migrated = this.frameRegistry.migrateLegacyFrames();
        if (migrated > 0) {
            this.getLogger().info("Migrated " + migrated + " legacy free frame entries.");
        }

        int removedEntries = this.frameRegistry.cleanupInvalidReferences();
        if (removedEntries > 0) {
            this.getLogger().info("Removed " + removedEntries + " invalid free frame references during startup.");
        }

        this.initializeOptionalBStats();
        this.registrar.registerCommands();
        this.registrar.registerListeners();
    }

    private void initializeOptionalBStats() {
        int pluginId = this.getPluginConfig().getInt("freeframe.metrics.bstatsPluginId", 0);
        if (pluginId <= 0) {
            return;
        }

        try {
            Class<?> metricsClass = Class.forName("org.bstats.bukkit.Metrics");
            Constructor<?> constructor = metricsClass.getConstructor(JavaPlugin.class, int.class);
            constructor.newInstance(this, pluginId);
            this.getLogger().info("bStats enabled (plugin id: " + pluginId + ").");
        } catch (Throwable throwable) {
            this.getLogger().warning("Could not initialize bStats: " + throwable.getMessage());
        }
    }

    private void logLifecycleState(boolean enabled) {
        PluginDescriptionFile description = this.getDescription();
        Bukkit.getConsoleSender().sendMessage(ChatColor.GOLD + "Name: " + ChatColor.YELLOW + "FreeFrame");
        Bukkit.getConsoleSender().sendMessage(ChatColor.GOLD + "Version: " + ChatColor.YELLOW + description.getVersion());
        Bukkit.getConsoleSender().sendMessage(
            ChatColor.GOLD + "Status: " + (enabled ? ChatColor.GREEN + "Enabled" : ChatColor.RED + "Disabled")
        );
    }

    public ConfigurationMessages getConfigHandler() {
        return this.configHandler;
    }

    public FrameRegistry getFrameRegistry() {
        return this.frameRegistry;
    }

    public MetricsTracker getMetricsTracker() {
        return this.metricsTracker;
    }

    public InteractionLimiter getInteractionLimiter() {
        return this.interactionLimiter;
    }

    public VaultEconomyService getEconomyService() {
        return this.economyService;
    }

    public FileConfiguration getPluginConfig() {
        return this.configHandler.getConfig();
    }

    public int getConfiguredItemAmount() {
        return AmountValidator.sanitize(this.getPluginConfig().getInt("freeframe.item.amount", 1));
    }

    public boolean canPlayerUseFrame(Player player, FreeFrameData frameData) {
        if (frameData == null || player == null) {
            return false;
        }

        if (!this.getPluginConfig().getBoolean("freeframe.access.requireOwner", false)) {
            return true;
        }

        if (player.hasPermission(this.configHandler.getAccessBypassPermissionNode())) {
            return true;
        }

        return frameData.isOwnedBy(player.getUniqueId().toString());
    }

    public String getPrefix() {
        String prefix = this.getPluginConfig().getString("freeframe.prefix", DEFAULT_PREFIX);
        return this.colorize(prefix);
    }

    public String getErrorPermissionMessage() {
        return this.getMessage("freeframe.error.permission", DEFAULT_PERMISSION_ERROR);
    }

    public String getMessage(String path, String fallback) {
        String raw = this.getPluginConfig().getString(path, fallback);
        return this.formatMessage(raw);
    }

    public String formatMessage(String raw) {
        if (raw == null) {
            return "";
        }
        return this.colorize(raw.replace("%prefix%", this.getPrefix()));
    }

    public String colorize(String text) {
        if (text == null) {
            return "";
        }
        return ChatColor.translateAlternateColorCodes('&', text);
    }
}
