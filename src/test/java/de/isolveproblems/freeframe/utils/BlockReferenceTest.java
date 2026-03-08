package de.isolveproblems.freeframe.utils;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class BlockReferenceTest {

    @Test
    void shouldRoundTripSerializedForm() {
        BlockReference reference = new BlockReference("world", 10, 64, -5);

        String serialized = reference.serialize();
        BlockReference restored = BlockReference.parse(serialized);

        assertEquals(reference, restored);
    }

    @Test
    void shouldReturnNullForInvalidSerializedForm() {
        assertNull(BlockReference.parse("world|x|64|0"));
        assertNull(BlockReference.parse("world|1|2"));
    }
}
