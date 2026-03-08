package de.isolveproblems.freeframe.utils;

import de.isolveproblems.freeframe.FreeFrame;
import de.isolveproblems.freeframe.api.ConfigAPI;
import org.bukkit.configuration.file.FileConfiguration;

import java.util.Collections;

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

        config.addDefault("freeframe.access.requireOwner", false);
        config.addDefault("freeframe.access.bypassPermission", "freeframe.access.bypass");
        config.addDefault("freeframe.access.denied", "%prefix% &cYou are not allowed to use this FreeFrame.");

        config.addDefault("freeframe.cooldown.playerMillis", 300L);
        config.addDefault("freeframe.cooldown.frameMillis", 100L);
        config.addDefault("freeframe.cooldown.message", "%prefix% &cPlease wait before using another FreeFrame.");
        config.addDefault("freeframe.rateLimit.message", "%prefix% &cThis FreeFrame is currently rate-limited.");

        config.addDefault("freeframe.purchase.success", "%prefix% &aPurchased item for &e%currency%%price%&a.");
        config.addDefault("freeframe.purchase.free", "%prefix% &aYou received this item for free.");
        config.addDefault("freeframe.purchase.notEnoughMoney", "%prefix% &cNot enough money. Required: &e%currency%%price%&c.");
        config.addDefault("freeframe.purchase.economyUnavailable", "%prefix% &cEconomy is not available right now.");
        config.addDefault("freeframe.purchase.inventoryDrop", "%prefix% &eYour inventory was full. Remaining items were dropped.");

        config.addDefault("freeframe.frame.inactive", "%prefix% &cThis FreeFrame is currently inactive.");

        config.addDefault("freeframe.metrics.bstatsPluginId", 0);

        config.addDefault("freeframe.economy.allowWithoutVault", false);

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
