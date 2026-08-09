package com.zenmanage.sdk.flags;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

/**
 * Supported flag value types.
 */
public enum FlagType {
    BOOLEAN("boolean"),
    STRING("string"),
    NUMBER("number");

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
            if (candidate.wireValue.equalsIgnoreCase(value)) {
                return candidate;
            }
        }

        throw new IllegalArgumentException("Unsupported flag type: " + value);
    }
}
