package de.isolveproblems.freeframe.utils;

import de.isolveproblems.freeframe.FreeFrame;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class AuditLogger {
    private final FreeFrame freeframe;

    public AuditLogger(FreeFrame freeframe) {
        this.freeframe = freeframe;
    }

    public void logPurchase(Player player, FreeFrameData frameData, int amount, double price, String result) {
        if (!this.freeframe.getPluginConfig().getBoolean("freeframe.logging.enabled", true)
            || !this.freeframe.getPluginConfig().getBoolean("freeframe.logging.purchaseEnabled", true)) {
            return;
        }

        String playerName = player == null ? "unknown" : player.getName();
        String frameId = frameData == null ? "unknown" : frameData.getId();
        String itemType = frameData == null ? "UNKNOWN" : frameData.getItemType();
        String currency = frameData == null ? "$" : frameData.getCurrency();

        this.appendLine(this.resolveLogFile(),
            this.nowIso() + ",PURCHASE," + this.escape(playerName)
                + "," + this.escape(frameId)
                + "," + this.escape(itemType)
                + "," + amount
                + "," + String.format(Locale.ENGLISH, "%.2f", Math.max(0.0D, price))
                + "," + this.escape(currency)
                + "," + this.escape(result)
        );
    }

    public void logAdminAction(CommandSender sender, String action, String details) {
        if (!this.freeframe.getPluginConfig().getBoolean("freeframe.logging.enabled", true)
            || !this.freeframe.getPluginConfig().getBoolean("freeframe.logging.adminEnabled", true)) {
            return;
        }

        String name = sender == null ? "unknown" : sender.getName();
        this.appendLine(this.resolveLogFile(),
            this.nowIso() + ",ADMIN," + this.escape(name)
                + "," + this.escape(action)
                + "," + this.escape(details)
        );
    }

    public File exportSnapshot(List<FreeFrameData> frames, Map<String, Long> metrics) {
        String exportDirectory = this.freeframe.getPluginConfig().getString("freeframe.logging.exportDirectory", "exports");
        if (exportDirectory == null || exportDirectory.trim().isEmpty()) {
            exportDirectory = "exports";
        }
        File exportFolder = new File(this.freeframe.getDataFolder(), exportDirectory);
        if (!exportFolder.exists() && !exportFolder.mkdirs()) {
            return null;
        }

        String timestamp = new SimpleDateFormat("yyyyMMdd-HHmmss", Locale.ENGLISH).format(new Date());
        File file = new File(exportFolder, "freeframe-export-" + timestamp + ".csv");

        try (BufferedWriter writer = new BufferedWriter(new FileWriter(file, false))) {
            writer.write("section,key,value");
            writer.newLine();

            for (Map.Entry<String, Long> entry : metrics.entrySet()) {
                writer.write("metrics," + this.escape(entry.getKey()) + "," + entry.getValue());
                writer.newLine();
            }

            writer.write("frames,id,owner,item,price,currency,stock,maxStock,active,revenue");
            writer.newLine();
            for (FreeFrameData frame : frames) {
                writer.write("frames,"
                    + this.escape(frame.getId()) + ","
                    + this.escape(frame.getOwnerName()) + ","
                    + this.escape(frame.getItemType()) + ","
                    + String.format(Locale.ENGLISH, "%.2f", frame.getPrice()) + ","
                    + this.escape(frame.getCurrency()) + ","
                    + frame.getStock() + ","
                    + frame.getMaxStock() + ","
                    + frame.isActive() + ","
                    + String.format(Locale.ENGLISH, "%.2f", frame.getRevenueTotal())
                );
                writer.newLine();
            }
        } catch (IOException exception) {
            this.freeframe.getLogger().warning("Could not export audit snapshot: " + exception.getMessage());
            return null;
        }

        return file;
    }

    private File resolveLogFile() {
        String loggingDirectory = this.freeframe.getPluginConfig().getString("freeframe.logging.directory", "logs");
        if (loggingDirectory == null || loggingDirectory.trim().isEmpty()) {
            loggingDirectory = "logs";
        }

        File logsFolder = new File(this.freeframe.getDataFolder(), loggingDirectory);
        if (!logsFolder.exists() && !logsFolder.mkdirs()) {
            return new File(this.freeframe.getDataFolder(), "freeframe.log");
        }

        String filePrefix = this.freeframe.getPluginConfig().getString("freeframe.logging.filePrefix", "audit");
        if (filePrefix == null || filePrefix.trim().isEmpty()) {
            filePrefix = "audit";
        }
        String extension = this.freeframe.getPluginConfig().getString("freeframe.logging.extension", ".csv");
        if (extension == null || extension.trim().isEmpty()) {
            extension = ".csv";
        }
        if (!extension.startsWith(".")) {
            extension = "." + extension;
        }

        String date = new SimpleDateFormat("yyyy-MM-dd", Locale.ENGLISH).format(new Date());
        return new File(logsFolder, filePrefix + "-" + date + extension);
    }

    private void appendLine(File file, String line) {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(file, true))) {
            writer.write(line);
            writer.newLine();
        } catch (IOException exception) {
            this.freeframe.getLogger().warning("Could not write audit log: " + exception.getMessage());
        }
    }

    private String nowIso() {
        return new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.ENGLISH).format(new Date());
    }

    private String escape(String value) {
        if (value == null) {
            return "";
        }
        return value.replace(",", ";").replace("\n", " ").replace("\r", " ").trim();
    }
}
