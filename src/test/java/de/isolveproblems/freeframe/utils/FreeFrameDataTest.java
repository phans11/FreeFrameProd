package de.isolveproblems.freeframe.utils;

import de.isolveproblems.freeframe.api.FrameType;
import de.isolveproblems.freeframe.api.PurchaseProfile;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.junit.jupiter.api.Test;

import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

class FreeFrameDataTest {

    @Test
    void shouldRoundTripViaConfigurationSection() {
        FrameReference reference = new FrameReference("world", 1, 64, 2, "NORTH");
        FreeFrameData original = new FreeFrameData(
            "abc12345",
            reference,
            "uuid-1",
            "Owner",
            123456789L,
            "STONE",
            19.99D,
            "$",
            true,
            32,
            64,
            true,
            12345L,
            98765L,
            42.50D,
            "entity-1",
            FrameType.LIMITED,
            new BlockReference("world", 5, 65, 6),
            Arrays.asList(
                new PurchaseProfile(2, 1, 19.99D, "&aSingle"),
                new PurchaseProfile(4, 16, 299.99D, "&bBulk")
            )
        );

        YamlConfiguration configuration = new YamlConfiguration();
        ConfigurationSection section = configuration.createSection("frame");
        original.writeToSection(section);

        FreeFrameData restored = FreeFrameData.fromSection("abc12345", section);
        assertNotNull(restored);

        assertEquals("abc12345", restored.getId());
        assertEquals(reference, restored.getReference());
        assertEquals("uuid-1", restored.getOwnerUuid());
        assertEquals("Owner", restored.getOwnerName());
        assertEquals(123456789L, restored.getCreatedAt());
        assertEquals("STONE", restored.getItemType());
        assertEquals(19.99D, restored.getPrice());
        assertEquals("$", restored.getCurrency());
        assertEquals(true, restored.isActive());
        assertEquals(32, restored.getStock());
        assertEquals(64, restored.getMaxStock());
        assertEquals(42.50D, restored.getRevenueTotal());
        assertEquals("entity-1", restored.getDisplayEntityUuid());
        assertEquals(FrameType.LIMITED, restored.getFrameType());
        assertEquals(new BlockReference("world", 5, 65, 6), restored.getLinkedChest());
        assertEquals(2, restored.getPurchaseProfiles().size());
        assertEquals(16, restored.findProfileBySlot(4).getAmount());
    }

    @Test
    void shouldReturnNullForInvalidReference() {
        YamlConfiguration configuration = new YamlConfiguration();
        ConfigurationSection section = configuration.createSection("frame");
        section.set("reference", "invalid");

        assertNull(FreeFrameData.fromSection("abc12345", section));
    }
}
