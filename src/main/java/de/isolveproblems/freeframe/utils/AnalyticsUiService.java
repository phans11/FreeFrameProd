package de.isolveproblems.freeframe.utils;

import de.isolveproblems.freeframe.FreeFrame;
import de.isolveproblems.freeframe.api.BuyerRiskProfile;
import de.isolveproblems.freeframe.inventory.AnalyticsInventoryHolder;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class AnalyticsUiService {
    private final FreeFrame freeframe;

    public AnalyticsUiService(FreeFrame freeframe) {
        this.freeframe = freeframe;
    }

    public void openGlobal(Player player) {
        if (player == null) {
            return;
        }
        Inventory inventory = Bukkit.createInventory(
            new AnalyticsInventoryHolder("global", "global"),
            27,
            this.freeframe.colorize("&8FreeFrame Analytics &7(Global)")
        );

        Map<String, Long> metrics = this.freeframe.getMetricsTracker().snapshot();
        place(inventory, 10, this.metricItem(Material.BOOK, "&6Tracked Frames", this.freeframe.getFrameRegistry().size()));
        place(inventory, 12, this.metricItem(Material.EMERALD, "&aPurchases", value(metrics, "purchases")));
        place(inventory, 13, this.metricItem(Material.BARRIER, "&cDenied Access", value(metrics, "deniedAccess")));
        place(inventory, 14, this.metricItem(Material.WATCH, "&eCooldown Hits", value(metrics, "cooldownHits")));
        place(inventory, 16, this.metricItem(Material.CHEST, "&bStock Outs", value(metrics, "stockOutHits")));
        place(inventory, 22, this.infoItem("&7Use /freeframe analytics frame <id> or player <name>"));
        player.openInventory(inventory);
    }

    public void openFrame(Player player, FreeFrameData frameData) {
        if (player == null || frameData == null) {
            return;
        }
        Map<String, Long> stats = this.freeframe.getStatisticsService().getFrameStats(frameData.getId());
        Inventory inventory = Bukkit.createInventory(
            new AnalyticsInventoryHolder("frame", frameData.getId()),
            27,
            this.freeframe.colorize("&8Analytics &7(Frame " + frameData.getId() + ")")
        );
        place(inventory, 10, this.metricItem(Material.ITEM_FRAME, "&6Frame", frameData.getId()));
        place(inventory, 12, this.metricItem(Material.EMERALD, "&aPurchases", value(stats, "purchases")));
        place(inventory, 13, this.metricItem(Material.CHEST, "&bItems Sold", value(stats, "items")));
        place(inventory, 14, this.metricItem(Material.GOLD_INGOT, "&eRevenue (cents)", value(stats, "moneyCents")));
        place(inventory, 16, this.metricItem(Material.PAPER, "&7Stock", frameData.getStock() + "/" + frameData.getMaxStock()));
        player.openInventory(inventory);
    }

    public void openPlayer(Player viewer, String playerId) {
        if (viewer == null || playerId == null || playerId.trim().isEmpty()) {
            return;
        }
        Map<String, Long> stats = this.freeframe.getStatisticsService().getPlayerStats(playerId);
        BuyerRiskProfile risk = this.freeframe.getBuyerReputationService().inspect(playerId);

        Inventory inventory = Bukkit.createInventory(
            new AnalyticsInventoryHolder("player", playerId),
            27,
            this.freeframe.colorize("&8Analytics &7(Player)")
        );
        place(inventory, 10, this.metricItem(Material.SKULL_ITEM, "&6Player", playerId));
        place(inventory, 12, this.metricItem(Material.EMERALD, "&aPurchases", value(stats, "purchases")));
        place(inventory, 13, this.metricItem(Material.CHEST, "&bItems Bought", value(stats, "items")));
        place(inventory, 14, this.metricItem(Material.GOLD_INGOT, "&eSpent (cents)", value(stats, "moneyCents")));
        place(inventory, 16, this.metricItem(Material.REDSTONE, "&cRisk Score", format(risk.getScore()) + (risk.isBlocked() ? " (BLOCKED)" : "")));
        viewer.openInventory(inventory);
    }

    private ItemStack metricItem(Material material, String title, Object value) {
        ItemStack stack = new ItemStack(material, 1);
        ItemMeta meta = stack.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(this.freeframe.colorize(title));
            List<String> lore = new ArrayList<String>();
            lore.add(this.freeframe.colorize("&7Value: &f" + String.valueOf(value)));
            meta.setLore(lore);
            stack.setItemMeta(meta);
        }
        return stack;
    }

    private ItemStack infoItem(String message) {
        ItemStack stack = new ItemStack(Material.PAPER, 1);
        ItemMeta meta = stack.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(this.freeframe.colorize("&6Hint"));
            List<String> lore = new ArrayList<String>();
            lore.add(this.freeframe.colorize(message));
            meta.setLore(lore);
            stack.setItemMeta(meta);
        }
        return stack;
    }

    private static void place(Inventory inventory, int slot, ItemStack itemStack) {
        if (inventory == null || itemStack == null || slot < 0 || slot >= inventory.getSize()) {
            return;
        }
        inventory.setItem(slot, itemStack);
    }

    private static long value(Map<String, Long> map, String key) {
        if (map == null || key == null) {
            return 0L;
        }
        Long value = map.get(key);
        return value == null ? 0L : value.longValue();
    }

    private static String format(double value) {
        return String.format(Locale.ENGLISH, "%.2f", Math.max(0.0D, value));
    }
}
