package de.isolveproblems.freeframe.utils;

import de.isolveproblems.freeframe.FreeFrame;
import de.isolveproblems.freeframe.api.ConfigAPI;
import de.isolveproblems.freeframe.config.FreeFrameConfigKey;
import org.bukkit.configuration.file.FileConfiguration;

import java.util.ArrayList;
import java.util.List;

public class ConfigurationMessages {
    private final FreeFrame freeframe;
    private final ConfigAPI configApi;

    private String reloadPermissionNode = "freeframe.reload";
    private String destroyPermissionNode = "freeframe.destroy";
    private String adminPermissionNode = "freeframe.admin";
    private String accessBypassPermissionNode = "freeframe.access.bypass";

    public ConfigurationMessages(FreeFrame freeframe) {
        this.freeframe = freeframe;
        this.configApi = new ConfigAPI(freeframe.getDataFolder(), "config.yml");
    }

    public void load() {
        FileConfiguration config = this.configApi.getConfig();
        this.configApi.setCommentsEnabled(true);
        this.configApi.addDefaults(FreeFrameConfigKey.values());

        config.options().copyDefaults(true);
        this.validateConfigurationValues();
        this.configApi.saveConfig();
        this.reloadPermissionNodes();
    }

    public void reload() {
        this.configApi.reloadConfig();
        this.load();
    }

