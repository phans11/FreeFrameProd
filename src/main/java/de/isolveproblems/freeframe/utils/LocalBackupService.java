package de.isolveproblems.freeframe.utils;

import de.isolveproblems.freeframe.FreeFrame;
import de.isolveproblems.freeframe.api.BackupService;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.Map;

public class LocalBackupService implements BackupService {
    private final FreeFrame freeframe;

    public LocalBackupService(FreeFrame freeframe) {
        this.freeframe = freeframe;
    }

    @Override
    public File createBackup() {
        File folder = new File(this.freeframe.getDataFolder(), "backups");
        if (!folder.exists() && !folder.mkdirs()) {
            return null;
        }

        String name = "backup-" + new SimpleDateFormat("yyyyMMdd-HHmmss", Locale.ENGLISH).format(new Date()) + ".yml";
        File target = new File(folder, name);
        try {
            YamlConfiguration backup = new YamlConfiguration();
            int index = 0;
            for (FreeFrameData frameData : this.freeframe.getFrameRegistry().listFrames()) {
                frameData.writeToSection(backup.createSection("frames." + index));
                backup.set("frames." + index + ".id", frameData.getId());
                index++;
            }
            for (Map.Entry<String, Long> metric : this.freeframe.getMetricsTracker().snapshot().entrySet()) {
                backup.set("metrics." + metric.getKey(), metric.getValue());
            }
            backup.save(target);
            return target;
        } catch (Exception exception) {
            this.freeframe.getLogger().warning("Could not create backup: " + exception.getMessage());
            return null;
        }
    }

    @Override
    public boolean restoreBackup(String fileName) {
        if (fileName == null || fileName.trim().isEmpty()) {
            return false;
        }

        File file = new File(new File(this.freeframe.getDataFolder(), "backups"), fileName);
        if (!file.isFile()) {
            return false;
        }

        try {
            YamlConfiguration backup = YamlConfiguration.loadConfiguration(file);
            java.util.List<FreeFrameData> restored = new java.util.ArrayList<FreeFrameData>();
            org.bukkit.configuration.ConfigurationSection section = backup.getConfigurationSection("frames");
            if (section != null) {
                for (String key : section.getKeys(false)) {
                    org.bukkit.configuration.ConfigurationSection frameSection = section.getConfigurationSection(key);
                    if (frameSection == null) {
                        continue;
                    }
                    FreeFrameData data = FreeFrameData.fromSection(frameSection.getString("id", key), frameSection);
                    if (data != null) {
                        restored.add(data);
                    }
                }
            }
            this.freeframe.getFrameRegistry().replaceAll(restored);
            return true;
        } catch (Exception exception) {
            this.freeframe.getLogger().warning("Could not restore backup: " + exception.getMessage());
            return false;
        }
    }

    @Override
    public String runDoctor() {
        FrameRepairReport report = this.freeframe.getFrameRegistry().repairFrames();
        return "invalid=" + report.getRemovedInvalidFrames()
            + " duplicates=" + report.getRemovedDuplicates()
            + " normalized=" + report.getNormalizedFrames();
    }
}
