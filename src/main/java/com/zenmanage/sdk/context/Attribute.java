package com.zenmanage.sdk.context;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Context attribute containing one or more values.
 */
public final class Attribute {
    private final String key;
    private final List<AttributeValue> values;

    public Attribute(String key) {
        this(key, new ArrayList<>());
    }

    public Attribute(String key, List<String> values) {
        this.key = key;
        this.values = values.stream().map(AttributeValue::new).collect(Collectors.toCollection(ArrayList::new));
    }

    public String getKey() {
        return key;
    }

    public List<String> getValues() {
        return values.stream().map(AttributeValue::getValue).collect(Collectors.toList());
    }

    public Attribute addValue(String value) {
        this.values.add(new AttributeValue(value));
        return this;
    }

    public Map<String, Object> toMap() {
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("key", key);
        result.put("values", values.stream().map(AttributeValue::toMap).collect(Collectors.toList()));
        return result;
    }
}
