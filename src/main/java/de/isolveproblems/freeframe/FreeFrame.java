package de.isolveproblems.freeframe;

import de.isolveproblems.freeframe.api.BackupService;
import de.isolveproblems.freeframe.api.ChestRestockService;
import de.isolveproblems.freeframe.api.DiscountService;
import de.isolveproblems.freeframe.api.LocalizationService;
import de.isolveproblems.freeframe.api.OfferMode;
import de.isolveproblems.freeframe.api.PurchaseProfile;
import de.isolveproblems.freeframe.api.PurchaseProcessor;
import de.isolveproblems.freeframe.api.RegionAccessService;
import de.isolveproblems.freeframe.api.StatisticsService;
import de.isolveproblems.freeframe.api.TransactionGuard;
import de.isolveproblems.freeframe.api.WebhookExportService;
import de.isolveproblems.freeframe.economy.VaultEconomyService;
import de.isolveproblems.freeframe.utils.AlertService;
import de.isolveproblems.freeframe.utils.AuctionService;
import de.isolveproblems.freeframe.utils.AmountValidator;
import de.isolveproblems.freeframe.utils.AuditLogger;
import de.isolveproblems.freeframe.utils.ChestInventoryRestockService;
import de.isolveproblems.freeframe.utils.CompositeRegionAccessService;
import de.isolveproblems.freeframe.utils.ConfigurationMessages;
import de.isolveproblems.freeframe.utils.DashboardServer;
import de.isolveproblems.freeframe.utils.DefaultDiscountService;
import de.isolveproblems.freeframe.utils.DefaultPurchaseProcessor;
import de.isolveproblems.freeframe.utils.DynamicPricingService;
import de.isolveproblems.freeframe.utils.FrameDisplayService;
import de.isolveproblems.freeframe.utils.FrameRegistry;
import de.isolveproblems.freeframe.utils.FreeFrameData;
import de.isolveproblems.freeframe.utils.InMemoryTransactionGuard;
import de.isolveproblems.freeframe.utils.InteractionLimiter;
import de.isolveproblems.freeframe.utils.ItemPolicy;
import de.isolveproblems.freeframe.utils.JournalReplayReport;
import de.isolveproblems.freeframe.utils.LocalBackupService;
import de.isolveproblems.freeframe.utils.LocalStatisticsService;
import de.isolveproblems.freeframe.utils.MetricsTracker;
import de.isolveproblems.freeframe.utils.PlaceholderSupport;
import de.isolveproblems.freeframe.utils.PurchaseSecurityService;
import de.isolveproblems.freeframe.utils.PurchaseWindowLimiter;
import de.isolveproblems.freeframe.utils.RegionRestrictionService;
import de.isolveproblems.freeframe.utils.RegisterClasses;
import de.isolveproblems.freeframe.utils.SeasonalRulesService;
import de.isolveproblems.freeframe.utils.ShopNetworkService;
import de.isolveproblems.freeframe.utils.TaxService;
import de.isolveproblems.freeframe.utils.TransactionJournalService;
import de.isolveproblems.freeframe.utils.WebhookNotifier;
import de.isolveproblems.freeframe.utils.YamlLocalizationService;
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
    private LocalizationService localizationService;
    private DiscountService discountService;
    private ChestRestockService chestRestockService;
    private StatisticsService statisticsService;
    private BackupService backupService;
    private RegionAccessService regionAccessService;
    private WebhookExportService webhookExportService;
    private TransactionGuard transactionGuard;
    private PurchaseProcessor purchaseProcessor;
    private TaxService taxService;
    private SeasonalRulesService seasonalRulesService;
    private DynamicPricingService dynamicPricingService;
    private ShopNetworkService shopNetworkService;
    private PurchaseSecurityService purchaseSecurityService;
    private TransactionJournalService transactionJournalService;
    private AlertService alertService;
    private DashboardServer dashboardServer;
    private AuctionService auctionService;

    @Override
    public void onEnable() {
        this.configHandler = new ConfigurationMessages(this);
        this.registrar = new RegisterClasses(this);
        this.load();
        this.logLifecycleState(true);
    }

    @Override
    public void onDisable() {
        if (this.dashboardServer != null) {
            this.dashboardServer.stop();
        }
        if (this.auctionService != null) {
            this.auctionService.stop();
        }

        if (this.statisticsService != null) {
            this.statisticsService.save();
        }

        if (this.frameRegistry != null) {
            this.frameRegistry.saveToConfig();
            this.frameRegistry.flushStorage();
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
        this.localizationService = new YamlLocalizationService(this);
        this.discountService = new DefaultDiscountService(this);
        this.chestRestockService = new ChestInventoryRestockService();
        this.statisticsService = new LocalStatisticsService(this.getDataFolder());
        this.backupService = new LocalBackupService(this);
        this.transactionGuard = new InMemoryTransactionGuard();
        this.webhookExportService = new WebhookNotifier(this);
        this.regionAccessService = new CompositeRegionAccessService(this, this.regionRestrictionService);
        this.taxService = new TaxService(this);
        this.seasonalRulesService = new SeasonalRulesService(this);
        this.dynamicPricingService = new DynamicPricingService(this);
        this.shopNetworkService = new ShopNetworkService(this);
        this.purchaseSecurityService = new PurchaseSecurityService(this);
        this.transactionJournalService = new TransactionJournalService(this);
        this.alertService = new AlertService(this);
        this.dashboardServer = new DashboardServer(this);
        this.auctionService = new AuctionService(this);
        this.purchaseProcessor = new DefaultPurchaseProcessor(
            this,
            this.discountService,
            this.chestRestockService,
            this.statisticsService
        );

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
        this.dashboardServer.start();
        this.auctionService.start();
        this.registrar.registerCommands();
        this.registrar.registerListeners();
    }

    public void reloadRuntimeState() {
        this.configHandler.reload();
        this.placeholderSupport = new PlaceholderSupport(this);
        this.localizationService = new YamlLocalizationService(this);
        this.discountService = new DefaultDiscountService(this);
        this.chestRestockService = new ChestInventoryRestockService();
        this.economyService.initialize();
        this.regionAccessService = new CompositeRegionAccessService(this, this.regionRestrictionService);
        this.webhookExportService = new WebhookNotifier(this);
        this.taxService = new TaxService(this);
        this.seasonalRulesService = new SeasonalRulesService(this);
        this.dynamicPricingService = new DynamicPricingService(this);
        this.shopNetworkService = new ShopNetworkService(this);
        this.purchaseSecurityService = new PurchaseSecurityService(this);
        this.transactionJournalService = new TransactionJournalService(this);
        this.alertService = new AlertService(this);
        if (this.dashboardServer == null) {
            this.dashboardServer = new DashboardServer(this);
        }
        this.dashboardServer.stop();
        this.dashboardServer.start();
        if (this.auctionService == null) {
            this.auctionService = new AuctionService(this);
        }
        this.auctionService.stop();
        this.auctionService.start();
        this.purchaseProcessor = new DefaultPurchaseProcessor(
            this,
            this.discountService,
            this.chestRestockService,
            this.statisticsService
        );
        this.frameRegistry.loadFromConfig();
        this.displayService.refreshAll(this.frameRegistry.listFrames());
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

    public LocalizationService getLocalizationService() {
        return this.localizationService;
    }

    public DiscountService getDiscountService() {
        return this.discountService;
    }

    public ChestRestockService getChestRestockService() {
        return this.chestRestockService;
    }

    public StatisticsService getStatisticsService() {
        return this.statisticsService;
    }

    public BackupService getBackupService() {
        return this.backupService;
    }

    public RegionAccessService getRegionAccessService() {
        return this.regionAccessService;
    }

    public WebhookExportService getWebhookExportService() {
        return this.webhookExportService;
    }

    public TransactionGuard getTransactionGuard() {
        return this.transactionGuard;
    }

    public PurchaseProcessor getPurchaseProcessor() {
        return this.purchaseProcessor;
    }

    public TaxService getTaxService() {
        return this.taxService;
    }

    public SeasonalRulesService getSeasonalRulesService() {
        return this.seasonalRulesService;
    }

    public DynamicPricingService getDynamicPricingService() {
        return this.dynamicPricingService;
    }

    public ShopNetworkService getShopNetworkService() {
        return this.shopNetworkService;
    }

    public PurchaseSecurityService getPurchaseSecurityService() {
        return this.purchaseSecurityService;
    }

    public TransactionJournalService getTransactionJournalService() {
        return this.transactionJournalService;
    }

    public AlertService getAlertService() {
        return this.alertService;
    }

    public DashboardServer getDashboardServer() {
        return this.dashboardServer;
    }

    public AuctionService getAuctionService() {
        return this.auctionService;
    }

    public JournalReplayReport replayTransactionJournal(boolean dryRun) {
        if (this.transactionJournalService == null || this.purchaseSecurityService == null) {
            return new JournalReplayReport(0, 0, 0, 0);
        }
        return this.transactionJournalService.replayIdempotency(this.purchaseSecurityService, dryRun);
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

    public List<PurchaseProfile> getDefaultPurchaseProfiles(double basePrice) {
        List<Integer> slots = this.getSaleSlots();
        List<Integer> amounts = this.getPluginConfig().getIntegerList("freeframe.profiles.amounts");
        List<Double> multipliers = this.readDoubleList("freeframe.profiles.priceMultipliers");
        List<PurchaseProfile> profiles = new ArrayList<PurchaseProfile>();

        for (int index = 0; index < slots.size(); index++) {
            int slot = slots.get(index);
            int amount = index < amounts.size() ? AmountValidator.sanitize(amounts.get(index)) : this.getConfiguredItemAmount();
            double multiplier = index < multipliers.size() ? Math.max(0.0D, multipliers.get(index)) : Math.max(1.0D, amount);
            double price = Math.max(0.0D, basePrice * multiplier);
            profiles.add(new PurchaseProfile(slot, amount, price, ""));
        }

        if (profiles.isEmpty()) {
            profiles.add(new PurchaseProfile(4, this.getConfiguredItemAmount(), Math.max(0.0D, basePrice), ""));
        }
        return profiles;
    }

    private List<Double> readDoubleList(String path) {
        List<?> raw = this.getPluginConfig().getList(path);
        List<Double> doubles = new ArrayList<Double>();
        if (raw == null) {
            return doubles;
        }

        for (Object entry : raw) {
            if (entry instanceof Number) {
                doubles.add(((Number) entry).doubleValue());
                continue;
            }

            if (entry != null) {
                try {
                    doubles.add(Double.parseDouble(String.valueOf(entry)));
                } catch (NumberFormatException ignored) {
                    // Keep defaults when entries are malformed.
                }
            }
        }
        return doubles;
    }

    public boolean isSaleSlot(int rawSlot) {
        return this.getSaleSlots().contains(rawSlot);
    }

    public OfferMode getOfferMode() {
        return OfferMode.fromString(this.getPluginConfig().getString("freeframe.shops.offerMode", "BOTH"));
    }

    public String getGuiTitle(Player player) {
        String template = this.getPluginConfig().getString("freeframe.gui.title", "%prefix%");
        return this.formatMessage(template, player);
    }

    public boolean canPlayerUseFrame(Player player, FreeFrameData frameData) {
        if (frameData == null || player == null) {
            return false;
        }

        if (!this.isShopTypeOffered(frameData)) {
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

    public boolean canPlayerManageFrame(Player player, FreeFrameData frameData) {
        if (player == null || frameData == null) {
            return false;
        }

        if (player.hasPermission(this.getConfigHandler().getAdminPermissionNode())) {
            return true;
        }

        if (!this.getPluginConfig().getBoolean("freeframe.ownerManagement.enabled", true)) {
            return false;
        }
        if (!player.hasPermission("freeframe.owner.manage")) {
            return false;
        }
        return frameData.isOwnedBy(player.getUniqueId().toString());
    }

    public boolean isShopTypeOffered(FreeFrameData frameData) {
        if (frameData == null) {
            return false;
        }

        OfferMode offerMode = this.getOfferMode();
        switch (offerMode) {
            case ADMIN:
                return "ADMIN".equalsIgnoreCase(frameData.getShopOwnerType().name());
            case USER:
                return "USER".equalsIgnoreCase(frameData.getShopOwnerType().name());
            default:
                return true;
        }
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
        return this.getMessage(path, fallback, null);
    }

    public String getMessage(String path, String fallback, Player player) {
        String configuredFallback = this.getPluginConfig().getString(path, fallback);
        String raw = this.localizationService == null
            ? configuredFallback
            : this.localizationService.resolveMessage(player, path, configuredFallback);
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
