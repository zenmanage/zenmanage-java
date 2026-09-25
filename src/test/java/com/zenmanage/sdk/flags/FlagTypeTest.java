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
        assertEquals(FlagType.JSON, FlagType.fromWireValue("json"));
    }

    @Test
    void parsesKnownWireValuesCaseInsensitively() {
        assertEquals(FlagType.BOOLEAN, FlagType.fromWireValue("Boolean"));
        assertEquals(FlagType.JSON, FlagType.fromWireValue("Json"));
    }

    @Test
    void unrecognizedWireValueDoesNotThrowAndResolvesToUnknown() {
        // "future-type" stands in for a flag type added on the platform after this SDK
        // release shipped (the same role "json" played before ZEN-1389 gave it a real member).
        FlagType type = assertDoesNotThrow(() -> FlagType.fromWireValue("future-type"));
        assertEquals(FlagType.UNKNOWN, type);
    }

    @Test
    void nullWireValueDoesNotThrowAndResolvesToUnknown() {
        FlagType type = assertDoesNotThrow(() -> FlagType.fromWireValue(null));
        assertEquals(FlagType.UNKNOWN, type);
    }
}
