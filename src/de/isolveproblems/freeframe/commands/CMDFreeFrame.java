
package de.isolveproblems.freeframe.commands;

import de.isolveproblems.freeframe.FreeFrame;
import de.isolveproblems.freeframe.utils.Var;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

public class CMDFreeFrame
implements CommandExecutor {
    private final FreeFrame freeframe = (FreeFrame)FreeFrame.getPlugin(FreeFrame.class);

    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length == 0) {
            sender.sendMessage(this.colorize(this.freeframe.getConfigHandler().config.getConfig().getString("freeframe.usage")));
            return true;
        }

        if (args.length == 1 && args[0].equalsIgnoreCase("info")) {
            sender.sendMessage(" ");
            sender.sendMessage(this.colorize("&6Plugin: &eFreeFrame"));
            sender.sendMessage(this.colorize("&6Version: &e" + this.freeframe.getDescription().getVersion()));
            sender.sendMessage(this.colorize("&6Author: &3isolveproblems - https://twitch.tv/FragsNetwork"));
            sender.sendMessage(" ");
            return true;
        }

        if (args.length == 1 && args[0].equalsIgnoreCase("reload")) {
            if (!sender.hasPermission(Var.PERMISSION_FREEFRAME_RELOAD)) {
                sender.sendMessage(this.freeframe.getError_Permission());
                return true;
            }
            this.freeframe.getConfigHandler().config.reloadConfig();
            this.freeframe.getConfigHandler().reloadPermissionNodes();
            String reload = this.freeframe.getConfigHandler().config.getConfig().getString("freeframe.reload.message");
            reload = reload.replace("%prefix%", this.freeframe.getPrefix());
            sender.sendMessage(this.freeframe.getPrefix());
            sender.sendMessage(ChatColor.translateAlternateColorCodes((char)'&', (String)reload));
            sender.sendMessage(this.freeframe.getPrefix());
            return true;
        }

        sender.sendMessage(this.colorize(this.freeframe.getConfigHandler().config.getConfig().getString("freeframe.usage")));
        return true;
    }

    private String colorize(String text) {
        if (text == null) {
            return "";
        }
        text = text.replace("%prefix%", this.freeframe.getPrefix());
        return ChatColor.translateAlternateColorCodes((char)'&', (String)text);
    }
}
