package de.isolveproblems.freeframe.commands;

import de.isolveproblems.freeframe.FreeFrame;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class FreeFrameCommand implements TabExecutor {
    private final FreeFrame freeframe;

    public FreeFrameCommand(FreeFrame freeframe) {
        this.freeframe = freeframe;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length == 0 || args[0].equalsIgnoreCase("help")) {
            this.sendHelp(sender);
            return true;
        }

        if (args.length == 1 && args[0].equalsIgnoreCase("info")) {
            this.sendInfo(sender);
            return true;
        }

        if (args.length == 1 && args[0].equalsIgnoreCase("reload")) {
            if (!sender.hasPermission(this.freeframe.getConfigHandler().getReloadPermissionNode())) {
                sender.sendMessage(this.freeframe.getErrorPermissionMessage());
                return true;
            }

            this.freeframe.getConfigHandler().reload();
            this.freeframe.getFrameRegistry().loadFromConfig();
            int removedEntries = this.freeframe.getFrameRegistry().cleanupInvalidReferences();

            sender.sendMessage(this.freeframe.getPrefix());
            sender.sendMessage(this.freeframe.getMessage(
                "freeframe.reload.message",
                "%prefix% &aFreeFrame successfully reloaded all configs."
            ));
            if (removedEntries > 0) {
                sender.sendMessage(this.freeframe.formatMessage(
                    "%prefix% &7Cleaned up &e" + removedEntries + " &7invalid frame references."
                ));
            }
            sender.sendMessage(this.freeframe.getPrefix());
            return true;
        }

        sender.sendMessage(this.freeframe.formatMessage("%prefix% &cUnknown subcommand."));
        this.sendHelp(sender);
        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (args.length != 1) {
            return Collections.emptyList();
        }

        List<String> completions = new ArrayList<String>();
        this.addCompletion(completions, "help", args[0]);
        this.addCompletion(completions, "info", args[0]);
        if (sender.hasPermission(this.freeframe.getConfigHandler().getReloadPermissionNode())) {
            this.addCompletion(completions, "reload", args[0]);
        }
        return completions;
    }

    private void sendHelp(CommandSender sender) {
        sender.sendMessage(this.freeframe.getPrefix());
        sender.sendMessage(this.freeframe.colorize("&7/freeframe &fhelp &8- &7Show command help"));
        sender.sendMessage(this.freeframe.colorize("&7/freeframe &finfo &8- &7Show plugin information"));
        sender.sendMessage(this.freeframe.colorize("&7/freeframe &freload &8- &7Reload config and frame cache"));
        sender.sendMessage(this.freeframe.getPrefix());
    }

    private void sendInfo(CommandSender sender) {
        sender.sendMessage(this.freeframe.getPrefix());
        sender.sendMessage(this.freeframe.colorize("&6Plugin: &eFreeFrame"));
        sender.sendMessage(this.freeframe.colorize("&6Version: &e" + this.freeframe.getDescription().getVersion()));
        sender.sendMessage(this.freeframe.colorize("&6Author: &3isolveproblems - https://twitch.tv/FragsNetwork"));
        sender.sendMessage(this.freeframe.getPrefix());
    }

    private void addCompletion(List<String> completions, String value, String currentInput) {
        String loweredInput = currentInput == null ? "" : currentInput.toLowerCase();
        if (value.startsWith(loweredInput)) {
            completions.add(value);
        }
    }
}
