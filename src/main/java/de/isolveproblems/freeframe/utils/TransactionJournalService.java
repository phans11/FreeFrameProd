package de.isolveproblems.freeframe.utils;

import de.isolveproblems.freeframe.FreeFrame;
import org.bukkit.entity.Player;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;

public class TransactionJournalService {
    private final FreeFrame freeframe;
    private final File journalFile;

    public TransactionJournalService(FreeFrame freeframe) {
        this.freeframe = freeframe;
        File folder = new File(freeframe.getDataFolder(), "journal");
        if (!folder.exists()) {
            folder.mkdirs();
        }
        this.journalFile = new File(folder, "events.log");
    }

    public synchronized void logPurchaseCommit(String txId, Player player, FreeFrameData frameData, int amount,
                                               double gross, double tax, double net, String status, String signature) {
        this.append("PURCHASE_COMMIT",
            this.escape(txId),
            this.escape(player == null ? "unknown" : player.getUniqueId().toString()),
            this.escape(player == null ? "unknown" : player.getName()),
            this.escape(frameData == null ? "unknown" : frameData.getId()),
            String.valueOf(amount),
            format(gross),
            format(tax),
            format(net),
            this.escape(status),
            this.escape(signature)
        );
    }

    public synchronized void logBid(String txId, Player player, FreeFrameData frameData, double bid) {
        this.append("BID",
            this.escape(txId),
            this.escape(player == null ? "unknown" : player.getUniqueId().toString()),
            this.escape(player == null ? "unknown" : player.getName()),
            this.escape(frameData == null ? "unknown" : frameData.getId()),
            format(bid)
        );
    }

    public synchronized void logAlert(String key, String message) {
        this.append("ALERT", this.escape(key), this.escape(message));
    }

    public synchronized JournalReplayReport replayIdempotency(PurchaseSecurityService securityService, boolean dryRun) {
        int total = 0;
        int commits = 0;
        int duplicates = 0;
        int rebuilt = 0;
        Set<String> seen = new HashSet<String>();

        if (!this.journalFile.isFile()) {
            return new JournalReplayReport(0, 0, 0, 0);
        }

        try (BufferedReader reader = new BufferedReader(new FileReader(this.journalFile))) {
            String line;
            while ((line = reader.readLine()) != null) {
                total++;
                String[] parts = line.split("\\|", -1);
                if (parts.length < 3) {
                    continue;
                }

                String eventType = parts[1];
                if (!"PURCHASE_COMMIT".equals(eventType)) {
                    continue;
                }

                commits++;
                String txId = unescape(parts[2]);
                if (!seen.add(txId)) {
                    duplicates++;
                    continue;
                }

                if (!dryRun && txId != null && !txId.trim().isEmpty() && !securityService.isCommitted(txId)) {
                    securityService.markCommitted(txId);
                    rebuilt++;
                }
            }
        } catch (IOException exception) {
            this.freeframe.getLogger().warning("Could not replay transaction journal: " + exception.getMessage());
        }
        return new JournalReplayReport(total, commits, duplicates, rebuilt);
    }

    private void append(String type, String... values) {
        StringBuilder builder = new StringBuilder();
        builder.append(this.timestamp()).append("|").append(this.escape(type));
        if (values != null) {
            for (String value : values) {
                builder.append("|").append(this.escape(value));
            }
        }
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(this.journalFile, true))) {
            writer.write(builder.toString());
            writer.newLine();
        } catch (IOException exception) {
            this.freeframe.getLogger().warning("Could not append transaction journal: " + exception.getMessage());
        }
    }

    private String timestamp() {
        return new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS", Locale.ENGLISH).format(new Date());
    }

    private static String format(double value) {
        return String.format(Locale.ENGLISH, "%.2f", Math.max(0.0D, value));
    }

    private String escape(String value) {
        if (value == null) {
            return "";
        }
        return value.replace("\\", "\\\\").replace("|", "\\p").replace("\n", " ").replace("\r", " ");
    }

    private String unescape(String value) {
        if (value == null) {
            return "";
        }
        return value.replace("\\p", "|").replace("\\\\", "\\");
    }
}
