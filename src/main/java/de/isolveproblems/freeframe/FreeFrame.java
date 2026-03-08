package de.isolveproblems.freeframe;

import de.isolveproblems.freeframe.economy.VaultEconomyService;
import de.isolveproblems.freeframe.utils.AmountValidator;
import de.isolveproblems.freeframe.utils.AuditLogger;
import de.isolveproblems.freeframe.utils.ConfigurationMessages;
import de.isolveproblems.freeframe.utils.FrameDisplayService;
import de.isolveproblems.freeframe.utils.FrameRegistry;
import de.isolveproblems.freeframe.utils.FreeFrameData;
import de.isolveproblems.freeframe.utils.InteractionLimiter;
import de.isolveproblems.freeframe.utils.ItemPolicy;
import de.isolveproblems.freeframe.utils.MetricsTracker;
import de.isolveproblems.freeframe.utils.PlaceholderSupport;
import de.isolveproblems.freeframe.utils.PurchaseWindowLimiter;
import de.isolveproblems.freeframe.utils.RegionRestrictionService;
import de.isolveproblems.freeframe.utils.RegisterClasses;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.plugin.java.JavaPlugin;

import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class FreeFrame extends JavaPlugin {
    private static final String DEFAULT_PREFIX = "&eFreeFrame &8>>";
    private static final String DEFAULT_PERMISSION_ERROR = "%prefix% &cYou don't have enough permissions to perform this command.";

    private RegisterClasses registrar;
    private ConfigurationMessages configHandler;
    private FrameRegistry frameRegistry;
    private MetricsTracker metricsTracker;
    private InteractionLimiter interactionLimiter;
    private PurchaseWindowLimiter purchaseWindowLimiter;
    private VaultEconomyService economyService;
    private ItemPolicy itemPolicy;
    private RegionRestrictionService regionRestrictionService;
    private AuditLogger auditLogger;
    private PlaceholderSupport placeholderSupport;
    private FrameDisplayService displayService;

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

        if (this.displayService != null && this.getPluginConfig().getBoolean("freeframe.display.removeOnDisable", false) && this.frameRegistry != null) {
            for (FreeFrameData frameData : this.frameRegistry.listFrames()) {
                this.displayService.remove(frameData);
            }
        }

        this.logLifecycleState(false);
    }

    private void load() {
        this.configHandler.load();

        this.placeholderSupport = new PlaceholderSupport(this);
        this.metricsTracker = new MetricsTracker();
        this.interactionLimiter = new InteractionLimiter();
        this.purchaseWindowLimiter = new PurchaseWindowLimiter();
        this.economyService = new VaultEconomyService(this);
        this.economyService.initialize();

        this.itemPolicy = new ItemPolicy();
        this.regionRestrictionService = new RegionRestrictionService();
        this.auditLogger = new AuditLogger(this);
        this.displayService = new FrameDisplayService(this);

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

        this.displayService.refreshAll(this.frameRegistry.listFrames());
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

    public PurchaseWindowLimiter getPurchaseWindowLimiter() {
        return this.purchaseWindowLimiter;
    }

    public VaultEconomyService getEconomyService() {
        return this.economyService;
    }

    public ItemPolicy getItemPolicy() {
        return this.itemPolicy;
    }

    public RegionRestrictionService getRegionRestrictionService() {
        return this.regionRestrictionService;
    }

    public AuditLogger getAuditLogger() {
        return this.auditLogger;
    }

    public PlaceholderSupport getPlaceholderSupport() {
        return this.placeholderSupport;
    }

    public FrameDisplayService getDisplayService() {
        return this.displayService;
    }

    public FileConfiguration getPluginConfig() {
        return this.configHandler.getConfig();
    }

    public int getConfiguredItemAmount() {
        return AmountValidator.sanitize(this.getPluginConfig().getInt("freeframe.item.amount", 1));
    }

    public int getGuiInventorySize() {
        int size = this.getPluginConfig().getInt("freeframe.gui.inventory.size", 9);
        if (size < 9) {
            return 9;
        }
        if (size > 54) {
            return 54;
        }
        if (size % 9 != 0) {
            size = (size / 9) * 9;
        }
        return Math.max(9, size);
    }

    public List<Integer> getSaleSlots() {
        List<Integer> configured = this.getPluginConfig().getIntegerList("freeframe.gui.saleSlots");
        if (configured == null || configured.isEmpty()) {
            List<Integer> defaults = new ArrayList<Integer>();
            defaults.add(2);
            defaults.add(4);
            defaults.add(6);
            return Collections.unmodifiableList(defaults);
        }

        int size = this.getGuiInventorySize();
        List<Integer> slots = new ArrayList<Integer>();
        for (Integer slot : configured) {
            if (slot != null && slot >= 0 && slot < size && !slots.contains(slot)) {
                slots.add(slot);
            }
        }

        if (slots.isEmpty()) {
            slots.add(2);
            slots.add(4);
            slots.add(6);
        }
        return Collections.unmodifiableList(slots);
    }

    public boolean isSaleSlot(int rawSlot) {
        return this.getSaleSlots().contains(rawSlot);
    }

    public String getGuiTitle(Player player) {
        String template = this.getPluginConfig().getString("freeframe.gui.title", "%prefix%");
        return this.formatMessage(template, player);
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

    public boolean isItemAllowed(Material material) {
        return this.itemPolicy.check(this.getPluginConfig(), material).isAllowed();
    }

    public boolean isLocationAllowed(Location location) {
        return this.regionRestrictionService.isAllowed(this.getPluginConfig(), location);
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
        return this.formatMessage(raw, null);
    }

    public String getMessage(String path, String fallback, Player player) {
        String raw = this.getPluginConfig().getString(path, fallback);
        return this.formatMessage(raw, player);
    }

    public String formatMessage(String raw) {
        return this.formatMessage(raw, null);
    }

    public String formatMessage(String raw, Player player) {
        if (raw == null) {
            return "";
        }

        String withPrefix = raw.replace("%prefix%", this.getPrefix());
        String withPlaceholders = this.placeholderSupport == null
            ? withPrefix
            : this.placeholderSupport.apply(player, withPrefix);

        return this.colorize(withPlaceholders);
    }

    public String colorize(String text) {
        if (text == null) {
            return "";
        }
        return ChatColor.translateAlternateColorCodes('&', text);
    }
}
