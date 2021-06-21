
package de.isolveproblems.freeframe.commands;

import de.isolveproblems.freeframe.FreeFrame;
import de.isolveproblems.freeframe.utils.Var;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class CMDFreeFrame
implements CommandExecutor {
    private final FreeFrame freeframe = (FreeFrame)FreeFrame.getPlugin(FreeFrame.class);

    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        Player player = (Player)sender;
        if (args.length == 0) {
            String usage = this.freeframe.getConfigHandler().config.getConfig().getString("freeframe.usage");
            usage = usage.replace("%prefix%", this.freeframe.getPrefix());
            usage = ChatColor.translateAlternateColorCodes((char)'&', (String)usage);
            player.sendMessage(usage);
        }
        if (args.length == 1 && args[0].equalsIgnoreCase("info")) {
            player.sendMessage(" ");
            player.sendMessage(" ");
            player.sendMessage("\u00a76Plugin: \u00a7eFreeFrame");
            player.sendMessage("\u00a76Version: \u00a7e" + this.freeframe.getFreeFrame().getDescription().getVersion());
            player.sendMessage("\u00a76Author: \u00a73isolveproblems - https://twitch.tv/FragsNetwork");
            player.sendMessage(" ");
            player.sendMessage(" ");
        }
        if (args.length == 1) {
            if (player.hasPermission(Var.PERMISSION_FREEFRAME_RELOAD)) {
                if (args[0].equalsIgnoreCase("reload")) {
                    this.freeframe.getConfigHandler().config.reloadConfig();
                    String reload = this.freeframe.getConfigHandler().config.getConfig().getString("freeframe.reload");
                    reload = reload.replace("%prefix%", this.freeframe.getPrefix());
                    reload = ChatColor.translateAlternateColorCodes((char)'&', (String)reload);
                    player.sendMessage(this.freeframe.getPrefix() + "");
                    player.sendMessage(reload);
                    player.sendMessage(this.freeframe.getPrefix() + "");
                }
            } else {
                player.sendMessage(this.freeframe.getError_Permission());
            }
        }
        return false;
    }
}

