package de.isolveproblems.freeframe.listener;

import de.isolveproblems.freeframe.config.FreeFrameConfigKey;
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

        int size = sanitizeInventorySize(freeframe.cfgInt(FreeFrameConfigKey.FREEFRAME_SETUP_EDITOR_INVENTORYSIZE));
        String titleTemplate = freeframe.cfgString(FreeFrameConfigKey.FREEFRAME_SETUP_EDITOR_TITLE);
        if (titleTemplate == null || titleTemplate.trim().isEmpty()) {
            titleTemplate = "&8FreeFrame Setup: &e%id%";
        }

        Inventory inventory = Bukkit.createInventory(
            new SetupEditorInventoryHolder(frameData.getId()),
            size,
            freeframe.colorize(titleTemplate.replace("%id%", frameData.getId()))
        );

        int infoSlot = resolveSlot(freeframe, "freeframe.setup.editor.slots.info", 4, size);
        int toggleActiveSlot = resolveSlot(freeframe, "freeframe.setup.editor.slots.toggleActive", 10, size);
        int stockDownSlot = resolveSlot(freeframe, "freeframe.setup.editor.slots.stockDown", 11, size);
        int stockUpSlot = resolveSlot(freeframe, "freeframe.setup.editor.slots.stockUp", 12, size);
        int refillSlot = resolveSlot(freeframe, "freeframe.setup.editor.slots.refill", 13, size);
        int priceDownSlot = resolveSlot(freeframe, "freeframe.setup.editor.slots.priceDown", 14, size);
        int priceUpSlot = resolveSlot(freeframe, "freeframe.setup.editor.slots.priceUp", 15, size);
        int autoRefillSlot = resolveSlot(freeframe, "freeframe.setup.editor.slots.toggleAutoRefill", 16, size);
        int maxStockDownSlot = resolveSlot(freeframe, "freeframe.setup.editor.slots.maxStockDown", 19, size);
        int maxStockUpSlot = resolveSlot(freeframe, "freeframe.setup.editor.slots.maxStockUp", 20, size);
        int closeSlot = resolveSlot(freeframe, "freeframe.setup.editor.slots.close", 22, size);

        placeButton(inventory, infoSlot, createButton(
            resolveMaterial(freeframe, "freeframe.setup.editor.materials.info", Material.PAPER),
            "&6Frame Info",
            "&7Item: &f" + frameData.getItemType(),
            "&7Owner: &f" + frameData.getOwnerName(),
            "&7Price: &f" + frameData.getCurrency() + String.format(Locale.ENGLISH, "%.2f", frameData.getPrice()),
            "&7Stock: &f" + frameData.getStock() + "/" + frameData.getMaxStock(),
            "&7Revenue: &f" + frameData.getCurrency() + String.format(Locale.ENGLISH, "%.2f", frameData.getRevenueTotal())
        ));

        placeButton(inventory, toggleActiveSlot, createButton(
            frameData.isActive()
                ? resolveMaterial(freeframe, "freeframe.setup.editor.materials.toggleActiveOn", Material.EMERALD_BLOCK)
                : resolveMaterial(freeframe, "freeframe.setup.editor.materials.toggleActiveOff", Material.REDSTONE_BLOCK),
            "&6Toggle Active",
            "&7Current: &f" + frameData.isActive()
        ));

        int stockStep = Math.max(1, freeframe.cfgInt(FreeFrameConfigKey.FREEFRAME_SETUP_EDITOR_STOCKSTEP));
        int maxStockStep = Math.max(1, freeframe.cfgInt(FreeFrameConfigKey.FREEFRAME_SETUP_EDITOR_MAXSTOCKSTEP));
        double priceStep = Math.max(0.01D, freeframe.cfgDouble(FreeFrameConfigKey.FREEFRAME_SETUP_EDITOR_PRICESTEP));

        placeButton(inventory, stockDownSlot, createButton(
            resolveMaterial(freeframe, "freeframe.setup.editor.materials.stockDown", Material.REDSTONE),
            "&cStock -" + stockStep,
            "&7Current: &f" + frameData.getStock()
        ));
        placeButton(inventory, stockUpSlot, createButton(
            resolveMaterial(freeframe, "freeframe.setup.editor.materials.stockUp", Material.EMERALD),
            "&aStock +" + stockStep,
            "&7Current: &f" + frameData.getStock()
        ));
        placeButton(inventory, refillSlot, createButton(
            resolveMaterial(freeframe, "freeframe.setup.editor.materials.refill", Material.CHEST),
            "&eRefill Stock",
            "&7Set stock to max stock"
        ));
        placeButton(inventory, priceDownSlot, createButton(
            resolveMaterial(freeframe, "freeframe.setup.editor.materials.priceDown", Material.GOLD_NUGGET),
            "&cPrice -" + String.format(Locale.ENGLISH, "%.2f", priceStep),
            "&7Current: &f" + frameData.getCurrency() + String.format(Locale.ENGLISH, "%.2f", frameData.getPrice())
        ));
        placeButton(inventory, priceUpSlot, createButton(
            resolveMaterial(freeframe, "freeframe.setup.editor.materials.priceUp", Material.GOLD_INGOT),
            "&aPrice +" + String.format(Locale.ENGLISH, "%.2f", priceStep),
            "&7Current: &f" + frameData.getCurrency() + String.format(Locale.ENGLISH, "%.2f", frameData.getPrice())
        ));
        placeButton(inventory, autoRefillSlot, createButton(
            resolveMaterial(freeframe, "freeframe.setup.editor.materials.toggleAutoRefill", Material.LEVER),
            "&6Toggle Auto Refill",
            "&7Current: &f" + frameData.isAutoRefill()
        ));

        placeButton(inventory, maxStockDownSlot, createButton(
            resolveMaterial(freeframe, "freeframe.setup.editor.materials.maxStockDown", Material.COAL),
            "&cMaxStock -" + maxStockStep,
            "&7Current: &f" + frameData.getMaxStock()
        ));
        placeButton(inventory, maxStockUpSlot, createButton(
            resolveMaterial(freeframe, "freeframe.setup.editor.materials.maxStockUp", Material.DIAMOND),
            "&aMaxStock +" + maxStockStep,
            "&7Current: &f" + frameData.getMaxStock()
        ));

        placeButton(inventory, closeSlot, createButton(
            resolveMaterial(freeframe, "freeframe.setup.editor.materials.close", Material.BARRIER),
            "&cClose Editor",
            "&7Close setup GUI"
        ));

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

        int size = top.getSize();
        int slot = event.getRawSlot();
        int toggleActiveSlot = resolveSlot(this.freeframe, "freeframe.setup.editor.slots.toggleActive", 10, size);
        int stockDownSlot = resolveSlot(this.freeframe, "freeframe.setup.editor.slots.stockDown", 11, size);
        int stockUpSlot = resolveSlot(this.freeframe, "freeframe.setup.editor.slots.stockUp", 12, size);
        int refillSlot = resolveSlot(this.freeframe, "freeframe.setup.editor.slots.refill", 13, size);
        int priceDownSlot = resolveSlot(this.freeframe, "freeframe.setup.editor.slots.priceDown", 14, size);
        int priceUpSlot = resolveSlot(this.freeframe, "freeframe.setup.editor.slots.priceUp", 15, size);
        int autoRefillSlot = resolveSlot(this.freeframe, "freeframe.setup.editor.slots.toggleAutoRefill", 16, size);
        int maxStockDownSlot = resolveSlot(this.freeframe, "freeframe.setup.editor.slots.maxStockDown", 19, size);
        int maxStockUpSlot = resolveSlot(this.freeframe, "freeframe.setup.editor.slots.maxStockUp", 20, size);
        int closeSlot = resolveSlot(this.freeframe, "freeframe.setup.editor.slots.close", 22, size);

        int stockStep = Math.max(1, this.freeframe.cfgInt(FreeFrameConfigKey.FREEFRAME_SETUP_EDITOR_STOCKSTEP));
        int maxStockStep = Math.max(1, this.freeframe.cfgInt(FreeFrameConfigKey.FREEFRAME_SETUP_EDITOR_MAXSTOCKSTEP));
        int maxStockCap = Math.max(1, this.freeframe.cfgInt(FreeFrameConfigKey.FREEFRAME_SETUP_EDITOR_MAXSTOCKCAP));
        double priceStep = Math.max(0.01D, this.freeframe.cfgDouble(FreeFrameConfigKey.FREEFRAME_SETUP_EDITOR_PRICESTEP));

        boolean changed = false;
        if (slot == closeSlot) {
            player.closeInventory();
            return;
        } else if (slot == toggleActiveSlot) {
            frameData.setActive(!frameData.isActive());
            changed = true;
        } else if (slot == stockDownSlot) {
            frameData.setStock(Math.max(0, frameData.getStock() - stockStep));
            changed = true;
        } else if (slot == stockUpSlot) {
            frameData.setStock(Math.min(frameData.getMaxStock(), frameData.getStock() + stockStep));
            changed = true;
        } else if (slot == refillSlot) {
            frameData.setStock(frameData.getMaxStock());
            frameData.setLastRefillAt(System.currentTimeMillis());
            changed = true;
        } else if (slot == priceDownSlot) {
            frameData.setPrice(Math.max(0.0D, frameData.getPrice() - priceStep));
            changed = true;
        } else if (slot == priceUpSlot) {
            frameData.setPrice(frameData.getPrice() + priceStep);
            changed = true;
        } else if (slot == autoRefillSlot) {
            frameData.setAutoRefill(!frameData.isAutoRefill());
            changed = true;
        } else if (slot == maxStockDownSlot) {
            frameData.setMaxStock(Math.max(1, frameData.getMaxStock() - maxStockStep));
            frameData.setStock(Math.min(frameData.getStock(), frameData.getMaxStock()));
            changed = true;
        } else if (slot == maxStockUpSlot) {
            frameData.setMaxStock(Math.min(maxStockCap, frameData.getMaxStock() + maxStockStep));
            changed = true;
        } else {
            return;
        }

        if (!changed) {
            return;
        }

        this.freeframe.getFrameRegistry().saveToConfig();
        if (this.freeframe.cfgBoolean(FreeFrameConfigKey.FREEFRAME_SETUP_EDITOR_REFRESHDISPLAY)) {
            this.freeframe.getDisplayService().refresh(frameData);
        }
        this.freeframe.getAuditLogger().logAdminAction(player, "setup-update", frameData.getId() + " slot=" + slot);

        if (this.freeframe.cfgBoolean(FreeFrameConfigKey.FREEFRAME_SETUP_EDITOR_CLOSEAFTERCHANGE)) {
            player.closeInventory();
            return;
        }

        openEditor(this.freeframe, player, frameData);
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onEditorDrag(InventoryDragEvent event) {
        if (event.getView().getTopInventory().getHolder() instanceof SetupEditorInventoryHolder) {
            event.setCancelled(true);
        }
    }

    private static void placeButton(Inventory inventory, int slot, ItemStack button) {
        if (inventory == null || button == null) {
            return;
        }

        if (slot < 0 || slot >= inventory.getSize()) {
            return;
        }

        inventory.setItem(slot, button);
    }

    private static int resolveSlot(FreeFrame freeframe, String path, int fallback, int inventorySize) {
        int slot = freeframe.getPluginConfig().getInt(path, fallback);
        if (slot < 0 || slot >= inventorySize) {
            return fallback >= 0 && fallback < inventorySize ? fallback : 0;
        }
        return slot;
    }

    private static Material resolveMaterial(FreeFrame freeframe, String path, Material fallback) {
        String configured = freeframe.getPluginConfig().getString(path, fallback.name());
        if (configured == null || configured.trim().isEmpty()) {
            return fallback;
        }

        try {
            Material material = Material.valueOf(configured.trim().toUpperCase(Locale.ENGLISH));
            return material == null ? fallback : material;
        } catch (IllegalArgumentException ignored) {
            return fallback;
        }
    }

    private static int sanitizeInventorySize(int configuredSize) {
        int size = configuredSize;
        if (size < 9) {
            size = 9;
        }
        if (size > 54) {
            size = 54;
        }
        if (size % 9 != 0) {
            size = (size / 9) * 9;
        }
        return Math.max(9, size);
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
