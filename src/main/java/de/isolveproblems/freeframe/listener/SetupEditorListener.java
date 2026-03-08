package de.isolveproblems.freeframe.listener;

import de.isolveproblems.freeframe.FreeFrame;
import de.isolveproblems.freeframe.inventory.SetupEditorInventoryHolder;
import de.isolveproblems.freeframe.utils.FreeFrameData;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class SetupEditorListener implements Listener {
    private final FreeFrame freeframe;

    public SetupEditorListener(FreeFrame freeframe) {
        this.freeframe = freeframe;
    }

    public static void openEditor(FreeFrame freeframe, Player player, FreeFrameData frameData) {
        if (freeframe == null || player == null || frameData == null) {
            return;
        }

        Inventory inventory = Bukkit.createInventory(new SetupEditorInventoryHolder(frameData.getId()), 27,
            freeframe.colorize("&8FreeFrame Setup: &e" + frameData.getId()));

        inventory.setItem(4, createButton(Material.PAPER, "&6Frame Info",
            "&7Item: &f" + frameData.getItemType(),
            "&7Owner: &f" + frameData.getOwnerName(),
            "&7Price: &f" + frameData.getCurrency() + String.format(Locale.ENGLISH, "%.2f", frameData.getPrice()),
            "&7Stock: &f" + frameData.getStock() + "/" + frameData.getMaxStock()
        ));

        inventory.setItem(10, createButton(frameData.isActive() ? Material.EMERALD_BLOCK : Material.REDSTONE_BLOCK,
            "&6Toggle Active", "&7Current: &f" + frameData.isActive()));
        inventory.setItem(11, createButton(Material.REDSTONE, "&cStock -1", "&7Current: &f" + frameData.getStock()));
        inventory.setItem(12, createButton(Material.EMERALD, "&aStock +1", "&7Current: &f" + frameData.getStock()));
        inventory.setItem(13, createButton(Material.CHEST, "&eRefill Stock", "&7Set stock to max stock"));
        inventory.setItem(14, createButton(Material.GOLD_NUGGET, "&cPrice -1", "&7Current: &f" + frameData.getCurrency() + String.format(Locale.ENGLISH, "%.2f", frameData.getPrice())));
        inventory.setItem(15, createButton(Material.GOLD_INGOT, "&aPrice +1", "&7Current: &f" + frameData.getCurrency() + String.format(Locale.ENGLISH, "%.2f", frameData.getPrice())));
        inventory.setItem(16, createButton(Material.LEVER, "&6Toggle Auto Refill", "&7Current: &f" + frameData.isAutoRefill()));

        inventory.setItem(19, createButton(Material.COAL, "&cMaxStock -8", "&7Current: &f" + frameData.getMaxStock()));
        inventory.setItem(20, createButton(Material.DIAMOND, "&aMaxStock +8", "&7Current: &f" + frameData.getMaxStock()));

        inventory.setItem(22, createButton(Material.BARRIER, "&cClose Editor", "&7Close setup GUI"));

        player.openInventory(inventory);
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onEditorClick(InventoryClickEvent event) {
        Inventory top = event.getView().getTopInventory();
        if (!(top.getHolder() instanceof SetupEditorInventoryHolder)) {
            return;
        }

        event.setCancelled(true);

        if (!(event.getWhoClicked() instanceof Player)) {
            return;
        }

        if (event.getClickedInventory() == null || !event.getClickedInventory().equals(top)) {
            return;
        }

        Player player = (Player) event.getWhoClicked();
        if (!player.hasPermission(this.freeframe.getConfigHandler().getAdminPermissionNode())) {
            player.sendMessage(this.freeframe.getErrorPermissionMessage());
            return;
        }

        SetupEditorInventoryHolder holder = (SetupEditorInventoryHolder) top.getHolder();
        FreeFrameData frameData = this.freeframe.getFrameRegistry().findById(holder.getFrameId());
        if (frameData == null) {
            player.sendMessage(this.freeframe.formatMessage("%prefix% &cFrame entry no longer exists."));
            player.closeInventory();
            return;
        }

        int slot = event.getRawSlot();
        boolean changed = false;
        switch (slot) {
            case 10:
                frameData.setActive(!frameData.isActive());
                changed = true;
                break;
            case 11:
                frameData.setStock(Math.max(0, frameData.getStock() - 1));
                changed = true;
                break;
            case 12:
                frameData.setStock(Math.min(frameData.getMaxStock(), frameData.getStock() + 1));
                changed = true;
                break;
            case 13:
                frameData.setStock(frameData.getMaxStock());
                frameData.setLastRefillAt(System.currentTimeMillis());
                changed = true;
                break;
            case 14:
                frameData.setPrice(Math.max(0.0D, frameData.getPrice() - 1.0D));
                changed = true;
                break;
            case 15:
                frameData.setPrice(frameData.getPrice() + 1.0D);
                changed = true;
                break;
            case 16:
                frameData.setAutoRefill(!frameData.isAutoRefill());
                changed = true;
                break;
            case 19:
                frameData.setMaxStock(Math.max(1, frameData.getMaxStock() - 8));
                frameData.setStock(Math.min(frameData.getStock(), frameData.getMaxStock()));
                changed = true;
                break;
            case 20:
                frameData.setMaxStock(Math.min(4096, frameData.getMaxStock() + 8));
                changed = true;
                break;
            case 22:
                player.closeInventory();
                return;
            default:
                return;
        }

        if (!changed) {
            return;
        }

        this.freeframe.getFrameRegistry().saveToConfig();
        this.freeframe.getDisplayService().refresh(frameData);
        this.freeframe.getAuditLogger().logAdminAction(player, "setup-update", frameData.getId() + " slot=" + slot);
        openEditor(this.freeframe, player, frameData);
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onEditorDrag(InventoryDragEvent event) {
        if (event.getView().getTopInventory().getHolder() instanceof SetupEditorInventoryHolder) {
            event.setCancelled(true);
        }
    }

    private static ItemStack createButton(Material material, String name, String... loreLines) {
        ItemStack itemStack = new ItemStack(material, 1);
        ItemMeta meta = itemStack.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(colorize(name));
            List<String> lore = new ArrayList<String>();
            if (loreLines != null) {
                for (String line : loreLines) {
                    lore.add(colorize(line));
                }
            }
            meta.setLore(lore);
            itemStack.setItemMeta(meta);
        }
        return itemStack;
    }

    private static String colorize(String input) {
        return org.bukkit.ChatColor.translateAlternateColorCodes('&', input == null ? "" : input);
    }
}
