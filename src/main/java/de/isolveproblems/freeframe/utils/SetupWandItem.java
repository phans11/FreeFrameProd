package de.isolveproblems.freeframe.utils;

import de.isolveproblems.freeframe.FreeFrame;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.Arrays;

public final class SetupWandItem {
    private SetupWandItem() {
    }

    public static ItemStack create(FreeFrame freeframe) {
        Material material = Material.BLAZE_ROD;
        ItemStack item = new ItemStack(material, 1);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            String displayName = freeframe.colorize(freeframe.getPluginConfig().getString(
                "freeframe.setup.wandName",
                "&6FreeFrame Setup Wand"
            ));
            meta.setDisplayName(displayName);
            meta.setLore(Arrays.asList(
                freeframe.colorize("&7Right-click an ItemFrame to open the editor."),
                freeframe.colorize("&7Requires admin permission.")
            ));
            item.setItemMeta(meta);
        }
        return item;
    }

    public static boolean isWand(FreeFrame freeframe, ItemStack itemStack) {
        if (itemStack == null || itemStack.getType() != Material.BLAZE_ROD || !itemStack.hasItemMeta()) {
            return false;
        }

        ItemMeta meta = itemStack.getItemMeta();
        if (meta == null || !meta.hasDisplayName()) {
            return false;
        }

        String configuredName = freeframe.colorize(freeframe.getPluginConfig().getString(
            "freeframe.setup.wandName",
            "&6FreeFrame Setup Wand"
        ));

        return ChatColor.stripColor(meta.getDisplayName()).equalsIgnoreCase(ChatColor.stripColor(configuredName));
    }
}
