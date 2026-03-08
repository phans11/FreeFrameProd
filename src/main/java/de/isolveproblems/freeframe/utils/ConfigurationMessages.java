package de.isolveproblems.freeframe.utils;

import de.isolveproblems.freeframe.FreeFrame;
import de.isolveproblems.freeframe.api.ConfigAPI;
import org.bukkit.configuration.file.FileConfiguration;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
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

        config.addDefault("freeframe.prefix", "&eFreeFrame &8>>");

        config.addDefault("freeframe.reload.permission", "freeframe.reload");
        config.addDefault("freeframe.reload.message", "%prefix% &aFreeFrame successfully reloaded all configs.");

        config.addDefault("freeframe.admin.permission", "freeframe.admin");
        config.addDefault("freeframe.admin.listPageSize", 8);

        config.addDefault("freeframe.error.permission", "%prefix% &cYou don't have enough permissions to perform this command.");
        config.addDefault("freeframe.error.unknownFrame", "%prefix% &cNo FreeFrame found for id &e%id%&c.");

        config.addDefault("freeframe.destroy.permission", "freeframe.destroy");
        config.addDefault("freeframe.destroy.message", "%prefix% &eYou've destroyed the &6FreeFrame &esuccessfully.");
        config.addDefault("freeframe.destroy.haveToSneak", "%prefix% &cYou have to sneak if you want to destroy this FreeFrame.");
        config.addDefault("freeframe.destroy.gamemode", "%prefix% &cYou have to be in creative mode to destroy this FreeFrame.");

        config.addDefault("freeframe.item.amount", 1);
        config.addDefault("freeframe.default.price", 0.0D);
        config.addDefault("freeframe.default.currency", "$");
        config.addDefault("freeframe.gui.inventory.size", 9);
        config.addDefault("freeframe.gui.title", "%prefix%");
        config.addDefault("freeframe.gui.saleSlots", Arrays.asList(2, 4, 6));
        config.addDefault("freeframe.gui.closeAfterPurchase", false);
        config.addDefault("freeframe.gui.dropOnFullInventory", true);

        config.addDefault("freeframe.access.requireOwner", false);
        config.addDefault("freeframe.access.bypassPermission", "freeframe.access.bypass");
        config.addDefault("freeframe.access.denied", "%prefix% &cYou are not allowed to use this FreeFrame.");
        config.addDefault("freeframe.compat.armorStandAmountFix", true);
        config.addDefault("freeframe.compat.cancelRotation", true);

        config.addDefault("freeframe.cooldown.playerMillis", 300L);
        config.addDefault("freeframe.cooldown.frameMillis", 100L);
        config.addDefault("freeframe.cooldown.message", "%prefix% &cPlease wait before using another FreeFrame.");
        config.addDefault("freeframe.rateLimit.message", "%prefix% &cThis FreeFrame is currently rate-limited.");

        config.addDefault("freeframe.purchase.success", "%prefix% &aPurchased item for &e%currency%%price%&a.");
        config.addDefault("freeframe.purchase.free", "%prefix% &aYou received this item for free.");
        config.addDefault("freeframe.purchase.notEnoughMoney", "%prefix% &cNot enough money. Required: &e%currency%%price%&c.");
        config.addDefault("freeframe.purchase.economyUnavailable", "%prefix% &cEconomy is not available right now.");
        config.addDefault("freeframe.purchase.inventoryDrop", "%prefix% &eYour inventory was full. Remaining items were dropped.");
        config.addDefault("freeframe.purchase.inventoryFull", "%prefix% &cYour inventory is full.");
        config.addDefault("freeframe.purchase.stockOut", "%prefix% &cThis frame is out of stock.");
        config.addDefault("freeframe.purchase.limited", "%prefix% &cPurchase limit reached. Try again later.");

        config.addDefault("freeframe.frame.inactive", "%prefix% &cThis FreeFrame is currently inactive.");

        config.addDefault("freeframe.metrics.bstatsPluginId", 0);
        config.addDefault("freeframe.placeholderapi.enabled", true);

        config.addDefault("freeframe.economy.allowWithoutVault", false);
        config.addDefault("freeframe.economy.payOwner", true);
        config.addDefault("freeframe.economy.payOwnerOnSelfPurchase", false);

        config.addDefault("freeframe.stock.default", 64);
        config.addDefault("freeframe.stock.defaultMax", 64);
        config.addDefault("freeframe.stock.autoRefill.defaultEnabled", false);
        config.addDefault("freeframe.stock.autoRefill.defaultIntervalMillis", 300000L);

        config.addDefault("freeframe.limits.enabled", false);
        config.addDefault("freeframe.limits.maxItemsPerWindow", 64);
        config.addDefault("freeframe.limits.windowMillis", 600000L);

        config.addDefault("freeframe.items.mode", "off");
        config.addDefault("freeframe.items.blacklist", Collections.emptyList());
        config.addDefault("freeframe.items.whitelist", Collections.emptyList());
        config.addDefault("freeframe.items.blockedMessage", "%prefix% &cThis item type is blocked by item policy.");

        config.addDefault("freeframe.logging.enabled", true);
        config.addDefault("freeframe.logging.purchaseEnabled", true);
        config.addDefault("freeframe.logging.adminEnabled", true);
        config.addDefault("freeframe.logging.directory", "logs");
        config.addDefault("freeframe.logging.filePrefix", "audit");
        config.addDefault("freeframe.logging.extension", ".csv");
        config.addDefault("freeframe.logging.exportDirectory", "exports");

        config.addDefault("freeframe.display.enabled", true);
        config.addDefault("freeframe.display.removeOnDisable", false);
        config.addDefault("freeframe.display.template", "&e%item% &7| &6%currency%%price% &7| &bStock: %stock%");
        config.addDefault("freeframe.display.onlyWhenInStock", false);
        config.addDefault("freeframe.display.loadChunk", false);
        config.addDefault("freeframe.display.offset.x", 0.5D);
        config.addDefault("freeframe.display.offset.y", 0.45D);
        config.addDefault("freeframe.display.offset.z", 0.5D);
        config.addDefault("freeframe.display.armorStand.visible", false);
        config.addDefault("freeframe.display.armorStand.gravity", false);
        config.addDefault("freeframe.display.armorStand.small", true);
        config.addDefault("freeframe.display.armorStand.marker", true);
        config.addDefault("freeframe.display.armorStand.customNameVisible", true);

        config.addDefault("freeframe.restrictions.denied", "%prefix% &cFreeFrame is disabled in this world/region.");
        config.addDefault("freeframe.restrictions.worlds.enabled", false);
        config.addDefault("freeframe.restrictions.worlds.mode", "whitelist");
        config.addDefault("freeframe.restrictions.worlds.list", Collections.emptyList());
        config.addDefault("freeframe.restrictions.regions.enabled", false);
        config.addDefault("freeframe.restrictions.regions.list", Collections.emptyList());

        config.addDefault("freeframe.setup.messages.emptyFrame", "%prefix% &cThe selected ItemFrame is empty.");
        config.addDefault("freeframe.setup.wandName", "&6FreeFrame Setup Wand");
        config.addDefault("freeframe.setup.wandReceived", "%prefix% &aSetup wand received.");
        config.addDefault("freeframe.setup.wandMaterial", "BLAZE_ROD");
        config.addDefault("freeframe.setup.wandAmount", 1);
        config.addDefault("freeframe.setup.wandLore", Arrays.asList(
            "&7Right-click an ItemFrame to open the editor.",
            "&7Requires admin permission."
        ));
        config.addDefault("freeframe.setup.editor.inventorySize", 27);
        config.addDefault("freeframe.setup.editor.title", "&8FreeFrame Setup: &e%id%");
        config.addDefault("freeframe.setup.editor.stockStep", 1);
        config.addDefault("freeframe.setup.editor.maxStockStep", 8);
        config.addDefault("freeframe.setup.editor.maxStockCap", 4096);
        config.addDefault("freeframe.setup.editor.priceStep", 1.0D);
        config.addDefault("freeframe.setup.editor.closeAfterChange", false);
        config.addDefault("freeframe.setup.editor.refreshDisplay", true);
        config.addDefault("freeframe.setup.editor.slots.info", 4);
        config.addDefault("freeframe.setup.editor.slots.toggleActive", 10);
        config.addDefault("freeframe.setup.editor.slots.stockDown", 11);
        config.addDefault("freeframe.setup.editor.slots.stockUp", 12);
        config.addDefault("freeframe.setup.editor.slots.refill", 13);
        config.addDefault("freeframe.setup.editor.slots.priceDown", 14);
        config.addDefault("freeframe.setup.editor.slots.priceUp", 15);
        config.addDefault("freeframe.setup.editor.slots.toggleAutoRefill", 16);
        config.addDefault("freeframe.setup.editor.slots.maxStockDown", 19);
        config.addDefault("freeframe.setup.editor.slots.maxStockUp", 20);
        config.addDefault("freeframe.setup.editor.slots.close", 22);
        config.addDefault("freeframe.setup.editor.materials.info", "PAPER");
        config.addDefault("freeframe.setup.editor.materials.toggleActiveOn", "EMERALD_BLOCK");
        config.addDefault("freeframe.setup.editor.materials.toggleActiveOff", "REDSTONE_BLOCK");
        config.addDefault("freeframe.setup.editor.materials.stockDown", "REDSTONE");
        config.addDefault("freeframe.setup.editor.materials.stockUp", "EMERALD");
        config.addDefault("freeframe.setup.editor.materials.refill", "CHEST");
        config.addDefault("freeframe.setup.editor.materials.priceDown", "GOLD_NUGGET");
        config.addDefault("freeframe.setup.editor.materials.priceUp", "GOLD_INGOT");
        config.addDefault("freeframe.setup.editor.materials.toggleAutoRefill", "LEVER");
        config.addDefault("freeframe.setup.editor.materials.maxStockDown", "COAL");
        config.addDefault("freeframe.setup.editor.materials.maxStockUp", "DIAMOND");
        config.addDefault("freeframe.setup.editor.materials.close", "BARRIER");

        config.addDefault("freeframe.storage.type", "yaml");
        config.addDefault("freeframe.storage.migrateOnSwitch", true);
        config.addDefault("freeframe.storage.sqlite.file", "freeframe.db");
        config.addDefault("freeframe.storage.mysql.host", "127.0.0.1");
        config.addDefault("freeframe.storage.mysql.port", 3306);
        config.addDefault("freeframe.storage.mysql.database", "freeframe");
        config.addDefault("freeframe.storage.mysql.username", "root");
        config.addDefault("freeframe.storage.mysql.password", "");
        config.addDefault("freeframe.storage.mysql.ssl", false);
        config.addDefault("freeframe.storage.mysql.table", "freeframe_frames");

        config.addDefault("freeframe.frames", Collections.emptyList());
        config.addDefault("freeframe.framesData", Collections.emptyMap());

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

        this.ensurePermissionNode("freeframe.reload.permission", "freeframe.reload");
        this.ensurePermissionNode("freeframe.destroy.permission", "freeframe.destroy");
        this.ensurePermissionNode("freeframe.admin.permission", "freeframe.admin");
        this.ensurePermissionNode("freeframe.access.bypassPermission", "freeframe.access.bypass");
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
        this.reloadPermissionNode = this.readPermissionNode("freeframe.reload.permission", "freeframe.reload");
        this.destroyPermissionNode = this.readPermissionNode("freeframe.destroy.permission", "freeframe.destroy");
        this.adminPermissionNode = this.readPermissionNode("freeframe.admin.permission", "freeframe.admin");
        this.accessBypassPermissionNode = this.readPermissionNode("freeframe.access.bypassPermission", "freeframe.access.bypass");
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
