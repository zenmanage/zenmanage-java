package com.zenmanage.sdk.flags;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.List;
import java.util.Map;

/**
 * Wire-level value object supporting boolean/string/number/json payloads.
 */
public final class RawFlagValue {
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    @JsonProperty("boolean")
    private Boolean booleanValue;

    @JsonProperty("string")
    private String stringValue;

    @JsonProperty("number")
    private Double numberValue;

    @JsonProperty("json")
    private JsonNode jsonValue;

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

    public JsonNode getJsonValue() {
        return jsonValue;
    }

    public void setJsonValue(JsonNode jsonValue) {
        this.jsonValue = jsonValue;
    }

    /**
     * Collapse the wrapper down to a plain scalar Java value for the generic
     * accessors ({@code asBool()}/{@code asString()}/{@code asNumber()}/{@code getValue()}).
     *
     * <p>{@code json} is intentionally excluded here: those accessors expect a plain
     * scalar (Boolean/String/Double), and a decoded {@link JsonNode} does not fit that
     * contract. {@link Flag#asJson()} reads {@link #getJsonValue()} directly instead of
     * going through this method, so json values still round-trip correctly — they just
     * fall through to the same {@code ""} safe fallback the other accessors already use
     * for a type that isn't their own.</p>
     */
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
        } else if (value instanceof JsonNode) {
            raw.setJsonValue((JsonNode) value);
        } else if (value instanceof Map || value instanceof List) {
            raw.setJsonValue(OBJECT_MAPPER.valueToTree(value));
        } else {
            raw.setStringValue(String.valueOf(value));
        }
        return raw;
    }
}
