package de.isolveproblems.freeframe.utils;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class AmountValidatorTest {

    @Test
    void sanitizeShouldClampLowValuesToOne() {
        assertEquals(1, AmountValidator.sanitize(0));
        assertEquals(1, AmountValidator.sanitize(-5));
    }

    @Test
    void sanitizeShouldClampHighValuesToSixtyFour() {
        assertEquals(64, AmountValidator.sanitize(100));
    }

    @Test
    void sanitizeShouldKeepValidValues() {
        assertEquals(16, AmountValidator.sanitize(16));
        assertEquals(64, AmountValidator.sanitize(64));
    }
}
