package com.zenmanage.sdk.flags;

/**
 * Nested target value structure.
 */
public final class TargetValue {
    private String version;
    private RawFlagValue value;

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public RawFlagValue getValue() {
        return value;
    }

    public void setValue(RawFlagValue value) {
        this.value = value;
    }
}
