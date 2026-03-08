package de.isolveproblems.freeframe.utils;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.junit.jupiter.api.Test;

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
            true
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
        assertEquals(64, restored.getStock());
        assertEquals(64, restored.getMaxStock());
        assertEquals(0.0D, restored.getRevenueTotal());
    }

    @Test
    void shouldReturnNullForInvalidReference() {
        YamlConfiguration configuration = new YamlConfiguration();
        ConfigurationSection section = configuration.createSection("frame");
        section.set("reference", "invalid");

        assertNull(FreeFrameData.fromSection("abc12345", section));
    }
}
