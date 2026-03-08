package de.isolveproblems.freeframe.utils;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

class FrameReferenceTest {

    @Test
    void parseShouldRoundTripSerializedValues() {
        FrameReference original = new FrameReference("world", 10, 64, -20, "NORTH");

        FrameReference parsed = FrameReference.parse(original.serialize());

        assertNotNull(parsed);
        assertEquals(original, parsed);
    }

    @Test
    void parseShouldReturnNullForMalformedInput() {
        assertNull(FrameReference.parse(null));
        assertNull(FrameReference.parse(""));
        assertNull(FrameReference.parse("world|x|y|z|NORTH"));
        assertNull(FrameReference.parse("world|10|64"));
    }
}
