package de.isolveproblems.freeframe.utils;

import org.bukkit.Material;
import org.bukkit.configuration.file.YamlConfiguration;
import org.junit.jupiter.api.Test;

import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ItemPolicyTest {

    @Test
    void blacklistModeShouldBlockListedItems() {
        ItemPolicy policy = new ItemPolicy();
        YamlConfiguration config = new YamlConfiguration();
        config.set("freeframe.items.mode", "blacklist");
        config.set("freeframe.items.blacklist", Arrays.asList("STONE", "BEDROCK"));

        assertFalse(policy.check(config, Material.STONE).isAllowed());
        assertTrue(policy.check(config, Material.DIRT).isAllowed());
    }

    @Test
    void whitelistModeShouldAllowOnlyListedItems() {
        ItemPolicy policy = new ItemPolicy();
        YamlConfiguration config = new YamlConfiguration();
        config.set("freeframe.items.mode", "whitelist");
        config.set("freeframe.items.whitelist", Arrays.asList("DIAMOND"));

        assertTrue(policy.check(config, Material.DIAMOND).isAllowed());
        assertFalse(policy.check(config, Material.STONE).isAllowed());
    }
}
