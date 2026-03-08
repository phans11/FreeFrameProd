package de.isolveproblems.freeframe.utils;

import de.isolveproblems.freeframe.api.FrameType;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class FrameTypeTest {

    @Test
    void shouldParseKnownTypesCaseInsensitive() {
        assertEquals(FrameType.FREE, FrameType.fromString("free"));
        assertEquals(FrameType.ADMIN_ONLY, FrameType.fromString("ADMIN_ONLY"));
        assertEquals(FrameType.PREVIEW_ONLY, FrameType.fromString("preview_only"));
    }

    @Test
    void shouldFallbackToShopForUnknownValues() {
        assertEquals(FrameType.SHOP, FrameType.fromString(null));
        assertEquals(FrameType.SHOP, FrameType.fromString(""));
        assertEquals(FrameType.SHOP, FrameType.fromString("invalid"));
    }
}
