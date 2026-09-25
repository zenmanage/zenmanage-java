package com.zenmanage.sdk.flags;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

/**
 * Supported flag value types.
 *
 * <p>{@link #UNKNOWN} is a sentinel used for wire values this SDK release does not
 * recognize yet (e.g. a new flag type added on the platform after this SDK shipped).
 * It intentionally does not throw during deserialization so that a single unrecognized
 * flag in a rules payload degrades to "missing" instead of breaking the whole payload.</p>
 */
public enum FlagType {
    BOOLEAN("boolean"),
    STRING("string"),
    NUMBER("number"),
    JSON("json"),
    UNKNOWN("unknown");

    private final String wireValue;

    FlagType(String wireValue) {
        this.wireValue = wireValue;
    }

    @JsonValue
    public String getWireValue() {
        return wireValue;
    }

    @JsonCreator
    public static FlagType fromWireValue(String value) {
        for (FlagType candidate : values()) {
            if (candidate != UNKNOWN && candidate.wireValue.equalsIgnoreCase(value)) {
                return candidate;
            }
        }

        return UNKNOWN;
    }
}
