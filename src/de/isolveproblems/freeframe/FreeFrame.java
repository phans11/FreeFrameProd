package de.isolveproblems.freeframe;

import de.isolveproblems.freeframe.utils.ConfigurationMessages;
import de.isolveproblems.freeframe.utils.RegisterClasses;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.plugin.java.JavaPlugin;

public class FreeFrame
extends JavaPlugin {
    private FreeFrame freeframe;
    private RegisterClasses register;
    private ConfigurationMessages messagesHandler;

    public void onEnable() {
        this.freeframe = this;
        this.register = new RegisterClasses();
        this.messagesHandler = new ConfigurationMessages();
        this.load();
        PluginDescriptionFile pdf = this.getDescription();
        Bukkit.getConsoleSender().sendMessage((Object)ChatColor.GOLD + "Name: " + (Object)ChatColor.YELLOW + "FreeFrame");
        Bukkit.getConsoleSender().sendMessage((Object)ChatColor.GOLD + "Version: " + (Object)ChatColor.YELLOW + pdf.getVersion());
        Bukkit.getConsoleSender().sendMessage((Object)ChatColor.GOLD + "Status: " + (Object)ChatColor.GREEN + "Enabled");
    }

    public void onDisable() {
        PluginDescriptionFile pdf = this.getDescription();
        Bukkit.getConsoleSender().sendMessage((Object)ChatColor.GOLD + "Name: " + (Object)ChatColor.YELLOW + "FreeFrame");
        Bukkit.getConsoleSender().sendMessage((Object)ChatColor.GOLD + "Version: " + (Object)ChatColor.YELLOW + pdf.getVersion());
        Bukkit.getConsoleSender().sendMessage((Object)ChatColor.GOLD + "Status: " + (Object)ChatColor.GREEN + "Enabled");
    }

    public void load() {
        this.register.commands();
        this.register.listener();
        this.register.configFiles();
    }

    public FreeFrame getFreeFrame() {
        return this.freeframe;
    }

    public RegisterClasses register() {
        return this.register;
    }

    public ConfigurationMessages getConfigHandler() {
        return this.messagesHandler;
    }

    public String getPrefix() {
        String prefix = this.freeframe.getConfigHandler().config.getConfig().getString("freeframe.prefix");
        prefix = ChatColor.translateAlternateColorCodes((char)'&', (String)prefix);
        return prefix;
    }

    public String getError_Permission() {
        String error = this.freeframe.getConfigHandler().config.getConfig().getString("freeframe.error.permission");
        error = error.replace("%prefix%", this.freeframe.getPrefix());
        error = ChatColor.translateAlternateColorCodes((char)'&', (String)error);
        return error;
    }
}

