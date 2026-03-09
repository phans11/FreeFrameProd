package de.isolveproblems.freeframe.commands;

import de.isolveproblems.freeframe.FreeFrame;
import de.isolveproblems.freeframe.api.BuyerRiskProfile;
import de.isolveproblems.freeframe.api.FrameType;
import de.isolveproblems.freeframe.api.MigrationPreview;
import de.isolveproblems.freeframe.api.PurchaseProfile;
import de.isolveproblems.freeframe.api.RestockRouteReport;
import de.isolveproblems.freeframe.api.SaleMode;
import de.isolveproblems.freeframe.api.ShopOwnerType;
import de.isolveproblems.freeframe.utils.BlockReference;
import de.isolveproblems.freeframe.utils.FrameReference;
import de.isolveproblems.freeframe.utils.FrameRepairReport;
import de.isolveproblems.freeframe.utils.FrameRegistry;
import de.isolveproblems.freeframe.utils.FreeFrameData;
import de.isolveproblems.freeframe.utils.JournalReplayReport;
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
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;

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
        if ("bid".equals(subCommand)) {
            this.handleBid(sender, args);
            return true;
        }
        if ("myshops".equals(subCommand)) {
            this.handleMyShops(sender);
            return true;
        }

        if ("list".equals(subCommand)) {
            if (!this.hasAdminPermission(sender)) {
                sender.sendMessage(this.freeframe.getErrorPermissionMessage());
                return true;
            }
            this.handleList(sender, args);
            return true;
        }
        if ("inspect".equals(subCommand)) {
            if (!this.hasAdminPermission(sender)) {
                sender.sendMessage(this.freeframe.getErrorPermissionMessage());
                return true;
            }
            this.handleInspect(sender, args);
            return true;
        }
        if ("remove".equals(subCommand)) {
            if (!this.hasAdminPermission(sender)) {
                sender.sendMessage(this.freeframe.getErrorPermissionMessage());
                return true;
            }
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
        if ("shoptype".equals(subCommand)) {
            if (!this.hasAdminPermission(sender)) {
                sender.sendMessage(this.freeframe.getErrorPermissionMessage());
                return true;
            }
            this.handleShopType(sender, args);
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
        if ("network".equals(subCommand)) {
            this.handleNetwork(sender, args);
            return true;
        }
        if ("season".equals(subCommand)) {
            this.handleSeason(sender, args);
            return true;
        }
        if ("auction".equals(subCommand)) {
            this.handleAuction(sender, args);
            return true;
        }
        if ("stats".equals(subCommand)) {
            this.handleStats(sender, args);
            return true;
        }
        if ("analytics".equals(subCommand)) {
            this.handleAnalytics(sender, args);
            return true;
        }
        if ("restockroute".equals(subCommand)) {
            this.handleRestockRoute(sender, args);
            return true;
        }
        if ("trust".equals(subCommand)) {
            this.handleTrust(sender, args);
            return true;
        }
        if ("brand".equals(subCommand)) {
            this.handleBrand(sender, args);
            return true;
        }
        if ("campaign".equals(subCommand)) {
            this.handleCampaign(sender, args);
            return true;
        }
        if ("moderate".equals(subCommand)) {
            this.handleModeration(sender, args);
            return true;
        }
        if ("sync".equals(subCommand)) {
            this.handleSync(sender, args);
            return true;
        }
        if ("platform".equals(subCommand)) {
            this.handlePlatform(sender);
            return true;
        }
        if ("zdm".equals(subCommand)) {
            this.handleZeroDowntimeMigration(sender, args);
            return true;
        }
        if ("backup".equals(subCommand)) {
            if (!this.hasAdminPermission(sender)) {
                sender.sendMessage(this.freeframe.getErrorPermissionMessage());
                return true;
            }
            this.handleBackup(sender);
            return true;
        }
        if ("restore".equals(subCommand)) {
            if (!this.hasAdminPermission(sender)) {
                sender.sendMessage(this.freeframe.getErrorPermissionMessage());
                return true;
            }
            this.handleRestore(sender, args);
            return true;
        }
        if ("doctor".equals(subCommand)) {
            if (!this.hasAdminPermission(sender)) {
                sender.sendMessage(this.freeframe.getErrorPermissionMessage());
                return true;
            }
            this.handleDoctor(sender);
            return true;
        }
        if ("replay".equals(subCommand)) {
            if (!this.hasAdminPermission(sender)) {
                sender.sendMessage(this.freeframe.getErrorPermissionMessage());
                return true;
            }
            this.handleReplay(sender, args);
            return true;
        }
        if ("wand".equals(subCommand)) {
            if (!this.hasAdminPermission(sender)) {
                sender.sendMessage(this.freeframe.getErrorPermissionMessage());
                return true;
            }
            this.handleWand(sender);
            return true;
        }
        if ("export".equals(subCommand)) {
            if (!this.hasAdminPermission(sender)) {
                sender.sendMessage(this.freeframe.getErrorPermissionMessage());
                return true;
            }
            this.handleExport(sender);
            return true;
        }
        if ("storage".equals(subCommand)) {
            if (!this.hasAdminPermission(sender)) {
                sender.sendMessage(this.freeframe.getErrorPermissionMessage());
                return true;
            }
            this.handleStorage(sender, args);
            return true;
        }
        if ("migrate".equals(subCommand)) {
            if (!this.hasAdminPermission(sender)) {
                sender.sendMessage(this.freeframe.getErrorPermissionMessage());
                return true;
            }
            int migrated = this.freeframe.getFrameRegistry().migrateLegacyFrames();
            this.freeframe.getDisplayService().refreshAll(this.freeframe.getFrameRegistry().listFrames());
            sender.sendMessage(this.freeframe.formatMessage("%prefix% &aMigration finished. Migrated entries: &e" + migrated + "&a."));
            this.logAction(sender, "migrate", "migrated=" + migrated);
            return true;
        }
        if ("repair".equals(subCommand)) {
            if (!this.hasAdminPermission(sender)) {
                sender.sendMessage(this.freeframe.getErrorPermissionMessage());
                return true;
            }
            FrameRepairReport report = this.freeframe.getFrameRegistry().repairFrames();
            this.freeframe.getDisplayService().refreshAll(this.freeframe.getFrameRegistry().listFrames());
            sender.sendMessage(this.freeframe.formatMessage(
                "%prefix% &aRepair finished. Removed invalid: &e" + report.getRemovedInvalidFrames()
                    + "&a, duplicates: &e" + report.getRemovedDuplicates()
                    + "&a, normalized: &e" + report.getNormalizedFrames() + "&a."
            ));
            this.logAction(sender, "repair", "invalid=" + report.getRemovedInvalidFrames());
            return true;
        }
        if ("debug".equals(subCommand)) {
            if (!this.hasAdminPermission(sender)) {
                sender.sendMessage(this.freeframe.getErrorPermissionMessage());
                return true;
            }
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
            this.addCompletion(completions, "bid", args[0]);
            this.addCompletion(completions, "myshops", args[0]);

            if (sender.hasPermission(this.freeframe.getConfigHandler().getReloadPermissionNode())) {
                this.addCompletion(completions, "reload", args[0]);
            }

            this.addCompletion(completions, "setprice", args[0]);
            this.addCompletion(completions, "setstock", args[0]);
            this.addCompletion(completions, "setprofile", args[0]);
            this.addCompletion(completions, "clearprofiles", args[0]);
            this.addCompletion(completions, "linkchest", args[0]);
            this.addCompletion(completions, "network", args[0]);
            this.addCompletion(completions, "season", args[0]);
            this.addCompletion(completions, "auction", args[0]);
            this.addCompletion(completions, "stats", args[0]);
            this.addCompletion(completions, "analytics", args[0]);
            this.addCompletion(completions, "restockroute", args[0]);
            this.addCompletion(completions, "brand", args[0]);
            this.addCompletion(completions, "campaign", args[0]);
            this.addCompletion(completions, "sync", args[0]);
            this.addCompletion(completions, "platform", args[0]);

            if (this.hasAdminPermission(sender)) {
                this.addCompletion(completions, "list", args[0]);
                this.addCompletion(completions, "inspect", args[0]);
                this.addCompletion(completions, "remove", args[0]);
                this.addCompletion(completions, "settype", args[0]);
                this.addCompletion(completions, "shoptype", args[0]);
                this.addCompletion(completions, "backup", args[0]);
                this.addCompletion(completions, "restore", args[0]);
                this.addCompletion(completions, "doctor", args[0]);
                this.addCompletion(completions, "replay", args[0]);
                this.addCompletion(completions, "wand", args[0]);
                this.addCompletion(completions, "export", args[0]);
                this.addCompletion(completions, "storage", args[0]);
                this.addCompletion(completions, "migrate", args[0]);
                this.addCompletion(completions, "repair", args[0]);
                this.addCompletion(completions, "debug", args[0]);
                this.addCompletion(completions, "trust", args[0]);
                this.addCompletion(completions, "moderate", args[0]);
                this.addCompletion(completions, "zdm", args[0]);
            }
            return completions;
        }

        String subCommand = args[0].toLowerCase(Locale.ENGLISH);
        if (args.length == 2 && this.equalsAny(subCommand, "inspect", "remove", "setprice", "setstock", "settype", "setprofile", "clearprofiles", "linkchest", "shoptype", "bid", "restockroute")) {
            return this.completeFrameIds(args[1]);
        }
        if (args.length == 2 && "restore".equals(subCommand)) {
            return this.completeBackupFiles(args[1]);
        }
        if (args.length == 2 && "auction".equals(subCommand)) {
            List<String> completions = new ArrayList<String>();
            this.addCompletion(completions, "start", args[1]);
            this.addCompletion(completions, "stop", args[1]);
            this.addCompletion(completions, "info", args[1]);
            return completions;
        }
        if (args.length == 2 && "network".equals(subCommand)) {
            List<String> completions = new ArrayList<String>();
            this.addCompletion(completions, "set", args[1]);
            this.addCompletion(completions, "clear", args[1]);
            this.addCompletion(completions, "info", args[1]);
            return completions;
        }
        if (args.length == 2 && "season".equals(subCommand)) {
            List<String> completions = new ArrayList<String>();
            this.addCompletion(completions, "set", args[1]);
            this.addCompletion(completions, "clear", args[1]);
            this.addCompletion(completions, "info", args[1]);
            return completions;
        }
        if (args.length == 3 && "auction".equals(subCommand) && this.equalsAny(args[1], "start", "stop", "info")) {
            return this.completeFrameIds(args[2]);
        }
        if (args.length == 3 && "network".equals(subCommand) && this.equalsAny(args[1], "set", "clear")) {
            return this.completeFrameIds(args[2]);
        }
        if (args.length == 3 && "season".equals(subCommand) && this.equalsAny(args[1], "set", "clear", "info")) {
            return this.completeFrameIds(args[2]);
        }
        if (args.length == 3 && "settype".equals(subCommand)) {
            List<String> completions = new ArrayList<String>();
            for (FrameType type : FrameType.values()) {
                this.addCompletion(completions, type.name().toLowerCase(Locale.ENGLISH), args[2]);
            }
            return completions;
        }
        if (args.length == 3 && "shoptype".equals(subCommand)) {
            List<String> completions = new ArrayList<String>();
            this.addCompletion(completions, "admin", args[2]);
            this.addCompletion(completions, "user", args[2]);
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
        if (args.length == 2 && "analytics".equals(subCommand)) {
            List<String> completions = new ArrayList<String>();
            this.addCompletion(completions, "global", args[1]);
            this.addCompletion(completions, "frame", args[1]);
            this.addCompletion(completions, "player", args[1]);
            return completions;
        }
        if (args.length == 3 && "analytics".equals(subCommand) && "frame".equalsIgnoreCase(args[1])) {
            return this.completeFrameIds(args[2]);
        }
        if (args.length == 3 && "analytics".equals(subCommand) && "player".equalsIgnoreCase(args[1])) {
            return this.completeOnlinePlayerNames(args[2]);
        }
        if (args.length == 2 && "trust".equals(subCommand)) {
            List<String> completions = new ArrayList<String>();
            this.addCompletion(completions, "info", args[1]);
            this.addCompletion(completions, "set", args[1]);
            this.addCompletion(completions, "clear", args[1]);
            return completions;
        }
        if (args.length == 3 && "trust".equals(subCommand) && this.equalsAny(args[1], "info", "set", "clear")) {
            return this.completeOnlinePlayerNames(args[2]);
        }
        if (args.length == 2 && "brand".equals(subCommand)) {
            List<String> completions = new ArrayList<String>();
            this.addCompletion(completions, "set", args[1]);
            this.addCompletion(completions, "clear", args[1]);
            this.addCompletion(completions, "info", args[1]);
            return completions;
        }
        if (args.length == 3 && "brand".equals(subCommand) && this.equalsAny(args[1], "set", "clear", "info")) {
            return this.completeFrameIds(args[2]);
        }
        if (args.length == 2 && "campaign".equals(subCommand)) {
            List<String> completions = new ArrayList<String>();
            this.addCompletion(completions, "set", args[1]);
            this.addCompletion(completions, "clear", args[1]);
            this.addCompletion(completions, "info", args[1]);
            return completions;
        }
        if (args.length == 3 && "campaign".equals(subCommand) && this.equalsAny(args[1], "set", "clear", "info")) {
            return this.completeFrameIds(args[2]);
        }
        if (args.length == 2 && "moderate".equals(subCommand)) {
            List<String> completions = new ArrayList<String>();
            this.addCompletion(completions, "frame", args[1]);
            this.addCompletion(completions, "player", args[1]);
            this.addCompletion(completions, "log", args[1]);
            return completions;
        }
        if (args.length == 3 && "moderate".equals(subCommand) && "frame".equalsIgnoreCase(args[1])) {
            List<String> completions = new ArrayList<String>();
            this.addCompletion(completions, "freeze", args[2]);
            this.addCompletion(completions, "unfreeze", args[2]);
            this.addCompletion(completions, "status", args[2]);
            return completions;
        }
        if (args.length == 4 && "moderate".equals(subCommand) && "frame".equalsIgnoreCase(args[1])) {
            return this.completeFrameIds(args[3]);
        }
        if (args.length == 3 && "moderate".equals(subCommand) && "player".equalsIgnoreCase(args[1])) {
            List<String> completions = new ArrayList<String>();
            this.addCompletion(completions, "restrict", args[2]);
            this.addCompletion(completions, "unrestrict", args[2]);
            this.addCompletion(completions, "status", args[2]);
            return completions;
        }
        if (args.length == 4 && "moderate".equals(subCommand) && "player".equalsIgnoreCase(args[1])) {
            return this.completeOnlinePlayerNames(args[3]);
        }
        if (args.length == 2 && "sync".equals(subCommand)) {
            List<String> completions = new ArrayList<String>();
            this.addCompletion(completions, "status", args[1]);
            this.addCompletion(completions, "push", args[1]);
            return completions;
        }
        if (args.length == 3 && "sync".equals(subCommand) && "push".equalsIgnoreCase(args[1])) {
            return this.completeFrameIds(args[2]);
        }
        if (args.length == 2 && "zdm".equals(subCommand)) {
            List<String> completions = new ArrayList<String>();
            this.addCompletion(completions, "plan", args[1]);
            this.addCompletion(completions, "apply", args[1]);
            return completions;
        }
        if (args.length == 2 && "storage".equals(subCommand)) {
            List<String> completions = new ArrayList<String>();
            this.addCompletion(completions, "yaml", args[1]);
            this.addCompletion(completions, "sqlite", args[1]);
            this.addCompletion(completions, "mysql", args[1]);
            return completions;
        }
        if (args.length == 2 && "replay".equals(subCommand)) {
            List<String> completions = new ArrayList<String>();
            this.addCompletion(completions, "dry", args[1]);
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
        this.logAction(sender, "reload", "migrated=" + migrated);
        return true;
    }

    private void handleBid(CommandSender sender, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(this.freeframe.formatMessage("%prefix% &cOnly players can place bids."));
            return;
        }
        if (args.length < 3) {
            sender.sendMessage(this.freeframe.formatMessage("%prefix% &cUsage: /freeframe bid <id> <amount>"));
            return;
        }

        FreeFrameData data = this.freeframe.getFrameRegistry().findById(args[1]);
        if (data == null) {
            sender.sendMessage(this.unknownFrameMessage(args[1]));
            return;
        }

        double bid;
        try {
            bid = Double.parseDouble(args[2]);
        } catch (NumberFormatException exception) {
            sender.sendMessage(this.freeframe.formatMessage("%prefix% &cBid must be numeric."));
            return;
        }
        if (bid <= 0.0D) {
            sender.sendMessage(this.freeframe.formatMessage("%prefix% &cBid must be > 0."));
            return;
        }

        String response = this.freeframe.getAuctionService().placeBid((Player) sender, data, bid);
        sender.sendMessage(response);
        this.logAction(sender, "bid", data.getId() + "=" + bid);
    }

    private void handleMyShops(CommandSender sender) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(this.freeframe.formatMessage("%prefix% &cOnly players have own shops."));
            return;
        }

        Player player = (Player) sender;
        List<FreeFrameData> all = this.freeframe.getFrameRegistry().listFrames();
        sender.sendMessage(this.freeframe.getPrefix());
        int count = 0;
        for (FreeFrameData data : all) {
            if (!data.isOwnedBy(player.getUniqueId().toString())) {
                continue;
            }
            count++;
            sender.sendMessage(this.freeframe.colorize(
                "&8- &e" + data.getId()
                    + " &8| &7type: &f" + data.getFrameType().name()
                    + " &8| &7ownerType: &f" + data.getShopOwnerType().name()
                    + " &8| &7mode: &f" + data.getSaleMode().name()
                    + " &8| &7price: &f" + data.getCurrency() + this.formatPrice(data.getPrice())
                    + " &8| &7stock: &f" + data.getStock() + "/" + data.getMaxStock()
            ));
        }
        sender.sendMessage(this.freeframe.colorize("&6Owned shops: &e" + count));
        sender.sendMessage(this.freeframe.getPrefix());
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
                    + " &8| &7ownerType: &f" + data.getShopOwnerType().name()
                    + " &8| &7mode: &f" + data.getSaleMode().name()
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
        sender.sendMessage(this.freeframe.colorize("&6Shop Owner Type: &e" + data.getShopOwnerType().name()));
        sender.sendMessage(this.freeframe.colorize("&6Item: &e" + data.getItemType()));
        sender.sendMessage(this.freeframe.colorize("&6Type: &e" + data.getFrameType().name()));
        sender.sendMessage(this.freeframe.colorize("&6Sale Mode: &e" + data.getSaleMode().name()));
        sender.sendMessage(this.freeframe.colorize("&6Network: &e" + (data.getNetworkId().isEmpty() ? "-" : data.getNetworkId())));
        sender.sendMessage(this.freeframe.colorize("&6Season Rule: &e" + (data.getSeasonRuleId().isEmpty() ? "-" : data.getSeasonRuleId())));
        sender.sendMessage(this.freeframe.colorize("&6Branding: &e" + (data.getBrandingId().isEmpty() ? "-" : data.getBrandingId())));
        sender.sendMessage(this.freeframe.colorize("&6Campaign: &e" + (data.getCampaignId().isEmpty() ? "-" : data.getCampaignId())));
        sender.sendMessage(this.freeframe.colorize("&6Base Price: &e" + data.getCurrency() + this.formatPrice(data.getPrice())));
        sender.sendMessage(this.freeframe.colorize("&6Stock: &e" + data.getStock() + "/" + data.getMaxStock()));
        sender.sendMessage(this.freeframe.colorize("&6Auto Refill: &e" + data.isAutoRefill() + " &7(" + data.getRefillIntervalMillis() + "ms)"));
        sender.sendMessage(this.freeframe.colorize("&6Revenue: &e" + data.getCurrency() + this.formatPrice(data.getRevenueTotal())));
        sender.sendMessage(this.freeframe.colorize("&6Collected Tax: &e" + data.getCurrency() + this.formatPrice(data.getCollectedTaxTotal())));
        sender.sendMessage(this.freeframe.colorize("&6Linked Chest: &e" + linkedChest));
        sender.sendMessage(this.freeframe.colorize("&6Profiles: &e" + this.describeProfiles(data.getPurchaseProfiles(), data.getCurrency())));
        sender.sendMessage(this.freeframe.colorize("&6Auction: &e" + this.freeframe.getAuctionService().describe(data)));
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
        this.logAction(sender, "remove", id.toLowerCase(Locale.ENGLISH));
    }

    private void handleSetPrice(CommandSender sender, String[] args) {
        if (args.length < 3) {
            sender.sendMessage(this.freeframe.formatMessage("%prefix% &cUsage: /freeframe setprice <id> <price> [currency]"));
            return;
        }

        FreeFrameData data = this.requireManagedFrame(sender, args[1], true);
        if (data == null) {
            return;
        }

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

        String currency = args.length >= 4 ? args[3] : data.getCurrency();
        data.setPrice(price);
        if (currency != null && !currency.trim().isEmpty()) {
            data.setCurrency(currency.trim());
        }
        this.freeframe.getFrameRegistry().saveToConfig();
        this.freeframe.getDisplayService().refresh(data);
        sender.sendMessage(this.freeframe.formatMessage("%prefix% &aUpdated price for &e" + data.getId() + " &ato &e" + data.getCurrency() + this.formatPrice(data.getPrice()) + "&a."));
        this.logAction(sender, "setprice", data.getId() + "=" + data.getPrice());
    }

    private void handleSetStock(CommandSender sender, String[] args) {
        if (args.length < 3) {
            sender.sendMessage(this.freeframe.formatMessage("%prefix% &cUsage: /freeframe setstock <id> <stock> [max]"));
            return;
        }

        FreeFrameData data = this.requireManagedFrame(sender, args[1], true);
        if (data == null) {
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
        this.logAction(sender, "setstock", data.getId() + "=" + data.getStock() + "/" + data.getMaxStock());
    }

    private void handleSetType(CommandSender sender, String[] args) {
        if (args.length < 3) {
            sender.sendMessage(this.freeframe.formatMessage("%prefix% &cUsage: /freeframe settype <id> <free|shop|limited|admin_only|preview_only>"));
            return;
        }

        FreeFrameData data = this.requireManagedFrame(sender, args[1], true);
        if (data == null) {
            return;
        }

        FrameType type = FrameType.fromString(args[2]);
        data.setFrameType(type);
        this.freeframe.getFrameRegistry().saveToConfig();
        this.freeframe.getDisplayService().refresh(data);
        sender.sendMessage(this.freeframe.formatMessage("%prefix% &aUpdated type for &e" + data.getId() + " &ato &e" + type.name() + "&a."));
        this.logAction(sender, "settype", data.getId() + "=" + type.name());
    }

    private void handleShopType(CommandSender sender, String[] args) {
        if (args.length < 3) {
            sender.sendMessage(this.freeframe.formatMessage("%prefix% &cUsage: /freeframe shoptype <id> <admin|user>"));
            return;
        }

        FreeFrameData data = this.freeframe.getFrameRegistry().findById(args[1]);
        if (data == null) {
            sender.sendMessage(this.unknownFrameMessage(args[1]));
            return;
        }

        ShopOwnerType ownerType = ShopOwnerType.fromString(args[2]);
        data.setShopOwnerType(ownerType);
        this.freeframe.getFrameRegistry().saveToConfig();
        this.freeframe.getDisplayService().refresh(data);
        sender.sendMessage(this.freeframe.formatMessage("%prefix% &aUpdated shop owner type for &e" + data.getId() + " &ato &e" + ownerType.name() + "&a."));
        this.logAction(sender, "shoptype", data.getId() + "=" + ownerType.name());
    }

    private void handleSetProfile(CommandSender sender, String[] args) {
        if (args.length < 5) {
            sender.sendMessage(this.freeframe.formatMessage("%prefix% &cUsage: /freeframe setprofile <id> <slot> <amount> <price> [displayName]"));
            return;
        }

        FreeFrameData data = this.requireManagedFrame(sender, args[1], true);
        if (data == null) {
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
        this.logAction(sender, "setprofile", data.getId() + " slot=" + slot + " amount=" + amount + " price=" + price);
    }

    private void handleClearProfiles(CommandSender sender, String[] args) {
        if (args.length < 2) {
            sender.sendMessage(this.freeframe.formatMessage("%prefix% &cUsage: /freeframe clearprofiles <id>"));
            return;
        }

        FreeFrameData data = this.requireManagedFrame(sender, args[1], true);
        if (data == null) {
            return;
        }

        data.setPurchaseProfiles(this.freeframe.getDefaultPurchaseProfiles(data.getPrice()));
        this.freeframe.getFrameRegistry().saveToConfig();
        this.freeframe.getDisplayService().refresh(data);
        sender.sendMessage(this.freeframe.formatMessage("%prefix% &aReset profiles for &e" + data.getId() + "&a."));
        this.logAction(sender, "clearprofiles", data.getId());
    }

    @SuppressWarnings("unchecked")
    private void handleLinkChest(CommandSender sender, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(this.freeframe.formatMessage("%prefix% &cOnly players can link a chest."));
            return;
        }
        if (args.length < 2) {
            sender.sendMessage(this.freeframe.formatMessage("%prefix% &cUsage: /freeframe linkchest <id>"));
            return;
        }

        FreeFrameData data = this.requireManagedFrame(sender, args[1], true);
        if (data == null) {
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
        this.logAction(sender, "linkchest", data.getId() + "=" + data.getLinkedChest().serialize());
    }

    private void handleNetwork(CommandSender sender, String[] args) {
        if (args.length < 2) {
            sender.sendMessage(this.freeframe.formatMessage("%prefix% &cUsage: /freeframe network <set|clear|info> ..."));
            return;
        }
        String action = args[1].toLowerCase(Locale.ENGLISH);
        if ("set".equals(action)) {
            if (args.length < 4) {
                sender.sendMessage(this.freeframe.formatMessage("%prefix% &cUsage: /freeframe network set <id> <networkId>"));
                return;
            }
            FreeFrameData data = this.requireManagedFrame(sender, args[2], true);
            if (data == null) {
                return;
            }
            String networkId = args[3].trim().toLowerCase(Locale.ENGLISH);
            if ("none".equals(networkId) || "null".equals(networkId)) {
                networkId = "";
            }
            data.setNetworkId(networkId);
            this.freeframe.getFrameRegistry().saveToConfig();
            this.freeframe.getDisplayService().refresh(data);
            sender.sendMessage(this.freeframe.formatMessage("%prefix% &aNetwork for &e" + data.getId() + " &aset to &e" + (networkId.isEmpty() ? "-" : networkId)));
            this.logAction(sender, "network-set", data.getId() + "=" + networkId);
            return;
        }
        if ("clear".equals(action)) {
            if (args.length < 3) {
                sender.sendMessage(this.freeframe.formatMessage("%prefix% &cUsage: /freeframe network clear <id>"));
                return;
            }
            FreeFrameData data = this.requireManagedFrame(sender, args[2], true);
            if (data == null) {
                return;
            }
            data.setNetworkId("");
            this.freeframe.getFrameRegistry().saveToConfig();
            this.freeframe.getDisplayService().refresh(data);
            sender.sendMessage(this.freeframe.formatMessage("%prefix% &aNetwork for &e" + data.getId() + " &acleared."));
            this.logAction(sender, "network-clear", data.getId());
            return;
        }
        if ("info".equals(action)) {
            if (args.length < 3) {
                sender.sendMessage(this.freeframe.formatMessage("%prefix% &cUsage: /freeframe network info <id|networkId>"));
                return;
            }
            FreeFrameData frame = this.freeframe.getFrameRegistry().findById(args[2]);
            String networkId = frame == null ? args[2].toLowerCase(Locale.ENGLISH) : frame.getNetworkId();
            int size = this.freeframe.getShopNetworkService().sizeOfNetwork(networkId);
            sender.sendMessage(this.freeframe.formatMessage("%prefix% &7Network &e" + networkId + " &7contains &e" + size + " &7frames."));
            return;
        }
        sender.sendMessage(this.freeframe.formatMessage("%prefix% &cUsage: /freeframe network <set|clear|info> ..."));
    }

    private void handleSeason(CommandSender sender, String[] args) {
        if (args.length < 2) {
            sender.sendMessage(this.freeframe.formatMessage("%prefix% &cUsage: /freeframe season <set|clear|info> ..."));
            return;
        }
        String action = args[1].toLowerCase(Locale.ENGLISH);
        if ("set".equals(action)) {
            if (args.length < 4) {
                sender.sendMessage(this.freeframe.formatMessage("%prefix% &cUsage: /freeframe season set <id> <ruleId>"));
                return;
            }
            FreeFrameData data = this.requireManagedFrame(sender, args[2], true);
            if (data == null) {
                return;
            }
            String ruleId = args[3].trim().toLowerCase(Locale.ENGLISH);
            if ("none".equals(ruleId) || "null".equals(ruleId)) {
                ruleId = "";
            }
            data.setSeasonRuleId(ruleId);
            this.freeframe.getFrameRegistry().saveToConfig();
            sender.sendMessage(this.freeframe.formatMessage("%prefix% &aSeason rule for &e" + data.getId() + " &aset to &e" + (ruleId.isEmpty() ? "-" : ruleId)));
            this.logAction(sender, "season-set", data.getId() + "=" + ruleId);
            return;
        }
        if ("clear".equals(action)) {
            if (args.length < 3) {
                sender.sendMessage(this.freeframe.formatMessage("%prefix% &cUsage: /freeframe season clear <id>"));
                return;
            }
            FreeFrameData data = this.requireManagedFrame(sender, args[2], true);
            if (data == null) {
                return;
            }
            data.setSeasonRuleId("");
            this.freeframe.getFrameRegistry().saveToConfig();
            sender.sendMessage(this.freeframe.formatMessage("%prefix% &aSeason rule for &e" + data.getId() + " &acleared."));
            this.logAction(sender, "season-clear", data.getId());
            return;
        }
        if ("info".equals(action)) {
            if (args.length < 3) {
                sender.sendMessage(this.freeframe.formatMessage("%prefix% &cUsage: /freeframe season info <id>"));
                return;
            }
            FreeFrameData data = this.freeframe.getFrameRegistry().findById(args[2]);
            if (data == null) {
                sender.sendMessage(this.unknownFrameMessage(args[2]));
                return;
            }
            String active = this.freeframe.getSeasonalRulesService().resolveRuleId(data, System.currentTimeMillis());
            sender.sendMessage(this.freeframe.formatMessage("%prefix% &7Configured rule: &e" + (data.getSeasonRuleId().isEmpty() ? "-" : data.getSeasonRuleId()) + " &7active now: &e" + (active.isEmpty() ? "-" : active)));
            return;
        }
        sender.sendMessage(this.freeframe.formatMessage("%prefix% &cUsage: /freeframe season <set|clear|info> ..."));
    }

    private void handleAuction(CommandSender sender, String[] args) {
        if (args.length < 2) {
            sender.sendMessage(this.freeframe.formatMessage("%prefix% &cUsage: /freeframe auction <start|stop|info> ..."));
            return;
        }
        String action = args[1].toLowerCase(Locale.ENGLISH);
        if ("start".equals(action)) {
            if (args.length < 4) {
                sender.sendMessage(this.freeframe.formatMessage("%prefix% &cUsage: /freeframe auction start <id> <minutes> [minBid]"));
                return;
            }
            FreeFrameData data = this.requireManagedFrame(sender, args[2], true);
            if (data == null) {
                return;
            }
            long minutes;
            try {
                minutes = Long.parseLong(args[3]);
            } catch (NumberFormatException exception) {
                sender.sendMessage(this.freeframe.formatMessage("%prefix% &cMinutes must be numeric."));
                return;
            }
            if (minutes <= 0L) {
                sender.sendMessage(this.freeframe.formatMessage("%prefix% &cMinutes must be > 0."));
                return;
            }
            double minBid = data.getPrice();
            if (args.length >= 5) {
                try {
                    minBid = Double.parseDouble(args[4]);
                } catch (NumberFormatException exception) {
                    sender.sendMessage(this.freeframe.formatMessage("%prefix% &cMinimum bid must be numeric."));
                    return;
                }
            }
            this.freeframe.getAuctionService().startAuction(data, minutes * 60_000L, minBid);
            this.freeframe.getDisplayService().refresh(data);
            sender.sendMessage(this.freeframe.formatMessage("%prefix% &aAuction started for &e" + data.getId() + "&a."));
            this.logAction(sender, "auction-start", data.getId() + " minutes=" + minutes + " minBid=" + minBid);
            return;
        }
        if ("stop".equals(action)) {
            if (args.length < 3) {
                sender.sendMessage(this.freeframe.formatMessage("%prefix% &cUsage: /freeframe auction stop <id>"));
                return;
            }
            FreeFrameData data = this.requireManagedFrame(sender, args[2], true);
            if (data == null) {
                return;
            }
            this.freeframe.getAuctionService().stopAuction(data);
            this.freeframe.getDisplayService().refresh(data);
            sender.sendMessage(this.freeframe.formatMessage("%prefix% &aAuction stopped for &e" + data.getId() + "&a."));
            this.logAction(sender, "auction-stop", data.getId());
            return;
        }
        if ("info".equals(action)) {
            if (args.length < 3) {
                sender.sendMessage(this.freeframe.formatMessage("%prefix% &cUsage: /freeframe auction info <id>"));
                return;
            }
            FreeFrameData data = this.freeframe.getFrameRegistry().findById(args[2]);
            if (data == null) {
                sender.sendMessage(this.unknownFrameMessage(args[2]));
                return;
            }
            sender.sendMessage(this.freeframe.formatMessage("%prefix% &7Auction: &e" + this.freeframe.getAuctionService().describe(data)));
            return;
        }
        sender.sendMessage(this.freeframe.formatMessage("%prefix% &cUsage: /freeframe auction <start|stop|info> ..."));
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
            if (!this.canManageOrAdmin(sender, data)) {
                sender.sendMessage(this.freeframe.getErrorPermissionMessage());
                return;
            }
            target = data.getId();
            stats = this.freeframe.getStatisticsService().getFrameStats(target);
        } else if ("player".equals(mode)) {
            if (!this.hasAdminPermission(sender)) {
                sender.sendMessage(this.freeframe.getErrorPermissionMessage());
                return;
            }
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

    private void handleAnalytics(CommandSender sender, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(this.freeframe.formatMessage("%prefix% &cOnly players can open analytics UI."));
            return;
        }
        if (!this.freeframe.getPluginConfig().getBoolean("freeframe.analytics.enabled", true)) {
            sender.sendMessage(this.freeframe.formatMessage("%prefix% &cAnalytics UI is disabled in config."));
            return;
        }

        Player player = (Player) sender;
        if (args.length < 2 || "global".equalsIgnoreCase(args[1])) {
            if (!this.hasAdminPermission(sender)) {
                sender.sendMessage(this.freeframe.getErrorPermissionMessage());
                return;
            }
            this.freeframe.getAnalyticsUiService().openGlobal(player);
            return;
        }

        String mode = args[1].toLowerCase(Locale.ENGLISH);
        if ("frame".equals(mode)) {
            if (args.length < 3) {
                sender.sendMessage(this.freeframe.formatMessage("%prefix% &cUsage: /freeframe analytics frame <id>"));
                return;
            }
            FreeFrameData data = this.requireManagedFrame(sender, args[2], true);
            if (data == null) {
                return;
            }
            this.freeframe.getAnalyticsUiService().openFrame(player, data);
            return;
        }
        if ("player".equals(mode)) {
            if (!this.hasAdminPermission(sender)) {
                sender.sendMessage(this.freeframe.getErrorPermissionMessage());
                return;
            }
            if (args.length < 3) {
                sender.sendMessage(this.freeframe.formatMessage("%prefix% &cUsage: /freeframe analytics player <name|uuid>"));
                return;
            }
            String playerId = this.resolvePlayerIdentifier(args[2]);
            this.freeframe.getAnalyticsUiService().openPlayer(player, playerId);
            return;
        }
        sender.sendMessage(this.freeframe.formatMessage("%prefix% &cUsage: /freeframe analytics <global|frame|player> [target]"));
    }

    private void handleRestockRoute(CommandSender sender, String[] args) {
        if (args.length < 2) {
            sender.sendMessage(this.freeframe.formatMessage("%prefix% &cUsage: /freeframe restockroute <id>"));
            return;
        }
        if (this.freeframe.getRestockRoutingService() == null) {
            sender.sendMessage(this.freeframe.formatMessage("%prefix% &cRestock routing service is unavailable."));
            return;
        }

        FreeFrameData data = this.requireManagedFrame(sender, args[1], true);
        if (data == null) {
            return;
        }

        Material material = Material.matchMaterial(data.getItemType());
        if (material == null || "AIR".equals(material.name())) {
            sender.sendMessage(this.freeframe.formatMessage("%prefix% &cCould not resolve item material for this frame."));
            return;
        }
        RestockRouteReport report = this.freeframe.getRestockRoutingService().previewRoute(data, new ItemStack(material, 1));
        sender.sendMessage(this.freeframe.formatMessage(
            "%prefix% &7Restock route for &e" + data.getId()
                + " &7requested=&e" + report.getRequested()
                + " &7moved=&e" + report.getMoved()
                + " &7missing=&e" + report.getMissing()
        ));
        if (report.getRouteNodes().isEmpty()) {
            sender.sendMessage(this.freeframe.formatMessage("%prefix% &8- &7No route nodes with matching stock found."));
            return;
        }
        for (String node : report.getRouteNodes()) {
            sender.sendMessage(this.freeframe.formatMessage("%prefix% &8- &7" + node));
        }
    }

    private void handleTrust(CommandSender sender, String[] args) {
        if (!this.hasAdminPermission(sender)) {
            sender.sendMessage(this.freeframe.getErrorPermissionMessage());
            return;
        }
        if (args.length < 3) {
            sender.sendMessage(this.freeframe.formatMessage("%prefix% &cUsage: /freeframe trust <info|set|clear> <player|uuid> [score]"));
            return;
        }

        String action = args[1].toLowerCase(Locale.ENGLISH);
        String playerId = this.resolvePlayerIdentifier(args[2]);
        if ("info".equals(action)) {
            BuyerRiskProfile profile = this.freeframe.getBuyerReputationService().inspect(playerId);
            sender.sendMessage(this.freeframe.formatMessage(
                "%prefix% &7Trust profile &e" + playerId
                    + " &7score=&e" + this.formatPrice(profile.getScore())
                    + " &7blocked=&e" + profile.isBlocked()
            ));
            return;
        }
        if ("set".equals(action)) {
            if (args.length < 4) {
                sender.sendMessage(this.freeframe.formatMessage("%prefix% &cUsage: /freeframe trust set <player|uuid> <score>"));
                return;
            }
            double score;
            try {
                score = Double.parseDouble(args[3]);
            } catch (NumberFormatException exception) {
                sender.sendMessage(this.freeframe.formatMessage("%prefix% &cScore must be numeric."));
                return;
            }
            this.freeframe.getBuyerReputationService().setManualRiskScore(playerId, score, sender.getName(), "manual-command");
            this.logAction(sender, "trust-set", playerId + "=" + score);
            sender.sendMessage(this.freeframe.formatMessage("%prefix% &aManual trust score set for &e" + playerId + "&a."));
            return;
        }
        if ("clear".equals(action)) {
            this.freeframe.getBuyerReputationService().clearManualRiskScore(playerId, sender.getName());
            this.logAction(sender, "trust-clear", playerId);
            sender.sendMessage(this.freeframe.formatMessage("%prefix% &aManual trust score cleared for &e" + playerId + "&a."));
            return;
        }
        sender.sendMessage(this.freeframe.formatMessage("%prefix% &cUsage: /freeframe trust <info|set|clear> <player|uuid> [score]"));
    }

    private void handleBrand(CommandSender sender, String[] args) {
        if (args.length < 2) {
            sender.sendMessage(this.freeframe.formatMessage("%prefix% &cUsage: /freeframe brand <set|clear|info> ..."));
            return;
        }
        String action = args[1].toLowerCase(Locale.ENGLISH);
        if ("set".equals(action)) {
            if (args.length < 4) {
                sender.sendMessage(this.freeframe.formatMessage("%prefix% &cUsage: /freeframe brand set <id> <themeId>"));
                return;
            }
            FreeFrameData data = this.requireManagedFrame(sender, args[2], true);
            if (data == null) {
                return;
            }
            String themeId = args[3].trim().toLowerCase(Locale.ENGLISH);
            if ("none".equals(themeId) || "null".equals(themeId)) {
                themeId = "";
            }
            data.setBrandingId(themeId);
            this.freeframe.getFrameRegistry().saveToConfig();
            this.freeframe.getDisplayService().refresh(data);
            this.logAction(sender, "brand-set", data.getId() + "=" + themeId);
            sender.sendMessage(this.freeframe.formatMessage("%prefix% &aBrand theme for &e" + data.getId() + " &aset to &e" + (themeId.isEmpty() ? "-" : themeId)));
            return;
        }
        if ("clear".equals(action)) {
            if (args.length < 3) {
                sender.sendMessage(this.freeframe.formatMessage("%prefix% &cUsage: /freeframe brand clear <id>"));
                return;
            }
            FreeFrameData data = this.requireManagedFrame(sender, args[2], true);
            if (data == null) {
                return;
            }
            data.setBrandingId("");
            this.freeframe.getFrameRegistry().saveToConfig();
            this.freeframe.getDisplayService().refresh(data);
            this.logAction(sender, "brand-clear", data.getId());
            sender.sendMessage(this.freeframe.formatMessage("%prefix% &aBrand theme for &e" + data.getId() + " &acleared."));
            return;
        }
        if ("info".equals(action)) {
            if (args.length < 3) {
                sender.sendMessage(this.freeframe.formatMessage("%prefix% &cUsage: /freeframe brand info <id>"));
                return;
            }
            FreeFrameData data = this.freeframe.getFrameRegistry().findById(args[2]);
            if (data == null) {
                sender.sendMessage(this.unknownFrameMessage(args[2]));
                return;
            }
            String resolved = this.freeframe.getBrandingService().resolveThemeId(data);
            sender.sendMessage(this.freeframe.formatMessage("%prefix% &7Branding for &e" + data.getId() + " &7configured=&e" + (data.getBrandingId().isEmpty() ? "-" : data.getBrandingId()) + " &7resolved=&e" + resolved));
            return;
        }
        sender.sendMessage(this.freeframe.formatMessage("%prefix% &cUsage: /freeframe brand <set|clear|info> ..."));
    }

    private void handleCampaign(CommandSender sender, String[] args) {
        if (args.length < 2) {
            sender.sendMessage(this.freeframe.formatMessage("%prefix% &cUsage: /freeframe campaign <set|clear|info> ..."));
            return;
        }
        String action = args[1].toLowerCase(Locale.ENGLISH);
        if ("set".equals(action)) {
            if (args.length < 4) {
                sender.sendMessage(this.freeframe.formatMessage("%prefix% &cUsage: /freeframe campaign set <id> <campaignId>"));
                return;
            }
            FreeFrameData data = this.requireManagedFrame(sender, args[2], true);
            if (data == null) {
                return;
            }
            String campaignId = args[3].trim().toLowerCase(Locale.ENGLISH);
            if ("none".equals(campaignId) || "null".equals(campaignId)) {
                campaignId = "";
            }
            data.setCampaignId(campaignId);
            this.freeframe.getFrameRegistry().saveToConfig();
            this.freeframe.getDisplayService().refresh(data);
            this.logAction(sender, "campaign-set", data.getId() + "=" + campaignId);
            sender.sendMessage(this.freeframe.formatMessage("%prefix% &aCampaign for &e" + data.getId() + " &aset to &e" + (campaignId.isEmpty() ? "-" : campaignId)));
            return;
        }
        if ("clear".equals(action)) {
            if (args.length < 3) {
                sender.sendMessage(this.freeframe.formatMessage("%prefix% &cUsage: /freeframe campaign clear <id>"));
                return;
            }
            FreeFrameData data = this.requireManagedFrame(sender, args[2], true);
            if (data == null) {
                return;
            }
            data.setCampaignId("");
            this.freeframe.getFrameRegistry().saveToConfig();
            this.freeframe.getDisplayService().refresh(data);
            this.logAction(sender, "campaign-clear", data.getId());
            sender.sendMessage(this.freeframe.formatMessage("%prefix% &aCampaign for &e" + data.getId() + " &acleared."));
            return;
        }
        if ("info".equals(action)) {
            if (args.length < 3) {
                sender.sendMessage(this.freeframe.formatMessage("%prefix% &cUsage: /freeframe campaign info <id>"));
                return;
            }
            FreeFrameData data = this.freeframe.getFrameRegistry().findById(args[2]);
            if (data == null) {
                sender.sendMessage(this.unknownFrameMessage(args[2]));
                return;
            }
            de.isolveproblems.freeframe.api.CampaignEffect effect = this.freeframe.getCampaignRuntimeService().resolve(data, System.currentTimeMillis());
            sender.sendMessage(this.freeframe.formatMessage(
                "%prefix% &7Campaign for &e" + data.getId()
                    + " &7configured=&e" + (data.getCampaignId().isEmpty() ? "-" : data.getCampaignId())
                    + " &7active=&e" + effect.isActive()
                    + " &7multiplier=&e" + this.formatPrice(effect.getPriceMultiplier())
            ));
            return;
        }
        sender.sendMessage(this.freeframe.formatMessage("%prefix% &cUsage: /freeframe campaign <set|clear|info> ..."));
    }

    private void handleModeration(CommandSender sender, String[] args) {
        if (!this.freeframe.getPluginConfig().getBoolean("freeframe.moderation.enabled", true)) {
            sender.sendMessage(this.freeframe.formatMessage("%prefix% &cModeration features are disabled."));
            return;
        }
        if (args.length < 2) {
            sender.sendMessage(this.freeframe.formatMessage("%prefix% &cUsage: /freeframe moderate <frame|player|log> ..."));
            return;
        }

        String mode = args[1].toLowerCase(Locale.ENGLISH);
        if ("frame".equals(mode)) {
            if (args.length < 4) {
                sender.sendMessage(this.freeframe.formatMessage("%prefix% &cUsage: /freeframe moderate frame <freeze|unfreeze|status> <id> [reason]"));
                return;
            }
            String action = args[2].toLowerCase(Locale.ENGLISH);
            FreeFrameData data = this.freeframe.getFrameRegistry().findById(args[3]);
            if (data == null) {
                sender.sendMessage(this.unknownFrameMessage(args[3]));
                return;
            }
            boolean ownerAllowed = this.freeframe.getPluginConfig().getBoolean("freeframe.moderation.allowOwnerFrameFreeze", true);
            boolean ownerCanManage = this.canManageOrAdmin(sender, data);
            if (!ownerCanManage || (!this.hasAdminPermission(sender) && !ownerAllowed)) {
                sender.sendMessage(this.freeframe.getErrorPermissionMessage());
                return;
            }

            if ("freeze".equals(action)) {
                String reason = args.length > 4 ? this.joinArgs(args, 4) : "manual";
                this.freeframe.getModerationService().freezeFrame(data, sender.getName(), reason);
                this.freeframe.getMetricsTracker().incrementModerationActions();
                this.logAction(sender, "moderate-freeze-frame", data.getId() + " reason=" + reason);
                sender.sendMessage(this.freeframe.formatMessage("%prefix% &aFrame &e" + data.getId() + " &afrozen."));
                return;
            }
            if ("unfreeze".equals(action)) {
                this.freeframe.getModerationService().unfreezeFrame(data.getId(), sender.getName());
                this.freeframe.getMetricsTracker().incrementModerationActions();
                this.logAction(sender, "moderate-unfreeze-frame", data.getId());
                sender.sendMessage(this.freeframe.formatMessage("%prefix% &aFrame &e" + data.getId() + " &aunfrozen."));
                return;
            }
            if ("status".equals(action)) {
                boolean frozen = this.freeframe.getModerationService().isFrameFrozen(data.getId());
                sender.sendMessage(this.freeframe.formatMessage(
                    "%prefix% &7Frame &e" + data.getId() + " &7frozen=&e" + frozen
                        + (frozen ? " &7reason=&e" + this.freeframe.getModerationService().frameRestrictionReason(data.getId()) : "")
                ));
                return;
            }
            sender.sendMessage(this.freeframe.formatMessage("%prefix% &cUsage: /freeframe moderate frame <freeze|unfreeze|status> <id> [reason]"));
            return;
        }

        if ("player".equals(mode)) {
            if (!this.hasAdminPermission(sender)) {
                sender.sendMessage(this.freeframe.getErrorPermissionMessage());
                return;
            }
            if (args.length < 4) {
                sender.sendMessage(this.freeframe.formatMessage("%prefix% &cUsage: /freeframe moderate player <restrict|unrestrict|status> <player|uuid> [reason]"));
                return;
            }
            String action = args[2].toLowerCase(Locale.ENGLISH);
            String playerId = this.resolvePlayerIdentifier(args[3]);
            if ("restrict".equals(action)) {
                String reason = args.length > 4 ? this.joinArgs(args, 4) : "manual";
                this.freeframe.getModerationService().restrictPlayer(playerId, sender.getName(), reason);
                this.freeframe.getMetricsTracker().incrementModerationActions();
                this.logAction(sender, "moderate-restrict-player", playerId + " reason=" + reason);
                sender.sendMessage(this.freeframe.formatMessage("%prefix% &aPlayer restricted: &e" + playerId));
                return;
            }
            if ("unrestrict".equals(action)) {
                this.freeframe.getModerationService().unrestrictPlayer(playerId, sender.getName());
                this.freeframe.getMetricsTracker().incrementModerationActions();
                this.logAction(sender, "moderate-unrestrict-player", playerId);
                sender.sendMessage(this.freeframe.formatMessage("%prefix% &aPlayer unrestricted: &e" + playerId));
                return;
            }
            if ("status".equals(action)) {
                boolean restricted = this.freeframe.getModerationService().isPlayerRestricted(playerId);
                sender.sendMessage(this.freeframe.formatMessage(
                    "%prefix% &7Player &e" + playerId + " &7restricted=&e" + restricted
                        + (restricted ? " &7reason=&e" + this.freeframe.getModerationService().playerRestrictionReason(playerId) : "")
                ));
                return;
            }
            sender.sendMessage(this.freeframe.formatMessage("%prefix% &cUsage: /freeframe moderate player <restrict|unrestrict|status> <player|uuid> [reason]"));
            return;
        }

        if ("log".equals(mode)) {
            if (!this.hasAdminPermission(sender)) {
                sender.sendMessage(this.freeframe.getErrorPermissionMessage());
                return;
            }
            int limit = 10;
            if (args.length >= 3) {
                try {
                    limit = Integer.parseInt(args[2]);
                } catch (NumberFormatException ignored) {
                    limit = 10;
                }
            }
            List<String> lines = this.freeframe.getModerationService().tailLog(limit);
            if (lines.isEmpty()) {
                sender.sendMessage(this.freeframe.formatMessage("%prefix% &7No moderation log entries."));
                return;
            }
            sender.sendMessage(this.freeframe.formatMessage("%prefix% &6Moderation log:"));
            for (String line : lines) {
                sender.sendMessage(this.freeframe.colorize("&8- &7" + line));
            }
            return;
        }

        sender.sendMessage(this.freeframe.formatMessage("%prefix% &cUsage: /freeframe moderate <frame|player|log> ..."));
    }

    private void handleSync(CommandSender sender, String[] args) {
        if (args.length < 2) {
            sender.sendMessage(this.freeframe.formatMessage("%prefix% &7Sync mode: &e" + this.freeframe.getNetworkSyncService().describeMode()));
            sender.sendMessage(this.freeframe.formatMessage("%prefix% &7Usage: /freeframe sync <status|push> [id]"));
            return;
        }
        String action = args[1].toLowerCase(Locale.ENGLISH);
        if ("status".equals(action)) {
            sender.sendMessage(this.freeframe.formatMessage("%prefix% &7Sync mode: &e" + this.freeframe.getNetworkSyncService().describeMode()));
            return;
        }
        if ("push".equals(action)) {
            if (args.length < 3) {
                sender.sendMessage(this.freeframe.formatMessage("%prefix% &cUsage: /freeframe sync push <id|all>"));
                return;
            }
            if ("all".equalsIgnoreCase(args[2])) {
                if (!this.hasAdminPermission(sender)) {
                    sender.sendMessage(this.freeframe.getErrorPermissionMessage());
                    return;
                }
                int pushed = 0;
                for (FreeFrameData data : this.freeframe.getFrameRegistry().listFrames()) {
                    this.freeframe.getNetworkSyncService().publishFrameUpdate(data, "manual-sync-all");
                    pushed++;
                }
                sender.sendMessage(this.freeframe.formatMessage("%prefix% &aPushed &e" + pushed + " &aframe updates to sync bridge."));
                this.logAction(sender, "sync-push-all", String.valueOf(pushed));
                return;
            }

            FreeFrameData data = this.requireManagedFrame(sender, args[2], true);
            if (data == null) {
                return;
            }
            this.freeframe.getNetworkSyncService().publishFrameUpdate(data, "manual-sync");
            sender.sendMessage(this.freeframe.formatMessage("%prefix% &aPushed sync update for &e" + data.getId() + "&a."));
            this.logAction(sender, "sync-push", data.getId());
            return;
        }
        sender.sendMessage(this.freeframe.formatMessage("%prefix% &cUsage: /freeframe sync <status|push> [id]"));
    }

    private void handlePlatform(CommandSender sender) {
        Map<String, String> capabilities = this.freeframe.getPlatformSupportService().describeCapabilities();
        sender.sendMessage(this.freeframe.getPrefix());
        sender.sendMessage(this.freeframe.colorize("&6Platform Capabilities:"));
        for (Map.Entry<String, String> entry : capabilities.entrySet()) {
            sender.sendMessage(this.freeframe.colorize("&8- &7" + entry.getKey() + ": &e" + entry.getValue()));
        }
        sender.sendMessage(this.freeframe.getPrefix());
    }

    private void handleZeroDowntimeMigration(CommandSender sender, String[] args) {
        if (!this.hasAdminPermission(sender)) {
            sender.sendMessage(this.freeframe.getErrorPermissionMessage());
            return;
        }
        if (!this.freeframe.getPluginConfig().getBoolean("freeframe.migration.zeroDowntime.enabled", true)) {
            sender.sendMessage(this.freeframe.formatMessage("%prefix% &cZero-downtime migration is disabled in config."));
            return;
        }
        if (args.length < 2) {
            sender.sendMessage(this.freeframe.formatMessage("%prefix% &cUsage: /freeframe zdm <plan|apply>"));
            return;
        }

        String action = args[1].toLowerCase(Locale.ENGLISH);
        MigrationPreview preview;
        if ("plan".equals(action)) {
            preview = this.freeframe.getZeroDowntimeMigrationService().preview();
            sender.sendMessage(this.freeframe.formatMessage(
                "%prefix% &7ZDM plan: scanned=&e" + preview.getScannedFrames()
                    + " &7needsBranding=&e" + preview.getNeedsBranding()
                    + " &7needsCampaign=&e" + preview.getNeedsCampaign()
                    + " &7needsNormalization=&e" + preview.getNeedsNormalization()
            ));
            this.logAction(sender, "zdm-plan", String.valueOf(preview.getScannedFrames()));
            return;
        }
        if ("apply".equals(action)) {
            preview = this.freeframe.getZeroDowntimeMigrationService().apply();
            sender.sendMessage(this.freeframe.formatMessage(
                "%prefix% &aZDM apply done: scanned=&e" + preview.getScannedFrames()
                    + " &aremainingBranding=&e" + preview.getNeedsBranding()
                    + " &aremainingCampaign=&e" + preview.getNeedsCampaign()
                    + " &aremainingNormalization=&e" + preview.getNeedsNormalization()
            ));
            this.logAction(sender, "zdm-apply", String.valueOf(preview.getScannedFrames()));
            return;
        }
        sender.sendMessage(this.freeframe.formatMessage("%prefix% &cUsage: /freeframe zdm <plan|apply>"));
    }

    private void handleBackup(CommandSender sender) {
        File backup = this.freeframe.getBackupService().createBackup();
        if (backup == null) {
            sender.sendMessage(this.freeframe.getMessage("freeframe.backup.failed", "%prefix% &cBackup action failed."));
            return;
        }
        sender.sendMessage(this.freeframe.getMessage("freeframe.backup.created", "%prefix% &aBackup created: &e%file%&a.").replace("%file%", backup.getName()));
        this.logAction(sender, "backup", backup.getName());
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
        this.logAction(sender, "restore", args[1]);
    }

    private void handleDoctor(CommandSender sender) {
        String result = this.freeframe.getBackupService().runDoctor();
        sender.sendMessage(this.freeframe.formatMessage("%prefix% &aDoctor finished: &e" + result));
        this.freeframe.getDisplayService().refreshAll(this.freeframe.getFrameRegistry().listFrames());
        this.logAction(sender, "doctor", result);
    }

    private void handleReplay(CommandSender sender, String[] args) {
        boolean dryRun = args.length > 1 && "dry".equalsIgnoreCase(args[1]);
        JournalReplayReport report = this.freeframe.replayTransactionJournal(dryRun);
        sender.sendMessage(this.freeframe.formatMessage(
            "%prefix% &aReplay finished (&e" + (dryRun ? "dry-run" : "apply") + "&a): entries=&e" + report.getTotalEntries()
                + " &a commits=&e" + report.getPurchaseCommits()
                + " &a duplicates=&e" + report.getDuplicateCommits()
                + " &a rebuilt=&e" + report.getRebuiltIdempotencyKeys()
        ));
        this.logAction(sender, "replay", "dry=" + dryRun + " entries=" + report.getTotalEntries());
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
        this.logAction(sender, "wand", "received");
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
        this.logAction(sender, "export", exported.getAbsolutePath());
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
        this.logAction(sender, "storage", requested);
    }

    private void handleDebug(CommandSender sender) {
        Map<String, Long> metrics = new LinkedHashMap<String, Long>(this.freeframe.getMetricsTracker().snapshot());
        FrameRegistry registry = this.freeframe.getFrameRegistry();

        sender.sendMessage(this.freeframe.getPrefix());
        sender.sendMessage(this.freeframe.colorize("&6Tracked Frames: &e" + registry.size()));
        sender.sendMessage(this.freeframe.colorize("&6Storage Backend: &e" + registry.getActiveStorageBackend()));
        sender.sendMessage(this.freeframe.colorize("&6Offer Mode: &e" + this.freeframe.getOfferMode().name()));
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
        sender.sendMessage(this.freeframe.colorize("&7/freeframe &fbid <id> <amount> &8- &7Place bid on auction frame"));
        sender.sendMessage(this.freeframe.colorize("&7/freeframe &fmyshops &8- &7List your owned shops"));
        sender.sendMessage(this.freeframe.colorize("&7/freeframe &fsetprice <id> <price> [currency] &8- &7Set frame base price (owner/admin)"));
        sender.sendMessage(this.freeframe.colorize("&7/freeframe &fsetstock <id> <stock> [max] &8- &7Set stock values (owner/admin)"));
        sender.sendMessage(this.freeframe.colorize("&7/freeframe &fsetprofile <id> <slot> <amount> <price> [name] &8- &7Set buy profile (owner/admin)"));
        sender.sendMessage(this.freeframe.colorize("&7/freeframe &fclearprofiles <id> &8- &7Reset buy profiles (owner/admin)"));
        sender.sendMessage(this.freeframe.colorize("&7/freeframe &flinkchest <id> &8- &7Link looked-at chest (owner/admin)"));
        sender.sendMessage(this.freeframe.colorize("&7/freeframe &fnetwork <set|clear|info> ... &8- &7Manage shop networks"));
        sender.sendMessage(this.freeframe.colorize("&7/freeframe &fseason <set|clear|info> ... &8- &7Manage seasonal rules"));
        sender.sendMessage(this.freeframe.colorize("&7/freeframe &fauction <start|stop|info> ... &8- &7Manage auctions"));
        sender.sendMessage(this.freeframe.colorize("&7/freeframe &fstats <frame|player> <target> &8- &7Show purchase stats"));
        sender.sendMessage(this.freeframe.colorize("&7/freeframe &fanalytics <global|frame|player> ... &8- &7Open analytics UI"));
        sender.sendMessage(this.freeframe.colorize("&7/freeframe &frestockroute <id> &8- &7Preview smart restock routes"));
        sender.sendMessage(this.freeframe.colorize("&7/freeframe &fbrand <set|clear|info> ... &8- &7Manage visual branding themes"));
        sender.sendMessage(this.freeframe.colorize("&7/freeframe &fcampaign <set|clear|info> ... &8- &7Manage campaign engine"));
        sender.sendMessage(this.freeframe.colorize("&7/freeframe &fsync <status|push> ... &8- &7Cross-server sync controls"));
        sender.sendMessage(this.freeframe.colorize("&7/freeframe &fplatform &8- &7Show platform compatibility status"));

        if (sender.hasPermission(this.freeframe.getConfigHandler().getReloadPermissionNode())) {
            sender.sendMessage(this.freeframe.colorize("&7/freeframe &freload &8- &7Reload config and frame cache"));
        }

        if (this.hasAdminPermission(sender)) {
            sender.sendMessage(this.freeframe.colorize("&7/freeframe &flist [page] &8- &7List tracked frames"));
            sender.sendMessage(this.freeframe.colorize("&7/freeframe &finspect <id> &8- &7Inspect frame metadata"));
            sender.sendMessage(this.freeframe.colorize("&7/freeframe &fremove <id> &8- &7Remove tracked frame"));
            sender.sendMessage(this.freeframe.colorize("&7/freeframe &fsettype <id> <type> &8- &7Set frame type"));
            sender.sendMessage(this.freeframe.colorize("&7/freeframe &fshoptype <id> <admin|user> &8- &7Set admin/user shop type"));
            sender.sendMessage(this.freeframe.colorize("&7/freeframe &fbackup|restore|doctor|replay &8- &7Recovery and journal tooling"));
            sender.sendMessage(this.freeframe.colorize("&7/freeframe &fstorage <backend> &8- &7Switch YAML/SQLite/MySQL"));
            sender.sendMessage(this.freeframe.colorize("&7/freeframe &fexport|migrate|repair|debug &8- &7Ops tools"));
            sender.sendMessage(this.freeframe.colorize("&7/freeframe &ftrust <info|set|clear> ... &8- &7Buyer reputation/fraud controls"));
            sender.sendMessage(this.freeframe.colorize("&7/freeframe &fmoderate <frame|player|log> ... &8- &7Compliance toolkit"));
            sender.sendMessage(this.freeframe.colorize("&7/freeframe &fzdm <plan|apply> &8- &7Zero-downtime migration tools"));
            sender.sendMessage(this.freeframe.colorize("&7/freeframe &fwand &8- &7Get setup wand"));
        }

        sender.sendMessage(this.freeframe.getPrefix());
    }

    private void sendInfo(CommandSender sender) {
        sender.sendMessage(this.freeframe.getPrefix());
        sender.sendMessage(this.freeframe.colorize("&6Plugin: &eFreeFrame"));
        sender.sendMessage(this.freeframe.colorize("&6Version: &e" + this.freeframe.getDescription().getVersion()));
        sender.sendMessage(this.freeframe.colorize("&6Backends: &e" + this.freeframe.getFrameRegistry().getActiveStorageBackend()));
        sender.sendMessage(this.freeframe.colorize("&6Offer Mode: &e" + this.freeframe.getOfferMode().name()));
        sender.sendMessage(this.freeframe.colorize("&6Platform: &e" + this.freeframe.getPlatformSupportService().detectRuntime()));
        sender.sendMessage(this.freeframe.colorize("&6Network Sync: &e" + this.freeframe.getNetworkSyncService().describeMode()));
        sender.sendMessage(this.freeframe.getPrefix());
    }

    private FreeFrameData requireManagedFrame(CommandSender sender, String id, boolean requireOwnerOrAdmin) {
        FreeFrameData data = this.freeframe.getFrameRegistry().findById(id);
        if (data == null) {
            sender.sendMessage(this.unknownFrameMessage(id));
            return null;
        }
        if (!requireOwnerOrAdmin) {
            return data;
        }
        if (!this.canManageOrAdmin(sender, data)) {
            sender.sendMessage(this.freeframe.getErrorPermissionMessage());
            return null;
        }
        return data;
    }

    private boolean canManageOrAdmin(CommandSender sender, FreeFrameData data) {
        if (this.hasAdminPermission(sender)) {
            return true;
        }
        if (!(sender instanceof Player)) {
            return false;
        }
        return this.freeframe.canPlayerManageFrame((Player) sender, data);
    }

    private void logAction(CommandSender sender, String action, String details) {
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

    private List<String> completeOnlinePlayerNames(String currentInput) {
        List<String> completions = new ArrayList<String>();
        String lowered = currentInput == null ? "" : currentInput.toLowerCase(Locale.ENGLISH);
        for (Player online : Bukkit.getOnlinePlayers()) {
            if (online == null || online.getName() == null) {
                continue;
            }
            String name = online.getName();
            if (name.toLowerCase(Locale.ENGLISH).startsWith(lowered)) {
                completions.add(name);
            }
        }
        Collections.sort(completions);
        return completions;
    }

    private String resolvePlayerIdentifier(String input) {
        if (input == null || input.trim().isEmpty()) {
            return "unknown";
        }
        String trimmed = input.trim();
        if (trimmed.contains("-") && trimmed.length() >= 32) {
            return trimmed;
        }

        Player online = Bukkit.getPlayerExact(trimmed);
        if (online != null && online.getUniqueId() != null) {
            return online.getUniqueId().toString();
        }

        OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(trimmed);
        if (offlinePlayer != null && offlinePlayer.getUniqueId() != null) {
            return offlinePlayer.getUniqueId().toString();
        }
        return trimmed;
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
            parts.add("#" + profile.getSlot() + "=" + profile.getAmount() + "x@" + effectiveCurrency + this.formatPrice(profile.getPrice()));
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
