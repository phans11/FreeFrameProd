package de.isolveproblems.freeframe;

import de.isolveproblems.freeframe.utils.AmountValidator;
import de.isolveproblems.freeframe.utils.ConfigurationMessages;
import de.isolveproblems.freeframe.utils.FrameRegistry;
import de.isolveproblems.freeframe.utils.RegisterClasses;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.plugin.java.JavaPlugin;

public class FreeFrame extends JavaPlugin {
    private static final String DEFAULT_PREFIX = "&eFreeFrame &8>>";
    private static final String DEFAULT_PERMISSION_ERROR = "%prefix% &cYou don't have enough permissions to perform this command.";

    private RegisterClasses registrar;
    private ConfigurationMessages configHandler;
    private FrameRegistry frameRegistry;

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

        this.frameRegistry = new FrameRegistry(this);
        this.frameRegistry.loadFromConfig();
        int removedEntries = this.frameRegistry.cleanupInvalidReferences();
        if (removedEntries > 0) {
            this.getLogger().info("Removed " + removedEntries + " invalid free frame references during startup.");
        }

        this.registrar.registerCommands();
        this.registrar.registerListeners();
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

    public FileConfiguration getPluginConfig() {
        return this.configHandler.getConfig();
    }

    public int getConfiguredItemAmount() {
        return AmountValidator.sanitize(this.getPluginConfig().getInt("freeframe.item.amount", 1));
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
