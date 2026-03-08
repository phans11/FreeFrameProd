package de.isolveproblems.freeframe.commands;

import de.isolveproblems.freeframe.FreeFrame;
import de.isolveproblems.freeframe.utils.FrameReference;
import de.isolveproblems.freeframe.utils.FrameRepairReport;
import de.isolveproblems.freeframe.utils.FrameRegistry;
import de.isolveproblems.freeframe.utils.FreeFrameData;
import de.isolveproblems.freeframe.utils.SetupWandItem;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class FreeFrameCommand implements TabExecutor {
    private final FreeFrame freeframe;

    public FreeFrameCommand(FreeFrame freeframe) {
        this.freeframe = freeframe;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length == 0 || this.equalsAny(args[0], "help", "?")) {
            this.sendHelp(sender);
            return true;
        }

        String subCommand = args[0].toLowerCase(Locale.ENGLISH);
        if ("info".equals(subCommand)) {
            this.sendInfo(sender);
            return true;
        }

        if ("reload".equals(subCommand)) {
            return this.handleReload(sender);
        }

        if (!this.hasAdminPermission(sender)) {
            sender.sendMessage(this.freeframe.getErrorPermissionMessage());
            return true;
        }

        if ("list".equals(subCommand)) {
            this.handleList(sender, args);
            return true;
        }

        if ("inspect".equals(subCommand)) {
            this.handleInspect(sender, args);
            return true;
        }

        if ("remove".equals(subCommand)) {
            this.handleRemove(sender, args);
            return true;
        }

        if ("setprice".equals(subCommand)) {
            this.handleSetPrice(sender, args);
            return true;
        }

        if ("setstock".equals(subCommand)) {
            this.handleSetStock(sender, args);
            return true;
        }

        if ("wand".equals(subCommand)) {
            this.handleWand(sender);
            return true;
        }

        if ("export".equals(subCommand)) {
            this.handleExport(sender);
            return true;
        }

        if ("storage".equals(subCommand)) {
            this.handleStorage(sender, args);
            return true;
        }

        if ("migrate".equals(subCommand)) {
            int migrated = this.freeframe.getFrameRegistry().migrateLegacyFrames();
            this.freeframe.getDisplayService().refreshAll(this.freeframe.getFrameRegistry().listFrames());
            sender.sendMessage(this.freeframe.formatMessage("%prefix% &aMigration finished. Migrated entries: &e" + migrated + "&a."));
            this.freeframe.getAuditLogger().logAdminAction(sender, "migrate", "migrated=" + migrated);
            return true;
        }

        if ("repair".equals(subCommand)) {
            FrameRepairReport report = this.freeframe.getFrameRegistry().repairFrames();
            this.freeframe.getDisplayService().refreshAll(this.freeframe.getFrameRegistry().listFrames());
            sender.sendMessage(this.freeframe.formatMessage(
                "%prefix% &aRepair finished. Removed invalid: &e" + report.getRemovedInvalidFrames()
                    + "&a, duplicates: &e" + report.getRemovedDuplicates()
                    + "&a, normalized: &e" + report.getNormalizedFrames() + "&a."
            ));
            this.freeframe.getAuditLogger().logAdminAction(sender, "repair",
                "invalid=" + report.getRemovedInvalidFrames() + " duplicates=" + report.getRemovedDuplicates());
            return true;
        }

        if ("debug".equals(subCommand)) {
            this.handleDebug(sender);
            return true;
        }

        sender.sendMessage(this.freeframe.formatMessage("%prefix% &cUnknown subcommand."));
        this.sendHelp(sender);
        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (args.length == 1) {
            List<String> completions = new ArrayList<String>();
            this.addCompletion(completions, "help", args[0]);
            this.addCompletion(completions, "info", args[0]);

            if (sender.hasPermission(this.freeframe.getConfigHandler().getReloadPermissionNode())) {
                this.addCompletion(completions, "reload", args[0]);
            }

            if (this.hasAdminPermission(sender)) {
                this.addCompletion(completions, "list", args[0]);
                this.addCompletion(completions, "inspect", args[0]);
                this.addCompletion(completions, "remove", args[0]);
                this.addCompletion(completions, "setprice", args[0]);
                this.addCompletion(completions, "setstock", args[0]);
                this.addCompletion(completions, "wand", args[0]);
                this.addCompletion(completions, "export", args[0]);
                this.addCompletion(completions, "storage", args[0]);
                this.addCompletion(completions, "migrate", args[0]);
                this.addCompletion(completions, "repair", args[0]);
                this.addCompletion(completions, "debug", args[0]);
            }
            return completions;
        }

        if (!this.hasAdminPermission(sender)) {
            return Collections.emptyList();
        }

        String subCommand = args[0].toLowerCase(Locale.ENGLISH);
        if (args.length == 2 && this.equalsAny(subCommand, "inspect", "remove", "setprice", "setstock")) {
            return this.completeFrameIds(args[1]);
        }

        if (args.length == 2 && "storage".equals(subCommand)) {
            List<String> completions = new ArrayList<String>();
            this.addCompletion(completions, "yaml", args[1]);
            this.addCompletion(completions, "sqlite", args[1]);
            this.addCompletion(completions, "mysql", args[1]);
            return completions;
        }

        if (args.length == 4 && "setprice".equals(subCommand)) {
            List<String> completions = new ArrayList<String>();
            this.addCompletion(completions, "$", args[3]);
            this.addCompletion(completions, "coins", args[3]);
            this.addCompletion(completions, "credits", args[3]);
            return completions;
        }

        return Collections.emptyList();
    }

    private boolean handleReload(CommandSender sender) {
        if (!sender.hasPermission(this.freeframe.getConfigHandler().getReloadPermissionNode())) {
            sender.sendMessage(this.freeframe.getErrorPermissionMessage());
            return true;
        }

        this.freeframe.getConfigHandler().reload();
        this.freeframe.getFrameRegistry().loadFromConfig();
        this.freeframe.getEconomyService().initialize();

        int migrated = this.freeframe.getFrameRegistry().migrateLegacyFrames();
        FrameRepairReport repairReport = this.freeframe.getFrameRegistry().repairFrames();
        this.freeframe.getDisplayService().refreshAll(this.freeframe.getFrameRegistry().listFrames());

        sender.sendMessage(this.freeframe.getPrefix());
        sender.sendMessage(this.freeframe.getMessage(
            "freeframe.reload.message",
            "%prefix% &aFreeFrame successfully reloaded all configs."
        ));
        sender.sendMessage(this.freeframe.formatMessage("%prefix% &7Migrated: &e" + migrated));
        sender.sendMessage(this.freeframe.formatMessage(
            "%prefix% &7Repair removed invalid: &e" + repairReport.getRemovedInvalidFrames()
                + " &7duplicates: &e" + repairReport.getRemovedDuplicates()
                + " &7normalized: &e" + repairReport.getNormalizedFrames()
        ));
        sender.sendMessage(this.freeframe.getPrefix());
        this.freeframe.getAuditLogger().logAdminAction(sender, "reload", "migrated=" + migrated);
        return true;
    }

    private void handleList(CommandSender sender, String[] args) {
        int page = this.parsePageArg(args, 1);
        int pageSize = Math.max(1, this.freeframe.getPluginConfig().getInt("freeframe.admin.listPageSize", 8));

        List<FreeFrameData> frames = this.freeframe.getFrameRegistry().listFrames();
        if (frames.isEmpty()) {
            sender.sendMessage(this.freeframe.formatMessage("%prefix% &7No FreeFrames are currently tracked."));
            return;
        }

        int totalPages = (int) Math.ceil((double) frames.size() / (double) pageSize);
        int safePage = Math.max(1, Math.min(page, totalPages));
        int from = (safePage - 1) * pageSize;
        int to = Math.min(from + pageSize, frames.size());

        sender.sendMessage(this.freeframe.getPrefix());
        sender.sendMessage(this.freeframe.colorize("&6Tracked FreeFrames: &e" + frames.size() + " &7(Page " + safePage + "/" + totalPages + ")"));

        for (int index = from; index < to; index++) {
            FreeFrameData data = frames.get(index);
            FrameReference reference = data.getReference();
            String location = reference == null
                ? "unknown"
                : reference.getWorldName() + ":" + reference.getX() + "," + reference.getY() + "," + reference.getZ();

            sender.sendMessage(this.freeframe.colorize(
                "&8- &e" + data.getId()
                    + " &8| &7owner: &f" + data.getOwnerName()
                    + " &8| &7price: &f" + data.getCurrency() + this.formatPrice(data.getPrice())
                    + " &8| &7stock: &f" + data.getStock() + "/" + data.getMaxStock()
                    + " &8| &7active: &f" + data.isActive()
                    + " &8| &7at: &f" + location
            ));
        }

        sender.sendMessage(this.freeframe.getPrefix());
    }

    private void handleInspect(CommandSender sender, String[] args) {
        if (args.length < 2) {
            sender.sendMessage(this.freeframe.formatMessage("%prefix% &cUsage: /freeframe inspect <id>"));
            return;
        }

        FreeFrameData data = this.freeframe.getFrameRegistry().findById(args[1]);
        if (data == null) {
            sender.sendMessage(this.unknownFrameMessage(args[1]));
            return;
        }

        FrameReference reference = data.getReference();
        String location = reference == null
            ? "unknown"
            : reference.getWorldName() + ":" + reference.getX() + "," + reference.getY() + "," + reference.getZ();
        String facing = reference == null ? "unknown" : reference.getAttachedFace();

        sender.sendMessage(this.freeframe.getPrefix());
        sender.sendMessage(this.freeframe.colorize("&6Frame ID: &e" + data.getId()));
        sender.sendMessage(this.freeframe.colorize("&6Owner: &e" + data.getOwnerName() + " &7(" + data.getOwnerUuid() + ")"));
        sender.sendMessage(this.freeframe.colorize("&6Item: &e" + data.getItemType()));
        sender.sendMessage(this.freeframe.colorize("&6Price: &e" + data.getCurrency() + this.formatPrice(data.getPrice())));
        sender.sendMessage(this.freeframe.colorize("&6Stock: &e" + data.getStock() + "/" + data.getMaxStock()));
        sender.sendMessage(this.freeframe.colorize("&6Auto Refill: &e" + data.isAutoRefill() + " &7(" + data.getRefillIntervalMillis() + "ms)"));
        sender.sendMessage(this.freeframe.colorize("&6Revenue: &e" + data.getCurrency() + this.formatPrice(data.getRevenueTotal())));
        sender.sendMessage(this.freeframe.colorize("&6Active: &e" + data.isActive()));
        sender.sendMessage(this.freeframe.colorize("&6Location: &e" + location));
        sender.sendMessage(this.freeframe.colorize("&6Attached Face: &e" + facing));
        sender.sendMessage(this.freeframe.colorize("&6Display Entity UUID: &e" + data.getDisplayEntityUuid()));
        sender.sendMessage(this.freeframe.colorize("&6Created At: &e" + this.formatEpochMillis(data.getCreatedAt())));
        sender.sendMessage(this.freeframe.getPrefix());
    }

    private void handleRemove(CommandSender sender, String[] args) {
        if (args.length < 2) {
            sender.sendMessage(this.freeframe.formatMessage("%prefix% &cUsage: /freeframe remove <id>"));
            return;
        }

        String id = args[1];
        if (!this.freeframe.getFrameRegistry().removeById(id)) {
            sender.sendMessage(this.unknownFrameMessage(id));
            return;
        }

        this.freeframe.getMetricsTracker().incrementAdminRemovals();
        sender.sendMessage(this.freeframe.formatMessage("%prefix% &aRemoved FreeFrame &e" + id.toLowerCase(Locale.ENGLISH) + "&a."));
        this.freeframe.getAuditLogger().logAdminAction(sender, "remove", id.toLowerCase(Locale.ENGLISH));
    }

    private void handleSetPrice(CommandSender sender, String[] args) {
        if (args.length < 3) {
            sender.sendMessage(this.freeframe.formatMessage("%prefix% &cUsage: /freeframe setprice <id> <price> [currency]"));
            return;
        }

        String id = args[1];
        double price;
        try {
            price = Double.parseDouble(args[2]);
        } catch (NumberFormatException exception) {
            sender.sendMessage(this.freeframe.formatMessage("%prefix% &cPrice must be a valid number."));
            return;
        }

        if (price < 0.0D) {
            sender.sendMessage(this.freeframe.formatMessage("%prefix% &cPrice must be >= 0."));
            return;
        }

        String currency = args.length >= 4 ? args[3] : null;
        if (!this.freeframe.getFrameRegistry().updatePrice(id, price, currency)) {
            sender.sendMessage(this.unknownFrameMessage(id));
            return;
        }

        FreeFrameData updated = this.freeframe.getFrameRegistry().findById(id);
        if (updated == null) {
            sender.sendMessage(this.unknownFrameMessage(id));
            return;
        }

        sender.sendMessage(this.freeframe.formatMessage(
            "%prefix% &aUpdated price for &e" + updated.getId() + " &ato &e"
                + updated.getCurrency() + this.formatPrice(updated.getPrice()) + "&a."
        ));
        this.freeframe.getAuditLogger().logAdminAction(sender, "setprice", updated.getId() + "=" + updated.getPrice());
    }

    private void handleSetStock(CommandSender sender, String[] args) {
        if (args.length < 3) {
            sender.sendMessage(this.freeframe.formatMessage("%prefix% &cUsage: /freeframe setstock <id> <stock> [max]"));
            return;
        }

        FreeFrameData data = this.freeframe.getFrameRegistry().findById(args[1]);
        if (data == null) {
            sender.sendMessage(this.unknownFrameMessage(args[1]));
            return;
        }

        int stock;
        try {
            stock = Integer.parseInt(args[2]);
        } catch (NumberFormatException exception) {
            sender.sendMessage(this.freeframe.formatMessage("%prefix% &cStock must be a valid integer."));
            return;
        }

        if (args.length >= 4) {
            try {
                int max = Integer.parseInt(args[3]);
                data.setMaxStock(max);
            } catch (NumberFormatException exception) {
                sender.sendMessage(this.freeframe.formatMessage("%prefix% &cMax stock must be a valid integer."));
                return;
            }
        }

        data.setStock(stock);
        this.freeframe.getFrameRegistry().saveToConfig();
        this.freeframe.getDisplayService().refresh(data);

        sender.sendMessage(this.freeframe.formatMessage("%prefix% &aUpdated stock for &e" + data.getId() + " &ato &e" + data.getStock() + "/" + data.getMaxStock() + "&a."));
        this.freeframe.getAuditLogger().logAdminAction(sender, "setstock", data.getId() + "=" + data.getStock() + "/" + data.getMaxStock());
    }

    private void handleWand(CommandSender sender) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(this.freeframe.formatMessage("%prefix% &cOnly players can receive the setup wand."));
            return;
        }

        Player player = (Player) sender;
        ItemStack wand = SetupWandItem.create(this.freeframe);
        Map<Integer, ItemStack> remaining = player.getInventory().addItem(wand);
        if (!remaining.isEmpty()) {
            for (ItemStack item : remaining.values()) {
                player.getWorld().dropItemNaturally(player.getLocation(), item);
            }
        }

        player.sendMessage(this.freeframe.getMessage(
            "freeframe.setup.wandReceived",
            "%prefix% &aSetup wand received.",
            player
        ));
        this.freeframe.getAuditLogger().logAdminAction(sender, "wand", "received");
    }

    private void handleExport(CommandSender sender) {
        File exported = this.freeframe.getAuditLogger().exportSnapshot(
            this.freeframe.getFrameRegistry().listFrames(),
            this.freeframe.getMetricsTracker().snapshot()
        );

        if (exported == null) {
            sender.sendMessage(this.freeframe.formatMessage("%prefix% &cCould not export snapshot."));
            return;
        }

        sender.sendMessage(this.freeframe.formatMessage("%prefix% &aExported snapshot to &e" + exported.getName() + "&a."));
        this.freeframe.getAuditLogger().logAdminAction(sender, "export", exported.getAbsolutePath());
    }

    private void handleStorage(CommandSender sender, String[] args) {
        if (args.length < 2) {
            sender.sendMessage(this.freeframe.formatMessage(
                "%prefix% &7Current backend: &e" + this.freeframe.getFrameRegistry().getActiveStorageBackend()
                    + " &7| Usage: /freeframe storage <yaml|sqlite|mysql>"
            ));
            return;
        }

        String requested = args[1].toLowerCase(Locale.ENGLISH);
        if (!this.equalsAny(requested, "yaml", "sqlite", "mysql")) {
            sender.sendMessage(this.freeframe.formatMessage("%prefix% &cInvalid backend. Use yaml/sqlite/mysql."));
            return;
        }

        List<FreeFrameData> backup = this.freeframe.getFrameRegistry().listFrames();
        this.freeframe.getPluginConfig().set("freeframe.storage.type", requested);
        this.freeframe.getConfigHandler().getConfigApi().saveConfig();
        this.freeframe.getFrameRegistry().loadFromConfig();
        boolean migrateOnSwitch = this.freeframe.getPluginConfig().getBoolean("freeframe.storage.migrateOnSwitch", true);
        if (migrateOnSwitch && this.freeframe.getFrameRegistry().size() == 0 && !backup.isEmpty()) {
            this.freeframe.getFrameRegistry().replaceAll(backup);
        }
        this.freeframe.getDisplayService().refreshAll(this.freeframe.getFrameRegistry().listFrames());

        sender.sendMessage(this.freeframe.formatMessage("%prefix% &aStorage backend changed to &e" + requested + "&a."));
        this.freeframe.getAuditLogger().logAdminAction(sender, "storage", requested);
    }

    private void handleDebug(CommandSender sender) {
        Map<String, Long> metrics = new LinkedHashMap<String, Long>(this.freeframe.getMetricsTracker().snapshot());
        FrameRegistry registry = this.freeframe.getFrameRegistry();

        sender.sendMessage(this.freeframe.getPrefix());
        sender.sendMessage(this.freeframe.colorize("&6Tracked Frames: &e" + registry.size()));
        sender.sendMessage(this.freeframe.colorize("&6Storage Backend: &e" + registry.getActiveStorageBackend()));
        sender.sendMessage(this.freeframe.colorize("&6Vault Economy: &e" + this.freeframe.getEconomyService().isAvailable()));
        sender.sendMessage(this.freeframe.colorize("&6PlaceholderAPI: &e" + this.freeframe.getServer().getPluginManager().isPluginEnabled("PlaceholderAPI")));
        for (Map.Entry<String, Long> entry : metrics.entrySet()) {
            sender.sendMessage(this.freeframe.colorize("&8- &7" + entry.getKey() + ": &e" + entry.getValue()));
        }
        sender.sendMessage(this.freeframe.getPrefix());
    }

    private void sendHelp(CommandSender sender) {
        sender.sendMessage(this.freeframe.getPrefix());
        sender.sendMessage(this.freeframe.colorize("&7/freeframe &fhelp &8- &7Show command help"));
        sender.sendMessage(this.freeframe.colorize("&7/freeframe &finfo &8- &7Show plugin information"));

        if (sender.hasPermission(this.freeframe.getConfigHandler().getReloadPermissionNode())) {
            sender.sendMessage(this.freeframe.colorize("&7/freeframe &freload &8- &7Reload config and frame cache"));
        }

        if (this.hasAdminPermission(sender)) {
            sender.sendMessage(this.freeframe.colorize("&7/freeframe &flist [page] &8- &7List tracked frames"));
            sender.sendMessage(this.freeframe.colorize("&7/freeframe &finspect <id> &8- &7Inspect frame metadata"));
            sender.sendMessage(this.freeframe.colorize("&7/freeframe &fremove <id> &8- &7Remove tracked frame"));
            sender.sendMessage(this.freeframe.colorize("&7/freeframe &fsetprice <id> <price> [currency] &8- &7Set frame price"));
            sender.sendMessage(this.freeframe.colorize("&7/freeframe &fsetstock <id> <stock> [max] &8- &7Set stock values"));
            sender.sendMessage(this.freeframe.colorize("&7/freeframe &fwand &8- &7Get setup wand"));
            sender.sendMessage(this.freeframe.colorize("&7/freeframe &fstorage <backend> &8- &7Switch YAML/SQLite/MySQL"));
            sender.sendMessage(this.freeframe.colorize("&7/freeframe &fexport &8- &7Export metrics and frame snapshot"));
            sender.sendMessage(this.freeframe.colorize("&7/freeframe &fmigrate &8- &7Run legacy migration"));
            sender.sendMessage(this.freeframe.colorize("&7/freeframe &frepair &8- &7Repair frame metadata"));
            sender.sendMessage(this.freeframe.colorize("&7/freeframe &fdebug &8- &7Show runtime metrics"));
        }

        sender.sendMessage(this.freeframe.getPrefix());
    }

    private void sendInfo(CommandSender sender) {
        sender.sendMessage(this.freeframe.getPrefix());
        sender.sendMessage(this.freeframe.colorize("&6Plugin: &eFreeFrame"));
        sender.sendMessage(this.freeframe.colorize("&6Version: &e" + this.freeframe.getDescription().getVersion()));
        sender.sendMessage(this.freeframe.colorize("&6Author: &3isolveproblems - https://twitch.tv/FragsNetwork"));
        sender.sendMessage(this.freeframe.colorize("&6Backends: &e" + this.freeframe.getFrameRegistry().getActiveStorageBackend()));
        sender.sendMessage(this.freeframe.getPrefix());
    }

    private List<String> completeFrameIds(String currentInput) {
        List<String> completions = new ArrayList<String>();
        String lowered = currentInput == null ? "" : currentInput.toLowerCase(Locale.ENGLISH);
        for (FreeFrameData frameData : this.freeframe.getFrameRegistry().listFrames()) {
            String id = frameData.getId();
            if (id.startsWith(lowered)) {
                completions.add(id);
            }
        }
        return completions;
    }

    private int parsePageArg(String[] args, int fallback) {
        if (args.length < 2) {
            return fallback;
        }

        try {
            return Integer.parseInt(args[1]);
        } catch (NumberFormatException ignored) {
            return fallback;
        }
    }

    private String unknownFrameMessage(String id) {
        String template = this.freeframe.getPluginConfig().getString(
            "freeframe.error.unknownFrame",
            "%prefix% &cNo FreeFrame found for id &e%id%&c."
        );
        if (template == null) {
            template = "%prefix% &cNo FreeFrame found for id &e%id%&c.";
        }
        return this.freeframe.formatMessage(template.replace("%id%", id));
    }

    private boolean hasAdminPermission(CommandSender sender) {
        return sender.hasPermission(this.freeframe.getConfigHandler().getAdminPermissionNode());
    }

    private boolean equalsAny(String value, String... candidates) {
        if (value == null || candidates == null) {
            return false;
        }

        for (String candidate : candidates) {
            if (candidate != null && candidate.equalsIgnoreCase(value)) {
                return true;
            }
        }
        return false;
    }

    private void addCompletion(List<String> completions, String value, String currentInput) {
        String loweredInput = currentInput == null ? "" : currentInput.toLowerCase(Locale.ENGLISH);
        if (value.startsWith(loweredInput)) {
            completions.add(value);
        }
    }

    private String formatEpochMillis(long epochMillis) {
        return new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.ENGLISH).format(new Date(epochMillis));
    }

    private String formatPrice(double price) {
        return String.format(Locale.ENGLISH, "%.2f", Math.max(0.0D, price));
    }
}
