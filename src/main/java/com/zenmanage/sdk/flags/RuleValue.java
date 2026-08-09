package com.zenmanage.sdk.flags;

/**
 * Rule result payload.
 */
public final class RuleValue {
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
