
package de.isolveproblems.freeframe.api;

import java.io.File;
import java.io.IOException;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.Plugin;

public class ConfigAPI {
    private File file;
    private FileConfiguration fileConfig;

    public ConfigAPI(String path, String fileName, Runnable callback, Plugin plugin) {
        if (!fileName.contains(".yml")) {
            fileName = fileName + ".yml";
        }
        this.file = new File(path, fileName);
        this.fileConfig = YamlConfiguration.loadConfiguration((File)this.file);
        if (!this.file.exists()) {
            this.fileConfig.options().copyDefaults(true);
            callback.run();
            try {
                this.fileConfig.save(this.file);
            }
            catch (IOException exception) {
                exception.printStackTrace();
            }
        }
    }

    public ConfigAPI(String path, String fileName, Plugin plugin) {
        if (!fileName.contains(".yml")) {
            fileName = fileName + ".yml";
        }
        this.file = new File(path, fileName);
        this.fileConfig = YamlConfiguration.loadConfiguration((File)this.file);
        if (!this.file.exists()) {
            this.fileConfig.options().copyDefaults(true);
            try {
                this.fileConfig.save(this.file);
            }
            catch (IOException exception) {
                exception.printStackTrace();
            }
        }
    }

    public FileConfiguration getConfig() {
        return this.fileConfig;
    }

    public void saveConfig() {
        try {
            this.fileConfig.save(this.file);
        }
        catch (IOException exception) {
            exception.printStackTrace();
        }
    }

    public void setLocation(String path, Location location) {
        this.fileConfig.set(path + ".World", (Object)location.getWorld().getName());
        this.fileConfig.set(path + ".X", (Object)location.getX());
        this.fileConfig.set(path + ".Y", (Object)location.getY());
        this.fileConfig.set(path + ".Z", (Object)location.getZ());
        this.fileConfig.set(path + ".Pitch", (Object)Float.valueOf(location.getPitch()));
        this.fileConfig.set(path + ".Yaw", (Object)Float.valueOf(location.getYaw()));
        this.saveConfig();
    }

    public Location getLocation(String path) {
        if (this.fileConfig.getString(path + ".World") == null) {
            return null;
        }
        Location location = new Location(Bukkit.getWorld((String)this.fileConfig.getString(path + ".World")), this.fileConfig.getDouble(path + ".X"), this.fileConfig.getDouble(path + ".Y"), this.fileConfig.getDouble(path + ".Z"), (float)this.fileConfig.getDouble(path + ".Yaw"), (float)this.fileConfig.getDouble(path + ".Pitch"));
        return location;
    }

    public void reloadConfig() {
        this.fileConfig = YamlConfiguration.loadConfiguration((File)this.file);
    }
}

