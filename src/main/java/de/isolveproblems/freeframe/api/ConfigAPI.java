package de.isolveproblems.freeframe.api;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;

public class ConfigAPI {
    private final File file;
    private FileConfiguration fileConfig;

    public ConfigAPI(String path, String fileName) {
        this(new File(path), fileName);
    }

    public ConfigAPI(File folder, String fileName) {
        String resolvedFileName = fileName.endsWith(".yml") ? fileName : fileName + ".yml";

        if (!folder.exists() && !folder.mkdirs()) {
            throw new IllegalStateException("Could not create config folder: " + folder.getAbsolutePath());
        }

        this.file = new File(folder, resolvedFileName);
        this.fileConfig = YamlConfiguration.loadConfiguration(this.file);

        if (!this.file.exists()) {
            this.fileConfig.options().copyDefaults(true);
            this.saveConfig();
        }
    }

    public FileConfiguration getConfig() {
        return this.fileConfig;
    }

    public void saveConfig() {
        try {
            this.fileConfig.save(this.file);
        } catch (IOException exception) {
            throw new RuntimeException("Could not save config file: " + this.file.getAbsolutePath(), exception);
        }
    }

    public void reloadConfig() {
        this.fileConfig = YamlConfiguration.loadConfiguration(this.file);
    }
}
