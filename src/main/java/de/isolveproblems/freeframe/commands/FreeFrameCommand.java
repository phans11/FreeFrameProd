package de.isolveproblems.freeframe.commands;

import de.isolveproblems.freeframe.FreeFrame;
import de.isolveproblems.freeframe.api.FrameType;
import de.isolveproblems.freeframe.api.PurchaseProfile;
import de.isolveproblems.freeframe.utils.BlockReference;
import de.isolveproblems.freeframe.utils.FrameReference;
import de.isolveproblems.freeframe.utils.FrameRepairReport;
import de.isolveproblems.freeframe.utils.FrameRegistry;
import de.isolveproblems.freeframe.utils.FreeFrameData;
import de.isolveproblems.freeframe.utils.SetupWandItem;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.InventoryHolder;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
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
        if ("settype".equals(subCommand)) {
            this.handleSetType(sender, args);
            return true;
        }
        if ("setprofile".equals(subCommand)) {
            this.handleSetProfile(sender, args);
            return true;
        }
        if ("clearprofiles".equals(subCommand)) {
            this.handleClearProfiles(sender, args);
            return true;
        }
        if ("linkchest".equals(subCommand)) {
            this.handleLinkChest(sender, args);
            return true;
        }
        if ("stats".equals(subCommand)) {
            this.handleStats(sender, args);
            return true;
        }
        if ("backup".equals(subCommand)) {
            this.handleBackup(sender);
            return true;
        }
        if ("restore".equals(subCommand)) {
            this.handleRestore(sender, args);
            return true;
        }
        if ("doctor".equals(subCommand)) {
            this.handleDoctor(sender);
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
            this.logAdminAction(sender, "migrate", "migrated=" + migrated);
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
            this.logAdminAction(sender, "repair",
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
                this.addCompletion(completions, "settype", args[0]);
                this.addCompletion(completions, "setprofile", args[0]);
                this.addCompletion(completions, "clearprofiles", args[0]);
                this.addCompletion(completions, "linkchest", args[0]);
                this.addCompletion(completions, "stats", args[0]);
                this.addCompletion(completions, "backup", args[0]);
                this.addCompletion(completions, "restore", args[0]);
                this.addCompletion(completions, "doctor", args[0]);
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
        if (args.length == 2 && this.equalsAny(subCommand, "inspect", "remove", "setprice", "setstock", "settype", "setprofile", "clearprofiles", "linkchest")) {
            return this.completeFrameIds(args[1]);
        }

        if (args.length == 2 && "storage".equals(subCommand)) {
            List<String> completions = new ArrayList<String>();
            this.addCompletion(completions, "yaml", args[1]);
            this.addCompletion(completions, "sqlite", args[1]);
            this.addCompletion(completions, "mysql", args[1]);
            return completions;
        }

        if (args.length == 2 && "stats".equals(subCommand)) {
            List<String> completions = new ArrayList<String>();
            this.addCompletion(completions, "frame", args[1]);
            this.addCompletion(completions, "player", args[1]);
            return completions;
        }

        if (args.length == 3 && "stats".equals(subCommand) && "frame".equalsIgnoreCase(args[1])) {
            return this.completeFrameIds(args[2]);
        }

        if (args.length == 2 && "restore".equals(subCommand)) {
            return this.completeBackupFiles(args[1]);
        }

        if (args.length == 3 && "settype".equals(subCommand)) {
            List<String> completions = new ArrayList<String>();
            for (FrameType type : FrameType.values()) {
                this.addCompletion(completions, type.name().toLowerCase(Locale.ENGLISH), args[2]);
            }
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

        this.freeframe.reloadRuntimeState();
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
        this.logAdminAction(sender, "reload", "migrated=" + migrated);
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
                    + " &8| &7type: &f" + data.getFrameType().name()
                    + " &8| &7owner: &f" + data.getOwnerName()
                    + " &8| &7price: &f" + data.getCurrency() + this.formatPrice(data.getPrice())
                    + " &8| &7stock: &f" + data.getStock() + "/" + data.getMaxStock()
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
        String linkedChest = data.getLinkedChest() == null ? "none" : data.getLinkedChest().serialize();

        sender.sendMessage(this.freeframe.getPrefix());
        sender.sendMessage(this.freeframe.colorize("&6Frame ID: &e" + data.getId()));
        sender.sendMessage(this.freeframe.colorize("&6Owner: &e" + data.getOwnerName() + " &7(" + data.getOwnerUuid() + ")"));
        sender.sendMessage(this.freeframe.colorize("&6Item: &e" + data.getItemType()));
        sender.sendMessage(this.freeframe.colorize("&6Type: &e" + data.getFrameType().name()));
        sender.sendMessage(this.freeframe.colorize("&6Base Price: &e" + data.getCurrency() + this.formatPrice(data.getPrice())));
        sender.sendMessage(this.freeframe.colorize("&6Stock: &e" + data.getStock() + "/" + data.getMaxStock()));
        sender.sendMessage(this.freeframe.colorize("&6Auto Refill: &e" + data.isAutoRefill() + " &7(" + data.getRefillIntervalMillis() + "ms)"));
        sender.sendMessage(this.freeframe.colorize("&6Revenue: &e" + data.getCurrency() + this.formatPrice(data.getRevenueTotal())));
        sender.sendMessage(this.freeframe.colorize("&6Linked Chest: &e" + linkedChest));
        sender.sendMessage(this.freeframe.colorize("&6Profiles: &e" + this.describeProfiles(data.getPurchaseProfiles(), data.getCurrency())));
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
        this.logAdminAction(sender, "remove", id.toLowerCase(Locale.ENGLISH));
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
        this.logAdminAction(sender, "setprice", updated.getId() + "=" + updated.getPrice());
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
        this.logAdminAction(sender, "setstock", data.getId() + "=" + data.getStock() + "/" + data.getMaxStock());
    }

    private void handleSetType(CommandSender sender, String[] args) {
        if (args.length < 3) {
            sender.sendMessage(this.freeframe.formatMessage("%prefix% &cUsage: /freeframe settype <id> <free|shop|limited|admin_only|preview_only>"));
            return;
        }

        FreeFrameData data = this.freeframe.getFrameRegistry().findById(args[1]);
        if (data == null) {
            sender.sendMessage(this.unknownFrameMessage(args[1]));
            return;
        }

        FrameType type = FrameType.fromString(args[2]);
        data.setFrameType(type);
        this.freeframe.getFrameRegistry().saveToConfig();
        this.freeframe.getDisplayService().refresh(data);
        sender.sendMessage(this.freeframe.formatMessage("%prefix% &aUpdated type for &e" + data.getId() + " &ato &e" + type.name() + "&a."));
        this.logAdminAction(sender, "settype", data.getId() + "=" + type.name());
    }

    private void handleSetProfile(CommandSender sender, String[] args) {
        if (args.length < 5) {
            sender.sendMessage(this.freeframe.formatMessage("%prefix% &cUsage: /freeframe setprofile <id> <slot> <amount> <price> [displayName]"));
            return;
        }

        FreeFrameData data = this.freeframe.getFrameRegistry().findById(args[1]);
        if (data == null) {
            sender.sendMessage(this.unknownFrameMessage(args[1]));
            return;
        }

        int slot;
        int amount;
        double price;
        try {
            slot = Integer.parseInt(args[2]);
            amount = Integer.parseInt(args[3]);
            price = Double.parseDouble(args[4]);
        } catch (NumberFormatException exception) {
            sender.sendMessage(this.freeframe.formatMessage("%prefix% &cSlot, amount and price must be numeric."));
            return;
        }

        if (slot < 0 || slot >= this.freeframe.getGuiInventorySize()) {
            sender.sendMessage(this.freeframe.formatMessage("%prefix% &cSlot is outside the configured GUI size."));
            return;
        }
        if (amount < 1 || price < 0.0D) {
            sender.sendMessage(this.freeframe.formatMessage("%prefix% &cAmount must be >= 1 and price must be >= 0."));
            return;
        }

        String displayName = args.length > 5 ? this.joinArgs(args, 5) : "";
        List<PurchaseProfile> profiles = new ArrayList<PurchaseProfile>(data.getPurchaseProfiles());
        for (int index = profiles.size() - 1; index >= 0; index--) {
            if (profiles.get(index).getSlot() == slot) {
                profiles.remove(index);
            }
        }
        profiles.add(new PurchaseProfile(slot, amount, price, displayName));
        data.setPurchaseProfiles(profiles);
        this.freeframe.getFrameRegistry().saveToConfig();
        this.freeframe.getDisplayService().refresh(data);
        sender.sendMessage(this.freeframe.formatMessage("%prefix% &aUpdated profile on slot &e" + slot + " &afor &e" + data.getId() + "&a."));
        this.logAdminAction(sender, "setprofile", data.getId() + " slot=" + slot + " amount=" + amount + " price=" + price);
    }

    private void handleClearProfiles(CommandSender sender, String[] args) {
        if (args.length < 2) {
            sender.sendMessage(this.freeframe.formatMessage("%prefix% &cUsage: /freeframe clearprofiles <id>"));
            return;
        }

        FreeFrameData data = this.freeframe.getFrameRegistry().findById(args[1]);
        if (data == null) {
            sender.sendMessage(this.unknownFrameMessage(args[1]));
            return;
        }

        data.setPurchaseProfiles(this.freeframe.getDefaultPurchaseProfiles(data.getPrice()));
        this.freeframe.getFrameRegistry().saveToConfig();
        this.freeframe.getDisplayService().refresh(data);
        sender.sendMessage(this.freeframe.formatMessage("%prefix% &aReset profiles for &e" + data.getId() + "&a."));
        this.logAdminAction(sender, "clearprofiles", data.getId());
    }

    private void handleLinkChest(CommandSender sender, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(this.freeframe.formatMessage("%prefix% &cOnly players can link a chest."));
            return;
        }
        if (args.length < 2) {
            sender.sendMessage(this.freeframe.formatMessage("%prefix% &cUsage: /freeframe linkchest <id>"));
            return;
        }

        FreeFrameData data = this.freeframe.getFrameRegistry().findById(args[1]);
        if (data == null) {
            sender.sendMessage(this.unknownFrameMessage(args[1]));
            return;
        }

        Player player = (Player) sender;
        Block target = player.getTargetBlock((HashSet<Byte>) null, 5);
        if (target == null) {
            sender.sendMessage(this.freeframe.formatMessage("%prefix% &cLook at an inventory block within 5 blocks."));
            return;
        }

        BlockState state = target.getState();
        if (!(state instanceof InventoryHolder)) {
            sender.sendMessage(this.freeframe.formatMessage("%prefix% &cThe targeted block is not a chest or inventory block."));
            return;
        }

        data.setLinkedChest(BlockReference.fromLocation(target.getLocation()));
        this.freeframe.getFrameRegistry().saveToConfig();
        sender.sendMessage(this.freeframe.formatMessage("%prefix% &aLinked inventory block to &e" + data.getId() + "&a."));
        this.logAdminAction(sender, "linkchest", data.getId() + "=" + data.getLinkedChest().serialize());
    }

    private void handleStats(CommandSender sender, String[] args) {
        if (args.length < 3) {
            sender.sendMessage(this.freeframe.formatMessage("%prefix% &cUsage: /freeframe stats <frame|player> <target>"));
            return;
        }

        String mode = args[1].toLowerCase(Locale.ENGLISH);
        Map<String, Long> stats;
        String target;

        if ("frame".equals(mode)) {
            FreeFrameData data = this.freeframe.getFrameRegistry().findById(args[2]);
            if (data == null) {
                sender.sendMessage(this.unknownFrameMessage(args[2]));
                return;
            }
            target = data.getId();
            stats = this.freeframe.getStatisticsService().getFrameStats(target);
        } else if ("player".equals(mode)) {
            OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(args[2]);
            target = offlinePlayer != null && offlinePlayer.getUniqueId() != null
                ? offlinePlayer.getUniqueId().toString()
                : args[2];
            stats = this.freeframe.getStatisticsService().getPlayerStats(target);
        } else {
            sender.sendMessage(this.freeframe.formatMessage("%prefix% &cUsage: /freeframe stats <frame|player> <target>"));
            return;
        }

        sender.sendMessage(this.freeframe.getMessage("freeframe.stats.header", "%prefix% &6Statistics for &e%target%&6:").replace("%target%", target));
        sender.sendMessage(this.freeframe.colorize("&8- &7Purchases: &e" + stats.get("purchases")));
        sender.sendMessage(this.freeframe.colorize("&8- &7Items: &e" + stats.get("items")));
        sender.sendMessage(this.freeframe.colorize("&8- &7Money (cents): &e" + stats.get("moneyCents")));
    }

    private void handleBackup(CommandSender sender) {
        File backup = this.freeframe.getBackupService().createBackup();
        if (backup == null) {
            sender.sendMessage(this.freeframe.getMessage("freeframe.backup.failed", "%prefix% &cBackup action failed."));
            return;
        }
        sender.sendMessage(this.freeframe.getMessage("freeframe.backup.created", "%prefix% &aBackup created: &e%file%&a.").replace("%file%", backup.getName()));
        this.logAdminAction(sender, "backup", backup.getName());
    }

    private void handleRestore(CommandSender sender, String[] args) {
        if (args.length < 2) {
            sender.sendMessage(this.freeframe.formatMessage("%prefix% &cUsage: /freeframe restore <file>"));
            return;
        }

        if (!this.freeframe.getBackupService().restoreBackup(args[1])) {
            sender.sendMessage(this.freeframe.getMessage("freeframe.backup.failed", "%prefix% &cBackup action failed."));
            return;
        }

        this.freeframe.getDisplayService().refreshAll(this.freeframe.getFrameRegistry().listFrames());
        sender.sendMessage(this.freeframe.getMessage("freeframe.backup.restored", "%prefix% &aBackup restored: &e%file%&a.").replace("%file%", args[1]));
        this.logAdminAction(sender, "restore", args[1]);
    }

    private void handleDoctor(CommandSender sender) {
        String result = this.freeframe.getBackupService().runDoctor();
        sender.sendMessage(this.freeframe.formatMessage("%prefix% &aDoctor finished: &e" + result));
        this.freeframe.getDisplayService().refreshAll(this.freeframe.getFrameRegistry().listFrames());
        this.logAdminAction(sender, "doctor", result);
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
        this.logAdminAction(sender, "wand", "received");
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
        this.logAdminAction(sender, "export", exported.getAbsolutePath());
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
        this.freeframe.reloadRuntimeState();
        boolean migrateOnSwitch = this.freeframe.getPluginConfig().getBoolean("freeframe.storage.migrateOnSwitch", true);
        if (migrateOnSwitch && this.freeframe.getFrameRegistry().size() == 0 && !backup.isEmpty()) {
            this.freeframe.getFrameRegistry().replaceAll(backup);
        }
        this.freeframe.getDisplayService().refreshAll(this.freeframe.getFrameRegistry().listFrames());

        sender.sendMessage(this.freeframe.formatMessage("%prefix% &aStorage backend changed to &e" + requested + "&a."));
        this.logAdminAction(sender, "storage", requested);
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
            sender.sendMessage(this.freeframe.colorize("&7/freeframe &fsetprice <id> <price> [currency] &8- &7Set frame base price"));
            sender.sendMessage(this.freeframe.colorize("&7/freeframe &fsetstock <id> <stock> [max] &8- &7Set stock values"));
            sender.sendMessage(this.freeframe.colorize("&7/freeframe &fsettype <id> <type> &8- &7Set frame type"));
            sender.sendMessage(this.freeframe.colorize("&7/freeframe &fsetprofile <id> <slot> <amount> <price> [name] &8- &7Set a buy profile"));
            sender.sendMessage(this.freeframe.colorize("&7/freeframe &fclearprofiles <id> &8- &7Reset buy profiles"));
            sender.sendMessage(this.freeframe.colorize("&7/freeframe &flinkchest <id> &8- &7Link looked-at chest/inventory"));
            sender.sendMessage(this.freeframe.colorize("&7/freeframe &fstats <frame|player> <target> &8- &7Show purchase stats"));
            sender.sendMessage(this.freeframe.colorize("&7/freeframe &fbackup &8- &7Create a backup"));
            sender.sendMessage(this.freeframe.colorize("&7/freeframe &frestore <file> &8- &7Restore a backup"));
            sender.sendMessage(this.freeframe.colorize("&7/freeframe &fdoctor &8- &7Run repair/health checks"));
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

    private void logAdminAction(CommandSender sender, String action, String details) {
        this.freeframe.getAuditLogger().logAdminAction(sender, action, details);
        this.freeframe.getWebhookExportService().sendAdminAction(sender, action, details);
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

    private List<String> completeBackupFiles(String currentInput) {
        List<String> completions = new ArrayList<String>();
        File folder = new File(this.freeframe.getDataFolder(), "backups");
        File[] files = folder.listFiles();
        if (files == null) {
            return completions;
        }

        String lowered = currentInput == null ? "" : currentInput.toLowerCase(Locale.ENGLISH);
        for (File file : files) {
            String name = file.getName();
            if (name.toLowerCase(Locale.ENGLISH).startsWith(lowered)) {
                completions.add(name);
            }
        }
        Collections.sort(completions);
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

    private String joinArgs(String[] args, int startIndex) {
        StringBuilder builder = new StringBuilder();
        for (int index = startIndex; index < args.length; index++) {
            if (builder.length() > 0) {
                builder.append(' ');
            }
            builder.append(args[index]);
        }
        return builder.toString();
    }

    private String describeProfiles(List<PurchaseProfile> profiles, String currency) {
        if (profiles == null || profiles.isEmpty()) {
            return "none";
        }

        List<String> parts = new ArrayList<String>();
        String effectiveCurrency = currency == null ? "$" : currency;
        for (PurchaseProfile profile : profiles) {
            parts.add(
                "#" + profile.getSlot()
                    + "=" + profile.getAmount() + "x@" + effectiveCurrency + this.formatPrice(profile.getPrice())
            );
        }
        return String.join(", ", parts);
    }

    private String formatEpochMillis(long epochMillis) {
        return new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.ENGLISH).format(new Date(epochMillis));
    }

    private String formatPrice(double price) {
        return String.format(Locale.ENGLISH, "%.2f", Math.max(0.0D, price));
    }
}
