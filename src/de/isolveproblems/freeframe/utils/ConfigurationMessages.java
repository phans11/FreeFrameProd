
package de.isolveproblems.freeframe.utils;

import de.isolveproblems.freeframe.FreeFrame;
import de.isolveproblems.freeframe.api.ConfigAPI;
import de.isolveproblems.freeframe.utils.Var;
import org.bukkit.plugin.Plugin;

public class ConfigurationMessages {
    private final FreeFrame freeframe = (FreeFrame)FreeFrame.getPlugin(FreeFrame.class);
    public ConfigAPI config;

    public void getConfig() {
        this.config = new ConfigAPI("plugins/FreeFrame/", "config.yml", (Plugin)this.freeframe);
        this.config.getConfig().addDefault("freeframe.prefix", (Object)"&eCorno &8\u00bb");
        this.config.getConfig().addDefault("freeframe.reload.permission", (Object)Var.PERMISSION_FREEFRAME_RELOAD);
        this.config.getConfig().addDefault("freeframe.reload.message", (Object)"%prefix% &aFreeFrame successfully reloaded all configs.");
        this.config.getConfig().addDefault("freeframe.usage", (Object)"&cUsage: &7Place an item frame and add an item to it. That's it! Here you have your own &eFreeFrame");
        this.config.getConfig().addDefault("freeframe.error.permission", (Object)"%prefix% &cYou don't have enough permissions to perform this command.");
        this.config.getConfig().addDefault("freeframe.destroy.permission", (Object)Var.PERMISSION_FREEFRAME_DESTROY);
        this.config.getConfig().addDefault("freeframe.destroy.message", (Object)"%prefix% &eYou've destroyed the &6FreeFrame &esuccessfully.");
        this.config.getConfig().addDefault("freeframe.destroy.haveToSneak", (Object)"%prefix% &cYou have to sneak if you want to destroy this FreeFrame.");
        this.config.getConfig().addDefault("freeframe.destroy.gamemode", (Object)"%prefix% &cYou have to be in creative mode to destroy this FreeFrame.");
        this.config.getConfig().addDefault("freeframe.item.amount", (Object)1);
        this.config.saveConfig();
        Var.PERMISSION_FREEFRAME_RELOAD = "freeframe.reload.permission";
        Var.PERMISSION_FREEFRAME_DESTROY = "freeframe.destroy.permission";
    }
}