    public void validateConfigurationValues() {
        FileConfiguration config = this.configApi.getConfig();

        int configuredAmount = config.getInt("freeframe.item.amount", 1);
        int validatedAmount = AmountValidator.sanitize(configuredAmount);
        if (configuredAmount != validatedAmount) {
            config.set("freeframe.item.amount", validatedAmount);
            this.freeframe.getLogger().warning(
                "Config value 'freeframe.item.amount' was out of range and got clamped to " + validatedAmount + "."
            );
        }

        int pageSize = config.getInt("freeframe.admin.listPageSize", 8);
        if (pageSize < 1) {
            config.set("freeframe.admin.listPageSize", 8);
        }

        if (config.getDouble("freeframe.default.price", 0.0D) < 0.0D) {
            config.set("freeframe.default.price", 0.0D);
        }

        int guiSize = config.getInt("freeframe.gui.inventory.size", 9);
        if (guiSize < 9) {
            guiSize = 9;
        }
        if (guiSize > 54) {
            guiSize = 54;
        }
        if (guiSize % 9 != 0) {
            guiSize = (guiSize / 9) * 9;
        }
        if (guiSize < 9) {
            guiSize = 9;
        }
        config.set("freeframe.gui.inventory.size", guiSize);

        List<Integer> saleSlots = config.getIntegerList("freeframe.gui.saleSlots");
        List<Integer> sanitizedSlots = new ArrayList<Integer>();
        for (Integer slot : saleSlots) {
            if (slot != null && slot >= 0 && slot < guiSize && !sanitizedSlots.contains(slot)) {
                sanitizedSlots.add(slot);
            }
        }
        if (sanitizedSlots.isEmpty()) {
            sanitizedSlots.add(2);
            sanitizedSlots.add(4);
            sanitizedSlots.add(6);
        }
        config.set("freeframe.gui.saleSlots", sanitizedSlots);

        List<Integer> profileAmounts = config.getIntegerList("freeframe.profiles.amounts");
        List<Integer> sanitizedAmounts = new ArrayList<Integer>();
        for (Integer amount : profileAmounts) {
            if (amount != null && amount > 0) {
                sanitizedAmounts.add(AmountValidator.sanitize(amount));
            }
        }
        if (sanitizedAmounts.isEmpty()) {
            sanitizedAmounts.add(1);
            sanitizedAmounts.add(16);
            sanitizedAmounts.add(64);
        }
        config.set("freeframe.profiles.amounts", sanitizedAmounts);

        List<?> rawMultipliers = config.getList("freeframe.profiles.priceMultipliers");
        List<Double> sanitizedMultipliers = new ArrayList<Double>();
        if (rawMultipliers != null) {
            for (Object raw : rawMultipliers) {
                double parsed;
                if (raw instanceof Number) {
                    parsed = ((Number) raw).doubleValue();
                } else {
                    try {
                        parsed = Double.parseDouble(String.valueOf(raw));
                    } catch (NumberFormatException exception) {
                        continue;
                    }
                }
                if (parsed >= 0.0D) {
                    sanitizedMultipliers.add(parsed);
                }
            }
        }
        if (sanitizedMultipliers.isEmpty()) {
            sanitizedMultipliers.add(1.0D);
            sanitizedMultipliers.add(16.0D);
            sanitizedMultipliers.add(64.0D);
        }
        config.set("freeframe.profiles.priceMultipliers", sanitizedMultipliers);

        if (config.getInt("freeframe.stock.defaultMax", 64) < 1) {
            config.set("freeframe.stock.defaultMax", 64);
        }

        int defaultMax = config.getInt("freeframe.stock.defaultMax", 64);
        int defaultStock = config.getInt("freeframe.stock.default", defaultMax);
        if (defaultStock < 0) {
            defaultStock = 0;
        }
        if (defaultStock > defaultMax) {
            defaultStock = defaultMax;
        }
        config.set("freeframe.stock.default", defaultStock);

        if (config.getLong("freeframe.stock.autoRefill.defaultIntervalMillis", 300000L) < 0L) {
            config.set("freeframe.stock.autoRefill.defaultIntervalMillis", 300000L);
        }
        int scanRadius = config.getInt("freeframe.chestRestock.route.scanRadius", 2);
        if (scanRadius < 1 || scanRadius > 6) {
            config.set("freeframe.chestRestock.route.scanRadius", 2);
        }

        String offerMode = config.getString("freeframe.shops.offerMode", "BOTH");
        if (!"ADMIN".equalsIgnoreCase(offerMode) && !"USER".equalsIgnoreCase(offerMode) && !"BOTH".equalsIgnoreCase(offerMode)) {
            config.set("freeframe.shops.offerMode", "BOTH");
        }

        if (config.getDouble("freeframe.tax.defaultPercent", 5.0D) < 0.0D) {
            config.set("freeframe.tax.defaultPercent", 0.0D);
        }
        if (config.getDouble("freeframe.tax.adminShopPercent", 5.0D) < 0.0D) {
            config.set("freeframe.tax.adminShopPercent", 0.0D);
        }
        if (config.getDouble("freeframe.tax.userShopPercent", 5.0D) < 0.0D) {
            config.set("freeframe.tax.userShopPercent", 0.0D);
        }

        if (config.getLong("freeframe.dynamicPricing.windowMillis", 600000L) < 5000L) {
            config.set("freeframe.dynamicPricing.windowMillis", 600000L);
        }
        if (config.getInt("freeframe.dynamicPricing.demandThreshold", 5) < 1) {
            config.set("freeframe.dynamicPricing.demandThreshold", 5);
        }

        if (config.getLong("freeframe.security.idempotencyBucketMillis", 1500L) < 250L) {
            config.set("freeframe.security.idempotencyBucketMillis", 1500L);
        }
        if (config.getDouble("freeframe.reputation.blockThreshold", 85.0D) < 0.0D) {
            config.set("freeframe.reputation.blockThreshold", 85.0D);
        }
        if (config.getDouble("freeframe.reputation.highValueThreshold", 500.0D) < 0.0D) {
            config.set("freeframe.reputation.highValueThreshold", 500.0D);
        }

        int dashboardPort = config.getInt("freeframe.dashboard.port", 8095);
        if (dashboardPort < 1 || dashboardPort > 65535) {
            config.set("freeframe.dashboard.port", 8095);
        }
        int webhookTimeout = config.getInt("freeframe.webhooks.timeoutMillis", 4000);
        if (webhookTimeout < 100 || webhookTimeout > 60000) {
            config.set("freeframe.webhooks.timeoutMillis", 4000);
        }
        if (config.getInt("freeframe.webhooks.maxRetries", 3) < 0) {
            config.set("freeframe.webhooks.maxRetries", 3);
        }
        if (config.getLong("freeframe.webhooks.retryDelayMillis", 2000L) < 100L) {
            config.set("freeframe.webhooks.retryDelayMillis", 2000L);
        }
        String syncMode = config.getString("freeframe.networkSync.mode", "none");
        if (!"none".equalsIgnoreCase(syncMode)
            && !"bungee".equalsIgnoreCase(syncMode)
            && !"velocity".equalsIgnoreCase(syncMode)
            && !"file".equalsIgnoreCase(syncMode)
            && !"hybrid".equalsIgnoreCase(syncMode)) {
            config.set("freeframe.networkSync.mode", "none");
        }
        if (config.getLong("freeframe.networkSync.filePollTicks", 100L) < 20L) {
            config.set("freeframe.networkSync.filePollTicks", 100L);
        }
        if (config.getLong("freeframe.networkSync.eventTtlMillis", 180000L) < 10000L) {
            config.set("freeframe.networkSync.eventTtlMillis", 180000L);
        }

        if (config.getInt("freeframe.limits.maxItemsPerWindow", 64) < 1) {
            config.set("freeframe.limits.maxItemsPerWindow", 64);
        }

        if (config.getLong("freeframe.limits.windowMillis", 600000L) < 1000L) {
            config.set("freeframe.limits.windowMillis", 600000L);
        }

        if (config.getInt("freeframe.setup.wandAmount", 1) < 1) {
            config.set("freeframe.setup.wandAmount", 1);
        }
        if (config.getInt("freeframe.setup.wandAmount", 1) > 64) {
            config.set("freeframe.setup.wandAmount", 64);
        }

        int editorSize = config.getInt("freeframe.setup.editor.inventorySize", 27);
        if (editorSize < 9) {
            editorSize = 9;
        }
        if (editorSize > 54) {
            editorSize = 54;
        }
        if (editorSize % 9 != 0) {
            editorSize = (editorSize / 9) * 9;
        }
        if (editorSize < 9) {
            editorSize = 27;
        }
        config.set("freeframe.setup.editor.inventorySize", editorSize);

        if (config.getInt("freeframe.setup.editor.stockStep", 1) < 1) {
            config.set("freeframe.setup.editor.stockStep", 1);
        }
        if (config.getInt("freeframe.setup.editor.maxStockStep", 8) < 1) {
            config.set("freeframe.setup.editor.maxStockStep", 8);
        }
        if (config.getInt("freeframe.setup.editor.maxStockCap", 4096) < 1) {
            config.set("freeframe.setup.editor.maxStockCap", 4096);
        }
        if (config.getDouble("freeframe.setup.editor.priceStep", 1.0D) <= 0.0D) {
            config.set("freeframe.setup.editor.priceStep", 1.0D);
        }

        String locale = config.getString("freeframe.localization.defaultLocale", "en");
        if (locale == null || locale.trim().isEmpty()) {
            config.set("freeframe.localization.defaultLocale", "en");
        }

        this.ensurePermissionNode(FreeFrameConfigKey.FREEFRAME_RELOAD_PERMISSION.path(), "freeframe.reload");
        this.ensurePermissionNode(FreeFrameConfigKey.FREEFRAME_DESTROY_PERMISSION.path(), "freeframe.destroy");
        this.ensurePermissionNode(FreeFrameConfigKey.FREEFRAME_ADMIN_PERMISSION.path(), "freeframe.admin");
        this.ensurePermissionNode(FreeFrameConfigKey.FREEFRAME_ACCESS_BYPASSPERMISSION.path(), "freeframe.access.bypass");
    }

