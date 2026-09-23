package com.zenmanage.sdk.flags;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;

class FlagTypeTest {
    @Test
    void parsesKnownWireValues() {
        assertEquals(FlagType.BOOLEAN, FlagType.fromWireValue("boolean"));
        assertEquals(FlagType.STRING, FlagType.fromWireValue("string"));
        assertEquals(FlagType.NUMBER, FlagType.fromWireValue("number"));
    }

    @Test
    void parsesKnownWireValuesCaseInsensitively() {
        assertEquals(FlagType.BOOLEAN, FlagType.fromWireValue("Boolean"));
    }

    @Test
    void unrecognizedWireValueDoesNotThrowAndResolvesToUnknown() {
        FlagType type = assertDoesNotThrow(() -> FlagType.fromWireValue("json"));
        assertEquals(FlagType.UNKNOWN, type);
    }

    @Test
    void nullWireValueDoesNotThrowAndResolvesToUnknown() {
        FlagType type = assertDoesNotThrow(() -> FlagType.fromWireValue(null));
        assertEquals(FlagType.UNKNOWN, type);
    }
}
