package com.zenmanage.sdk.context;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Single attribute value object.
 */
public final class AttributeValue {
    private final String value;

    public AttributeValue(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }

    public Map<String, String> toMap() {
        Map<String, String> result = new LinkedHashMap<>();
        result.put("value", value);
        return result;
    }
}
