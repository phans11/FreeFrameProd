package de.isolveproblems.freeframe.utils;

import de.isolveproblems.freeframe.FreeFrame;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public final class SetupWandItem {
    private SetupWandItem() {
    }

    public static ItemStack create(FreeFrame freeframe) {
        Material material = resolveMaterial(
            freeframe.getPluginConfig().getString("freeframe.setup.wandMaterial", "BLAZE_ROD"),
            Material.BLAZE_ROD
        );

        int amount = Math.max(1, Math.min(64, freeframe.getPluginConfig().getInt("freeframe.setup.wandAmount", 1)));
        ItemStack item = new ItemStack(material, amount);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            String displayName = freeframe.colorize(freeframe.getPluginConfig().getString(
                "freeframe.setup.wandName",
                "&6FreeFrame Setup Wand"
            ));
            meta.setDisplayName(displayName);

            List<String> loreLines = freeframe.getPluginConfig().getStringList("freeframe.setup.wandLore");
            if (loreLines == null || loreLines.isEmpty()) {
                loreLines = new ArrayList<String>();
                loreLines.add("&7Right-click an ItemFrame to open the editor.");
                loreLines.add("&7Requires admin permission.");
            }

            List<String> coloredLore = new ArrayList<String>();
            for (String line : loreLines) {
                coloredLore.add(freeframe.colorize(line));
            }
            meta.setLore(coloredLore);
            item.setItemMeta(meta);
        }
        return item;
    }

    public static boolean isWand(FreeFrame freeframe, ItemStack itemStack) {
        Material configuredMaterial = resolveMaterial(
            freeframe.getPluginConfig().getString("freeframe.setup.wandMaterial", "BLAZE_ROD"),
            Material.BLAZE_ROD
        );

        if (itemStack == null || itemStack.getType() != configuredMaterial || !itemStack.hasItemMeta()) {
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

    private static Material resolveMaterial(String configured, Material fallback) {
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
}
