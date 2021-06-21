
package de.isolveproblems.freeframe.utils;

import de.isolveproblems.freeframe.FreeFrame;
import de.isolveproblems.freeframe.commands.CMDFreeFrame;
import de.isolveproblems.freeframe.listener.CreateFrameListener;
import de.isolveproblems.freeframe.listener.DestroyFrameListener;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandExecutor;
import org.bukkit.event.Listener;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginManager;

public class RegisterClasses {
    private final FreeFrame freeframe = (FreeFrame)FreeFrame.getPlugin(FreeFrame.class);

    public void commands() {
        this.freeframe.getCommand("freeframe").setExecutor((CommandExecutor)new CMDFreeFrame());
    }

    public void listener() {
        PluginManager pm = Bukkit.getPluginManager();
        pm.registerEvents((Listener)new CreateFrameListener(), (Plugin)this.freeframe);
        pm.registerEvents((Listener)new DestroyFrameListener(), (Plugin)this.freeframe);
    }

    public void configFiles() {
        this.freeframe.getConfigHandler().getConfig();
    }
}

