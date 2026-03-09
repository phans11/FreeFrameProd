package de.isolveproblems.freeframe.utils;

import de.isolveproblems.freeframe.FreeFrame;
import de.isolveproblems.freeframe.commands.FreeFrameCommand;
import de.isolveproblems.freeframe.listener.AnalyticsInventoryListener;
import de.isolveproblems.freeframe.listener.CreateFrameListener;
import de.isolveproblems.freeframe.listener.DestroyFrameListener;
import de.isolveproblems.freeframe.listener.FrameBreakCleanupListener;
import de.isolveproblems.freeframe.listener.FrameInventoryListener;
import de.isolveproblems.freeframe.listener.SetupEditorListener;
import de.isolveproblems.freeframe.listener.SetupWandListener;
import org.bukkit.command.PluginCommand;
import org.bukkit.plugin.PluginManager;

public class RegisterClasses {
    private final FreeFrame freeframe;

    public RegisterClasses(FreeFrame freeframe) {
        this.freeframe = freeframe;
    }

    public void registerCommands() {
        PluginCommand command = this.freeframe.getCommand("freeframe");
        if (command == null) {
            this.freeframe.getLogger().warning("Command 'freeframe' is not defined in plugin.yml");
            return;
        }

        FreeFrameCommand commandHandler = new FreeFrameCommand(this.freeframe);
        command.setExecutor(commandHandler);
        command.setTabCompleter(commandHandler);
    }

    public void registerListeners() {
        PluginManager pluginManager = this.freeframe.getServer().getPluginManager();
        pluginManager.registerEvents(new CreateFrameListener(this.freeframe), this.freeframe);
        pluginManager.registerEvents(new DestroyFrameListener(this.freeframe), this.freeframe);
        pluginManager.registerEvents(new FrameBreakCleanupListener(this.freeframe), this.freeframe);
        pluginManager.registerEvents(new FrameInventoryListener(this.freeframe), this.freeframe);
        pluginManager.registerEvents(new SetupWandListener(this.freeframe), this.freeframe);
        pluginManager.registerEvents(new SetupEditorListener(this.freeframe), this.freeframe);
        pluginManager.registerEvents(new AnalyticsInventoryListener(), this.freeframe);
    }
}