    public FileConfiguration getConfig() {
        return this.configApi.getConfig();
    }

    public ConfigAPI getConfigApi() {
        return this.configApi;
    }

    public String getReloadPermissionNode() {
        return this.reloadPermissionNode;
    }

    public String getDestroyPermissionNode() {
        return this.destroyPermissionNode;
    }

    public String getAdminPermissionNode() {
        return this.adminPermissionNode;
    }

    public String getAccessBypassPermissionNode() {
        return this.accessBypassPermissionNode;
    }

    private void reloadPermissionNodes() {
        this.reloadPermissionNode = this.readPermissionNode(FreeFrameConfigKey.FREEFRAME_RELOAD_PERMISSION.path(), "freeframe.reload");
        this.destroyPermissionNode = this.readPermissionNode(FreeFrameConfigKey.FREEFRAME_DESTROY_PERMISSION.path(), "freeframe.destroy");
        this.adminPermissionNode = this.readPermissionNode(FreeFrameConfigKey.FREEFRAME_ADMIN_PERMISSION.path(), "freeframe.admin");
        this.accessBypassPermissionNode = this.readPermissionNode(FreeFrameConfigKey.FREEFRAME_ACCESS_BYPASSPERMISSION.path(), "freeframe.access.bypass");
    }

    private void ensurePermissionNode(String path, String fallback) {
        String node = this.configApi.getConfig().getString(path, fallback);
        if (node == null || node.trim().isEmpty()) {
            this.configApi.getConfig().set(path, fallback);
        }
    }

    private String readPermissionNode(String path, String fallback) {
        String node = this.configApi.getConfig().getString(path, fallback);
        if (node == null || node.trim().isEmpty()) {
            return fallback;
        }
        return node.trim();
    }
}
