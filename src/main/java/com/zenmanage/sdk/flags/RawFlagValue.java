package com.zenmanage.sdk.flags;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Wire-level value object supporting boolean/string/number payloads.
 */
public final class RawFlagValue {
    @JsonProperty("boolean")
    private Boolean booleanValue;

    @JsonProperty("string")
    private String stringValue;

    @JsonProperty("number")
    private Double numberValue;

    public Boolean getBooleanValue() {
        return booleanValue;
    }

    public void setBooleanValue(Boolean booleanValue) {
        this.booleanValue = booleanValue;
    }

    public String getStringValue() {
        return stringValue;
    }

    public void setStringValue(String stringValue) {
        this.stringValue = stringValue;
    }

    public Double getNumberValue() {
        return numberValue;
    }

    public void setNumberValue(Double numberValue) {
        this.numberValue = numberValue;
    }

    public Object toJavaValue() {
        if (booleanValue != null) {
            return booleanValue;
        }

        if (stringValue != null) {
            return stringValue;
        }

        if (numberValue != null) {
            return numberValue;
        }

        return "";
    }

    public static RawFlagValue fromJavaValue(Object value) {
        RawFlagValue raw = new RawFlagValue();
        if (value instanceof Boolean) {
            raw.setBooleanValue((Boolean) value);
        } else if (value instanceof Number) {
            raw.setNumberValue(((Number) value).doubleValue());
        } else {
            raw.setStringValue(String.valueOf(value));
        }
        return raw;
    }
}
