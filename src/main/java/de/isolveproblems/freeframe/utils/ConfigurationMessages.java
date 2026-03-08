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

    public ConfigurationMessages(FreeFrame freeframe) {
        this.freeframe = freeframe;
        this.configApi = new ConfigAPI(freeframe.getDataFolder(), "config.yml");
    }

    public void load() {
        FileConfiguration config = this.configApi.getConfig();

        config.addDefault("freeframe.prefix", "&eFreeFrame &8>>");
        config.addDefault("freeframe.reload.permission", "freeframe.reload");
        config.addDefault("freeframe.reload.message", "%prefix% &aFreeFrame successfully reloaded all configs.");
        config.addDefault("freeframe.usage", "&cUsage: &7Place an item frame and add an item to it. That's it! Here you have your own &eFreeFrame");
        config.addDefault("freeframe.error.permission", "%prefix% &cYou don't have enough permissions to perform this command.");
        config.addDefault("freeframe.destroy.permission", "freeframe.destroy");
        config.addDefault("freeframe.destroy.message", "%prefix% &eYou've destroyed the &6FreeFrame &esuccessfully.");
        config.addDefault("freeframe.destroy.haveToSneak", "%prefix% &cYou have to sneak if you want to destroy this FreeFrame.");
        config.addDefault("freeframe.destroy.gamemode", "%prefix% &cYou have to be in creative mode to destroy this FreeFrame.");
        config.addDefault("freeframe.item.amount", 1);
        config.addDefault("freeframe.frames", Collections.emptyList());

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

        this.ensurePermissionNode("freeframe.reload.permission", "freeframe.reload");
        this.ensurePermissionNode("freeframe.destroy.permission", "freeframe.destroy");
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

    private void reloadPermissionNodes() {
        this.reloadPermissionNode = this.readPermissionNode("freeframe.reload.permission", "freeframe.reload");
        this.destroyPermissionNode = this.readPermissionNode("freeframe.destroy.permission", "freeframe.destroy");
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
