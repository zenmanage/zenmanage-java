package com.zenmanage.sdk.rollout;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;

class RolloutBucketerTest {
    @Test
    void crc32IsDeterministic() {
        long a = RolloutBucketer.crc32b("abc");
        long b = RolloutBucketer.crc32b("abc");
        assertEquals(a, b);
    }

    @Test
    void nullIdentifierIsNeverInBucket() {
        assertFalse(RolloutBucketer.isInBucket("salt", null, 10));
    }

    @Test
    void invalidPercentageThrows() {
        assertThrows(IllegalArgumentException.class, () -> RolloutBucketer.isInBucket("s", "u", 101));
    }
}
